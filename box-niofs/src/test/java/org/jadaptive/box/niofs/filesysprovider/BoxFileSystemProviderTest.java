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
package org.jadaptive.box.niofs.filesysprovider;

import org.jadaptive.box.niofs.setup.AbstractRemoteSetup;
import org.jadaptive.box.niofs.stream.NullFilter;
import org.jadaptive.niofs.exception.JadNioFsFileAlreadyExistsFoundException;
import org.jadaptive.niofs.exception.JadNioFsFileNotFoundException;
import org.jadaptive.niofs.exception.JadNioFsParentPathInvalidException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BoxFileSystemProviderTest extends AbstractRemoteSetup {

    @BeforeAll
    void setup() throws Exception {
        super.init();
    }

    @Test
    @DisplayName("It should create a directory in Box.")
    void testShouldCreateDirectory() {
        var provider = getNewBoxFileSystemProvider();

        var path = provider.getPath(getPath("box:///test_box/test_create"));

        provider.createDirectory(path);

        var attributes = provider.readAttributes(path , BasicFileAttributes.class);

        assertNotNull(attributes);
        assertTrue(attributes.isDirectory());
    }

    @Test
    @DisplayName("It should throw exception if a directory to create already exists in Box.")
    void testShouldThrowExceptionIfDirectoryToCreateAlreadyExists() {
        var provider = getNewBoxFileSystemProvider();

        var path = provider.getPath(getPath("box:///test_box/test_create_exists"));

        provider.createDirectory(path);

        assertThrowsExactly(JadNioFsFileAlreadyExistsFoundException.class, () -> {
            provider.createDirectory(path);
        });
    }

    @Test
    @DisplayName("It should throw exception if a directory to create parent's directory does not exists in Box.")
    void testShouldThrowExceptionIfParentDirectoryDoesNotExistsOnCreate() {
        var provider = getNewBoxFileSystemProvider();

        var path = provider.getPath(getPath("box:///test_box/parent_not_present/test_create_exists"));

        assertThrowsExactly(JadNioFsParentPathInvalidException.class, () -> {
            provider.createDirectory(path);
        });
    }

    @Test
    @DisplayName("It should delete a directory in Box.")
    void testShouldDeleteDirectory() {
        var provider = getNewBoxFileSystemProvider();

        var path = provider.getPath(getPath("box:///test_box/test_delete"));

        provider.createDirectory(path);

        var attributes = provider.readAttributes(path , BasicFileAttributes.class);

        assertNotNull(attributes);
        assertTrue(attributes.isDirectory());

        provider.delete(path);

        assertThrowsExactly(JadNioFsFileNotFoundException.class, () -> {
            provider.readAttributes(path , BasicFileAttributes.class);
        });

    }

    @Test
    @DisplayName("It should throw exception if directory to delete does not exists in Box.")
    void testShouldThrowExceptionDeleteDirectoryNotPresent() {
        var provider = getNewBoxFileSystemProvider();

        var path = provider.getPath(getPath("box:///test_box/test_delete_does_not_exists"));

        assertThrowsExactly(JadNioFsFileNotFoundException.class, () -> {
            provider.delete(path);
        });
    }

    @Test
    @DisplayName("It should delete a file.")
    void testDeleteFile() throws IOException {

        var provider = getNewBoxFileSystemProvider();

        var path = provider.getPath(getPath("box:///test_box/test_delete"));

        provider.createDirectory(path);

        var fileToDeletePath = provider.getPath(getPath("box:///test_box/test_delete/file_to_copy.txt"));
        writeFileInBox(provider,
                fileToDeletePath,
                Path.of("src/test/resources/data/file_with_content.txt"));

        provider.delete(fileToDeletePath);

        assertThrowsExactly(JadNioFsFileNotFoundException.class, () -> {
            provider.readAttributes(fileToDeletePath , BasicFileAttributes.class);
        });
    }

    @Test
    @DisplayName("It should throw exception if file to delete is not present in Box.")
    void testShouldThrowExceptionIfDeleteFileDoesNotExists() {

        var provider = getNewBoxFileSystemProvider();

        var fileToDeletePath = provider.getPath(getPath("box:///test_box/test_delete_does_not_exists.txt"));

        assertThrowsExactly(JadNioFsFileNotFoundException.class, () -> {
            provider.delete(fileToDeletePath);
        });
    }

    @Test
    @DisplayName("It should copy file one directory to another.")
    void testShouldCopyFileFromOneDirectoryToAnother() throws IOException {
        var provider = getNewBoxFileSystemProvider();

        var sourceDirectory = provider.getPath(getPath("box:///test_box/copy_source"));
        var destinationDirectory = provider.getPath(getPath("box:///test_box/copy_destination"));

        provider.createDirectory(sourceDirectory);
        provider.createDirectory(destinationDirectory);

        String sourcePath = "box:///test_box/copy_source/file_to_copy.txt";
        writeFileInBox(provider,
                provider.getPath(getPath(sourcePath)),
                Path.of("src/test/resources/data/file_with_content.txt"));


        var sourceFile = provider.getPath(getPath(sourcePath));
        var destinationFile = provider.getPath(getPath("box:///test_box/copy_destination/file_to_copy.txt"));

        provider.copy(sourceFile, destinationFile);

        var attributes = provider.readAttributes(destinationFile , BasicFileAttributes.class);

        assertNotNull(attributes);
        assertTrue(attributes.isRegularFile());
    }

    @Test
    @DisplayName("It should copy file one directory to another.")
    void testShouldThrowExceptionIfSourceIsNotPresentCopyFileFromOneDirectoryToAnother() {
        var provider = getNewBoxFileSystemProvider();

        var sourceFile = provider.getPath(getPath("box:///test_box/copy_source_file_does_not_exists.txt"));
        var destinationDirectory = provider.getPath(getPath("box:///test_box/copy_destination_exception"));

        provider.createDirectory(destinationDirectory);

        var destinationFile = provider.getPath(getPath("box:///test_box/copy_destination/file_to_copy.txt"));

        assertThrowsExactly(JadNioFsFileNotFoundException.class, () -> {
            provider.copy(sourceFile, destinationFile);
        });
    }

    @Test
    @DisplayName("It should copy file one directory to another.")
    void testShouldThrowExceptionIfDestinationIsNotPresentCopyFileFromOneDirectoryToAnother() throws IOException {
        var provider = getNewBoxFileSystemProvider();

        var sourceDirectory = provider.getPath(getPath("box:///test_box/copy_source_destination_missing"));

        provider.createDirectory(sourceDirectory);

        String sourcePath = "box:///test_box/copy_source_destination_missing/file_to_copy.txt";
        writeFileInBox(provider,
                provider.getPath(getPath(sourcePath)),
                Path.of("src/test/resources/data/file_with_content.txt"));

        var sourceFile = provider.getPath(getPath(sourcePath));
        var destinationFile = provider.getPath(getPath("box:///test_box/copy_destination_not_present/file_to_copy.txt"));

        assertThrowsExactly(JadNioFsFileNotFoundException.class, () -> {
            provider.copy(sourceFile, destinationFile);
        });
    }

    @Test
    @DisplayName("It should move file one directory to another.")
    void testShouldMoveFileFromOneDirectoryToAnother() throws IOException {
        var provider = getNewBoxFileSystemProvider();

        var sourceDirectory = provider.getPath(getPath("box:///test_box/move_source"));
        var destinationDirectory = provider.getPath(getPath("box:///test_box/move_destination"));

        provider.createDirectory(sourceDirectory);
        provider.createDirectory(destinationDirectory);

        String sourcePath = "box:///test_box/move_source/file_to_move.txt";
        writeFileInBox(provider,
                provider.getPath(getPath(sourcePath)),
                Path.of("src/test/resources/data/file_with_content.txt"));


        var sourceFile = provider.getPath(getPath(sourcePath));
        var destinationFile = provider.getPath(getPath("box:///test_box/move_destination/file_to_move.txt"));

        provider.move(sourceFile, destinationFile);

        var attributes = provider.readAttributes(destinationFile , BasicFileAttributes.class);

        assertNotNull(attributes);
        assertTrue(attributes.isRegularFile());

        assertThrowsExactly(JadNioFsFileNotFoundException.class, () -> {
            provider.readAttributes(sourceFile , BasicFileAttributes.class);
        });
    }

    @Test
    @DisplayName("It should copy file one directory to another.")
    void testShouldThrowExceptionIfSourceIsNotPresentMoveFileFromOneDirectoryToAnother() {
        var provider = getNewBoxFileSystemProvider();

        var sourceFile = provider.getPath(getPath("box:///test_box/move_source_file_does_not_exists.txt"));
        var destinationDirectory = provider.getPath(getPath("box:///test_box/move_destination_exception"));

        provider.createDirectory(destinationDirectory);

        var destinationFile = provider.getPath(getPath("box:///test_box/move_destination/file_to_copy.txt"));

        assertThrowsExactly(JadNioFsFileNotFoundException.class, () -> {
            provider.move(sourceFile, destinationFile);
        });
    }

    @Test
    @DisplayName("It should copy file one directory to another.")
    void testShouldThrowExceptionIfDestinationIsNotPresentMoveFileFromOneDirectoryToAnother() throws IOException {
        var provider = getNewBoxFileSystemProvider();

        var sourceDirectory = provider.getPath(getPath("box:///test_box/move_source_destination_missing"));

        provider.createDirectory(sourceDirectory);

        String sourcePath = "box:///test_box/move_source_destination_missing/file_to_copy.txt";
        writeFileInBox(provider,
                provider.getPath(getPath(sourcePath)),
                Path.of("src/test/resources/data/file_with_content.txt"));

        var sourceFile = provider.getPath(getPath(sourcePath));
        var destinationFile = provider.getPath(getPath("box:///test_box/move_destination_not_present/file_to_copy.txt"));

        assertThrowsExactly(JadNioFsFileNotFoundException.class, () -> {
            provider.move(sourceFile, destinationFile);
        });
    }

    @Test
    @DisplayName("It should return true for to paths pointing to same file, false otherwise.")
    void testFileSame() throws IOException {
        var provider = getNewBoxFileSystemProvider();

        var sourceDirectory = provider.getPath(getPath("box:///test_box/test_file_same"));

        provider.createDirectory(sourceDirectory);

        String sourcePath = "box:///test_box/test_file_same/file_to_copy.txt";

        writeFileInBox(provider,
                provider.getPath(getPath(sourcePath)),
                Path.of("src/test/resources/data/file_with_content.txt"));

        var path1 = provider.getPath(getPath("box:///test_box/test_file_same/file.txt"));
        var path2 = provider.getPath(getPath("box:///test_box/test_file_same/file.txt"));
        var path3 = provider.getPath(getPath("box:///test_box/test_file_same/another_folder/../file.txt"));
        var path4 = provider.getPath(getPath("box:///test_box/test_file_same/to/another/../../file.txt"));
        var path5 = provider.getPath(getPath("box:///test_box/test_file_same/to/another/../../../file.txt"));


        // test same
        assertTrue(provider.isSameFile(path1, path2));
        assertTrue(provider.isSameFile(path1, path3));
        assertTrue(provider.isSameFile(path1, path4));

        // test different
        assertFalse(provider.isSameFile(path1, path5));
    }

    @Test
    @DisplayName("It should return an iterator without any filter applied when none is provided.")
    void testDirectoryStreamWithNoFilter() throws IOException {

        var provider = getNewBoxFileSystemProvider();
        var directory = provider.getPath(getPath("box:///test_box/filter"));

        var collectFiles = new ArrayList<String>();

        try (DirectoryStream<Path> stream = provider.newDirectoryStream(directory, NullFilter.INSTANCE)) {
            for (Path p : stream) {
                collectFiles.add(p.toString());
            }
        }

        assertTrue(collectFiles.size() == 6);
    }

    @Test
    @DisplayName("It should return an iterator with filter applied when one is provided.")
    void testDirectoryStreamWithFilter() throws IOException {

        var provider = getNewBoxFileSystemProvider();
        var directory = provider.getPath(getPath("box:///test_box/filter"));

        var collectFiles = new ArrayList<String>();

        try (DirectoryStream<Path> stream = provider
                .newDirectoryStream(directory, entry -> entry.getFileName().toString().endsWith("_pdf.txt"))
        ) {
            for (Path p : stream) {
                collectFiles.add(p.toString());
            }
        }

        assertTrue(collectFiles.size() == 3);
    }

    @Test
    @DisplayName("It should return file store for box.")
    void testShouldReturnBoxFileStore() throws URISyntaxException {
        var provider = getNewBoxFileSystemProvider();
        var path = provider.getPath(new URI("box:///"));

        var fileStore = provider.getFileStore(path);

        assertNotNull(fileStore);

        assertNotNull(fileStore.name());
        assertNotNull(fileStore.type());

        assertFalse(fileStore.isReadOnly());
    }

    public void setUpTestData() throws Exception {
        var provider = getNewBoxFileSystemProvider();
        var path = provider.getPath(new URI("box:///test_box"));

        try {
            provider.delete(path);
        } catch (JadNioFsFileNotFoundException e) {}

        provider.createDirectory(path);

        writeFileInBox(provider, "file_with_content.txt");

        var filter_path = provider.getPath(new URI("box:///test_box/filter"));

        try {
            provider.delete(filter_path);
        } catch (JadNioFsFileNotFoundException e) {}

        provider.createDirectory(filter_path);

        writeFileInBox(provider, "filter", "file_to_filter_1_doc.txt");
        writeFileInBox(provider, "filter", "file_to_filter_2_doc.txt");
        writeFileInBox(provider, "filter", "file_to_filter_3_doc.txt");

        writeFileInBox(provider, "filter", "file_to_filter_1_pdf.txt");
        writeFileInBox(provider, "filter", "file_to_filter_2_pdf.txt");
        writeFileInBox(provider, "filter", "file_to_filter_3_pdf.txt");
    }
}
