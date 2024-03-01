package org.jadaptive.onedrive.niofs.path;

import org.jadaptive.niofs.path.BasePathService;
import org.jadaptive.niofs.path.BasePathServiceTest;
import org.jadaptive.onedrive.niofs.api.OneDriveFileSysRemoteAPI;
import org.jadaptive.onedrive.niofs.api.OneDriveRemoteAPICaller;
import org.jadaptive.onedrive.niofs.filesys.OneDriveFileSystem;
import org.jadaptive.onedrive.niofs.filesysprovider.OneDriveFileSystemProvider;

public class OneDrivePathServiceTest extends BasePathServiceTest {
    @Override
    protected BasePathService getNewBasePathService() {
        var pathService =  new OneDrivePathService();
        var provider = new OneDriveFileSystemProvider();
        var api = new OneDriveFileSysRemoteAPI(new OneDriveRemoteAPICaller(null));

        var fileSystem = new OneDriveFileSystem(provider,pathService, api);

        pathService.setFileSystem(fileSystem);

        return pathService;
    }
}
