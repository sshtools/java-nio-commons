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
import org.jadaptive.niofs.stream.NullFilter;
import org.jadaptive.onedrive.niofs.path.OneDrivePath;

import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class OneDriveDirectoryIterator implements Iterator<Path> {

    private final OneDrivePath dir;

    private final Iterator<DriveItem> infoIterator;

    public OneDriveDirectoryIterator(List<DriveItem> items, OneDrivePath dir, DirectoryStream.Filter<? super Path> filter) {
        this.dir = dir;
        this.infoIterator = filter == null || filter instanceof NullFilter
                ? items.iterator()
                : items.stream()
                .filter(p -> matchFileFilter(dir, filter, p)).iterator();

    }

    @Override
    public boolean hasNext() {
        return this.infoIterator.hasNext();
    }

    @Override
    public Path next() {
        var info = this.infoIterator.next();
        return dir.resolve(info.getName());
    }

    private static boolean matchFileFilter(OneDrivePath dir, DirectoryStream.Filter<? super Path> filter, DriveItem p) {
        try {
            var path = dir.resolve(Objects.requireNonNull(p.getName()));
            return filter.accept(path);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
