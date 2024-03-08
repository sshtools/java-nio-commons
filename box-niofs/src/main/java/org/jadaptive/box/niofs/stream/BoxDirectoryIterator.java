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
package org.jadaptive.box.niofs.stream;

import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import org.jadaptive.box.niofs.path.BoxPath;
import org.jadaptive.niofs.stream.NullFilter;

import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Iterator;

import static java.util.stream.StreamSupport.stream;

public class BoxDirectoryIterator implements Iterator<Path> {

    private final BoxFolder folder;

    private final BoxPath dir;

    private final Iterator<BoxItem.Info> infoIterator;
    public BoxDirectoryIterator(BoxFolder folder, BoxPath dir, DirectoryStream.Filter<? super Path> filter) {
        this.folder = folder;
        this.dir = dir;
        this.infoIterator = filter == null || filter instanceof NullFilter
                ? this.folder.iterator()
                : stream(this.folder.spliterator(), false)
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

    private static boolean matchFileFilter(BoxPath dir, DirectoryStream.Filter<? super Path> filter, BoxItem.Info p) {
        try {
            var path = dir.resolve(p.getName());
            return filter.accept(path);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
