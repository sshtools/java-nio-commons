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

import org.jadaptive.api.folder.AbstractJadFsTreeWalker;
import org.jadaptive.api.folder.JadFsResource;

public class OneDriveFsTreeWalker extends AbstractJadFsTreeWalker {

    private final OneDriveJadFsResourceMapper mapper;

    public OneDriveFsTreeWalker(OneDriveJadFsResourceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected JadFsResource.JadFsResourceMapper getMapper() {
        return this.mapper;
    }
}
