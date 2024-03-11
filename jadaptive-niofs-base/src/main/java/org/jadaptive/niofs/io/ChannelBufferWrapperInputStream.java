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
package org.jadaptive.niofs.io;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChannelBufferWrapperInputStream extends InputStream {

    private final ByteBuffer buffer;
    private int counter;

    private Lock lock = new ReentrantLock();

    public ChannelBufferWrapperInputStream(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public int read() {
        try {
            lock.lock();
            if (counter >= buffer.capacity()) return -1;
            return buffer.get(counter++);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int available() {
        return buffer.capacity();
    }
}
