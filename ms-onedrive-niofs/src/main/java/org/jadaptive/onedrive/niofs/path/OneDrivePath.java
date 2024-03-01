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
package org.jadaptive.onedrive.niofs.path;

import org.jadaptive.niofs.path.BasePath;
import org.jadaptive.onedrive.niofs.filesys.OneDriveFileSystem;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;

public class OneDrivePath extends BasePath {

    public OneDrivePath(OneDrivePathService oneDrivePathService, String root, List<String> names) {
        super(oneDrivePathService, root, names);
    }

    @Override
    protected boolean isSameInstance(Object path) {
        return path instanceof OneDrivePath;
    }

    @NotNull
    @Override
    public OneDriveFileSystem getFileSystem() {
        return (OneDriveFileSystem) getBasePathService().getFileSystem();
    }

    @NotNull
    @Override
    public WatchKey register(@NotNull WatchService watcher, @NotNull WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
        return null;
    }
}
