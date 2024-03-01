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
package org.jadaptive.onedrive.niofs.filesys;

import org.jadaptive.api.FileSystemRemoteAPI;
import org.jadaptive.niofs.filesys.BaseFileSystem;
import org.jadaptive.onedrive.niofs.filesysprovider.OneDriveFileSystemProvider;
import org.jadaptive.onedrive.niofs.filestore.OneDriveFileStore;
import org.jadaptive.onedrive.niofs.path.OneDrivePath;
import org.jadaptive.onedrive.niofs.path.OneDrivePathService;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.List;
import java.util.Set;

public class OneDriveFileSystem extends BaseFileSystem {

    private static final String SEPARATOR = "/";

    private final OneDriveFileSystemProvider oneDriveFileSystemProvider;

    private final FileSystemRemoteAPI<OneDrivePath> oneDriveRemoteAPI;

    private final OneDriveFileStore oneDriveFileStore;
    public OneDriveFileSystem(OneDriveFileSystemProvider oneDriveFileSystemProvider,
                              OneDrivePathService oneDrivePathService,
                              FileSystemRemoteAPI<OneDrivePath> oneDriveRemoteAPI) {
        super(oneDrivePathService);
        this.oneDriveFileSystemProvider = oneDriveFileSystemProvider;
        this.oneDriveRemoteAPI = oneDriveRemoteAPI;
        this.oneDriveFileStore = new OneDriveFileStore(this.oneDriveRemoteAPI);
    }

    @Override
    public OneDrivePathService getPathService() {
        return (OneDrivePathService) this.basePathService;
    }

    @Override
    public String getRegExFriendlySeparator() {
        return SEPARATOR;
    }

    @Override
    public OneDriveFileSystemProvider provider() {
        return this.oneDriveFileSystemProvider;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public String getSeparator() {
        return SEPARATOR;
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        return Set.of(getPathService().createRoot());
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return List.of(this.oneDriveFileStore);
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return Set.of("basic");
    }

    @NotNull
    @Override
    public Path getPath(@NotNull String first, @NotNull String... more) {
        return getPathService().getPath(first, more);
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        return getPathService().getPathMatcher(syntaxAndPattern);
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        return null;
    }

    @Override
    public WatchService newWatchService() throws IOException {
        return null;
    }
}
