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
package org.jadaptive.niofs.path;

import java.net.URI;
import java.nio.file.FileSystem;
import java.util.List;

public abstract class BasePathService {
    protected FileSystem fileSystem;

    public BasePathService() {
    }

    public abstract FileSystem getFileSystem();
    public abstract void setFileSystem(FileSystem fileSystem);
    public abstract String getRootName();
    public abstract BasePath createRoot();
    public abstract BasePath createPathFromIndex(int beginIndex, int endIndex, List<String> names);
    public abstract BasePath createPath(String root, List<String> names);
    public BasePath createEmptyPath() {
        return createPath(null, List.of(""));
    }
    public abstract BasePath getPath(String first, String... more);
    public abstract BasePath getPath(URI uri);
    public abstract BasePath getWorkingDirectory();

}
