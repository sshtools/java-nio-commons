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
package org.jadaptive.niofs.watcher.key;

import org.jadaptive.niofs.path.BasePath;
import org.jadaptive.niofs.watcher.PollingWatchService;
import org.jadaptive.niofs.watcher.cache.CacheEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PollingWatchKey extends BaseWatchKey {

    private static final Logger logger = LoggerFactory.getLogger(PollingWatchKey.class);

    private final Object fileKey;

    // current event set
    private Set<? extends WatchEvent.Kind<?>> events;

    // the result of the periodic task that causes this key to be polled
    private ScheduledFuture<?> poller;

    // indicates if the key is valid
    private volatile boolean valid;

    // used to detect files that have been deleted
    private int tickCount;

    // map of entries in directory
    private Map<Path, CacheEntry> entries;
    public PollingWatchKey(BasePath dir, PollingWatchService watcher, Object fileKey)
            throws IOException
    {
        super(dir, watcher);

        this.fileKey = fileKey;
        this.valid = true;
        this.tickCount = 0;
        this.entries = new HashMap<Path, CacheEntry>();

        // get the initial entries in the directory
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry: stream) {
                // don't follow links
                long lastModified =
                        Files.getLastModifiedTime(entry, LinkOption.NOFOLLOW_LINKS).toMillis();
                entries.put(entry.getFileName(), new CacheEntry(lastModified, tickCount));
            }
        } catch (DirectoryIteratorException e) {
            throw e.getCause();
        }
    }

    Object fileKey() {
        return fileKey;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    public void invalidate() {
        valid = false;
    }

    // enables periodic polling
    public void enable(Set<? extends WatchEvent.Kind<?>> events, long period) {
        lock.lock();
        try {
            // update the events
            this.events = events;
            logger.info("Scheduling poll to happen in {} seconds. ", period);
            // create the periodic task
            this.poller = watcher().scheduledExecutor()
                    .scheduleAtFixedRate(() -> poll(), period, period, TimeUnit.SECONDS);
        } finally {
            lock.unlock();
        }
    }

    // disables periodic polling
    public void disable() {
        lock.lock();
        try {
            if (poller != null)
                poller.cancel(false);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void cancel() {
        valid = false;
        watcher().mapLock().lock();
        try {
            watcher().map().remove(fileKey());
        } finally {
            watcher().mapLock().unlock();
        }
        disable();
    }

    /**
     * Polls the directory to detect for new files, modified files, or
     * deleted files.
     */
     protected void poll() {
        lock.lock();
        try {
            if (!valid) {
                return;
            }

            // update tick
            tickCount++;

            logger.debug("Tick count updated to {}", tickCount);

            // open directory
            DirectoryStream<Path> stream = null;
            try {
                stream = Files.newDirectoryStream((Path) watchable());
            } catch (IOException x) {
                logger.error("Problem opening directory stream.", x);
                // directory is no longer accessible so cancel key
                cancel();
                signal();
                return;
            }

            // iterate over all entries in directory
            try {
                for (Path entry: stream) {
                    long lastModified = 0L;
                    try {
                        lastModified =
                                Files.getLastModifiedTime(entry, LinkOption.NOFOLLOW_LINKS).toMillis();
                    } catch (IOException x) {
                        // unable to get attributes of entry. If file has just
                        // been deleted then we'll report it as deleted on the
                        // next poll
                        continue;
                    }

                    // lookup cache
                    CacheEntry e = entries.get(entry.getFileName());
                    if (e == null) {
                        // new file found
                        entries.put(entry.getFileName(),
                                new CacheEntry(lastModified, tickCount));

                        logger.info("New file entry for {}.", entry);

                        // queue ENTRY_CREATE if event enabled
                        if (events.contains(StandardWatchEventKinds.ENTRY_CREATE)) {
                            signalEvent(StandardWatchEventKinds.ENTRY_CREATE, entry.getFileName());
                            continue;
                        } else {
                            // if ENTRY_CREATE is not enabled and ENTRY_MODIFY is
                            // enabled then queue event to avoid missing out on
                            // modifications to the file immediately after it is
                            // created.
                            if (events.contains(StandardWatchEventKinds.ENTRY_MODIFY)) {
                                signalEvent(StandardWatchEventKinds.ENTRY_MODIFY, entry.getFileName());
                            }
                        }
                        continue;
                    }

                    // check if file has changed
                    if (e.lastModified() != lastModified) {
                        logger.info("Last modified change detected for {}.", entry);
                        if (events.contains(StandardWatchEventKinds.ENTRY_MODIFY)) {
                            signalEvent(StandardWatchEventKinds.ENTRY_MODIFY,
                                    entry.getFileName());
                        }
                    }
                    // entry in cache so update poll time
                    e.update(lastModified, tickCount);

                }
            } catch (DirectoryIteratorException e) {
                // ignore for now; if the directory is no longer accessible
                // then the key will be cancelled on the next poll
            } finally {

                // close directory stream
                try {
                    stream.close();
                } catch (IOException x) {
                    // ignore
                }
            }

            // iterate over cache to detect entries that have been deleted
            Iterator<Map.Entry<Path, CacheEntry>> i = entries.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry<Path, CacheEntry> mapEntry = i.next();
                CacheEntry entry = mapEntry.getValue();
                if (entry.lastTickCount() != tickCount) {
                    Path name = mapEntry.getKey();
                    logger.info("File {} found deleted.", name.getFileName());
                    // remove from map and queue delete event (if enabled)
                    i.remove();
                    if (events.contains(StandardWatchEventKinds.ENTRY_DELETE)) {
                        signalEvent(StandardWatchEventKinds.ENTRY_DELETE, name);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }
}
