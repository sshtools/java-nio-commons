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

import org.jadaptive.box.niofs.api.client.locator.BoxConnectionAPILocator;
import org.jadaptive.box.niofs.filesys.BoxFileSystem;
import org.jadaptive.box.niofs.filesysprovider.BoxFileSystemProvider;
import org.jadaptive.niofs.api.FileSystemRemoteAPIStub;
import org.jadaptive.niofs.filesys.BaseFileSystem;
import org.jadaptive.niofs.path.BasePathTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BoxPathTest extends BasePathTest {

    @BeforeAll
    static void init() {
        BoxConnectionAPILocator.setBoxRemoteAPI(new FileSystemRemoteAPIStub<>() {});
    }

    @Override
    public BaseFileSystem getFileSystem() {

        var boxRemoteAPI = BoxConnectionAPILocator.getBoxRemoteAPI();
        var pathService = new BoxPathService();
        var provider = new BoxFileSystemProvider();

        var fs = new BoxFileSystem(provider, pathService, boxRemoteAPI);
        pathService.setFileSystem(fs);

        return fs;
    }

    @Override
    @Test
    @DisplayName("Box path URI")
    public void testToUri() {
        test(fs -> {
           var uriString = fs.getPath("/", "some", "path", "to", "file").toUri().toString();
           assertEquals("box:///some/path/to/file", uriString);
        });
    }
}
