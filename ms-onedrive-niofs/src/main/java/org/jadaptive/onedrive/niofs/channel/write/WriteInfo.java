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
package org.jadaptive.onedrive.niofs.channel.write;

import org.jadaptive.api.folder.JadFsResource;

public class WriteInfo {

    private final long totalBytes;

    private final long uploadedBytes;

    private final JadFsResource jadFsResource;

    public WriteInfo(long totalBytes, long uploadedBytes, JadFsResource jadFsResource) {
        this.totalBytes = totalBytes;
        this.uploadedBytes = uploadedBytes;
        this.jadFsResource = jadFsResource;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public long getUploadedBytes() {
        return uploadedBytes;
    }

    public JadFsResource getJadFsResource() {
        return jadFsResource;
    }
}
