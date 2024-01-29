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
package org.jadaptive.box.niofs.api;

import org.jadaptive.box.niofs.path.BoxPath;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.CopyOption;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;
import java.util.Set;

public interface BoxRemoteAPI {
    void createDirectory(BoxPath path, FileAttribute<?>... attrs);

    void delete(BoxPath path);

    void copy(BoxPath source, BoxPath target, CopyOption...options);

    void move(BoxPath source, BoxPath target, CopyOption...options);

    <A extends BasicFileAttributes> A readAttributes(BoxPath path, LinkOption...options);

    Map<String, Object> readAttributes(BoxPath path, String attributes, LinkOption... options);

    SeekableByteChannel newByteChannel(BoxPath path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws FileNotFoundException, IOException;

    BoxFileInfo getBoxFileInfo(BoxPath path);

}
