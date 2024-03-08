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
package org.jadaptive.onedrive.niofs.filestore;

import org.jadaptive.api.FileSystemRemoteAPI;
import org.jadaptive.onedrive.niofs.path.OneDrivePath;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class OneDriveFileStore extends FileStore {

    private final FileSystemRemoteAPI<OneDrivePath> oneDriveRemoteAPI;

    private volatile String name;

    private final Lock lock = new ReentrantLock();

    public OneDriveFileStore(FileSystemRemoteAPI<OneDrivePath> oneDriveRemoteAPI) {
        this.oneDriveRemoteAPI = oneDriveRemoteAPI;
    }
    @Override
    public String name() {
        if (name == null) {
            lock.lock();
            try {
                if (name == null) {
                    name = String.format("onedrive-%s", this.oneDriveRemoteAPI.getSessionName());
                }
            } finally {
                lock.unlock();
            }
        }

        return name;
    }

    @Override
    public String type() {
        return "remote";
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public long getTotalSpace() throws IOException {
        return this.oneDriveRemoteAPI.getFileSysUserInfo().getSpaceAmount();
    }

    @Override
    public long getUsableSpace() throws IOException {
        return this.oneDriveRemoteAPI.getFileSysUserInfo().getSpaceAmount();
    }

    @Override
    public long getUnallocatedSpace() throws IOException {
        var oneDriveUserInfo = this.oneDriveRemoteAPI.getFileSysUserInfo();
        return oneDriveUserInfo.getSpaceAmount() - oneDriveUserInfo.getSpaceUsed();
    }

    @Override
    public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
        return BasicFileAttributeView.class.equals(type);
    }

    @Override
    public boolean supportsFileAttributeView(String name) {
        return name.equals("basic");
    }

    @Override
    public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> type) {
        return null; // no supported views
    }

    @Override
    public Object getAttribute(String attribute) throws IOException {
        throw new UnsupportedOperationException();
    }
}
