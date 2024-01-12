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
package org.jadaptive.niofs.path;

import org.jadaptive.niofs.test.BaseFileSystemTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

public abstract class BasePathTest extends BaseFileSystemTest {

    @Test
    @DisplayName("Parent for root of the filesystem should return null.")
    public void testRootParentIsNull() {
        test(fs -> {
            var path = fs.getPath("/");
            assertNull(path.getParent());
        });
    }

    @Test
    @DisplayName("Path throws IllegalArgumentException when getName is called with a negative index value.")
    public void testPathNegativeIndex() {
        test(fs -> {
            var path = fs.getPath("/some/sub/folder/file");
            assertThrowsExactly(IllegalArgumentException.class, () -> {
                path.getName(-1);
            });
        });
    }
}
