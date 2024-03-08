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
package org.jadaptive.onedrive.niofs.setup;

import com.azure.core.credential.AccessToken;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import org.jadaptive.onedrive.niofs.api.OneDriveFileSysRemoteAPI;
import org.jadaptive.onedrive.niofs.api.OneDriveRemoteAPICaller;
import org.jadaptive.onedrive.niofs.api.client.locator.OneDriveConnectionAPILocator;
import org.jadaptive.onedrive.niofs.filesys.OneDriveFileSystem;
import org.jadaptive.onedrive.niofs.filesysprovider.OneDriveFileSystemProvider;
import org.jadaptive.onedrive.niofs.path.OneDrivePath;
import org.jadaptive.onedrive.niofs.path.OneDrivePathService;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;

public abstract class AbstractRemoteSetup {

    private String token;

    protected void init() throws Exception {
        // this check is not thread safe (in case parallel threads for running tests)
        token = Files.readString(Path.of("src/test/resources/config/dev_token"));
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalStateException("One Drive Oauth Access Token is null or empty.");
        }

        setUpConnectionAPILocator();

        setUpTestData();
    }

    protected void setUpTestData() throws Exception {}

    protected void writeFileInOneDrive(OneDriveFileSystemProvider provider, String fileName) throws URISyntaxException, IOException {
        var fileWithContentPath = provider.getPath(new URI(String.format("onedrive:///test_onedrive/%s", fileName)));
        writeFileInOneDrive(provider, fileWithContentPath, Path.of(String.format("src/test/resources/data/%s", fileName)));
    }

    protected void writeFileInOneDrive(OneDriveFileSystemProvider provider, String directoryPrefix, String fileName) throws URISyntaxException, IOException {
        var fileWithContentPath = provider.getPath(new URI(String.format("onedrive:///test_onedrive/%s/%s", directoryPrefix, fileName)));
        writeFileInOneDrive(provider, fileWithContentPath, Path.of(String.format("src/test/resources/data/%s", fileName)));
    }

    protected void writeFileInOneDrive(OneDriveFileSystemProvider provider, OneDrivePath fileToWrite, Path fileToRead) throws IOException {
        var channel = provider.newByteChannel(fileToWrite, Set.of());

        String s = Files.readString(fileToRead);
        ByteBuffer bfSrc = ByteBuffer.wrap(s.getBytes());
        channel.write(bfSrc);
    }

    protected URI toURI(String path) {
        try {
            return new URI(path);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    protected OneDriveFileSystemProvider getNewOneDriveFileSystemProvider() {
        var oneDriveRemoteAPI = OneDriveConnectionAPILocator.getOneDriveRemoteAPI();
        var pathService = new OneDrivePathService();
        var provider = new OneDriveFileSystemProvider();


        var fs = new OneDriveFileSystem(provider, pathService, oneDriveRemoteAPI);
        pathService.setFileSystem(fs);

        return provider;
    }

    private void setUpConnectionAPILocator() {
        var graphServiceClient = new GraphServiceClient(request ->
                Mono.just(new AccessToken(token,
                        OffsetDateTime.now().plus(30, ChronoUnit.MINUTES))),
                "User.Read", "Files.Read", "Files.Read.All", "Files.ReadWrite.All");


        var apiCaller = new OneDriveRemoteAPICaller(graphServiceClient);

        var oneDriveRemoteAPI = new OneDriveFileSysRemoteAPI(apiCaller);

        OneDriveConnectionAPILocator.setOneDriveRemoteAPI(oneDriveRemoteAPI);
    }
}
