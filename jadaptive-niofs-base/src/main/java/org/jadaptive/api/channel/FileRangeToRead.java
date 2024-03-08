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
package org.jadaptive.api.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

public class FileRangeToRead {

    private static final Logger logger = LoggerFactory.getLogger(FileRangeToRead.class);

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
