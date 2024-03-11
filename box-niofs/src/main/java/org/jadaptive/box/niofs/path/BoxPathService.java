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
package org.jadaptive.box.niofs.path;

import org.jadaptive.box.niofs.filesys.BoxFileSystem;
import org.jadaptive.niofs.path.BasePath;
import org.jadaptive.niofs.path.BasePathService;

import java.net.URI;
import java.nio.file.FileSystem;
import java.util.Collections;
import java.util.List;

public class BoxPathService extends BasePathService {

    private static final String ROOT_NAME = "/";

    @Override
    public BoxPath createPath(BasePathService basePathService, String root, List<String> names) {
        return new BoxPath(this, null, names);
    }

    @Override
    public BoxFileSystem getFileSystem() {
        return (BoxFileSystem) this.fileSystem;
    }

    @Override
    public void setFileSystem(FileSystem fileSystem) {
        if (fileSystem instanceof BoxFileSystem) {
            this.fileSystem = fileSystem;
            return;
        }
        throw new IllegalArgumentException("Filesystem is not BoxFileSystem");
    }

    @Override
    public String getRootName() {
        return ROOT_NAME;
    }

    @Override
    public BoxPath createRoot() {
        return new BoxPath(this, getRootName(), Collections.emptyList());
    }

    @Override
    public BoxPath createPath(String root, List<String> names) {
        return new BoxPath(this, root, names);
    }

    @Override
    public BoxPath getPath(String[] paths) {
        return (BoxPath) getPathHelper(paths);
    }

    @Override
    public BoxPath getPath(String first, String... more) {
        return (BoxPath) getPathHelper(first,more);
    }

    @Override
    public BoxPath getPath(URI uri) {
        return (BoxPath) getPathHelper(uri);
    }

    @Override
    public BasePath getWorkingDirectory() {
        return this.workingDirectory;
    }

    @Override
    public String getScheme() {
        return "box";
    }

    @Override
    public BoxPath createPathFromIndex(int beginIndex, int endIndex, List<String> names) {
        return (BoxPath) createPathFromIndexHelper(beginIndex,endIndex, names);
    }

}
