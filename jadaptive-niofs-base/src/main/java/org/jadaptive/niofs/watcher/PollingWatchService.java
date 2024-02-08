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
import org.jadaptive.niofs.watcher.key.PollingWatchKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PollingWatchService extends BaseWatchService {

    private static final Logger logger = LoggerFactory.getLogger(PollingWatchService.class);

    // map of registrations
    private final Map<Object, PollingWatchKey> map = new HashMap<>();

    protected final int pollInSeconds;

    public PollingWatchService(int pollInSeconds) {
        this.pollInSeconds = pollInSeconds;
    }

    public PollingWatchService() {
        this.pollInSeconds = 10;
    }

    @Override
    public WatchKey register(final BasePath path,
                      WatchEvent.Kind<?>[] events,
                      WatchEvent.Modifier... modifiers)
            throws IOException
    {
        // check events - CCE will be thrown if there are invalid elements
        final Set<WatchEvent.Kind<?>> eventSet = new HashSet<>(events.length);
        for (WatchEvent.Kind<?> event: events) {
            // standard events
            if (event == StandardWatchEventKinds.ENTRY_CREATE ||
                    event == StandardWatchEventKinds.ENTRY_MODIFY ||
                    event == StandardWatchEventKinds.ENTRY_DELETE)
            {
                eventSet.add(event);
                continue;
            }

            // OVERFLOW is ignored
            if (event == StandardWatchEventKinds.OVERFLOW) {
                continue;
            }

            // null/unsupported
            if (event == null)
                throw new NullPointerException("An element in event set is 'null'");
            throw new UnsupportedOperationException(event.name());
        }
        if (eventSet.isEmpty())
            throw new IllegalArgumentException("No events to register");

        // check if watch service is closed
        if (!isOpen())
            throw new ClosedWatchServiceException();

        // registration is done in privileged block as it requires the
        // attributes of the entries in the directory.
        try {
            return AccessController.doPrivileged(
                    (PrivilegedExceptionAction<PollingWatchKey>) () -> doPrivilegedRegister(path, eventSet, pollInSeconds));
        } catch (PrivilegedActionException pae) {
            Throwable cause = pae.getCause();
            if (cause != null && cause instanceof IOException)
                throw (IOException)cause;
            throw new AssertionError(pae);
        }
    }

    // registers directory returning a new key if not already registered or
    // existing key if already registered
    private PollingWatchKey doPrivilegedRegister(BasePath path,
                                                 Set<? extends WatchEvent.Kind<?>> events,
                                                 int pollInSeconds)
            throws IOException
    {
        // check file is a directory and get its file key if possible
        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
        if (!attrs.isDirectory()) {
            throw new NotDirectoryException(path.toString());
        }
        Object fileKey = attrs.fileKey();
        logger.debug("The directory object key is {}.", fileKey);
        if (fileKey == null)
            throw new AssertionError("File keys must be supported");

        // grab close lock to ensure that watch service cannot be closed
        closeLock().lock();
        try {
            if (!isOpen())
                throw new ClosedWatchServiceException();

            PollingWatchKey watchKey;
            mapLock().lock();
            try {
                watchKey = map.get(fileKey);
                if (watchKey == null) {
                    logger.info("New registration for directory key {}.", fileKey);
                    // new registration
                    watchKey = new PollingWatchKey(path, this, fileKey);
                    map.put(fileKey, watchKey);
                } else {
                    logger.info("Old registration for directory key {}.", fileKey);
                    // update to existing registration
                    watchKey.disable();
                }
            } finally {
                mapLock().unlock();
            }
            watchKey.enable(events, pollInSeconds);
            return watchKey;
        } finally {
            closeLock().unlock();
        }

    }

    @Override
    protected void implClose() throws IOException {
        mapLock().lock();
        try {
            for (Map.Entry<Object, PollingWatchKey> entry: map.entrySet()) {
                PollingWatchKey watchKey = entry.getValue();
                watchKey.disable();
                watchKey.invalidate();
            }
            map.clear();
        } finally {
            mapLock().unlock();
        }
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            scheduledExecutor.shutdown();
            return null;
        });
    }

    @Override
    protected String getWatcherServiceName() {
        return "JadaptivePollingWatchService";
    }
}
