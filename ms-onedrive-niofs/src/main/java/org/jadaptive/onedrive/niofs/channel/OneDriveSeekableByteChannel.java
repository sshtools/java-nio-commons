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
package org.jadaptive.onedrive.niofs.channel;

import org.jadaptive.api.channel.FileRangeToRead;
import org.jadaptive.api.file.FileSysFileInfo;
import org.jadaptive.niofs.io.ChannelBufferWrapperInputStream;
import org.jadaptive.niofs.io.ChannelBufferWrapperOutputStream;
import org.jadaptive.onedrive.niofs.api.http.ChunkedFileDownload;
import org.jadaptive.onedrive.niofs.api.http.ChunkedFileUpload;
import org.jadaptive.onedrive.niofs.api.http.client.OneDriveHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class OneDriveSeekableByteChannel implements SeekableByteChannel {

    private static final Logger logger = LoggerFactory.getLogger(OneDriveSeekableByteChannel.class);

    private final OneDriveHttpClient oneDriveHttpClient;
    private long position;
    private FileSysFileInfo oneDriveFileInfo;

    private final Lock lock = new ReentrantLock();

    private final ChunkedFileDownload chunkedFileDownload;

    private final ChunkedFileUpload chunkedFileUpload;

    public OneDriveSeekableByteChannel(FileSysFileInfo oneDriveFileInfo, Optional<String> downloadUrl,
                                       String uploadUrl,
                                       OneDriveHttpClient oneDriveHttpClient,
                                       long position) {
        this.oneDriveHttpClient = oneDriveHttpClient;
        this.oneDriveFileInfo = oneDriveFileInfo;
        this.chunkedFileDownload = new ChunkedFileDownload(downloadUrl, this.oneDriveHttpClient);
        this.chunkedFileUpload = new ChunkedFileUpload(uploadUrl, this.oneDriveHttpClient);
        this.position = position;
    }

    public OneDriveSeekableByteChannel(FileSysFileInfo oneDriveFileInfo, Optional<String> downloadUrl,
                                       String uploadUrl,
                                       OneDriveHttpClient oneDriveHttpClient) {
        this(oneDriveFileInfo, downloadUrl, uploadUrl, oneDriveHttpClient, 0);
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        try {
            lock.lock();

            if (this.oneDriveFileInfo.isNotPresent()) {
                throw new FileNotFoundException(String.format("The file with id %s is not present.",
                        this.oneDriveFileInfo.getFileId()));
            }

            var fileRangeToRead = new FileRangeToRead();
            fileRangeToRead.compute(this, dst.capacity());

            var bytesToRead = fileRangeToRead.getBytesToRead();
            var indexOfRangeToRead = fileRangeToRead.getIndexOfRangeToRead();

            if (bytesToRead == -1) {
                return -1;
            }

            logger.debug("Bytes to read is {} , position is {} and index of range to read is {}",
                    bytesToRead, this.position, indexOfRangeToRead);

            // download chunk and put in buffer
            var byteStream = new ChannelBufferWrapperOutputStream(dst);
            this.chunkedFileDownload.downloadRange(byteStream,this.position, fileRangeToRead.getIndexOfRangeToRead());

            this.position = (int) (fileRangeToRead.getIndexOfRangeToRead() + 1);

            logger.debug("File chunk downloaded and position updated to {}",
                    this.position);

            return fileRangeToRead.getBytesToRead();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Note: This functions assumes you will provide data bytes to be written as one chunk
     * of ByteBuffer, true seekable write is not supported.
     *
     * @param src
     *         The buffer from which bytes are to be retrieved
     *
     * @return
     * @throws IOException
     */
    @Override
    public int write(ByteBuffer src) throws IOException {
        try {
            lock.lock();

            var parentId = this.oneDriveFileInfo.getParentId();

            if (parentId == null) {
                throw new IllegalStateException("No parent folder information.");
            }

            var size = src.capacity();

            logger.info("Buffer capacity is {}", size);

            var byteStream = new ChannelBufferWrapperInputStream(src);

            var writeInfo = this.chunkedFileUpload.upload(byteStream);

            this.oneDriveFileInfo = new FileSysFileInfo(writeInfo.getJadFsResource().name,
                    writeInfo.getJadFsResource().id, parentId, writeInfo.getTotalBytes());
            this.position = writeInfo.getTotalBytes();

            return size;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public long position() throws IOException {
        return this.position;
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        return null;
    }

    @Override
    public long size() throws IOException {
        return this.oneDriveFileInfo.getSize();
    }

    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void close() throws IOException {

    }
}
