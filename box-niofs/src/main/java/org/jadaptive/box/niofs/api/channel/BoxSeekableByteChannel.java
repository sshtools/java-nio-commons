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
package org.jadaptive.box.niofs.api.channel;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFile;
import org.jadaptive.api.file.FileSysFileInfo;
import org.jadaptive.box.niofs.api.channel.write.LargeFileSessionWrite;
import org.jadaptive.box.niofs.api.channel.write.SmallFileWrite;
import org.jadaptive.box.niofs.api.channel.write.WriteInfo;
import org.jadaptive.box.niofs.api.io.ChannelBufferWrapperOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BoxSeekableByteChannel implements SeekableByteChannel {

    private static final Logger logger = LoggerFactory.getLogger(BoxSeekableByteChannel.class);

    private BoxFile boxFile;
    private final BoxAPIConnection api;
    private FileSysFileInfo boxFileInfo;
    private long position;
    private final Lock lock = new ReentrantLock();


    private BoxSeekableByteChannel(BoxAPIConnection api, FileSysFileInfo boxFileInfo) {
        this.api = api;
        this.boxFileInfo = boxFileInfo;
        if (this.boxFileInfo.isPresent()) {
            this.boxFile = new BoxFile(api,boxFileInfo.getFileId());
        }
        this.position = 0;
    }

    private BoxSeekableByteChannel(BoxAPIConnection api, FileSysFileInfo boxFileInfo, long position) {
        this(api, boxFileInfo);
        this.position = position;
    }

    public static SeekableByteChannel getBoxFileChannel(FileSysFileInfo boxFileInfo, BoxAPIConnection api) {
        return new BoxSeekableByteChannel(api, boxFileInfo);
    }

    public static SeekableByteChannel getBoxFileChannel(FileSysFileInfo boxFileInfo, BoxAPIConnection api, long position) {
        return new BoxSeekableByteChannel(api, boxFileInfo, position);
    }


    @Override
    public int read(ByteBuffer dst) throws IOException {

        try {
            lock.lock();

            if (this.boxFileInfo.isNotPresent()) {
                throw new FileNotFoundException(String.format("The file with id %s is not present.",
                        this.boxFileInfo.getFileId()));
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
            this.boxFile.downloadRange(byteStream, this.position, fileRangeToRead.indexOfRangeToRead);

            this.position = (int) (fileRangeToRead.indexOfRangeToRead + 1);

            logger.debug("File chunk downloaded and position updated to {}",
                    this.position);

            return fileRangeToRead.bytesToRead;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int write(ByteBuffer src) throws IOException {

        try {
            lock.lock();

            var parentId = this.boxFileInfo.getParentId();

            if (parentId == null) {
                throw new IllegalStateException("No parent folder information.");
            }

            var size = src.capacity();

            logger.info("Buffer capacity is {}", size);

            WriteInfo writeInfo;

            if (size < 20000000) {
                var smallFileDelegate = new SmallFileWrite(api,this.boxFileInfo,this.boxFile);
                writeInfo =  smallFileDelegate.write(src);
            } else {
                var largeWriteDelegate = new LargeFileSessionWrite(api, boxFileInfo);
                writeInfo =  largeWriteDelegate.write(src);
            }

            var info = writeInfo.getFileInfo();

            this.boxFileInfo = new FileSysFileInfo(info.getName(), info.getID(), parentId, info.getSize());
            this.boxFile = new BoxFile(api, info.getID());
            this.position = info.getSize();

            return writeInfo.getCapacity();
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

        if (newPosition < 0) {
            throw new IllegalArgumentException("Position cannot be negative");
        }

        var size = this.boxFileInfo.getSize();

        if (newPosition > size) {
            logger.warn("New position {} is greater than the current file size {}.", newPosition, size);
        }

        return BoxSeekableByteChannel.getBoxFileChannel(this.boxFileInfo, this.api, newPosition);
    }

    @Override
    public long size() throws IOException {
        return this.boxFileInfo.getSize();
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

    private static class FileRangeToRead {
        private int bytesToRead;
        private long indexOfRangeToRead;

        public void compute(SeekableByteChannel channel, int capacity) throws IOException {

            var size = channel.size();
            var position = channel.position();

            logger.debug("The file size is {} current position is {} and destination buffer capacity is {}",
                    size, position, capacity);

            var maxIndex = size - 1; // max index of file when read byte by byte
            if (position >= maxIndex) {

                logger.debug("File read done, the position is {} and max index is {}",
                        position, maxIndex);

                this.bytesToRead =  -1;
                this.indexOfRangeToRead = -1;

                return;
            }

            this.bytesToRead = capacity; // we will try to read bytes as per capacity of buffer
            if (position + bytesToRead > maxIndex) {
                this.indexOfRangeToRead = (int) maxIndex;
                // in case not much to read as capacity given.
                // basically last chunk remaining
                this.bytesToRead = (int) (size - position);
            } else {
                this.indexOfRangeToRead = position + (this.bytesToRead - 1);
            }
        }

        public int getBytesToRead() {
            return this.bytesToRead;
        }

        public long getIndexOfRangeToRead() {
            return this.indexOfRangeToRead;
        }
    }
}
