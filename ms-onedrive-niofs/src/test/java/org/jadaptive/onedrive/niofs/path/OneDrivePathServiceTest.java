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

import org.jadaptive.niofs.api.FileSystemRemoteAPIStub;
import org.jadaptive.niofs.path.BasePathService;
import org.jadaptive.niofs.path.BasePathServiceTest;
import org.jadaptive.onedrive.niofs.api.client.locator.OneDriveConnectionAPILocator;
import org.jadaptive.onedrive.niofs.filesys.OneDriveFileSystem;
import org.jadaptive.onedrive.niofs.filesysprovider.OneDriveFileSystemProvider;
import org.junit.jupiter.api.BeforeAll;

public class OneDrivePathServiceTest extends BasePathServiceTest {

    @BeforeAll
    static void init() {
        OneDriveConnectionAPILocator.setOneDriveRemoteAPI(new FileSystemRemoteAPIStub<>() {});
    }
    @Override
    protected BasePathService getNewBasePathService() {
        var oneDriveRemoteAPI = OneDriveConnectionAPILocator.getOneDriveRemoteAPI();
        var pathService = new OneDrivePathService();
        var provider = new OneDriveFileSystemProvider();

        var fs = new OneDriveFileSystem(provider, pathService, oneDriveRemoteAPI);
        pathService.setFileSystem(fs);

        return pathService;
    }
}
