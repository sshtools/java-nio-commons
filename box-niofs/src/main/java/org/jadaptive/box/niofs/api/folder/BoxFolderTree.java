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
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;

import java.util.Collection;

public class BoxFolderTree {

    public static BoxResource walk(Collection<String> pathToCheck, BoxAPIConnection api) {
        var rootFolder = BoxFolder.getRootFolder(api);
        return walk(pathToCheck.toArray(new String[0]), 0, rootFolder, "ROOT", api);
    }

    private static BoxResource walk(String[] pathToCheck, int index, BoxFolder current, String name,
                                    BoxAPIConnection api) {
        // all paths matched all exists return true
        if (index == pathToCheck.length) {
            return new BoxResource(current.getID(), name, BoxResourceType.Folder);
        }

        for (BoxItem.Info itemInfo : current) {
            if (itemInfo instanceof BoxFolder.Info) {
                var folderName = itemInfo.getName();
                if (folderName.equals(pathToCheck[index])) {
                    var latest = new BoxFolder(api, itemInfo.getID());
                    return walk(pathToCheck, ++index, latest, folderName, api);
                }
            } else if (index == (pathToCheck.length - 1) && itemInfo instanceof BoxFile.Info) {
                var fileName = itemInfo.getName();
                if (fileName.equals(pathToCheck[index])) {
                    return new BoxResource(itemInfo.getID(), itemInfo.getName(), BoxResourceType.File);
                }
            }
        }

        return BoxResource.NULL_BOX_RESOURCE;
    }
}
