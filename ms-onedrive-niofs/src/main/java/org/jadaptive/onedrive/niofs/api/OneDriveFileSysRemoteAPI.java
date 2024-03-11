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
package org.jadaptive.onedrive.niofs.api;

import com.microsoft.graph.models.DriveItem;
import org.jadaptive.api.FileSystemRemoteAPI;
import org.jadaptive.api.file.FileSysFileInfo;
import org.jadaptive.api.folder.JadFsResource;
import org.jadaptive.api.folder.JadFsResourceType;
import org.jadaptive.api.user.FileSysUserInfo;
import org.jadaptive.niofs.attr.JadNioFileAttributes;
import org.jadaptive.niofs.exception.JadNioFsFileAlreadyExistsFoundException;
import org.jadaptive.niofs.exception.JadNioFsFileNotFoundException;
import org.jadaptive.niofs.exception.JadNioFsNotADirectoryException;
import org.jadaptive.niofs.exception.JadNioFsParentPathInvalidException;
import org.jadaptive.onedrive.niofs.api.folder.OneDriveFsTreeWalker;
import org.jadaptive.onedrive.niofs.api.folder.OneDriveJadFsResourceMapper;
import org.jadaptive.onedrive.niofs.api.http.client.OneDriveHttpClient;
import org.jadaptive.onedrive.niofs.channel.OneDriveSeekableByteChannel;
import org.jadaptive.onedrive.niofs.path.OneDrivePath;
import org.jadaptive.onedrive.niofs.stream.OneDriveDirectoryStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.sql.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class OneDriveFileSysRemoteAPI implements FileSystemRemoteAPI<OneDrivePath> {

    private static final Logger logger = LoggerFactory.getLogger(OneDriveFileSysRemoteAPI.class);

    private final OneDriveRemoteAPICaller apiCaller;

    private final JadFsResource.JadFsTreeWalker oneDriveFolderTree;

    private final OneDriveHttpClient oneDriveHttpClient;


    public OneDriveFileSysRemoteAPI(OneDriveRemoteAPICaller apiCaller) {
        this.apiCaller = apiCaller;
        this.oneDriveFolderTree = new OneDriveFsTreeWalker(new OneDriveJadFsResourceMapper(this.apiCaller));
        this.oneDriveHttpClient = new OneDriveHttpClient();
    }

    @Override
    public void createDirectory(OneDrivePath path, FileAttribute<?>... attrs) {

        logger.info("The given path is {}", path);

        var normalizePath = getNormalizePath(path);

        logger.info("The path normalized as {}", normalizePath);

        var parent = normalizePath.getParent();
        var pathNames = parent.getNames();

        var parentResourceInOneDrive = oneDriveFolderTree.walk(pathNames);

        var created = createFolderResource(parentResourceInOneDrive, (OneDrivePath) normalizePath, apiCaller);

        logger.info("Folder created '{}' with id '{}'", created.getName(), created.getId());

    }

    @Override
    public void delete(OneDrivePath path) {

        logger.info("The given path is {}", path);

        var normalizePath = getNormalizePath(path);
        var pathNames = normalizePath.getNames();

        logger.info("The path normalized as {}", normalizePath);

        var resourceInOneDrive = oneDriveFolderTree.walk(pathNames);

        deleteResource(resourceInOneDrive, apiCaller);

        logger.info("OneDrive resource '{}' deleted with id '{}'", resourceInOneDrive.resourceType, resourceInOneDrive.id);
    }

    @Override
    public void copy(OneDrivePath source, OneDrivePath target, CopyOption... options) {
        var targetName = target.getFileName().toString();

        var pair = sourceTargetResources(oneDriveFolderTree, source, target);

        checkSourceTarget(pair.first, pair.second);

        apiCaller.copy(pair.first, pair.second, targetName);

    }

    @Override
    public void move(OneDrivePath source, OneDrivePath target, CopyOption... options) {
        var targetName = target.getFileName().toString();

        var pair = sourceTargetResources(oneDriveFolderTree, source, target);

        checkSourceTarget(pair.first, pair.second);

        var moved = apiCaller.move(pair.first, pair.second, targetName);

        logger.info("File moved '{}' with id '{}'", moved.getName(), moved.getId());
    }

    @Override
    public JadNioFileAttributes readAttributes(OneDrivePath path, LinkOption... options) {
        logger.info("The given path is {}", path);

        var normalizePath = getNormalizePath(path);

        logger.info("The path normalized as {}", normalizePath);

        var pathNames = normalizePath.getNames();

        var resource = oneDriveFolderTree.walk(pathNames);

        if (resource instanceof JadFsResource.NullJadFsResource) {
            throw new JadNioFsFileNotFoundException("Path is not present in remote account.");
        }

        return apiCaller.getJadAttributesForPath(resource, options);
    }

    @Override
    public SeekableByteChannel newByteChannel(OneDrivePath path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws FileNotFoundException, IOException {

        logger.info("The given path is {}", path);

        var normalizePath = getNormalizePath(path);

        var resource = oneDriveFolderTree.walk(normalizePath.getNames());

        var downloadUrl = Optional.<String>empty();

        if (!JadFsResource.isNullResource(resource)) {
            downloadUrl = Optional.ofNullable(apiCaller.getDriveItemDownloadUrl(resource));
        }

        var uploadUrl = apiCaller.getUploadUrl(normalizePath.toString());

        var fileInfo = getFileSysFileInfo(path);

        return new OneDriveSeekableByteChannel(fileInfo, downloadUrl, uploadUrl, this.oneDriveHttpClient);
    }

    @Override
    public FileSysFileInfo getFileSysFileInfo(OneDrivePath path) {
        logger.info("The given path is {}", path);

        var normalizePath = getNormalizePath(path);

        logger.info("The path normalized as {}", normalizePath);

        var nameFromPath = normalizePath.getFileName();

        logger.info("Name from path is {}", nameFromPath);

        var parent = normalizePath.getParent();
        var pathNames = parent.getNames();

        var parentResource = oneDriveFolderTree.walk(pathNames);

        if (parentResource instanceof JadFsResource.NullJadFsResource) {
            throw new JadNioFsParentPathInvalidException("Parent path is not present in remote account.");
        }

        var current = normalizePath.getFileName();

        var parentDriveItem = new DriveItem();

        parentDriveItem.setId(parentResource.id);
        parentDriveItem.setName(parentResource.name);

        var driveItems = apiCaller
                .getDriveItems(parentDriveItem);

        String id = null;
        long size = 0;
        String name = null;

        for (var item : driveItems) {
            if (item.getName().equals(current.toString())) {
                id = item.getId();
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
    public DirectoryStream<Path> newDirectoryStream(OneDrivePath dir, DirectoryStream.Filter<? super Path> filter) {
        logger.info("The given path is {}", dir);

        var normalizePath = getNormalizePath(dir);

        logger.info("The path normalized as {}", dir);

        var pathNames = normalizePath.getNames();

        var resourceInOneDrive = oneDriveFolderTree.walk(pathNames);

        if (resourceInOneDrive instanceof JadFsResource.NullJadFsResource) {
            throw new JadNioFsParentPathInvalidException("Path is not present in remote account.");
        }

        if (resourceInOneDrive.resourceType == JadFsResourceType.File) {
            throw new JadNioFsNotADirectoryException(String.format("Resource path not a directory '%s' ", normalizePath));
        }

        var folder = new DriveItem();
        folder.setId(resourceInOneDrive.id);
        folder.setName(resourceInOneDrive.name);

        var items = apiCaller.getDriveItems(folder);
        return new OneDriveDirectoryStream(items, dir, filter);
    }

    @Override
    public String getSessionName() {
        var oneDriveUser = apiCaller.getUser();
        Objects.requireNonNull(oneDriveUser,"One Drive User cannot be null.");
        return oneDriveUser.getUserPrincipalName();
    }

    @Override
    public String getCurrentUserId() {
        var oneDriveUser = apiCaller.getUser();
        Objects.requireNonNull(oneDriveUser,"One Drive User cannot be null.");
        return oneDriveUser.getId();
    }

    @Override
    public FileSysUserInfo getFileSysUserInfo() {
        var oneDriveUser = apiCaller.getUser();
        var oneDrive = apiCaller.getDrive();
        var quota = oneDrive.getQuota();

        var info = new FileSysUserInfo(oneDriveUser.getId(), oneDriveUser.getUserPrincipalName(),
                oneDriveUser.getDisplayName(), quota.getTotal(),
                quota.getUsed(), Date.from(oneDriveUser.getCreatedDateTime().toInstant()));

        return info;
    }

    @Override
    public void log_info(String format, Object... arguments) {
        logger.info(format,arguments);
    }

    private static DriveItem createFolderResource(JadFsResource parentResource, OneDrivePath normalizePath,
                                                       OneDriveRemoteAPICaller apiCaller) {
        if (parentResource instanceof JadFsResource.NullJadFsResource) {
            throw new JadNioFsParentPathInvalidException("Parent path is not present in remote account.");
        }

        var current = normalizePath.getFileName();
        var folderNameToCreate = current.toString();

        if (apiCaller
                .getDriveItems(parentResource)
                .stream()
                .anyMatch(driveItem -> driveItem.getName().equals(folderNameToCreate))) {
            throw new JadNioFsFileAlreadyExistsFoundException("Folder already exists");
        }

        return apiCaller.createFolder(folderNameToCreate, parentResource);
    }

    private void deleteResource(JadFsResource resourceInOneDrive, OneDriveRemoteAPICaller apiCaller) {
        if (resourceInOneDrive instanceof JadFsResource.NullJadFsResource) {
            throw new JadNioFsFileNotFoundException("Folder is not present in remote account.");
        }

        apiCaller.delete(resourceInOneDrive);
    }

    private void checkSourceTarget(JadFsResource sourceResource, JadFsResource targetResource) {

        if (sourceResource instanceof JadFsResource.NullJadFsResource) {
            throw new JadNioFsFileNotFoundException("Source path is not present in remote account.");
        }

        if (targetResource instanceof JadFsResource.NullJadFsResource) {
            throw new JadNioFsFileNotFoundException("Target path is not present in remote account.");
        }

    }
}
