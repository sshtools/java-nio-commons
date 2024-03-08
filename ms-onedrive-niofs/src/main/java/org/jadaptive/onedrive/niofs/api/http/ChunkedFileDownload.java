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
package org.jadaptive.onedrive.niofs.api.http;

import org.jadaptive.onedrive.niofs.api.http.client.OneDriveHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class ChunkedFileDownload {

    private static final Logger logger = LoggerFactory.getLogger(ChunkedFileDownload.class);

    private final Optional<String> downloadUrl;

    private final OneDriveHttpClient oneDriveHttpClient;


    public ChunkedFileDownload(Optional<String> downloadUrl, OneDriveHttpClient oneDriveHttpClient) {
        this.downloadUrl = downloadUrl;
        this.oneDriveHttpClient = oneDriveHttpClient;
    }

    public void downloadRange(OutputStream output, long rangeStart, long rangeEnd) {

        try {
            logger.debug("Downloading range from {} to {}", rangeStart, rangeEnd);

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(this.downloadUrl.orElseThrow(() -> new IllegalStateException("Download URL cannot be empty."))))
                    .setHeader("Range", String.format("bytes=%s-%s", rangeStart, rangeEnd))
                    .GET()
                    .build();

            var httpResponse = this.oneDriveHttpClient
                    .httpClient()
                    .send(request, HttpResponse.BodyHandlers.ofInputStream());

            try (InputStream inputStream = httpResponse.body()) {
                inputStream.transferTo(output);
            }

        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }

    }
}
