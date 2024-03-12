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
package org.jadaptive.onedrive.niofs.watcher;

import com.azure.core.credential.AccessToken;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import org.jadaptive.niofs.watcher.AbstractWatchServiceTest;
import org.jadaptive.niofs.watcher.PollingWatchService;
import org.jadaptive.onedrive.niofs.api.OneDriveFileSysRemoteAPI;
import org.jadaptive.onedrive.niofs.api.OneDriveRemoteAPICaller;
import org.jadaptive.onedrive.niofs.api.client.locator.OneDriveConnectionAPILocator;
import org.jadaptive.onedrive.niofs.filesys.OneDriveFileSystem;
import org.jadaptive.onedrive.niofs.filesysprovider.OneDriveFileSystemProvider;
import org.jadaptive.onedrive.niofs.path.OneDrivePathService;
import org.junit.jupiter.api.TestInstance;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OneDriveWatchServiceTest extends AbstractWatchServiceTest {

    private static final String BASE_DIRECTORY = "onedrive:///test_one_drive_watch_service";

    @Override
    protected PollingWatchService getWatchService() {
        return new OneDriveWatcherService(30);
    }

    @Override
    protected FileSystemProvider getNewFileSystemProvider() {
        var oneDriveRemoteAPI = OneDriveConnectionAPILocator.getOneDriveRemoteAPI();
        var pathService = new OneDrivePathService();
        var provider = new OneDriveFileSystemProvider();


        var fs = new OneDriveFileSystem(provider, pathService, oneDriveRemoteAPI);
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
            throw new IllegalStateException("One Drive Oauth Access Token is null or empty.");
        }

        var graphServiceClient = new GraphServiceClient(request ->
                Mono.just(new AccessToken(token,
                        OffsetDateTime.now().plus(30, ChronoUnit.MINUTES))),
                "User.Read", "Files.Read", "Files.Read.All", "Files.ReadWrite.All");


        var apiCaller = new OneDriveRemoteAPICaller(graphServiceClient);

        var oneDriveRemoteAPI = new OneDriveFileSysRemoteAPI(apiCaller);

        OneDriveConnectionAPILocator.setOneDriveRemoteAPI(oneDriveRemoteAPI);

        setUpTestData();
    }

}