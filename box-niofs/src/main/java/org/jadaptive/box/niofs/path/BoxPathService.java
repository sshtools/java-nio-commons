package org.jadaptive.box.niofs.path;

import org.jadaptive.box.niofs.filesys.BoxFileSystem;
import org.jadaptive.niofs.path.BasePathService;

import java.net.URI;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class BoxPathService extends BasePathService {

    private static final String ROOT_NAME = "/";

    public BoxPathService() {
    }

    @Override
    public BoxFileSystem getFileSystem() {
        return (BoxFileSystem) this.fileSystem;
    }

    @Override
    public void setFileSystem(FileSystem fileSystem) {
        if (fileSystem instanceof BoxFileSystem) {
            this.fileSystem = fileSystem;
            return;
        }
        throw new IllegalArgumentException("Filesystem is not BoxFileSystem");
    }

    @Override
    public String getRootName() {
        return ROOT_NAME;
    }

    @Override
    public BoxPath createRoot() {
        return new BoxPath(this, getRootName(), Collections.emptyList());
    }

    @Override
    public BoxPath createPath(String root, List<String> names) {
        return new BoxPath(this, root, names);
    }

    @Override
    public BoxPath getPath(String first, String... more) {

        if (first == null || more == null) {
            throw new IllegalArgumentException("Arguments passed cannot be null.");
        }

        if (first.isBlank() && more.length == 0) {
            return (BoxPath) createEmptyPath();
        }

        var parts = new ArrayList<String>();
        parts.add(first);
        parts.addAll(Arrays.asList(more));

        var nonEmptyParts = parts
                .stream()
                .filter(p -> !p.isBlank())
                .collect(Collectors.toList());

        if (nonEmptyParts.isEmpty()) {
            return (BoxPath) createEmptyPath();
        }

        var hasRoot = nonEmptyParts.get(0).equals(getRootName());

        var concatenatedPath = String.join("", nonEmptyParts);

        var finalNames = getFileSystem().getNamesForPath(concatenatedPath);

        return hasRoot ? createPath(getRootName(), finalNames) : createPath(null, finalNames);
    }

    @Override
    public BoxPath getPath(URI uri) {
        checkURI(uri, true);

        var path = extractPathFromURI(uri);

        if (!path.startsWith(getRootName())) {
            throw new IllegalArgumentException("Uri path must start with root name.");
        }

        var names = getFileSystem().getNamesForPath(path);

        return getPath(getRootName(), names.toArray(new String[0]));
    }

    @Override
    public BoxPath createPathFromIndex(int beginIndex, int endIndex, List<String> names) {
        if (beginIndex < 0 || beginIndex > names.size()) {
            throw new IllegalArgumentException("Begin index is out of bounds.");
        }

        if (endIndex < 0 || endIndex > names.size()) {
            throw new IllegalArgumentException("End index is out of bounds.");
        }

        if (beginIndex > endIndex) {
            throw new IllegalArgumentException("Begin index cannot be greater than End index.");
        }

        var subName = names.subList(beginIndex, endIndex);

        return new BoxPath(this, null, subName);
    }

    private String extractPathFromURI(URI uri) {
        requireNonNull(uri, "URI cannot be null");
        var path = uri.getPath();
        requireNonNull(path, "Path of URI cannot be null");
        return path;
    }

    /**
     * Requires URI in format as mentioned below, no path required.
     * <ul>
     *     <li>box://[user-id].</li>
     * </ul>
     *
     * Note: where user-id is numeric id from box.
     *
     * @param uri
     */
    private static void checkURI(URI uri) {
        checkURI(uri, false);
    }

    /**
     * Requires URI in format as mentioned below, can have path if required.
     * <ul>
     * 		<li>box://[user-id].</li>
     * 		<li>box://[user-id]/path/to/file.</li>
     * </ul>
     *
     * Note: where user-id is numeric id from box.
     *
     * @param uri
     * @param needsPath pass boolean true if path is required in URI.
     */
    private static void checkURI(URI uri, boolean needsPath) {
        requireNonNull(uri, "URI cannot be null.");

        var scheme = uri.getScheme();
        requireNonNull(scheme, "Scheme not found in URI.");
        if (!"box".equals(scheme)) {
            throw new IllegalStateException(format("URI scheme is not 'box' is %s", scheme));
        }

        var authority = uri.getRawAuthority();
        System.out.println("The authority is " + authority);
        if (authority != null) {
            throw new IllegalStateException("Authority is not required.");
        }

        var path = uri.getPath();
        if (needsPath && path == null) {
            throw new IllegalStateException("Path is required.");
        }

        if (!needsPath && path != null && !path.isEmpty()) {
            throw new IllegalStateException("Path is not required.");
        }

        var port = uri.getPort();
        if (port > -1) {
            throw new IllegalStateException("Port is not required.");
        }

        var fragment = uri.getRawFragment();
        if (fragment != null) {
            throw new IllegalStateException("Fragment is not required.");
        }

        var query = uri.getRawQuery();
        if (query != null) {
            throw new IllegalStateException("Query is not required.");
        }
    }
}