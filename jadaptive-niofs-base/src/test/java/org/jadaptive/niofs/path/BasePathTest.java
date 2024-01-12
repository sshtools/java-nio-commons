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
