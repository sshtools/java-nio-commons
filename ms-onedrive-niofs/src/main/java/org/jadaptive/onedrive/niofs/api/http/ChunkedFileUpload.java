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

import com.nimbusds.jose.util.JSONObjectUtils;
import org.jadaptive.api.folder.JadFsResource;
import org.jadaptive.api.folder.JadFsResourceType;
import org.jadaptive.onedrive.niofs.api.http.client.OneDriveHttpClient;
import org.jadaptive.onedrive.niofs.channel.write.WriteInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;

public class ChunkedFileUpload {

    private static final Logger logger = LoggerFactory.getLogger(ChunkedFileUpload.class);

    private final String uploadUrl;

    private final OneDriveHttpClient oneDriveHttpClient;

    private static final int CHUNK_UPLOAD_SIZE = 320 * 1000;

    public ChunkedFileUpload(String uploadUrl, OneDriveHttpClient oneDriveHttpClient) {
        this.uploadUrl = uploadUrl;
        this.oneDriveHttpClient = oneDriveHttpClient;
    }

    public WriteInfo upload(InputStream inputStream) throws IOException {

        var available = inputStream.available();

        logger.info("Total available bytes {}.", available);

        var start = 0;

        WriteInfo writeInfo = null;

        while (start < available) {
            var end = (start + CHUNK_UPLOAD_SIZE) - 1;

            logger.debug("The upload chunk range start is {} and end is {}.", start, end);

            if (end >= available) {
                end = available - 1;
                logger.debug("The last upload chunk range start is {} and end is {}.", start, end);
            }

            var data = new byte[(end - start) + 1];

            inputStream.read(data);

            writeInfo = uploadRange(data, start, end, available);

            start = end + 1;
        }

        return writeInfo;
    }

    public WriteInfo uploadRange(byte[] input, long rangeStart, long rangeEnd, long available) {

        try {

            logger.debug("Uploading range from {} to {}.", rangeStart, rangeEnd);

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(this.uploadUrl))
                    .setHeader("Content-Range", String.format("bytes %s-%s/%s", rangeStart, rangeEnd, available))
                    .PUT(HttpRequest.BodyPublishers.ofByteArray(input))
                    .build();

            var httpResponse = this.oneDriveHttpClient
                    .httpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            var body = httpResponse.body();
            var json = JSONObjectUtils.parse(body);

            return new WriteInfo(available, input.length, new JadFsResource(String.valueOf(json.get("id")),
                    String.valueOf(json.get("name")), JadFsResourceType.File));

        } catch (IOException | InterruptedException | ParseException e) {
            throw new IllegalStateException(e);
        }

    }
}
