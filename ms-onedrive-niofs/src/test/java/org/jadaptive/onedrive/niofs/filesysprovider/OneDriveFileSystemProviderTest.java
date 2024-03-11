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
package org.jadaptive.onedrive.niofs.filesysprovider;

import org.jadaptive.niofs.exception.JadNioFsFileAlreadyExistsFoundException;
import org.jadaptive.niofs.exception.JadNioFsFileNotFoundException;
import org.jadaptive.niofs.exception.JadNioFsParentPathInvalidException;
import org.jadaptive.niofs.stream.NullFilter;
import org.jadaptive.onedrive.niofs.setup.AbstractRemoteSetup;
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
public class OneDriveFileSystemProviderTest extends AbstractRemoteSetup {

    @BeforeAll
    void setup() throws Exception {
        super.init();
    }

    @Test
    @DisplayName("It should create a directory in OneDrive.")
    void testShouldCreateDirectory() {
        var provider = getNewOneDriveFileSystemProvider();

        var path = provider.getPath(toURI("onedrive:///test_onedrive/test_create"));

        provider.createDirectory(path);

        var attributes = provider.readAttributes(path , BasicFileAttributes.class);

        assertNotNull(attributes);
        assertTrue(attributes.isDirectory());
    }

    @Test
    @DisplayName("It should throw exception if a directory to create already exists in OneDrive.")
    void testShouldThrowExceptionIfDirectoryToCreateAlreadyExists() {
        var provider = getNewOneDriveFileSystemProvider();

        var path = provider.getPath(toURI("onedrive:///test_onedrive/test_create_exists"));

        provider.createDirectory(path);

        assertThrowsExactly(JadNioFsFileAlreadyExistsFoundException.class, () -> {
            provider.createDirectory(path);
        });
    }

    @Test
    @DisplayName("It should throw exception if a directory to create parent's directory does not exists in OneDrive.")
    void testShouldThrowExceptionIfParentDirectoryDoesNotExistsOnCreate() {
        var provider = getNewOneDriveFileSystemProvider();

        var path = provider.getPath(toURI("onedrive:///test_onedrive/parent_not_present/test_create_exists"));

        assertThrowsExactly(JadNioFsParentPathInvalidException.class, () -> {
            provider.createDirectory(path);
        });
    }

    @Test
    @DisplayName("It should delete a directory in OneDrive.")
    void testShouldDeleteDirectory() {
        var provider = getNewOneDriveFileSystemProvider();

        var path = provider.getPath(toURI("onedrive:///test_onedrive/test_delete"));

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
    @DisplayName("It should throw exception if directory to delete does not exists in OneDrive.")
    void testShouldThrowExceptionDeleteDirectoryNotPresent() {
        var provider = getNewOneDriveFileSystemProvider();

        var path = provider.getPath(toURI("onedrive:///test_onedrive/test_delete_does_not_exists"));

        assertThrowsExactly(JadNioFsFileNotFoundException.class, () -> {
            provider.delete(path);
        });
    }

    @Test
    @DisplayName("It should delete a file.")
    void testDeleteFile() throws IOException {

        var provider = getNewOneDriveFileSystemProvider();

        var path = provider.getPath(toURI("onedrive:///test_onedrive/test_delete"));

        provider.createDirectory(path);

        var fileToDeletePath = provider.getPath(toURI("onedrive:///test_onedrive/test_delete/file_to_copy.txt"));
        writeFileInOneDrive(provider,
                fileToDeletePath,
                Path.of("src/test/resources/data/file_with_content.txt"));

        provider.delete(fileToDeletePath);

        assertThrowsExactly(JadNioFsFileNotFoundException.class, () -> {
            provider.readAttributes(fileToDeletePath , BasicFileAttributes.class);
        });
    }

    @Test
    @DisplayName("It should throw exception if file to delete is not present in OneDrive.")
    void testShouldThrowExceptionIfDeleteFileDoesNotExists() {

        var provider = getNewOneDriveFileSystemProvider();

        var fileToDeletePath = provider.getPath(toURI("onedrive:///test_onedrive/test_delete_does_not_exists.txt"));

        assertThrowsExactly(JadNioFsFileNotFoundException.class, () -> {
            provider.delete(fileToDeletePath);
        });
    }

    @Test
    @DisplayName("It should copy file from one directory to another.")
    void testShouldCopyFileFromOneDirectoryToAnother() throws IOException {
        var provider = getNewOneDriveFileSystemProvider();

        var sourceDirectory = provider.getPath(toURI("onedrive:///test_onedrive/copy_source"));
        var destinationDirectory = provider.getPath(toURI("onedrive:///test_onedrive/copy_destination"));

        provider.createDirectory(sourceDirectory);
        provider.createDirectory(destinationDirectory);

        String sourcePath = "onedrive:///test_onedrive/copy_source/file_to_copy.txt";
        writeFileInOneDrive(provider,
                provider.getPath(toURI(sourcePath)),
                Path.of("src/test/resources/data/file_with_content.txt"));


        var sourceFile = provider.getPath(toURI(sourcePath));
        var destinationFile = provider.getPath(toURI("onedrive:///test_onedrive/copy_destination/file_to_copy.txt"));

        provider.copy(sourceFile, destinationFile);

        var attributes = provider.readAttributes(destinationFile , BasicFileAttributes.class);

        assertNotNull(attributes);
        assertTrue(attributes.isRegularFile());
    }

    @Test
    @DisplayName("It should throw exception if file to copy does not exists.")
    void testShouldThrowExceptionIfSourceIsNotPresentCopyFileFromOneDirectoryToAnother() {
        var provider = getNewOneDriveFileSystemProvider();

        var sourceFile = provider.getPath(toURI("onedrive:///test_onedrive/copy_source_file_does_not_exists.txt"));
        var destinationDirectory = provider.getPath(toURI("onedrive:///test_onedrive/copy_destination_exception"));

        provider.createDirectory(destinationDirectory);

        var destinationFile = provider.getPath(toURI("onedrive:///test_onedrive/copy_destination/file_to_copy.txt"));

        assertThrowsExactly(JadNioFsFileNotFoundException.class, () -> {
            provider.copy(sourceFile, destinationFile);
        });
    }

    @Test
    @DisplayName("It should throw exception when file is copied to a folder which does not exists.")
    void testShouldThrowExceptionIfDestinationIsNotPresentCopyFileFromOneDirectoryToAnother() throws IOException {
        var provider = getNewOneDriveFileSystemProvider();

        var sourceDirectory = provider.getPath(toURI("onedrive:///test_onedrive/copy_source_destination_missing"));

        provider.createDirectory(sourceDirectory);

        String sourcePath = "onedrive:///test_onedrive/copy_source_destination_missing/file_to_copy.txt";
        writeFileInOneDrive(provider,
                provider.getPath(toURI(sourcePath)),
                Path.of("src/test/resources/data/file_with_content.txt"));

        var sourceFile = provider.getPath(toURI(sourcePath));
        var destinationFile = provider.getPath(toURI("onedrive:///test_onedrive/copy_destination_not_present/file_to_copy.txt"));

        assertThrowsExactly(JadNioFsFileNotFoundException.class, () -> {
            provider.copy(sourceFile, destinationFile);
        });
    }

    @Test
    @DisplayName("It should move file from one directory to another.")
    void testShouldMoveFileFromOneDirectoryToAnother() throws IOException {
        var provider = getNewOneDriveFileSystemProvider();

        var sourceDirectory = provider.getPath(toURI("onedrive:///test_onedrive/move_source"));
        var destinationDirectory = provider.getPath(toURI("onedrive:///test_onedrive/move_destination"));

        provider.createDirectory(sourceDirectory);
        provider.createDirectory(destinationDirectory);

        String sourcePath = "onedrive:///test_onedrive/move_source/file_to_move.txt";
        writeFileInOneDrive(provider,
                provider.getPath(toURI(sourcePath)),
                Path.of("src/test/resources/data/file_with_content.txt"));


        var sourceFile = provider.getPath(toURI(sourcePath));
        var destinationFile = provider.getPath(toURI("onedrive:///test_onedrive/move_destination/file_to_move.txt"));

        provider.move(sourceFile, destinationFile);

        var attributes = provider.readAttributes(destinationFile , BasicFileAttributes.class);

        assertNotNull(attributes);
        assertTrue(attributes.isRegularFile());

        assertThrowsExactly(JadNioFsFileNotFoundException.class, () -> {
            provider.readAttributes(sourceFile , BasicFileAttributes.class);
        });
    }

    @Test
    @DisplayName("It should throw exception when file to move does not exists.")
    void testShouldThrowExceptionIfSourceIsNotPresentMoveFileFromOneDirectoryToAnother() {
        var provider = getNewOneDriveFileSystemProvider();

        var sourceFile = provider.getPath(toURI("onedrive:///test_onedrive/move_source_file_does_not_exists.txt"));
        var destinationDirectory = provider.getPath(toURI("onedrive:///test_onedrive/move_destination_exception"));

        provider.createDirectory(destinationDirectory);

        var destinationFile = provider.getPath(toURI("onedrive:///test_onedrive/move_destination/file_to_copy.txt"));

        assertThrowsExactly(JadNioFsFileNotFoundException.class, () -> {
            provider.move(sourceFile, destinationFile);
        });
    }

    @Test
    @DisplayName("It should throw exception when file to be moved to a folder does not exists.")
    void testShouldThrowExceptionIfDestinationIsNotPresentMoveFileFromOneDirectoryToAnother() throws IOException {
        var provider = getNewOneDriveFileSystemProvider();

        var sourceDirectory = provider.getPath(toURI("onedrive:///test_onedrive/move_source_destination_missing"));

        provider.createDirectory(sourceDirectory);

        String sourcePath = "onedrive:///test_onedrive/move_source_destination_missing/file_to_copy.txt";
        writeFileInOneDrive(provider,
                provider.getPath(toURI(sourcePath)),
                Path.of("src/test/resources/data/file_with_content.txt"));

        var sourceFile = provider.getPath(toURI(sourcePath));
        var destinationFile = provider.getPath(toURI("onedrive:///test_onedrive/move_destination_not_present/file_to_copy.txt"));

        assertThrowsExactly(JadNioFsFileNotFoundException.class, () -> {
            provider.move(sourceFile, destinationFile);
        });
    }

    @Test
    @DisplayName("It should return true for to paths pointing to same file, false otherwise.")
    void testFileSame() throws IOException {
        var provider = getNewOneDriveFileSystemProvider();

        var sourceDirectory = provider.getPath(toURI("onedrive:///test_onedrive/test_file_same"));

        provider.createDirectory(sourceDirectory);

        String sourcePath = "onedrive:///test_onedrive/test_file_same/file.txt";

        writeFileInOneDrive(provider,
                provider.getPath(toURI(sourcePath)),
                Path.of("src/test/resources/data/file_with_content.txt"));

        var path1 = provider.getPath(toURI("onedrive:///test_onedrive/test_file_same/file.txt"));
        var path2 = provider.getPath(toURI("onedrive:///test_onedrive/test_file_same/file.txt"));
        var path3 = provider.getPath(toURI("onedrive:///test_onedrive/test_file_same/another_folder/../file.txt"));
        var path4 = provider.getPath(toURI("onedrive:///test_onedrive/test_file_same/to/another/../../file.txt"));
        var path5 = provider.getPath(toURI("onedrive:///test_onedrive/test_file_same/to/another/../../../file.txt"));


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

        var provider = getNewOneDriveFileSystemProvider();
        var directory = provider.getPath(toURI("onedrive:///test_onedrive/filter"));

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

        var provider = getNewOneDriveFileSystemProvider();
        var directory = provider.getPath(toURI("onedrive:///test_onedrive/filter"));

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
    @DisplayName("It should return file store for OneDrive.")
    void testShouldReturnOneDriveFileStore() throws URISyntaxException {
        var provider = getNewOneDriveFileSystemProvider();
        var path = provider.getPath(new URI("onedrive:///"));

        var fileStore = provider.getFileStore(path);

        assertNotNull(fileStore);

        assertNotNull(fileStore.name());
        assertNotNull(fileStore.type());

        assertFalse(fileStore.isReadOnly());
    }

    public void setUpTestData() throws Exception {
        var provider = getNewOneDriveFileSystemProvider();
        var path = provider.getPath(new URI("onedrive:///test_onedrive"));

        try {
            provider.delete(path);
        } catch (JadNioFsFileNotFoundException e) {}

        provider.createDirectory(path);

        writeFileInOneDrive(provider, "file_with_content.txt");

        var filter_path = provider.getPath(new URI("onedrive:///test_onedrive/filter"));

        try {
            provider.delete(filter_path);
        } catch (JadNioFsFileNotFoundException e) {}

        provider.createDirectory(filter_path);

        writeFileInOneDrive(provider, "filter", "file_to_filter_1_doc.txt");
        writeFileInOneDrive(provider, "filter", "file_to_filter_2_doc.txt");
        writeFileInOneDrive(provider, "filter", "file_to_filter_3_doc.txt");

        writeFileInOneDrive(provider, "filter", "file_to_filter_1_pdf.txt");
        writeFileInOneDrive(provider, "filter", "file_to_filter_2_pdf.txt");
        writeFileInOneDrive(provider, "filter", "file_to_filter_3_pdf.txt");
    }
}
