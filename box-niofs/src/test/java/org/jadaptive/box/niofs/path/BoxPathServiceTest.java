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
package org.jadaptive.box.niofs.path;

import org.jadaptive.box.niofs.api.MockBoxRemoteAPI;
import org.jadaptive.box.niofs.api.client.locator.BoxConnectionAPILocator;
import org.jadaptive.box.niofs.filesys.BoxFileSystem;
import org.jadaptive.box.niofs.filesysprovider.BoxFileSystemProvider;
import org.jadaptive.niofs.path.BasePathService;
import org.jadaptive.niofs.path.BasePathServiceTest;
import org.junit.jupiter.api.BeforeAll;

public class BoxPathServiceTest extends BasePathServiceTest {

    @BeforeAll
    static void init() {
        BoxConnectionAPILocator.setBoxRemoteAPI(new MockBoxRemoteAPI() {});
    }

    @Override
    protected BasePathService getNewBasePathService() {
        var boxRemoteAPI = BoxConnectionAPILocator.getBoxRemoteAPI();
        var pathService = new BoxPathService();
        var provider = new BoxFileSystemProvider();

        var fs = new BoxFileSystem(provider, pathService, boxRemoteAPI);
        pathService.setFileSystem(fs);

        return pathService;
    }

}
