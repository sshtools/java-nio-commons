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

public abstract class AbstractJadFsTreeWalker implements JadFsResource.JadFsTreeWalker {

    protected abstract JadFsResource.JadFsResourceMapper getMapper();

    public JadFsResource walk(Collection<String> pathToCheck) {
        return JadFsResourceFolderTree.walk(pathToCheck, new JadFsResource.JadFsResourceChildrenFetcher() {
            @Override
            public Iterable<JadFsResource> children(JadFsResource jadFsResource) {
                return () -> getMapper().iterator(jadFsResource);
            }

            @Override
            public JadFsResource root() {
                return getMapper().root();
            }
        });
    }

}
