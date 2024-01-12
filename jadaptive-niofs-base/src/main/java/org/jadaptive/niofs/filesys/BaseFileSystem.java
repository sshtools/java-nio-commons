package org.jadaptive.niofs.filesys;

import org.jadaptive.niofs.path.BasePathService;

import java.nio.file.FileSystem;

public abstract class BaseFileSystem extends FileSystem {

    protected final BasePathService basePathService;

    public BaseFileSystem(BasePathService basePathService) {
        this.basePathService = basePathService;
    }

    public abstract BasePathService getPathService();
}
