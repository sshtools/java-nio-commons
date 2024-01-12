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
}
