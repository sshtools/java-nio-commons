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
package org.jadaptive.box.niofs.filestore;

import org.jadaptive.box.niofs.api.BoxRemoteAPI;

import java.nio.file.FileStore;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BoxFileStore extends FileStore {

    private final BoxRemoteAPI boxRemoteAPI;

    private volatile String name;

    private final Lock lock = new ReentrantLock();

    public BoxFileStore(BoxRemoteAPI boxRemoteAPI) {
        this.boxRemoteAPI = boxRemoteAPI;
    }

    @Override
    public String name() {

        if (name == null) {
            lock.lock();
            try {
                if (name == null) {
                    name = String.format("box-%s", this.boxRemoteAPI.getSessionName());
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
    public long getTotalSpace() {
        return this.boxRemoteAPI.getBoxUserInfo().getSpaceAmount();
    }

    @Override
    public long getUsableSpace() {
        return this.boxRemoteAPI.getBoxUserInfo().getSpaceAmount();
    }

    @Override
    public long getUnallocatedSpace() {
        var boxUserInfo = this.boxRemoteAPI.getBoxUserInfo();
        return boxUserInfo.getSpaceAmount() - boxUserInfo.getSpaceUsed();
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
    public Object getAttribute(String attribute) {
        throw new UnsupportedOperationException();
    }
}
