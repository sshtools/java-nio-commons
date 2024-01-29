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
package org.jadaptive.box.niofs.api.channel.write;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import org.jadaptive.box.niofs.api.BoxFileInfo;
import org.jadaptive.box.niofs.api.io.ChannelBufferWrapperInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class SmallFileWrite implements FileWriteDelegate {

    private static final Logger logger = LoggerFactory.getLogger(SmallFileWrite.class);

    private final BoxAPIConnection api;
    private final BoxFileInfo fileInfo;

    private final BoxFile boxFile;

    public SmallFileWrite(BoxAPIConnection api, BoxFileInfo fileInfo, BoxFile boxFile) {
        this.api = api;
        this.fileInfo = fileInfo;
        this.boxFile = boxFile;
    }

    @Override
    public WriteInfo write(ByteBuffer src) {

        var parentId = this.fileInfo.getParentId();

        var isPresent = this.fileInfo.isPresent();
        var name = this.fileInfo.getName();

        var byteStream = new ChannelBufferWrapperInputStream(src);

        BoxFile.Info info;

        if (isPresent) {
            info = this.boxFile.uploadNewVersion(byteStream);
        } else {
            var parentFolder = new BoxFolder(api, parentId);
            info = parentFolder.uploadFile(byteStream, name);
        }

        return new WriteInfo(info, src.capacity());
    }
}
