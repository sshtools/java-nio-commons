package org.jadaptive.niofs.test;

import org.jadaptive.niofs.filesys.BaseFileSystem;

@FunctionalInterface
public interface FileSystemTestTask {

    void execute(BaseFileSystem fs);
}
