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
package org.jadaptive.niofs.api;

import org.jadaptive.api.FileSystemRemoteAPI;
import org.jadaptive.api.file.FileSysFileInfo;
import org.jadaptive.api.user.FileSysUserInfo;
import org.jadaptive.niofs.path.BasePath;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.Set;

public class FileSystemRemoteAPIStub<T extends BasePath> implements FileSystemRemoteAPI<T> {

    @Override
    public void createDirectory(T path, FileAttribute<?>... attrs) {

    }

    @Override
    public void delete(T path) {

    }

    @Override
    public void copy(T source, T target, CopyOption... options) {

    }

    @Override
    public void move(T source, T target, CopyOption... options) {

    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(T path, LinkOption... options) {
        return null;
    }

    @Override
    public SeekableByteChannel newByteChannel(T path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws FileNotFoundException, IOException {
        return null;
    }

    @Override
    public FileSysFileInfo getFileSysFileInfo(T path) {
        return null;
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(T dir, DirectoryStream.Filter<? super Path> filter) {
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

    }
}
