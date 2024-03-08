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
package org.jadaptive.onedrive.niofs.stream;

import com.microsoft.graph.models.DriveItem;
import org.jadaptive.onedrive.niofs.path.OneDrivePath;

import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

public class OneDriveDirectoryStream implements DirectoryStream<Path> {

    private final List<DriveItem> items;

    private final OneDrivePath dir;

    private final Filter<? super Path> filter;

    public OneDriveDirectoryStream(List<DriveItem> items, OneDrivePath dir, Filter<? super Path> filter) {
        this.items = items;
        this.dir = dir;
        this.filter = filter;
    }

    @Override
    public Iterator<Path> iterator() {
        return new OneDriveDirectoryIterator(items, dir, filter);
    }

    @Override
    public void close() {}
}
