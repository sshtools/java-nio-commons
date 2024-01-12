package org.jadaptive.box.niofs.path;

import org.jadaptive.box.niofs.api.BoxRemoteAPI;
import org.jadaptive.box.niofs.api.client.locator.BoxConnectionAPILocator;
import org.jadaptive.box.niofs.filesys.BoxFileSystem;
import org.jadaptive.box.niofs.filesysprovider.BoxFileSystemProvider;
import org.jadaptive.niofs.filesys.BaseFileSystem;
import org.jadaptive.niofs.path.BasePathTest;
import org.junit.jupiter.api.BeforeAll;

public class BoxPathTest extends BasePathTest {

    @BeforeAll
    static void init() {
        BoxConnectionAPILocator.setBoxRemoteAPI(new BoxRemoteAPI() {});
    }

    @Override
    public BaseFileSystem getFileSystem() {

        var boxRemoteAPI = BoxConnectionAPILocator.getBoxRemoteAPI();
        var pathService = new BoxPathService();
        var provider = new BoxFileSystemProvider();

        var fs = new BoxFileSystem(provider, pathService, boxRemoteAPI);
        pathService.setFileSystem(fs);

        return fs;
    }
}
