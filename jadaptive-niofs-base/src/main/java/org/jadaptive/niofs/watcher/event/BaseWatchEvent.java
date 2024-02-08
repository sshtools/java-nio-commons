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
package org.jadaptive.niofs.watcher.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.WatchEvent;

public class BaseWatchEvent<T> implements WatchEvent<T> {

    private static final Logger logger = LoggerFactory.getLogger(BaseWatchEvent.class);

    private final WatchEvent.Kind<T> kind;
    private final T context;

    // synchronize on watch key to access/increment count
    private int count;
    public BaseWatchEvent(WatchEvent.Kind<T> type, T context) {
        this.kind = type;
        this.context = context;
        this.count = 1;
    }

    @Override
    public Kind<T> kind() {
        return kind;
    }

    @Override
    public int count() {
        return count;
    }

    @Override
    public T context() {
        return context;
    }

    // for repeated events
    public void increment() {
        count++;
    }
}
