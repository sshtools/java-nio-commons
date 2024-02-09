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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

public abstract class BasePathServiceTest {

    protected abstract BasePathService getNewBasePathService();

    @Test
    @DisplayName("When null arguments are passed during path construction, it should throw argument exception.")
    void testPathConstructionNull() {
        var service = getNewBasePathService();
        assertThrowsExactly(IllegalArgumentException.class, () -> service.getPath(null, null));
    }

    @Test
    @DisplayName("When first null argument is passed during path construction, it should throw argument exception.")
    void testPathConstructionFirstNull() {
        var service = getNewBasePathService();
        assertThrowsExactly(IllegalArgumentException.class, () -> service.getPath(null, ""));
    }

    @Test
    @DisplayName("When more null argument is passed during path construction, it should throw argument exception.")
    void testPathConstructionMoreNull() {
        var service = getNewBasePathService();
        assertThrowsExactly(IllegalArgumentException.class, () -> service.getPath("/", null));
    }

    @Test
    @DisplayName("It should construct a path when only root is specified.")
    void testPathConstructionOnlyRoot() {
        var service = getNewBasePathService();
        var rootName = service.getRootName(); // /
        assertEquals(rootName, service.getPath(rootName).toString());
    }

    @Test
    @DisplayName("It should construct an empty path when empty value is specified.")
    void testPathConstructionEmptyPath() {
        var service = getNewBasePathService();
        assertEquals("", service.getPath("").toString());
    }

    @Test
    @DisplayName("It should construct an empty path when empty values are specified.")
    void testPathConstructionEmptyPathMultipleEmpty() {
        var service = getNewBasePathService();
        assertEquals("", service.getPath("", "", "").toString());
    }

    @Test
    @DisplayName("It should construct path when each path argument contains separator, with root as first.")
    void testPathConstructionEachPartWithSeparator() {
        var service = getNewBasePathService();
        var rootName = service.getRootName(); // /
        var separator = service.fileSystem.getSeparator();
        // /path/to/random/file
        var expectedPath = String.join(separator, rootName + "path", "to", "random", "file");
        assertEquals(expectedPath, service.getPath(rootName, "path",
                        separator + "to", // /to
                        separator + "random", // /random
                        separator + "file") // /file
                .toString());
    }

    @Test
    @DisplayName("It should construct path when each path argument contains separator, no root as first.")
    void testPathConstructionEachPartWithSeparatorNoRootAsFirst() {
        var service = getNewBasePathService();
        var rootName = service.getRootName(); // /
        var separator = service.fileSystem.getSeparator();
        // /path/to/random/file
        var expectedPath = String.join(separator, rootName + "path", "to", "random", "file");
        assertEquals(expectedPath, service.getPath("", rootName + "path", // /path
                        separator + "to", // /to
                        separator + "random", // /random
                        separator + "file") // /file
                .toString());
    }

    @Test
    @DisplayName("It should construct a path when blank values are specified.")
    void testPathConstructionWithSpaces() {
        var service = getNewBasePathService();
        var rootName = service.getRootName(); // /
        var separator = service.fileSystem.getSeparator();
        var expectedPath = separator + " " + separator + " "; // => / /
        assertEquals(expectedPath, service.getPath(rootName, " ", " ").toString());
    }

    @Test
    @DisplayName("It should construct a path when path values have spaces leading or ending.")
    void testPathConstructionWithLeadingEndingSpaces() {
        var service = getNewBasePathService();
        var rootName = service.getRootName(); // /
        var separator = service.fileSystem.getSeparator();
        var expectedPath = separator + " path" + separator + "to " + separator + "file"; // => / path/to /file
        assertEquals(expectedPath, service.getPath(rootName, " path", "to ", "file").toString());
    }

    @Test
    @DisplayName("It should construct a relative path when path values have no root.")
    void testPathConstructionRelative() {
        var service = getNewBasePathService();
        var separator = service.fileSystem.getSeparator();
        var expectedPath =  "path" + separator + "to" + separator + "file"; // => path/to/file
        assertEquals(expectedPath, service.getPath("", "path", "to", "file").toString());
    }

    @Test
    @DisplayName("It should construct a path and consider multiple separator together as single separator for absolute path.")
    void testPathConstructionMultipleSeparatorsTogetherShouldBeConsideredAsSingle() {
        var service = getNewBasePathService();
        var rootName = service.getRootName(); // /
        var separator = service.fileSystem.getSeparator();
        var expectedPath =  rootName + "path" + separator + "to" + separator + "file"; // => /path/to/file
        assertEquals(expectedPath, service.getPath(rootName, separator + separator + "path", "to", "file").toString());
    }

    @Test
    @DisplayName("It should construct a path and consider multiple separator together as single separator for relative path.")
    void testPathConstructionMultipleSeparatorsTogetherShouldBeConsideredAsSingleRelative() {
        var service = getNewBasePathService();
        var separator = service.fileSystem.getSeparator();
        var expectedPath =   "path" + separator + "to" + separator + "file"; // => path/to/file
        assertEquals(expectedPath, service.getPath("", "path", separator + separator + "to", "file").toString());
    }
}
