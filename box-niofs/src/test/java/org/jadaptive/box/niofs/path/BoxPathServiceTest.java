package org.jadaptive.box.niofs.path;

import org.jadaptive.box.niofs.api.BoxRemoteAPI;
import org.jadaptive.box.niofs.api.client.locator.BoxConnectionAPILocator;
import org.jadaptive.box.niofs.filesys.BoxFileSystem;
import org.jadaptive.box.niofs.filesysprovider.BoxFileSystemProvider;
import org.jadaptive.niofs.path.BasePathService;
import org.jadaptive.niofs.path.BasePathServiceTest;
import org.junit.jupiter.api.BeforeAll;

public class BoxPathServiceTest extends BasePathServiceTest {

    @BeforeAll
    static void init() {
        BoxConnectionAPILocator.setBoxRemoteAPI(new BoxRemoteAPI() {});
    }

    @Override
    protected BasePathService getNewBasePathService() {
        var boxRemoteAPI = BoxConnectionAPILocator.getBoxRemoteAPI();
        var pathService = new BoxPathService();
        var provider = new BoxFileSystemProvider();

        var fs = new BoxFileSystem(provider, pathService, boxRemoteAPI);
        pathService.setFileSystem(fs);

        return pathService;
    }

}
