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
package org.jadaptive.onedrive.niofs.api.client.locator;

import org.jadaptive.api.FileSystemRemoteAPI;
import org.jadaptive.onedrive.niofs.path.OneDrivePath;

import java.util.Objects;

public class OneDriveConnectionAPILocator {

    private static final OneDriveConnectionAPILocator INSTANCE = new OneDriveConnectionAPILocator();

    private FileSystemRemoteAPI<OneDrivePath> fileSystemRemoteAPI;

    private OneDriveConnectionAPILocator() {}

    public static FileSystemRemoteAPI<OneDrivePath> getOneDriveRemoteAPI() {
        return Objects.requireNonNull(INSTANCE.fileSystemRemoteAPI, "OneDrive Remote API is not set.");
    }

    public static void setOneDriveRemoteAPI(FileSystemRemoteAPI<OneDrivePath> oneDriveRemoteAPI) {
        Objects.requireNonNull(oneDriveRemoteAPI, "OneDrive Remote API cannot be null.");
        INSTANCE.fileSystemRemoteAPI = oneDriveRemoteAPI;
    }
}
