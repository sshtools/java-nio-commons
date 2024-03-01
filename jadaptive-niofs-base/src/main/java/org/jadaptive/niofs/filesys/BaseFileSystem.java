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
package org.jadaptive.niofs.filesys;

import org.jadaptive.niofs.path.BasePathService;

import java.nio.file.FileSystem;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public abstract class BaseFileSystem extends FileSystem {

    protected final BasePathService basePathService;

    public BaseFileSystem(BasePathService basePathService) {
        this.basePathService = basePathService;
    }

    public abstract BasePathService getPathService();

    public abstract String getRegExFriendlySeparator();

    public List<String> getNamesForPath(String path) {
        requireNonNull(path, "Path cannot be null");
        // if we don't remove first separator split will return first value as empty string ""
        // /get_started.pdf => "", "get_started.pdf"
        if (path.startsWith(getPathService().getRootName())) {
            path = path.substring(1);
        }

        // a blank path value when split on separator would end up in collection as the only value
        // which is not desired, we need to return empty collection
        // special case for "/", above starts with check will convert "/" => "" i,e, an empty string
        if (path.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(path.split(getRegExFriendlySeparator())).collect(Collectors.toList());
    }
}
