/**
 *    Copyright 2013 Jadaptive Limited
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.jadaptive.box.niofs.api;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import org.jadaptive.box.niofs.api.auth.session.AuthenticatedSession;
import org.jadaptive.box.niofs.api.channel.BoxSeekableByteChannel;
import org.jadaptive.box.niofs.api.folder.BoxFolderTree;
import org.jadaptive.box.niofs.api.folder.BoxResource;
import org.jadaptive.box.niofs.api.folder.BoxResourceType;
import org.jadaptive.box.niofs.exception.BoxFileAlreadyExistsFoundException;
import org.jadaptive.box.niofs.exception.BoxFileNotFoundException;
import org.jadaptive.box.niofs.exception.BoxParentPathInvalidException;
import org.jadaptive.box.niofs.path.BoxPath;
import org.jadaptive.niofs.attr.JadNioFileAttributes;
import org.jadaptive.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.CopyOption;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public abstract class BaseBoxRemoteAPI implements BoxRemoteAPI {

    private static final Logger logger = LoggerFactory.getLogger(BaseBoxRemoteAPI.class);

    protected final AuthenticatedSession authenticatedSession;

    public BaseBoxRemoteAPI(AuthenticatedSession authenticatedSession) {
        this.authenticatedSession = authenticatedSession;
    }

    protected AuthenticatedSession getAuthenticatedSession() {
        return this.authenticatedSession;
    }

    @Override
    public void createDirectory(BoxPath path, FileAttribute<?>... attrs) {

        logger.info("The given path is {}", path);

        var normalizePath = getNormalizePath(path);

        logger.info("The path normalized as {}", normalizePath);

        var api = getBoxAPIConnection();

        var parent = normalizePath.getParent();
        var pathNames = parent.getNames();

        var parentResourceInBox = BoxFolderTree.walk(pathNames, api);

        var created = createFolderResource(parentResourceInBox, normalizePath, api);

        logger.info("Folder created '{}' with id '{}'", created.getName(), created.getID());

    }

    @Override
    public void delete(BoxPath path) {

        logger.info("The given path is {}", path);

        var normalizePath = getNormalizePath(path);
        var pathNames = normalizePath.getNames();

        logger.info("The path normalized as {}", normalizePath);

        var api = getBoxAPIConnection();

        var resourceInBox = BoxFolderTree.walk(pathNames, api);

        deleteResource(resourceInBox, api);

        logger.info("Box resource '{}' deleted with id '{}'", resourceInBox.resourceType, resourceInBox.id);
    }

    @Override
    public void copy(BoxPath source, BoxPath target, CopyOption...options) {

        var targetName = target.getFileName().toString();

        var pair = sourceTargetResources(source,target);

        var api = getBoxAPIConnection();

        var copied = actOnSourceTargetResources(pair.first, pair.second, api,
                (s, t) -> s.copy(t, targetName), BoxFolder::copy);

        logger.info("Folder copied '{}' with id '{}'", copied.getName(), copied.getID());
    }

    @Override
    public void move(BoxPath source, BoxPath target, CopyOption...options) {

        var targetName = target.getFileName().toString();

        var pair = sourceTargetResources(source,target);

        var api = getBoxAPIConnection();

        var moved = actOnSourceTargetResources(pair.first, pair.second, api,
                (s, t) -> s.move(t, targetName), BoxFolder::move);

        logger.info("Folder moved '{}' with id '{}'", moved.getName(), moved.getID());
    }

    @Override
    @SuppressWarnings("unchecked")
    public JadNioFileAttributes readAttributes(BoxPath path, LinkOption... options) {
        logger.info("The given path is {}", path);

        var normalizePath = getNormalizePath(path);

        logger.info("The path normalized as {}", normalizePath);

        var api = getBoxAPIConnection();

        var parent = normalizePath.getParent();
        var pathNames = parent.getNames();

        var resource = BoxFolderTree.walk(pathNames, api);

        if (resource instanceof BoxResource.NullBoxResource) {
            throw new BoxParentPathInvalidException("Parent path is not present in remote account.");
        }

        var current = normalizePath.getFileName();

        var parentFolder = new BoxFolder(api, resource.id);
        var iterable =  parentFolder.getChildren("name", "id", "size", "created_by", "created_at", "modified_at");
        for (BoxItem.Info item : iterable) {
            if (item.getName().equals(current.toString())) {
                return setUpJadNioFileAttributes(item);
            }
        }

        throw new BoxFileNotFoundException("Resource does not exists.");
    }

    @Override
    public Map<String, Object> readAttributes(BoxPath path, String attributes, LinkOption... options) {

        if (attributes == null) {
            return Collections.emptyMap();
        }

        var jadAttributes = readAttributes(path, options);
        var jadAttributesMap = jadAttributes.toMap();

        var keys = Arrays.stream(attributes.split(","))
                    .filter(s -> !s.isBlank())
                    .map(s -> s.trim())
                    .collect(Collectors.toSet());


        var map = new HashMap<String, Object>();

        for (String key: keys) {
            var value = jadAttributesMap.get(key);
            if (value != null) {
                map.put(key, value);
            }
        }

        return map;
    }

    @Override
    public SeekableByteChannel newByteChannel(BoxPath path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {

        logger.info("The given path is {}", path);

        var normalizePath = getNormalizePath(path);

        logger.info("The path normalized as {}", normalizePath);

        var api = getBoxAPIConnection();

        return BoxSeekableByteChannel.getBoxFileChannel(getBoxFileInfo(path), api);
    }

    @Override
    public BoxFileInfo getBoxFileInfo(BoxPath path) {

        logger.info("The given path is {}", path);

        var normalizePath = getNormalizePath(path);

        logger.info("The path normalized as {}", normalizePath);

        var nameFromPath = normalizePath.getFileName();

        logger.info("Name from path is {}", nameFromPath);

        var api = getBoxAPIConnection();

        var parent = normalizePath.getParent();
        var pathNames = parent.getNames();

        var parentResource = BoxFolderTree.walk(pathNames, api);

        if (parentResource instanceof BoxResource.NullBoxResource) {
            throw new BoxParentPathInvalidException("Parent path is not present in remote account.");
        }

        var current = normalizePath.getFileName();

        var parentFolder = new BoxFolder(api, parentResource.id);
        var iterable =  parentFolder.getChildren("name", "id", "size");
        String id = null;
        long size = 0;
        String name = null;
        for (BoxItem.Info item : iterable) {
            if (item.getName().equals(current.toString())) {
                id = item.getID();
                size = item.getSize();
                name = item.getName();
                break;
            }
        }

        // if not present in box, take name from path
        if (id == null) {
            name = nameFromPath.toString();
        }

        return new BoxFileInfo(name, id, parentResource.id, size);
    }

    private BoxAPIConnection getBoxAPIConnection() {
        var session = getAuthenticatedSession();
        return session.getBoxAPIConnection();
    }

    private Pair<BoxResource> sourceTargetResources(BoxPath source, BoxPath target) {

        logger.info("The given source path is {}", source);
        var normalizeSourcePath = getNormalizePath(source);
        var sourcePathNames = normalizeSourcePath.getNames();

        logger.info("The source path normalized as {}", normalizeSourcePath);

        logger.info("The given target path is {}", target);
        var normalizeTargetPath = getNormalizePath(target);
        var targetPathNames = normalizeTargetPath.getNames();

        var api = getBoxAPIConnection();

        var targetResourceInBox  = BoxFolderTree.walk(targetPathNames, api);

        if (targetResourceInBox instanceof BoxResource.NullBoxResource
                || targetResourceInBox.resourceType == BoxResourceType.File) {

            var parent = normalizeTargetPath.getParent();
            var parentPathNames = parent.getNames();

            logger.info("Checking for parent path for target {}", parent);

            targetResourceInBox = BoxFolderTree.walk(parentPathNames, api);
        }

        var sourceResourceInBox = BoxFolderTree.walk(sourcePathNames, api);

        return new Pair<>(sourceResourceInBox, targetResourceInBox);
    }

    private static JadNioFileAttributes setUpJadNioFileAttributes(BoxItem.Info item) {
        var regularFile = isRegularFile(item);

        var size = item.getSize();
        var fileKey = item.getID();
        var creationTime = item.getCreatedAt() == null 
                    ? FileTime.fromMillis(0) 
                    : FileTime.fromMillis(item.getCreatedAt().getTime());

        var fileAttributes = new JadNioFileAttributes(creationTime, regularFile, size, fileKey);

        if (item.getModifiedAt() != null) {
            var lastModifiedTime = FileTime.fromMillis(item.getModifiedAt().getTime());
            fileAttributes.setLastModifiedTime(lastModifiedTime);
        }
        return fileAttributes;
    }

    private static boolean isRegularFile(BoxItem.Info item) {
        var regularFile = false;
        if (item instanceof BoxFolder.Info){
            regularFile = false;
        } else if (item instanceof BoxFile.Info) {
            regularFile = true;
        } else {
            throw new IllegalArgumentException("Resource could not be matched.");
        }
        return regularFile;
    }

    private static BoxPath getNormalizePath(BoxPath dir) {
        BoxPath normalizePath;
        if (dir.isAbsolute()) {
            normalizePath = normalizeForAbsolutePath(dir);
        } else {
            normalizePath = normalizeForRelativePath(dir);
        }
        return normalizePath;
    }

    private static BoxFolder.Info createFolderResource(BoxResource resource, BoxPath normalizePath, BoxAPIConnection api) {
        if (resource instanceof BoxResource.NullBoxResource) {
            throw new BoxParentPathInvalidException("Parent path is not present in remote account.");
        }

        var current = normalizePath.getFileName();

        var parentFolder = new BoxFolder(api, resource.id);
        var iterable =  parentFolder.getChildren("name");
        for (BoxItem.Info item : iterable) {
            if (item.getName().equals(current.toString()) && item instanceof BoxFolder.Info) {
                throw new BoxFileAlreadyExistsFoundException("Folder already exists");
            }
        }

        return parentFolder.createFolder(current.toString());
    }

    private static void deleteResource(BoxResource resource, BoxAPIConnection api) {
        if (resource instanceof BoxResource.NullBoxResource) {
            throw new BoxFileNotFoundException("Folder is not present in remote account.");
        }

        if (resource.resourceType == BoxResourceType.Folder) {
            var folder = new BoxFolder(api, resource.id);
            folder.delete(true);
        } else {
            var file = new BoxFile(api, resource.id);
            file.delete();
        }

    }

    private static BoxItem.Info actOnSourceTargetResources(BoxResource sourceResource,
                                                           BoxResource targetResource,
                                                           BoxAPIConnection api,
                                                           BiFunction<BoxFile, BoxFolder, BoxItem.Info> fileToFolderStrategy,
                                                           BiFunction<BoxFolder, BoxFolder, BoxItem.Info> folderToFolderStrategy) {

        if (sourceResource instanceof BoxResource.NullBoxResource) {
            throw new BoxFileNotFoundException("Source path is not present in remote account.");
        }

        if (targetResource instanceof BoxResource.NullBoxResource) {
            throw new BoxFileNotFoundException("Target path is not present in remote account.");
        }

        if (sourceResource.resourceType == BoxResourceType.Folder
            && targetResource.resourceType == BoxResourceType.Folder) {

            var sourceFolder = new BoxFolder(api, sourceResource.id);
            var targetFolder = new BoxFolder(api, targetResource.id);

            return folderToFolderStrategy.apply(sourceFolder, targetFolder);
        } else if (sourceResource.resourceType == BoxResourceType.File
            && targetResource.resourceType == BoxResourceType.Folder) {

            var sourceFile = new BoxFile(api, sourceResource.id);
            var targetFolder = new BoxFolder(api, targetResource.id);

            return fileToFolderStrategy.apply(sourceFile, targetFolder);

        } else {
            throw new IllegalStateException("Source and Target combination not understood.");
        }

    }

    private static BoxPath normalizeForAbsolutePath(BoxPath dir) {
        BoxPath normalizePath;
        normalizePath = (BoxPath) dir.normalize();
        return normalizePath;
    }

    private static BoxPath normalizeForRelativePath(BoxPath dir) {
        BoxPath normalizePath;
        var fs =  dir.getFileSystem();
        var service = fs.getPathService();

        var workingDirectory = service.getWorkingDirectory();
        logger.info("The working directory is {}", workingDirectory);

        // if no names its root
        var workingDirectoryNames = workingDirectory.getNames().isEmpty() ?
                Collections.singletonList(service.getRootName()) :
                    workingDirectory.getNames();

        var dirNames = dir.getNames();

        var mergedNames = new ArrayList<String>();
        mergedNames.addAll(workingDirectoryNames);
        mergedNames.addAll(dirNames);

        logger.debug("The merged names are {}", mergedNames);

        var mergedPath = service.getPath(mergedNames.toArray(new String[0]));

        logger.debug("The merged path is {}", mergedPath);

        normalizePath = (BoxPath) mergedPath.normalize();
        return normalizePath;
    }
}
