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
import org.jadaptive.api.user.FileSysUserInfo;
import org.jadaptive.niofs.exception.JadNioFsFileAlreadyExistsFoundException;
import org.jadaptive.niofs.exception.JadNioFsFileNotFoundException;
import org.jadaptive.niofs.exception.JadNioFsParentPathInvalidException;
import org.jadaptive.onedrive.niofs.api.folder.OneDriveFsTreeWalker;
import org.jadaptive.onedrive.niofs.api.folder.OneDriveJadFsResourceMapper;
import org.jadaptive.onedrive.niofs.path.OneDrivePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;
import java.util.Set;

public class OneDriveFileSysRemoteAPI implements FileSystemRemoteAPI<OneDrivePath> {

    private static final Logger logger = LoggerFactory.getLogger(OneDriveFileSysRemoteAPI.class);

    private final OneDriveRemoteAPICaller apiCaller;

    private final JadFsResource.JadFsTreeWalker oneDriveFolderTree;


    public OneDriveFileSysRemoteAPI(OneDriveRemoteAPICaller apiCaller) {
        this.apiCaller = apiCaller;
        oneDriveFolderTree = new OneDriveFsTreeWalker(new OneDriveJadFsResourceMapper(this.apiCaller));
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

    }

    @Override
    public void move(OneDrivePath source, OneDrivePath target, CopyOption... options) {

    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(OneDrivePath path, LinkOption... options) {
        return null;
    }

    @Override
    public Map<String, Object> readAttributes(OneDrivePath path, String attributes, LinkOption... options) {
        return null;
    }

    @Override
    public SeekableByteChannel newByteChannel(OneDrivePath path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws FileNotFoundException, IOException {
        return null;
    }

    @Override
    public FileSysFileInfo getFileSysFileInfo(OneDrivePath path) {
        return null;
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(OneDrivePath dir, DirectoryStream.Filter<? super Path> filter) {
        return null;
    }

    @Override
    public String getSessionName() {
        return null;
    }

    @Override
    public String getCurrentUserId() {
        return null;
    }

    @Override
    public FileSysUserInfo getFileSysUserInfo() {
        return null;
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

        var parentDriveItem = new DriveItem();
        parentDriveItem.setId(parentResource.id);
        parentDriveItem.setName(parentResource.name);

        if (apiCaller
                .getDriveItems(parentDriveItem)
                .stream()
                .anyMatch(driveItem -> driveItem.getName().equals(folderNameToCreate))) {
            throw new JadNioFsFileAlreadyExistsFoundException("Folder already exists");
        }

        return apiCaller.createFolder(folderNameToCreate, parentDriveItem);
    }

    private void deleteResource(JadFsResource resourceInOneDrive, OneDriveRemoteAPICaller apiCaller) {
        if (resourceInOneDrive instanceof JadFsResource.NullJadFsResource) {
            throw new JadNioFsFileNotFoundException("Folder is not present in remote account.");
        }

        var driveItem = new DriveItem();
        driveItem.setId(resourceInOneDrive.id);
        driveItem.setName(resourceInOneDrive.name);

        apiCaller.deleteDriveItem(driveItem);
    }
}
