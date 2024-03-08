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
package org.jadaptive.box.niofs.setup;

import org.jadaptive.box.niofs.api.DeveloperTokenRemoteAPI;
import org.jadaptive.box.niofs.api.client.locator.BoxConnectionAPILocator;
import org.jadaptive.box.niofs.filesys.BoxFileSystem;
import org.jadaptive.box.niofs.filesysprovider.BoxFileSystemProvider;
import org.jadaptive.box.niofs.path.BoxPath;
import org.jadaptive.box.niofs.path.BoxPathService;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public abstract class AbstractRemoteSetup {

    private String token;

    protected void init() throws Exception {
        // this check is not thread safe (in case parallel threads for running tests)
        token = Files.readString(Path.of("src/test/resources/config/dev_token"));
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalStateException("Box Developer Token is null or empty.");
        }

        BoxConnectionAPILocator.setBoxRemoteAPI(new DeveloperTokenRemoteAPI(token));

        setUpTestData();
    }

    protected void setUpTestData() throws Exception {}

    protected void writeFileInBox(BoxFileSystemProvider provider, String fileName) throws URISyntaxException, IOException {
        var fileWithContentPath = provider.getPath(new URI(String.format("box:///test_box/%s", fileName)));
        writeFileInBox(provider, fileWithContentPath, Path.of(String.format("src/test/resources/data/%s", fileName)));
    }

    protected void writeFileInBox(BoxFileSystemProvider provider, String directoryPrefix, String fileName) throws URISyntaxException, IOException {
        var fileWithContentPath = provider.getPath(new URI(String.format("box:///test_box/%s/%s", directoryPrefix, fileName)));
        writeFileInBox(provider, fileWithContentPath, Path.of(String.format("src/test/resources/data/%s", fileName)));
    }

    protected void writeFileInBox(BoxFileSystemProvider provider, BoxPath fileToWrite, Path fileToRead) throws IOException {
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

    protected BoxFileSystemProvider getNewBoxFileSystemProvider() {
        var boxRemoteAPI = BoxConnectionAPILocator.getBoxRemoteAPI();
        var pathService = new BoxPathService();
        var provider = new BoxFileSystemProvider();


        var fs = new BoxFileSystem(provider, pathService, boxRemoteAPI);
        pathService.setFileSystem(fs);

        return provider;
    }
}
