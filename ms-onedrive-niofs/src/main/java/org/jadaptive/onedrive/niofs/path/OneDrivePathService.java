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
package org.jadaptive.onedrive.niofs.path;

import org.jadaptive.niofs.path.BasePath;
import org.jadaptive.niofs.path.BasePathService;
import org.jadaptive.onedrive.niofs.filesys.OneDriveFileSystem;

import java.net.URI;
import java.nio.file.FileSystem;
import java.util.Collections;
import java.util.List;

public class OneDrivePathService extends BasePathService {

    private static final String ROOT_NAME = "/";

    @Override
    public OneDrivePath createPath(BasePathService basePathService, String root, List<String> names) {
        return new OneDrivePath(this, null, names);
    }

    @Override
    public OneDriveFileSystem getFileSystem() {
        return (OneDriveFileSystem) this.fileSystem;
    }

    @Override
    public void setFileSystem(FileSystem fileSystem) {
        if (fileSystem instanceof OneDriveFileSystem) {
            this.fileSystem = fileSystem;
            return;
        }
        throw new IllegalArgumentException("Filesystem is not OneDriveFileSystem");
    }

    @Override
    public String getRootName() {
        return ROOT_NAME;
    }

    @Override
    public OneDrivePath createRoot() {
        return new OneDrivePath(this, getRootName(), Collections.emptyList());
    }

    @Override
    public OneDrivePath createPath(String root, List<String> names) {
        return new OneDrivePath(this, root, names);
    }

    @Override
    public OneDrivePath getPath(String[] paths) {
        return (OneDrivePath) getPathHelper(paths);
    }

    @Override
    public OneDrivePath getPath(String first, String... more) {
        return (OneDrivePath) getPathHelper(first,more);
    }

    @Override
    public OneDrivePath getPath(URI uri) {
        return (OneDrivePath) getPathHelper(uri);
    }

    @Override
    public BasePath getWorkingDirectory() {
        return this.workingDirectory;
    }

    @Override
    public OneDrivePath createPathFromIndex(int beginIndex, int endIndex, List<String> names) {
        return (OneDrivePath) createPathFromIndexHelper(beginIndex,endIndex, names);
    }

}
