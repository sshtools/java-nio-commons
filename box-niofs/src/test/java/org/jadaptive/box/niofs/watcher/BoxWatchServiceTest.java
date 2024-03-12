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
package org.jadaptive.box.niofs.watcher;

import org.jadaptive.box.niofs.api.DeveloperTokenRemoteAPI;
import org.jadaptive.box.niofs.api.client.locator.BoxConnectionAPILocator;
import org.jadaptive.box.niofs.filesys.BoxFileSystem;
import org.jadaptive.box.niofs.filesysprovider.BoxFileSystemProvider;
import org.jadaptive.box.niofs.path.BoxPathService;
import org.jadaptive.niofs.watcher.AbstractWatchServiceTest;
import org.jadaptive.niofs.watcher.PollingWatchService;
import org.junit.jupiter.api.TestInstance;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BoxWatchServiceTest extends AbstractWatchServiceTest {

    private static final String BASE_DIRECTORY = "box:///test_box_watch_service";

    @Override
    protected PollingWatchService getWatchService() {
        return new BoxWatcherService(30);
    }

    @Override
    protected FileSystemProvider getNewFileSystemProvider() {
        var boxRemoteAPI = BoxConnectionAPILocator.getBoxRemoteAPI();
        var pathService = new BoxPathService();
        var provider = new BoxFileSystemProvider();


        var fs = new BoxFileSystem(provider, pathService, boxRemoteAPI);
        pathService.setFileSystem(fs);

        return provider;
    }

    @Override
    public String getBaseDirectory() {
        return BASE_DIRECTORY;
    }

    @Override
    protected void init() throws Exception {
        // this check is not thread safe (in case parallel threads for running tests)
        token = Files.readString(Path.of("src/test/resources/config/dev_token"));
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalStateException("Box Developer Token is null or empty.");
        }

        BoxConnectionAPILocator.setBoxRemoteAPI(new DeveloperTokenRemoteAPI(token));

        setUpTestData();
    }

}