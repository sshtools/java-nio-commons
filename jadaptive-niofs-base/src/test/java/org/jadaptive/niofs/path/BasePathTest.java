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

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public abstract class BasePathTest extends BaseFileSystemTest {

    @Test
    @DisplayName("Path throws IllegalArgumentException when getName is called with a negative index value.")
    public void testPathNegativeIndex() {
        test(fs -> {
            var path = fs.getPath("/some/sub/folder/file");
            assertThrowsExactly(IllegalArgumentException.class, () -> path.getName(-1));
        });
    }

    @Test
    @DisplayName("Path throws IllegalArgumentException when getName is called with out of bounds index value.")
    public void testPathOverflowIndex() {
        test(fs -> {
            var path = fs.getPath("/some/sub/folder/file");
            assertThrowsExactly(IllegalArgumentException.class, () -> path.getName(9999));
        });
    }

    @Test
    @DisplayName("Parent for root of the filesystem should return null.")
    public void testRootParentIsNull() {
        test(fs -> {
            var path = fs.getPath("/");
            assertNull(path.getParent());
        });
    }
    @Test
    @DisplayName("Filename call for root path should return null.")
    public void testRootFilenameIsNull() {
        test(fs -> {
            var path = fs.getPath("/");
            assertNull(path.getFileName());
        });
    }

    @Test
    @DisplayName("Root for a relative path should return null.")
    public void testRelativeRootIsNull() {
        test(fs -> {
            var path = fs.getPath("def");
            assertNull(path.getRoot());
        });
    }

    @Test
    @DisplayName("If single path part provided starts with a root of file system it should be considered.")
    public void testSinglePathPartContainsRoot() {
        test(fs -> {
            var pathShort = fs.getPath("/tmp");
            assertEquals("/tmp", pathShort.toString());

            var pathLong = fs.getPath("/tmp/some/random/file");
            assertEquals("/tmp/some/random/file", pathLong.toString());
        });
    }

    @Test
    @DisplayName("If single path part provided does not starts with a root of file system it should not be considered.")
    public void testSinglePathPartDoesNotContainsRoot() {
        test(fs -> {
            var pathShort = fs.getPath("tmp");
            assertEquals("tmp", pathShort.toString());

            var pathLong = fs.getPath("tmp/some/random/file");
            assertEquals("tmp/some/random/file", pathLong.toString());
        });
    }

    @Test
    @DisplayName("If multiple path parts are provided and first is a root of file system it should be considered.")
    public void testMultiplePathPartsStartsWithRoot() {
        test(fs -> {
            var pathShort = fs.getPath("/", "tmp");
            assertEquals("/tmp", pathShort.toString());

            var pathLong = fs.getPath("/", "tmp/some" , "/random/file");
            assertEquals("/tmp/some/random/file", pathLong.toString());
        });
    }

    @Test
    @DisplayName("When we normalize a path which is already in a normalize state it should return the same path.")
    public void testNormalizeSameFile() {
        test(fs -> {
            var path = fs.getPath("some/sub/folder/somefile");
            path = path.normalize();
            assertEquals("some/sub/folder/somefile", path.toString());
        });
    }

    @Test
    @DisplayName("When you normalize a relative path few levels up, it should jump paths to parent and resolve path from there.")
    public void testRelativeNormalizeJumpUpThePath() {
        test(fs -> {
            // from current position of somefile jump up the folder tree
            var p1 = fs.getPath("some/sub/folder/somefile/./../../c1");
            var t1 = p1.normalize();
            assertEquals("some/sub/c1", t1.toString());
        });
    }

    @Test
    @DisplayName("When you normalize a relative path levels up past the start, it should jump to start and resolve path from there.")
    public void testRelativeNormalizeJumpUpPastTheRoot() {
        test(fs -> {
            // jump past the start
            var p = fs.getPath("some/sub/c1/../../../../../../d1");
            var t = p.normalize();
            assertEquals("../../../d1", t.toString());
        });
    }

    @Test
    @DisplayName("When you normalize an absolute path few levels up, it should jump paths to parent and resolve path from there.")
    public void testAbsoluteNormalizeJumpUpThePath() {
        test(fs -> {
            // from current position of somefile jump up the folder tree
            var p1 = fs.getPath("/some/sub/folder/somefile/../../c1");
            var t1 = p1.normalize();
            assertEquals("/some/sub/c1", t1.toString());
        });
    }

    @Test
    @DisplayName("When you normalize an absolute path levels up past the root, it should jump to the root and append path from there.")
    public void testAbsoluteNormalizeJumpUpPastTheRoot() {
        test(fs -> {
            // jump past the start
            var p = fs.getPath("/some/sub/c1/../../../../../../d1");
            var t = p.normalize();
            assertEquals("/d1", t.toString());
        });
    }

    @Test
    @DisplayName("When a relative path instance or string is provided for starts with check, on match it should return true.")
    public void testRelativeStartsWithMatch() {
        test(fs -> {
            var p1 = fs.getPath("some/sub/folder");
            var p2 = fs.getPath("some/sub/folder/somefile");
            assertTrue(p2.startsWith(p1), "Path 2 must start with Path 1");
            assertTrue(p2.startsWith("some/sub/folder"), "Path 2 must start with Path 1");
        });
    }

    @Test
    @DisplayName("When a relative path instance or string is provided for starts with check, on no match it should return false.")
    public void testRelativeStartsWithNotMatch() {
        test(fs -> {
            var p1 = fs.getPath("some/sub/folder");
            var p2 = fs.getPath("other/sub/folder/somefile");
            assertFalse(p2.startsWith(p1), "Path 2 does not starts with Path 1");
            assertFalse(p2.startsWith("some/sub/folder"), "Path 2 does not starts with Path 1");
        });
    }

    @Test
    @DisplayName("When an absolute path instance or string is provided for starts with check, on match it should return true.")
    public void testAbsoluteStartsWith() {
        test(fs -> {
            var p1 = fs.getPath("/some/sub/folder");
            var p2 = fs.getPath("/some/sub/folder/somefile");
            assertTrue(p2.startsWith(p1), "Path 2 must start with Path 1");
            assertTrue(p2.startsWith("/some/sub/folder"), "Path 2 must start with Path 1");
        });
    }

    @Test
    @DisplayName("When an absolute path instance or string is provided for starts with check, on no match it should return false.")
    public void testAbsoluteStartsWithNotMatch() {
        test(fs -> {
            var p1 = fs.getPath("/some/sub/folder");
            var p2 = fs.getPath("/other/sub/folder/somefile");
            assertFalse(p2.startsWith(p1), "Path 2 does not starts with Path 1");
            assertFalse(p2.startsWith("/some/sub/folder"), "Path 2 does not starts with Path 1");
        });
    }

    @Test
    @DisplayName("If two paths are provided even though one starts with other, but in case they belong to different file systems, starts with check should return false.")
    public void testNotStartsWithCrossFileSystem() throws Exception {
        var p = Paths.get("some/sub/folder");
        test(fs -> {
            var path = fs.getPath("some/sub/folder/somefile");
            assertFalse(path.startsWith(p), "Path 2 must not start with Path 1");
        });
    }

    @Test
    @DisplayName("When a relative path instance or string is provided for ends with check, on match it should return true.")
    public void testRelativeEndsWithMatch() {
        test(fs -> {
            var p1 = fs.getPath("folder/somefile");
            var p2 = fs.getPath("some/sub/folder/somefile");
            assertTrue(p2.endsWith(p1), "Path 2 must end with Path 1");
            assertTrue(p2.endsWith("folder/somefile"), "Path 2 must end with Path 1");
        });
    }

    @Test
    @DisplayName("When a relative path instance or string is provided for ends with check, on match it should return true.")
    public void testRelativeEndsWithNoMatch() {
        test(fs -> {
            var p1 = fs.getPath("other/somefile");
            var p2 = fs.getPath("some/sub/folder/somefile");
            assertFalse(p2.endsWith(p1), "Path 2 must end with Path 1");
            assertFalse(p2.endsWith("other/somefile"), "Path 2 must end with Path 1");
        });
    }

    @Test
    @DisplayName("When an absolute path instance or string is provided for ends with check, on match it should return true.")
    public void testAbsoluteEndsWith() {
        test(fs -> {
            var p1 = fs.getPath("/folder/somefile");
            var p2 = fs.getPath("/some/sub/folder/somefile");
            assertTrue(p2.endsWith(p1), "Path 2 must start with Path 1");
            assertTrue(p2.endsWith("/folder/somefile"), "Path 2 must start with Path 1");
        });
    }

    @Test
    @DisplayName("When an absolute path instance or string is provided for ends with check, on no match it should return false.")
    public void testAbsoluteEndsWithNotMatch() {
        test(fs -> {
            var p1 = fs.getPath("/folder/otherfile");
            var p2 = fs.getPath("/other/sub/folder/somefile");
            assertFalse(p2.endsWith(p1), "Path 2 does not starts with Path 1");
            assertFalse(p2.endsWith("/folder/otherfile"), "Path 2 does not starts with Path 1");
        });
    }

    @Test
    @DisplayName("If two paths are provided even though one ends with other, but in case they belong to different file systems, starts with check should return false.")
    public void testNotEndsWithCrossFileSystem() throws Exception {
        var p = Paths.get("folder/somefile");
        test(fs -> {
            var path = fs.getPath("some/sub/folder/somefile");
            assertFalse(path.endsWith(p), "Path 2 must not start with Path 1");
        });
    }

    @Test
    @DisplayName("When a path is already absolute it should return same path.")
    public void testToAbsoluteWhenAlreadyAbsolute() {
        test(fs -> {
            var path = fs.getPath("/some/path/to/file");
            var absolutePath = path.toAbsolutePath();
            assertEquals(path, absolutePath);
        });
    }

    @Test
    @DisplayName("When a path is relative, call to absolute should resolve it relative to working directory.")
    public void testToAbsoluteWhenRelative() {
        test(fs -> {
            var pathService = fs.getPathService();
            var workingDirectory = pathService.getWorkingDirectory();

            var path = fs.getPath("some/path/to/file");
            var absolutePath = path.toAbsolutePath();
            var resolved = workingDirectory.resolve(path);

            assertEquals(resolved.toString(), absolutePath.toString());
        });
    }

    @Test
    @DisplayName("A path instance is matched with another instance pointing to same path in file system, starts and ends with test should be true.")
    public void testEqualPathWithStartsAndEndsWith() {
        test(fs -> {
            var p1 = fs.getPath("some/sub/folder/somefile");
            var p2 = fs.getPath("some/sub/folder/somefile");
            assertTrue(p2.startsWith(p1), "Path 2 must start with Path 1");
            assertTrue(p2.endsWith(p1), "Path 2 must end with Path 1");
            assertTrue(p1.startsWith(p2), "Path 1 must start with Path 2");
            assertTrue(p2.endsWith(p2), "Path 1 must end with Path 2");
        });
    }

    @Test
    @DisplayName("toFile call is not supported.")
    public void testToFile() {
        test(fs -> {
            assertThrowsExactly(UnsupportedOperationException.class,
                    () -> fs.getPath("some/sub/folder/somefile").toFile());
        });
    }

    public abstract void testToUri();

}
