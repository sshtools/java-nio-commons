package org.jadaptive.box.niofs.api.folder;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import org.jadaptive.api.folder.JadFsResource;
import org.jadaptive.api.folder.JadFsResourceType;

import java.util.Iterator;

public class BoxJadFsResourceMapper implements JadFsResource.JadFsResourceMapper {

    private final BoxAPIConnection api;

    public BoxJadFsResourceMapper(BoxAPIConnection api) {
        this.api = api;
    }

    @Override
    public Iterator<JadFsResource> iterator(JadFsResource jadFsResource) {

        var folder = new BoxFolder(api, jadFsResource.id);

        Iterator<BoxItem.Info> innerIterator = folder.iterator();

        return new Iterator<>() {

            @Override
            public boolean hasNext() {
                return innerIterator.hasNext();
            }

            @Override
            public JadFsResource next() {
                var info = innerIterator.next();
                var resourceType = info instanceof BoxFolder.Info ? JadFsResourceType.Folder :
                        JadFsResourceType.File;
                return new JadFsResource(info.getID(), info.getName(), resourceType);
            }
        };
    }

    @Override
    public JadFsResource root() {
        var rootFolder = BoxFolder.getRootFolder(api);
        return new JadFsResource(rootFolder.getID(),"ROOT", JadFsResourceType.Folder);
    }
}
