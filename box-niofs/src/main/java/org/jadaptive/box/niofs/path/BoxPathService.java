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
package org.jadaptive.box.niofs.path;

import org.jadaptive.box.niofs.filesys.BoxFileSystem;
import org.jadaptive.niofs.path.BasePath;
import org.jadaptive.niofs.path.BasePathService;

import java.net.URI;
import java.nio.file.FileSystem;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class BoxPathService extends BasePathService {

    private static final String ROOT_NAME = "/";

    private final BoxPath workingDirectory;

    public BoxPathService() {
        this.workingDirectory = createRoot();
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

        var regExFriendlySeparator = getFileSystem().getRegExFriendlySeparator();

        var allPartsTogether = new ArrayList<String>();
        allPartsTogether.add(first);
        allPartsTogether.addAll(Arrays.asList(more));

        var hasRoot = false;
        var relativePart = "";

        var iterator = allPartsTogether.listIterator();
        while (iterator.hasNext()) {
            var part = iterator.next();
            var strippedLeadingPart = part.stripLeading();
            // if there is no relative part found so far and stripped leading starts with root
            // we have our part with a root
            if (relativePart.isBlank() && strippedLeadingPart.startsWith(getRootName())) {
                hasRoot = true;
                var rootPart = strippedLeadingPart.replaceFirst(getRootName(), "");
                iterator.set(rootPart);
                break;
            }

            // mark a relative part
            // if we find one we do not have a root, it is a relative path
            if (!part.isBlank()) {
                relativePart = part;
            }
        }

        // collect all the parts, by splitting given arguments by separator
        var nonEmptyParts = allPartsTogether
                .stream()
                .flatMap(p -> Stream.of(p.split(regExFriendlySeparator)))
                .filter(p -> !p.isEmpty())
                .collect(Collectors.toList());

        // only root will have non empty pats, hence !hasRoot part
        if (!hasRoot && nonEmptyParts.isEmpty()) {
            return (BoxPath) createEmptyPath();
        }

        return hasRoot ? createPath(getRootName(), nonEmptyParts)
                : createPath(null, nonEmptyParts);
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
    public BasePath getWorkingDirectory() {
        return this.workingDirectory;
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
