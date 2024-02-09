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
import org.jadaptive.niofs.watcher.BaseWatchService;
import org.jadaptive.niofs.watcher.event.BaseWatchEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.Watchable;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class BaseWatchKey implements WatchKey {

    private static final Logger logger = LoggerFactory.getLogger(BaseWatchKey.class);

    /**
     * Maximum size of event list (in the future this may be tunable)
     */
    static final int MAX_EVENT_LIST_SIZE = 512;

    /**
     * Possible key states
     */
    private enum State { READY, SIGNALLED };

    protected final BasePath path;

    protected final BaseWatchService baseWatchService;

    // key state
    private State state;

    // pending events
    private List<WatchEvent<?>> events;

    // maps a context to the last event for the context (iff the last queued
    // event for the context is an ENTRY_MODIFY event).
    private Map<Object,WatchEvent<?>> lastModifyEvents;

    protected final Lock lock = new ReentrantLock();

    public BaseWatchKey(BasePath path, BaseWatchService baseWatchService) {
        this.path = path;
        this.baseWatchService = baseWatchService;
        this.state = State.READY;
        this.events = new ArrayList<>();
        this.lastModifyEvents = new HashMap<>();

    }

    final BaseWatchService watcher() {
        return baseWatchService;
    }

    /**
     * Enqueues this key to the watch service
     */
    protected void signal() {
        lock.lock();
        try {
            if (state == State.READY) {
                logger.debug("Ready to fire signal.");
                state = State.SIGNALLED;
                baseWatchService.enqueueKey(this);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Adds the event to this key and signals it.
     */
    @SuppressWarnings("unchecked")
    protected void signalEvent(WatchEvent.Kind<?> kind, Object context) {
        lock.lock();
        try {

            logger.debug("Firing event for kind {}", kind.name());

            boolean isModify = (kind == StandardWatchEventKinds.ENTRY_MODIFY);
            int size = events.size();
            if (size > 0) {
                // if the previous event is an OVERFLOW event or this is a
                // repeated event then we simply increment the counter
                WatchEvent<?> prev = events.get(size-1);
                if ((prev.kind() == StandardWatchEventKinds.OVERFLOW) ||
                        ((kind == prev.kind() &&
                                Objects.equals(context, prev.context()))))
                {
                    ((BaseWatchEvent<?>)prev).increment();
                    return;
                }

                // if this is a modify event and the last entry for the context
                // is a modify event then we simply increment the count
                if (!lastModifyEvents.isEmpty()) {
                    if (isModify) {
                        WatchEvent<?> ev = lastModifyEvents.get(context);
                        if (ev != null) {
                            assert ev.kind() == StandardWatchEventKinds.ENTRY_MODIFY;
                            ((BaseWatchEvent<?>)ev).increment();
                            return;
                        }
                    } else {
                        // not a modify event so remove from the map as the
                        // last event will no longer be a modify event.
                        lastModifyEvents.remove(context);
                    }
                }

                // if the list has reached the limit then drop pending events
                // and queue an OVERFLOW event
                if (size >= MAX_EVENT_LIST_SIZE) {
                    kind = StandardWatchEventKinds.OVERFLOW;
                    isModify = false;
                    context = null;
                }
            }

            // non-repeated event
            BaseWatchEvent ev =
                    new BaseWatchEvent<>((WatchEvent.Kind<Object>)kind, context);
            if (isModify) {
                lastModifyEvents.put(context, ev);
            } else if (kind == StandardWatchEventKinds.OVERFLOW) {
                // drop all pending events
                events.clear();
                lastModifyEvents.clear();
            }
            events.add(ev);
            signal();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public final List<WatchEvent<?>> pollEvents() {
        lock.lock();
        try {
            List<WatchEvent<?>> result = events;
            events = new ArrayList<>();
            lastModifyEvents.clear();
            return result;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean reset() {
        lock.lock();
        try {
            if (state == State.SIGNALLED && isValid()) {
                if (events.isEmpty()) {
                    state = State.READY;
                } else {
                    // pending events so re-queue key
                    baseWatchService.enqueueKey(this);
                }
            }
            return isValid();
        } finally {
            lock.unlock();
        }

    }

    @Override
    public Watchable watchable() {
        return this.path;
    }

}
