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
