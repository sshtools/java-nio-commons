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
package org.jadaptive.onedrive.niofs.attr;

import org.jadaptive.api.FileSystemRemoteAPI;
import org.jadaptive.onedrive.niofs.path.OneDrivePath;

import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public class OneDriveNioFileAttributeView implements BasicFileAttributeView {

    private final FileSystemRemoteAPI<OneDrivePath> oneDriveRemoteAPI;

    private final OneDrivePath path;

    public OneDriveNioFileAttributeView(FileSystemRemoteAPI<OneDrivePath> oneDriveRemoteAPI, OneDrivePath path) {
        this.oneDriveRemoteAPI = oneDriveRemoteAPI;
        this.path = path;
    }

    @Override
    public String name() {
        return "basic";
    }

    @Override
    public BasicFileAttributes readAttributes() {
        return this.oneDriveRemoteAPI.readAttributes(path);
    }

    @Override
    public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime) {
        throw new UnsupportedOperationException("No remote api support.");
    }
}
