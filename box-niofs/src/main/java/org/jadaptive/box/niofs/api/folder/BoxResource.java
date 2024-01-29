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

public class BoxResource {

    public static final NullBoxResource NULL_BOX_RESOURCE = new NullBoxResource();

    public final String id;

    public final BoxResourceType resourceType;

    public BoxResource(String id, BoxResourceType resourceType) {
        this.id = id;
        this.resourceType = resourceType;
    }

    public static class NullBoxResource extends BoxResource {
        private NullBoxResource() {
            super(null, null);
        }
    }
}
