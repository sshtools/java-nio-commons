package org.jadaptive.niofs.test;

import org.jadaptive.niofs.filesys.BaseFileSystem;

public abstract class BaseFileSystemTest {

    public abstract BaseFileSystem getFileSystem();

    public void test(FileSystemTestTask testTask) {
        var fs = getFileSystem();
        testTask.execute(fs);
    }

}
