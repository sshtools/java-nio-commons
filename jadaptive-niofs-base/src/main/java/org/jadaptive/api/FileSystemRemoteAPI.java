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
package org.jadaptive.api;

import org.jadaptive.api.file.FileSysFileInfo;
import org.jadaptive.api.user.FileSysUserInfo;
import org.jadaptive.niofs.filesys.BaseFileSystem;
import org.jadaptive.niofs.path.BasePath;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public interface FileSystemRemoteAPI<P> {

    void createDirectory(P path, FileAttribute<?>... attrs);

    void delete(P path);

    void copy(P source, P target, CopyOption...options);

    void move(P source, P target, CopyOption...options);

    <A extends BasicFileAttributes> A readAttributes(P path, LinkOption...options);

    Map<String, Object> readAttributes(P path, String attributes, LinkOption... options);

    SeekableByteChannel newByteChannel(P path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws FileNotFoundException, IOException;

    FileSysFileInfo getFileSysFileInfo(P path);

    DirectoryStream<Path> newDirectoryStream(P dir, DirectoryStream.Filter<? super Path> filter);

    String getSessionName();

    String getCurrentUserId();

    FileSysUserInfo getFileSysUserInfo();

    default BasePath getNormalizePath(BasePath dir) {
        BasePath normalizePath;
        if (dir.isAbsolute()) {
            normalizePath = normalizeForAbsolutePath(dir);
        } else {
            normalizePath = normalizeForRelativePath(dir);
        }
        return normalizePath;
    }

    default BasePath normalizeForAbsolutePath(BasePath dir) {
        return (BasePath) dir.normalize();
    }

    default BasePath normalizeForRelativePath(BasePath dir) {
        BasePath normalizePath;
        var fs =  dir.getFileSystem();
        var service = ((BaseFileSystem) fs).getPathService();

        var workingDirectory = service.getWorkingDirectory();

        // if no names its root
        var workingDirectoryNames = workingDirectory.getNames().isEmpty() ?
                Collections.singletonList(service.getRootName()) :
                workingDirectory.getNames();

        var dirNames = dir.getNames();

        var mergedNames = new ArrayList<String>();
        mergedNames.addAll(workingDirectoryNames);
        mergedNames.addAll(dirNames);

        var mergedPath = service.getPath(mergedNames.toArray(new String[0]));

        return (BasePath) mergedPath.normalize();
    }
}
