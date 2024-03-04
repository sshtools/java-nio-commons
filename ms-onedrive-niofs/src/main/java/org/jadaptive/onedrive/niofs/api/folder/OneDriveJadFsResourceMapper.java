/**
 *    Copyright 2013 Jadaptive Limited
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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

        Iterator<DriveItem> innerIterator = oneDriveRemoteAPICaller.getDriveItems(jadFsResource).iterator();
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
