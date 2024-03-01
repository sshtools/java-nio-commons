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

public class JadFsResourceFolderTree {

    public static JadFsResource walk(Collection<String> pathToCheck, JadFsResource.JadFsResourceChildrenFetcher fetcher) {
        var rootFolder = fetcher.root();
        return walk(pathToCheck.toArray(new String[0]), 0, rootFolder, "ROOT", fetcher);
    }

    private static JadFsResource walk(String[] pathToCheck, int index, JadFsResource current, String name,
                                      JadFsResource.JadFsResourceChildrenFetcher fetcher) {
        // all paths matched all exists return true
        if (index == pathToCheck.length) {
            return new JadFsResource(current.id, name, JadFsResourceType.Folder);
        }

        var children = fetcher.children(current);

        for (JadFsResource child: children) {
            if (child.isFolder()) {
                var folderName = child.name;
                if (folderName.equals(pathToCheck[index])) {
                    return walk(pathToCheck, ++index, child, folderName, fetcher);
                }
            }  else if (index == (pathToCheck.length - 1) && child.isFile()) {
                var fileName = child.name;
                if (fileName.equals(pathToCheck[index])) {
                    return child;
                }
            }
        }

        return JadFsResource.NULL_JAD_FS_RESOURCE;
    }
}
