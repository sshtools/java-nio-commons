package org.jadaptive.box.niofs.filesysprovider;

import org.jadaptive.box.niofs.api.DeveloperTokenRemoteAPI;
import org.jadaptive.box.niofs.api.client.locator.BoxConnectionAPILocator;
import org.jadaptive.box.niofs.exception.BoxFileAlreadyExistsFoundException;
import org.jadaptive.box.niofs.exception.BoxFileNotFoundException;
import org.jadaptive.box.niofs.exception.BoxParentPathInvalidException;
import org.jadaptive.box.niofs.filesys.BoxFileSystem;
import org.jadaptive.box.niofs.path.BoxPath;
import org.jadaptive.box.niofs.path.BoxPathService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class BoxFileSystemProviderTest {

    private static String token;

    @BeforeAll
    static void init() throws IOException, URISyntaxException {
        token = Files.readString(Path.of("src/test/resources/config/dev_token"));
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalStateException("Box Developer Token is null or empty.");
        }

        BoxConnectionAPILocator.setBoxRemoteAPI(new DeveloperTokenRemoteAPI(token));

        setUpTestData();
    }

    @Test
    @DisplayName("It should create a directory in Box.")
    void testShouldCreateDirectory() throws IOException {
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

        assertThrowsExactly(BoxFileAlreadyExistsFoundException.class, () -> {
            provider.createDirectory(path);
        });
    }

    @Test
    @DisplayName("It should throw exception if a directory to create parent's directory does not exists in Box.")
    void testShouldThrowExceptionIfParentDirectoryDoesNotExistsOnCreate() {
        var provider = getNewBoxFileSystemProvider();

        var path = provider.getPath(getPath("box:///test_box/parent_not_present/test_create_exists"));

        assertThrowsExactly(BoxParentPathInvalidException.class, () -> {
            provider.createDirectory(path);
        });
    }

    @Test
    @DisplayName("It should delete a directory in Box.")
    void testShouldDeleteDirectory() throws IOException {
        var provider = getNewBoxFileSystemProvider();

        var path = provider.getPath(getPath("box:///test_box/test_delete"));

        provider.createDirectory(path);

        var attributes = provider.readAttributes(path , BasicFileAttributes.class);

        assertNotNull(attributes);
        assertTrue(attributes.isDirectory());

        provider.delete(path);

        assertThrowsExactly(BoxFileNotFoundException.class, () -> {
            provider.readAttributes(path , BasicFileAttributes.class);
        });

    }

    @Test
    @DisplayName("It should throw exception if directory to delete does not exists in Box.")
    void testShouldThrowExceptionDeleteDirectoryNotPresent() {
        var provider = getNewBoxFileSystemProvider();

        var path = provider.getPath(getPath("box:///test_box/test_delete_does_not_exists"));

        assertThrowsExactly(BoxFileNotFoundException.class, () -> {
            provider.delete(path);
        });
    }

    @Test
    @DisplayName("It should copy file one directory to another.")
    void testShouldCopyFileFromOneDirectoryToAnother() throws URISyntaxException, IOException {
        var provider = getNewBoxFileSystemProvider();

        var sourceDirectory = provider.getPath(getPath("box:///test_box/copy_source"));
        var destinationDirectory = provider.getPath(getPath("box:///test_box/copy_destination"));

        provider.createDirectory(sourceDirectory);
        provider.createDirectory(destinationDirectory);

        writeFileInBox(provider,
                provider.getPath(getPath("box:///test_box/copy_source/file_to_copy.txt")),
                Path.of("src/test/resources/data/file_with_content.txt"));


        var sourceFile = provider.getPath(getPath("box:///test_box/copy_source/file_to_copy.txt"));
        var destinationFile = provider.getPath(getPath("box:///test_box/copy_destination/file_to_copy.txt"));

        provider.copy(sourceFile, destinationFile);

        var attributes = provider.readAttributes(destinationFile , BasicFileAttributes.class);

        assertNotNull(attributes);
        assertTrue(attributes.isRegularFile());
    }

    @Test
    @DisplayName("It should delete a file")
    void testDeleteFile() throws IOException {

        var provider = getNewBoxFileSystemProvider();

        var path = provider.getPath(getPath("box:///test_box/test_delete"));

        provider.createDirectory(path);

        var fileToDeletePath = provider.getPath(getPath("box:///test_box/test_delete/file_to_copy.txt"));
        writeFileInBox(provider,
                fileToDeletePath,
                Path.of("src/test/resources/data/file_with_content.txt"));

        provider.delete(fileToDeletePath);

        assertThrowsExactly(BoxFileNotFoundException.class, () -> {
            provider.readAttributes(fileToDeletePath , BasicFileAttributes.class);
        });
    }

    @Test
    @DisplayName("It should move file one directory to another.")
    void testShouldMoveFileFromOneDirectoryToAnother() throws URISyntaxException, IOException {
        var provider = getNewBoxFileSystemProvider();

        var sourceDirectory = provider.getPath(getPath("box:///test_box/move_source"));
        var destinationDirectory = provider.getPath(getPath("box:///test_box/move_destination"));

        provider.createDirectory(sourceDirectory);
        provider.createDirectory(destinationDirectory);

        writeFileInBox(provider,
                provider.getPath(getPath("box:///test_box/move_source/file_to_move.txt")),
                Path.of("src/test/resources/data/file_with_content.txt"));


        var sourceFile = provider.getPath(getPath("box:///test_box/move_source/file_to_move.txt"));
        var destinationFile = provider.getPath(getPath("box:///test_box/move_destination/file_to_move.txt"));

        provider.move(sourceFile, destinationFile);

        var attributes = provider.readAttributes(destinationFile , BasicFileAttributes.class);

        assertNotNull(attributes);
        assertTrue(attributes.isRegularFile());

        assertThrowsExactly(BoxFileNotFoundException.class, () -> {
            provider.readAttributes(sourceFile , BasicFileAttributes.class);
        });
    }


    private static BoxFileSystemProvider getNewBoxFileSystemProvider() {
        var boxRemoteAPI = BoxConnectionAPILocator.getBoxRemoteAPI();
        var pathService = new BoxPathService();
        var provider = new BoxFileSystemProvider();


        var fs = new BoxFileSystem(provider, pathService, boxRemoteAPI);
        pathService.setFileSystem(fs);

        return provider;
    }

    private static void setUpTestData() throws URISyntaxException, IOException {
        var provider = getNewBoxFileSystemProvider();
        var path = provider.getPath(new URI("box:///test_box"));

        try {
            provider.delete(path);
        } catch (BoxFileNotFoundException e) {}

        provider.createDirectory(path);

        writeFileInBox(provider, "file_with_content.txt");

        writeFileInBox(provider, "file_to_delete.txt");
    }

    private static void writeFileInBox(BoxFileSystemProvider provider, String fileName) throws URISyntaxException, IOException {
        var fileWithContentPath = provider.getPath(new URI(String.format("box:///test_box/%s", fileName)));
        writeFileInBox(provider, fileWithContentPath, Path.of(String.format("src/test/resources/data/%s", fileName)));
    }

    private static void writeFileInBox(BoxFileSystemProvider provider, BoxPath fileToWrite, Path fileToRead) throws IOException {
        var channel = provider.newByteChannel(fileToWrite, Set.of());

        String s = Files.readString(fileToRead);
        ByteBuffer bfSrc = ByteBuffer.wrap(s.getBytes());
        channel.write(bfSrc);
    }

    private URI getPath(String path) {
        try {
            return new URI(path);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}
