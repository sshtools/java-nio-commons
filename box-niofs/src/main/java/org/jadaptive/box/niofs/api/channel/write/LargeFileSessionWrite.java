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
package org.jadaptive.box.niofs.api.channel.write;

import com.box.sdk.*;
import org.jadaptive.api.file.FileSysFileInfo;
import org.jadaptive.niofs.io.ChannelBufferWrapperInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Objects;

import static java.lang.String.format;

public class LargeFileSessionWrite implements FileWriteDelegate {

    private static final Logger logger = LoggerFactory.getLogger(LargeFileSessionWrite.class);

    private final BoxAPIConnection api;
    private final FileSysFileInfo fileInfo;

    public LargeFileSessionWrite(BoxAPIConnection api, FileSysFileInfo fileInfo) {
        this.api = api;
        this.fileInfo = fileInfo;
    }
    @Override
    public WriteInfo write(ByteBuffer src) {

        Objects.requireNonNull(src, "Byte source cannot be null.");

        var capacity =  src.capacity();

        //  File size is less than minimum allowed for this API: 20000000
        if (capacity < 20000000) {
            throw new IllegalArgumentException(
                    format("File size less than API requirement of 20000000 is %s", capacity)
            );
        }

        var sessionInfo = createUploadSession(capacity);

        //Get the session resource from the session info
        var session = sessionInfo.getResource();

        //Create the Message Digest for the whole file
        var digest = getMessageDigest();

        var byteStream = new ChannelBufferWrapperInputStream(src);

        //Create the digest input stream to calculate the digest for the whole file.
        var dis = new DigestInputStream(byteStream, digest);

        var parts = new ArrayList<BoxFileUploadSessionPart>();

        //Get the part size. Each uploaded part should match the part size returned as part of the upload session.
        //The last part of the file can be less than part size if the remaining bytes of the last part is less than
        //the given part size
        var partSize = (long) sessionInfo.getPartSize();
        //Start byte of the part
        var offset = 0;
        //Overall of bytes processed so far
        var processed = 0;

        while (processed < capacity) {
            long diff = capacity - processed;
            //The size last part of the file can be less than the part size.
            if (diff < partSize) {
                partSize = diff;
            }

            //Upload a part. It can be uploaded asynchronously
            var part = session.uploadPart(dis, offset, (int)partSize, capacity);
            parts.add(part);

            logger.debug("Uploaded part {} with digest {}", part.getPartId(), part.getSha1());

            //Increase the offset and processed bytes to calculate the Content-Range header.
            processed += partSize;
            offset += partSize;
        }

        //Creates the file hash
        byte[] digestBytes = dis.getMessageDigest().digest();
        //Base64 encoding of the hash
        var digestStr = Base64.getEncoder().encodeToString(digestBytes);

        //Commit the upload session. If there is a failure, abort the commit.
        var fileInfo = session.commit(digestStr, parts, null, null, null);

        logger.info("File uploaded with id {} and name {}", fileInfo.getID(), fileInfo.getName());

        return new WriteInfo(fileInfo, capacity);
    }

    private BoxFileUploadSession.Info createUploadSession(int fileSize) {
        BoxFileUploadSession.Info sessionInfo;
        if (this.fileInfo.isNotPresent()) {
            // Create the upload session for a new file
            BoxFolder parentFolder = new BoxFolder(this.api, this.fileInfo.getParentId());
            sessionInfo = parentFolder.createUploadSession(this.fileInfo.getName(), fileSize);
        } else /* uploading a new version of an exiting file */ {
            // Create the upload session for a new version of an existing file
            String fileID = fileInfo.getFileId();
            BoxFile file = new BoxFile(api, fileID);
            sessionInfo = file.createUploadSession(fileSize);
        }
        return sessionInfo;
    }

    private static MessageDigest getMessageDigest() {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException ae) {
            throw new IllegalStateException("Digest algorithm not found", ae);
        }
        return digest;
    }
}
