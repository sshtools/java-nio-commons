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

import com.box.sdk.*;
import org.jadaptive.api.FileSystemRemoteAPI;
import org.jadaptive.api.file.FileSysFileInfo;
import org.jadaptive.api.folder.JadFsResource;
import org.jadaptive.api.folder.JadFsResourceType;
import org.jadaptive.api.user.FileSysUserInfo;
import org.jadaptive.box.niofs.api.auth.session.AbstractAuthenticatedSession;
import org.jadaptive.box.niofs.api.auth.session.AuthenticatedSession;
import org.jadaptive.box.niofs.api.channel.BoxSeekableByteChannel;
import org.jadaptive.box.niofs.api.folder.BoxFsTreeWalker;
import org.jadaptive.box.niofs.api.folder.BoxJadFsResourceMapper;
import org.jadaptive.box.niofs.path.BoxPath;
import org.jadaptive.box.niofs.stream.BoxDirectoryStream;
import org.jadaptive.niofs.attr.JadNioFileAttributes;
import org.jadaptive.niofs.exception.JadNioFsFileAlreadyExistsFoundException;
import org.jadaptive.niofs.exception.JadNioFsFileNotFoundException;
import org.jadaptive.niofs.exception.JadNioFsNotADirectoryException;
import org.jadaptive.niofs.exception.JadNioFsParentPathInvalidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public abstract class BaseBoxRemoteAPI implements FileSystemRemoteAPI<BoxPath> {

    private static final Logger logger = LoggerFactory.getLogger(BaseBoxRemoteAPI.class);

    protected final AuthenticatedSession authenticatedSession;

    private final JadFsResource.JadFsTreeWalker boxFsTreeWalker;

    public BaseBoxRemoteAPI(AuthenticatedSession authenticatedSession) {
        this.authenticatedSession = authenticatedSession;
        boxFsTreeWalker = new BoxFsTreeWalker(new BoxJadFsResourceMapper(getBoxAPIConnection()));
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

        var parentResourceInBox = boxFsTreeWalker.walk(pathNames);

        var created = createFolderResource(parentResourceInBox, (BoxPath) normalizePath, api);

        logger.info("Folder created '{}' with id '{}'", created.getName(), created.getID());

    }

    @Override
    public void delete(BoxPath path) {

        logger.info("The given path is {}", path);

        var normalizePath = getNormalizePath(path);
        var pathNames = normalizePath.getNames();

        logger.info("The path normalized as {}", normalizePath);

        var api = getBoxAPIConnection();

        var resourceInBox = boxFsTreeWalker.walk(pathNames);

        deleteResource(resourceInBox, api);

        logger.info("Box resource '{}' deleted with id '{}'", resourceInBox.resourceType, resourceInBox.id);
    }

    @Override
    public void copy(BoxPath source, BoxPath target, CopyOption...options) {

        var targetName = target.getFileName().toString();

        var pair = sourceTargetResources(boxFsTreeWalker, source, target);

        var api = getBoxAPIConnection();

        var copied = actOnSourceTargetResources(pair.first, pair.second, api,
                (s, t) -> s.copy(t, targetName), BoxFolder::copy);

        logger.info("Folder copied '{}' with id '{}'", copied.getName(), copied.getID());
    }

    @Override
    public void move(BoxPath source, BoxPath target, CopyOption...options) {

        var targetName = target.getFileName().toString();

        var pair = sourceTargetResources(boxFsTreeWalker, source, target);

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

        var resource = boxFsTreeWalker.walk(pathNames);

        if (resource instanceof JadFsResource.NullJadFsResource) {
            throw new JadNioFsParentPathInvalidException("Parent path is not present in remote account.");
        }

        var current = normalizePath.getFileName();

        var parentFolder = new BoxFolder(api, resource.id);
        var iterable =  parentFolder.getChildren("name", "id", "size", "created_by", "created_at", "modified_at");
        for (BoxItem.Info item : iterable) {
            if (item.getName().equals(current.toString())) {
                return setUpJadNioFileAttributes(item);
            }
        }

        throw new JadNioFsFileNotFoundException("Resource does not exists.");
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
    public SeekableByteChannel newByteChannel(BoxPath path, Set<? extends OpenOption> options,
                                              FileAttribute<?>... attrs) throws IOException {

        logger.info("The given path is {}", path);

        var normalizePath = getNormalizePath(path);

        logger.info("The path normalized as {}", normalizePath);

        var api = getBoxAPIConnection();

        return BoxSeekableByteChannel.getBoxFileChannel(getFileSysFileInfo(path), api);
    }

    @Override
    public void log_info(String format, Object... arguments) {
        logger.info(format,arguments);
    }

    @Override
    public FileSysFileInfo getFileSysFileInfo(BoxPath path) {

        logger.info("The given path is {}", path);

        var normalizePath = getNormalizePath(path);

        logger.info("The path normalized as {}", normalizePath);

        var nameFromPath = normalizePath.getFileName();

        logger.info("Name from path is {}", nameFromPath);

        var api = getBoxAPIConnection();

        var parent = normalizePath.getParent();
        var pathNames = parent.getNames();

        var parentResource = boxFsTreeWalker.walk(pathNames);

        if (parentResource instanceof JadFsResource.NullJadFsResource) {
            throw new JadNioFsParentPathInvalidException("Parent path is not present in remote account.");
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

        return new FileSysFileInfo(name, id, parentResource.id, size);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(BoxPath dir, DirectoryStream.Filter<? super Path> filter) {

        logger.info("The given path is {}", dir);

        var normalizePath = getNormalizePath(dir);

        logger.info("The path normalized as {}", dir);

        var api = getBoxAPIConnection();

        var pathNames = normalizePath.getNames();

        var resourceInBox = boxFsTreeWalker.walk(pathNames);

        if (resourceInBox instanceof JadFsResource.NullJadFsResource) {
            throw new JadNioFsParentPathInvalidException("Path is not present in remote account.");
        }

        if (resourceInBox.resourceType == JadFsResourceType.File) {
            throw new JadNioFsNotADirectoryException(String.format("Resource path not a directory '%s' ", normalizePath));
        }

        var folder = new BoxFolder(api, resourceInBox.id);
        return new BoxDirectoryStream(folder, dir, filter);
    }

    @Override
    public String getSessionName() {
        return ((AbstractAuthenticatedSession) this.authenticatedSession).getName();
    }

    @Override
    public String getCurrentUserId() {
        return ((AbstractAuthenticatedSession) this.authenticatedSession).getId();
    }

    @Override
    public FileSysUserInfo getFileSysUserInfo() {
        var currentUserId = getCurrentUserId();

        var api = getBoxAPIConnection();

        var boxUser = new BoxUser(api,currentUserId);

        var boxUserInfo = boxUser.getInfo("id", "name", "login", "space_amount", "space_used", "created_at");

        var info = new FileSysUserInfo(boxUserInfo.getID(), boxUserInfo.getName(),
                boxUserInfo.getLogin(), boxUserInfo.getSpaceAmount(),
                boxUserInfo.getSpaceUsed(), boxUserInfo.getCreatedAt());

        return info;
    }

    private BoxAPIConnection getBoxAPIConnection() {
        var session = getAuthenticatedSession();
        return session.getBoxAPIConnection();
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

    private static BoxFolder.Info createFolderResource(JadFsResource resource, BoxPath normalizePath, BoxAPIConnection api) {
        if (resource instanceof JadFsResource.NullJadFsResource) {
            throw new JadNioFsParentPathInvalidException("Parent path is not present in remote account.");
        }

        var current = normalizePath.getFileName();

        var parentFolder = new BoxFolder(api, resource.id);
        var iterable =  parentFolder.getChildren("name");
        for (BoxItem.Info item : iterable) {
            if (item.getName().equals(current.toString()) && item instanceof BoxFolder.Info) {
                throw new JadNioFsFileAlreadyExistsFoundException("Folder already exists");
            }
        }

        return parentFolder.createFolder(current.toString());
    }

    private static void deleteResource(JadFsResource resource, BoxAPIConnection api) {
        if (resource instanceof JadFsResource.NullJadFsResource) {
            throw new JadNioFsFileNotFoundException("Folder is not present in remote account.");
        }

        if (resource.resourceType == JadFsResourceType.Folder) {
            var folder = new BoxFolder(api, resource.id);
            folder.delete(true);
        } else {
            var file = new BoxFile(api, resource.id);
            file.delete();
        }

    }

    private static BoxItem.Info actOnSourceTargetResources(JadFsResource sourceResource,
                                                           JadFsResource targetResource,
                                                           BoxAPIConnection api,
                                                           BiFunction<BoxFile, BoxFolder, BoxItem.Info> fileToFolderStrategy,
                                                           BiFunction<BoxFolder, BoxFolder, BoxItem.Info> folderToFolderStrategy) {

        if (sourceResource instanceof JadFsResource.NullJadFsResource) {
            throw new JadNioFsFileNotFoundException("Source path is not present in remote account.");
        }

        if (targetResource instanceof JadFsResource.NullJadFsResource) {
            throw new JadNioFsFileNotFoundException("Target path is not present in remote account.");
        }

        if (sourceResource.resourceType == JadFsResourceType.Folder
            && targetResource.resourceType == JadFsResourceType.Folder) {

            var sourceFolder = new BoxFolder(api, sourceResource.id);
            var targetFolder = new BoxFolder(api, targetResource.id);

            return folderToFolderStrategy.apply(sourceFolder, targetFolder);
        } else if (sourceResource.resourceType == JadFsResourceType.File
            && targetResource.resourceType == JadFsResourceType.Folder) {

            var sourceFile = new BoxFile(api, sourceResource.id);
            var targetFolder = new BoxFolder(api, targetResource.id);

            return fileToFolderStrategy.apply(sourceFile, targetFolder);

        } else {
            throw new IllegalStateException("Source and Target combination not understood.");
        }

    }
}
