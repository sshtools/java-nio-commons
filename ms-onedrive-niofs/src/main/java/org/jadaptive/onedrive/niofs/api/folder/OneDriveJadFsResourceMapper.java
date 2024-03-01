package org.jadaptive.onedrive.niofs.api.folder;

import com.microsoft.graph.models.DriveItem;
import org.jadaptive.api.folder.JadFsResource;
import org.jadaptive.api.folder.JadFsResourceType;
import org.jadaptive.onedrive.niofs.api.OneDriveRemoteAPICaller;

import java.util.Iterator;

public class OneDriveJadFsResourceMapper implements JadFsResource.JadFsResourceMapper {

    private final OneDriveRemoteAPICaller oneDriveRemoteAPICaller;

    public OneDriveJadFsResourceMapper(OneDriveRemoteAPICaller oneDriveRemoteAPICaller) {
        this.oneDriveRemoteAPICaller = oneDriveRemoteAPICaller;
    }

    @Override
    public Iterator<JadFsResource> iterator(JadFsResource jadFsResource) {

        var folder = new DriveItem();
        folder.setId(jadFsResource.id);
        folder.setName(jadFsResource.name);

        Iterator<DriveItem> innerIterator = oneDriveRemoteAPICaller.getDriveItems(folder).iterator();
        return new Iterator<>() {

            @Override
            public boolean hasNext() {
                return innerIterator.hasNext();
            }

            @Override
            public JadFsResource next() {
                var item = innerIterator.next();
                var resourceType = item.getFolder() != null ? JadFsResourceType.Folder :
                        JadFsResourceType.File;
                return new JadFsResource(item.getId(), item.getName(), resourceType);
            }
        };
    }

    @Override
    public JadFsResource root() {
        var rootFolder = oneDriveRemoteAPICaller.getDriveRootItem().orElseThrow();
        return new JadFsResource(rootFolder.getId(),"ROOT", JadFsResourceType.Folder);
    }
}
