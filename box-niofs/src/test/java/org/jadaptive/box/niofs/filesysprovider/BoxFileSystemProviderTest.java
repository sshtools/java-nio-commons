package org.jadaptive.box.niofs.filesysprovider;

import org.jadaptive.box.niofs.api.DeveloperTokenRemoteAPI;
import org.jadaptive.box.niofs.api.client.locator.BoxConnectionAPILocator;
import org.jadaptive.box.niofs.exception.BoxFileNotFoundException;
import org.jadaptive.box.niofs.filesys.BoxFileSystem;
import org.jadaptive.box.niofs.path.BoxPathService;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class BoxFileSystemProviderTest {

    private static String token;

    @BeforeAll
    static void init() throws IOException, URISyntaxException {
        token = Files.readString(Path.of("src/test/resources/config/dev_token"));
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalStateException("Box Developer Token is null or empty.");
        }

        BoxConnectionAPILocator.setBoxRemoteAPI(new DeveloperTokenRemoteAPI(token));
        var provider = getNewBoxFileSystemProvider();

        setUpTestData(provider);
    }

    private static BoxFileSystemProvider getNewBoxFileSystemProvider() {
        var boxRemoteAPI = BoxConnectionAPILocator.getBoxRemoteAPI();
        var pathService = new BoxPathService();
        var provider = new BoxFileSystemProvider();


        var fs = new BoxFileSystem(provider, pathService, boxRemoteAPI);
        pathService.setFileSystem(fs);

        return provider;
    }

    private static void setUpTestData(BoxFileSystemProvider provider) throws URISyntaxException, IOException {
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
        var channel = provider.newByteChannel(fileWithContentPath, Set.of());

        String s = Files.readString(Path.of(String.format("src/test/resources/data/%s", fileName)));
        ByteBuffer bfSrc = ByteBuffer.wrap(s.getBytes());
        channel.write(bfSrc);
    }
}
