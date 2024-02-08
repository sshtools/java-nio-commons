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
package org.jadaptive.niofs.watcher;

import org.jadaptive.niofs.path.BasePath;
import org.jadaptive.niofs.watcher.key.BaseWatchKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class BaseWatchService implements WatchService {

    private static final Logger logger = LoggerFactory.getLogger(BaseWatchService.class);

    protected final LinkedBlockingDeque<BaseWatchKey> pendingKeys = new LinkedBlockingDeque<>();

    protected final ScheduledExecutorService scheduledExecutor;

    protected final Map<Object, BaseWatchKey> map = new HashMap<>();

    // special key to indicate that watch service is closed
    private final BaseWatchKey CLOSE_KEY =
            new BaseWatchKey(null, this) {
                @Override
                public boolean isValid() {
                    return true;
                }

                @Override
                public void cancel() {
                }
            };

    private volatile boolean closed;
    private final Lock closeLock = new ReentrantLock();

    private final Lock mapLock = new ReentrantLock();

    protected abstract void implClose() throws IOException;

    public abstract WatchKey register(BasePath path, WatchEvent.Kind<?>[] events, WatchEvent.Modifier[] modifiers) throws IOException;

    public BaseWatchService() {
        scheduledExecutor = Executors
                .newSingleThreadScheduledExecutor(new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(null, r, getWatcherServiceName(), 0, false);
                        t.setDaemon(true);
                        return t;
                    }});
    }

    @Override
    public void close() throws IOException {

        closeLock.lock();

        try {
            // nothing to do if already closed
            if (closed)
                return;
            closed = true;

            implClose();

            // clear pending keys and queue special key to ensure that any
            // threads blocked in take/poll wakeup
            pendingKeys.clear();
            pendingKeys.offer(CLOSE_KEY);
        } finally {
             closeLock.unlock();
        }
    }

    @Override
    public WatchKey poll() {
        return pendingKeys.poll();
    }

    @Override
    public WatchKey poll(long timeout, TimeUnit unit) throws InterruptedException {
        return pendingKeys.poll(timeout, unit);
    }

    @Override
    public WatchKey take() throws InterruptedException {
        return pendingKeys.take();
    }

    public void removeFromMap(Object fileKey) {
        mapLock.lock();
        try {
            map.remove(fileKey);
        } finally {
            mapLock.unlock();
        }
    }


    // used by AbstractWatchKey to enqueue key
    public final void enqueueKey(BaseWatchKey key) {
        pendingKeys.offer(key);
    }

    /**
     * Tells whether this watch service is open.
     */
    protected boolean isOpen() {
        return !closed;
    }

    /**
     * Retrieves the object upon which the close method synchronizes.
     */
    protected Lock closeLock() {
        return closeLock;
    }

    public ScheduledExecutorService scheduledExecutor() {
        return scheduledExecutor;
    }

    public Map<Object, BaseWatchKey> map() {
        return map;
    }

    public Lock mapLock() {
        return mapLock;
    }

    protected abstract String getWatcherServiceName();
}
