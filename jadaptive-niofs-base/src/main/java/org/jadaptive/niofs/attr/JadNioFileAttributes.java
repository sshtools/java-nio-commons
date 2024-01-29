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

package org.jadaptive.niofs.attr;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.Map;

public class JadNioFileAttributes implements BasicFileAttributes {

    private final FileTime creationTime;
    private final boolean regularFile;
    private final long size;
    private final String fileKey;
    private FileTime lastAccessTime;
    private FileTime lastModifiedTime;

    public JadNioFileAttributes(FileTime creationTime, boolean regularFile, long size, String fileKey) {
        this.creationTime = creationTime;
        this.regularFile = regularFile;
        this.size = size;
        this.fileKey = fileKey;
    }

    @Override
    public FileTime lastModifiedTime() {
        return null;
    }

    @Override
    public FileTime lastAccessTime() {
        return null;
    }

    @Override
    public FileTime creationTime() {
        return null;
    }

    @Override
    public boolean isRegularFile() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public boolean isSymbolicLink() {
        return false;
    }

    @Override
    public boolean isOther() {
        return false;
    }

    @Override
    public long size() {
        return 0;
    }

    @Override
    public String fileKey() {
        return null;
    }

    public FileTime getCreationTime() {
        return creationTime;
    }

    public long getSize() {
        return size;
    }

    public String getFileKey() {
        return fileKey;
    }

    public FileTime getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(FileTime lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public FileTime getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(FileTime lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public Map<String, Object> toMap() {

        var map = new HashMap<String, Object>();

        map.put("creationTime", creationTime);
        map.put("regularFile", regularFile);
        map.put("size", size);
        map.put("fileKey", fileKey);
        map.put("lastAccessTime", lastAccessTime);
        map.put("lastModifiedTime", lastModifiedTime);

        return map;
    }

    @Override
    public String toString() {
        return "JadNioFileAttributes{" +
                "creationTime=" + creationTime +
                ", regularFile=" + regularFile +
                ", size=" + size +
                ", fileKey='" + fileKey + '\'' +
                ", lastAccessTime=" + lastAccessTime +
                ", lastModifiedTime=" + lastModifiedTime +
                '}';
    }
}
