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
import org.jadaptive.api.folder.JadFsResource;
import org.jadaptive.api.folder.JadFsResourceType;
import org.jadaptive.api.user.FileSysUserInfo;
import org.jadaptive.niofs.attr.JadNioFileAttributes;
import org.jadaptive.niofs.filesys.BaseFileSystem;
import org.jadaptive.niofs.path.BasePath;
import org.jadaptive.util.Pair;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.*;
import java.util.stream.Collectors;

public interface FileSystemRemoteAPI<P extends BasePath> {

    void createDirectory(P path, FileAttribute<?>... attrs);

    void delete(P path);

    void copy(P source, P target, CopyOption...options);

    void move(P source, P target, CopyOption...options);

    <A extends BasicFileAttributes> A readAttributes(P path, LinkOption...options);

    SeekableByteChannel newByteChannel(P path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws FileNotFoundException, IOException;

    FileSysFileInfo getFileSysFileInfo(P path);

    DirectoryStream<Path> newDirectoryStream(P dir, DirectoryStream.Filter<? super Path> filter);

    String getSessionName();

    String getCurrentUserId();

    FileSysUserInfo getFileSysUserInfo();

    void log_info(String format, Object... arguments);

    default Map<String, Object> readAttributes(P path, String attributes, LinkOption... options) {

        if (attributes == null) {
            return Collections.emptyMap();
        }

        var jadAttributes = (JadNioFileAttributes) readAttributes(path, options);
        var jadAttributesMap = jadAttributes.toMap();

        var keys = Arrays.stream(attributes.split(","))
                .filter(s -> !s.isBlank())
                .map(s -> s.trim())
                .collect(Collectors.toSet());


        var map = new HashMap<String, Object>();

        for (String key: keys) {
            var value = jadAttributesMap.get(key);
            if (value != null) {
                map.put(key, value);
            }
        }

        return map;
    }

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

    /**
     * Usually when we copy or move we need target files parent for operation.
     * As it is the parent where file is to be copied or moved.
     * Here by walking file tree we try to find objects in remote host, source file
     * and target parent folder.
     *
     * @param jadFsTreeWalker
     * @param source
     * @param target
     *
     * @return Pair of source file and target folder.
     */
    default Pair<JadFsResource> sourceTargetResources(JadFsResource.JadFsTreeWalker jadFsTreeWalker,
                                                      BasePath source, BasePath target) {

        log_info("The given source path is {}", source);
        var normalizeSourcePath = getNormalizePath(source);
        var sourcePathNames = normalizeSourcePath.getNames();

        log_info("The source path normalized as {}", normalizeSourcePath);

        log_info("The given target path is {}", target);
        var normalizeTargetPath = getNormalizePath(target);
        var targetPathNames = normalizeTargetPath.getNames();

        var targetResourceInBox  = jadFsTreeWalker.walk(targetPathNames);

        if (targetResourceInBox instanceof JadFsResource.NullJadFsResource
                || targetResourceInBox.resourceType == JadFsResourceType.File) {

            var parent = normalizeTargetPath.getParent();
            var parentPathNames = parent.getNames();

            log_info("Checking for parent path for target {}", parent);

            targetResourceInBox = jadFsTreeWalker.walk(parentPathNames);
        }

        var sourceResourceInBox = jadFsTreeWalker.walk(sourcePathNames);

        return new Pair<>(sourceResourceInBox, targetResourceInBox);
    }

}
