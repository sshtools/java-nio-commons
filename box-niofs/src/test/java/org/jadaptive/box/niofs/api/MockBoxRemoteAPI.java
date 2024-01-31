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

import org.jadaptive.box.niofs.api.user.BoxUserInfo;
import org.jadaptive.box.niofs.path.BoxPath;

import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;
import java.util.Set;

public class MockBoxRemoteAPI implements BoxRemoteAPI {
    @Override
    public void createDirectory(BoxPath dir, FileAttribute<?>... attrs) {

    }

    @Override
    public void delete(BoxPath path) {

    }

    @Override
    public void copy(BoxPath source, BoxPath target, CopyOption[] options) {

    }

    @Override
    public void move(BoxPath source, BoxPath target, CopyOption[] options) {

    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(BoxPath path, LinkOption... options) {
        return null;
    }

    @Override
    public Map<String, Object> readAttributes(BoxPath path, String attributes, LinkOption... options) {
        return null;
    }

    @Override
    public SeekableByteChannel newByteChannel(BoxPath path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) {
        return null;
    }

    @Override
    public BoxFileInfo getBoxFileInfo(BoxPath path) {
        return null;
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(BoxPath dir, DirectoryStream.Filter<? super Path> filter) {
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
    public BoxUserInfo getBoxUserInfo() {
        return null;
    }
}
