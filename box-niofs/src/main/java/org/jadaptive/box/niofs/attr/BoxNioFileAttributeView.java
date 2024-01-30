package org.jadaptive.box.niofs.attr;

import org.jadaptive.box.niofs.api.BoxRemoteAPI;
import org.jadaptive.box.niofs.path.BoxPath;

import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public class BoxNioFileAttributeView implements BasicFileAttributeView {

    private final BoxRemoteAPI boxRemoteAPI;

    private final BoxPath path;

    public BoxNioFileAttributeView(BoxRemoteAPI boxRemoteAPI, BoxPath path) {
        this.boxRemoteAPI = boxRemoteAPI;
        this.path = path;
    }

    @Override
    public String name() {
        return "basic";
    }

    @Override
    public BasicFileAttributes readAttributes() {
        return this.boxRemoteAPI.readAttributes(path);
    }

    @Override
    public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime) {
        throw new UnsupportedOperationException("No remote api support.");
    }
}
