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
package org.jadaptive.onedrive.niofs.filesysprovider;

import org.jadaptive.onedrive.niofs.api.client.locator.OneDriveConnectionAPILocator;
import org.jadaptive.onedrive.niofs.attr.OneDriveNioFileAttributeView;
import org.jadaptive.onedrive.niofs.filesys.OneDriveFileSystem;
import org.jadaptive.onedrive.niofs.path.OneDrivePath;
import org.jadaptive.onedrive.niofs.path.OneDrivePathService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class OneDriveFileSystemProvider extends FileSystemProvider {

    private static final Logger logger = LoggerFactory.getLogger(OneDriveFileSystemProvider.class);

    private volatile OneDriveFileSystem oneDriveFileSystem;

    private final Lock lock = new ReentrantLock();

    @Override
    public String getScheme() {
        return "onedrive";
    }

    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Path getPath(@NotNull URI uri) {
        if (oneDriveFileSystem == null) {
            initOneDriveFileSystem();
        }
        return oneDriveFileSystem.getPathService().getPath(uri);
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        checkPath(path);
        return oneDriveFileSystem.getOneDriveRemoteAPI().newByteChannel((OneDrivePath) path, options, attrs);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
        checkPath(dir);
        return oneDriveFileSystem.getOneDriveRemoteAPI().newDirectoryStream((OneDrivePath) dir, filter);
    }

    @Override
    public void createDirectory(Path path, FileAttribute<?>... attrs) throws IOException {
        checkPath(path);
        oneDriveFileSystem.getOneDriveRemoteAPI().createDirectory((OneDrivePath) path, attrs);
    }

    @Override
    public void delete(Path path) throws IOException {
        checkPath(path);
        oneDriveFileSystem.getOneDriveRemoteAPI().delete((OneDrivePath) path);
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        checkPath(source);
        checkPath(target);
        oneDriveFileSystem.getOneDriveRemoteAPI().copy((OneDrivePath) source, (OneDrivePath) target, options);
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        checkPath(source);
        checkPath(target);
        oneDriveFileSystem.getOneDriveRemoteAPI().move((OneDrivePath) source, (OneDrivePath) target, options);
    }

    @Override
    public boolean isSameFile(Path path1, Path path2) throws IOException {
        if(path1 instanceof OneDrivePath && path2 instanceof OneDrivePath && Files.exists(path1) && Files.exists(path2)) {
            var full1 = path1.normalize().toAbsolutePath();
            var full2 = path2.normalize().toAbsolutePath();
            return full1.equals(full2);
        } else
            return false;
    }

    @Override
    public boolean isHidden(Path path) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException {
        checkPath(path);
        return ((OneDriveFileSystem) path.getFileSystem()).getOneDriveFileStore();
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        checkPath(path);
        var file = oneDriveFileSystem.getOneDriveRemoteAPI().getFileSysFileInfo((OneDrivePath) path);

        if (file.getFileId() == null) {
            throw new FileNotFoundException(String.format("File not found %s.", path));
        }
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        checkPath(path);
        return (V) new OneDriveNioFileAttributeView(oneDriveFileSystem.getOneDriveRemoteAPI(), (OneDrivePath) path);
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
        checkPath(path);
        return oneDriveFileSystem.getOneDriveRemoteAPI().readAttributes((OneDrivePath) path, options);
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        checkPath(path);
        return oneDriveFileSystem.getOneDriveRemoteAPI().readAttributes((OneDrivePath) path, attributes, options);
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {

    }

    private void initOneDriveFileSystem() {
        if (oneDriveFileSystem == null) {
            try {
                lock.lock();
                if (oneDriveFileSystem == null) {
                    logger.info("File system does not exists, creating new.");
                    var oneDriveRemoteAPI = OneDriveConnectionAPILocator.getOneDriveRemoteAPI();
                    var pathService = new OneDrivePathService();
                    oneDriveFileSystem = new OneDriveFileSystem(this, pathService, oneDriveRemoteAPI);
                    pathService.setFileSystem(oneDriveFileSystem);
                }
            } finally {
                lock.unlock();
            }
        }
    }

    private void checkPath(Path dir) {
        if (!isSameInstance(dir)) {
            throw new IllegalArgumentException("Path is not an instance of OneDrivePath.");
        }
    }
    private boolean isSameInstance(Path path) {
        return path instanceof OneDrivePath;
    }
}
