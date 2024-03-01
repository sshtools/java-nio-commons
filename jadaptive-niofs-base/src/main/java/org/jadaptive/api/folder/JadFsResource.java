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
package org.jadaptive.api.folder;

import java.util.Collection;
import java.util.Iterator;

public class JadFsResource {

    public static final NullJadFsResource NULL_JAD_FS_RESOURCE = new NullJadFsResource();

    public final String id;

    public final String name;

    public final JadFsResourceType resourceType;


    public JadFsResource(String id, String name, JadFsResourceType resourceType) {
        this.id = id;
        this.name = name;
        this.resourceType = resourceType;
    }

    public boolean isFile() {
        return this.resourceType == JadFsResourceType.File;
    }

    public boolean isFolder() {
        return this.resourceType == JadFsResourceType.Folder;
    }

    public static class NullJadFsResource extends JadFsResource {
        private NullJadFsResource() {
            super(null, null, null);
        }
    }

    public interface JadFsResourceChildrenFetcher {
        Iterable<JadFsResource> children(JadFsResource jadFsResource);

        JadFsResource root();
    }

    public interface JadFsResourceMapper {
        Iterator<JadFsResource> iterator(JadFsResource jadFsResource);

        JadFsResource root();
    }

    public interface JadFsTreeWalker {
        JadFsResource walk(Collection<String> pathToCheck);
    }
}
