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

import org.jadaptive.niofs.filesys.BaseFileSystem;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public abstract class BasePathService {
    protected FileSystem fileSystem;

    protected final BasePath workingDirectory;

    public BasePathService() {
        this.workingDirectory = createRoot();
    }

    public abstract BasePath createPath(BasePathService basePathService, String root, List<String> names);
    public abstract FileSystem getFileSystem();
    public abstract void setFileSystem(FileSystem fileSystem);
    public abstract String getRootName();
    public abstract BasePath createRoot();
    public abstract BasePath createPathFromIndex(int beginIndex, int endIndex, List<String> names);
    public abstract BasePath createPath(String root, List<String> names);
    public BasePath createEmptyPath() {
        return createPath(null, List.of(""));
    }
    public abstract BasePath getPath(String[] paths);
    public abstract BasePath getPath(String first, String... more);
    public abstract BasePath getPath(URI uri);
    public abstract BasePath getWorkingDirectory();
    public abstract String getScheme();

    protected BasePath getPathHelper(String first, String... more) {

        if (first == null || more == null) {
            throw new IllegalArgumentException("Arguments passed cannot be null.");
        }

        if (first.isBlank() && more.length == 0) {
            return createEmptyPath();
        }

        var regExFriendlySeparator = ((BaseFileSystem) getFileSystem()).getRegExFriendlySeparator();

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

        // only root will have non empty parts, hence !hasRoot part
        if (!hasRoot && nonEmptyParts.isEmpty()) {
            return createEmptyPath();
        }

        return hasRoot ? createPath(getRootName(), nonEmptyParts)
                : createPath(null, nonEmptyParts);
    }

    protected BasePath getPathHelper(URI uri) {
        checkURI(uri, true);

        var path = extractPathFromURI(uri);

        if (!path.startsWith(getRootName())) {
            throw new IllegalArgumentException("Uri path must start with root name.");
        }

        var names = ((BaseFileSystem) getFileSystem()).getNamesForPath(path);

        return getPath(getRootName(), names.toArray(new String[0]));
    }

    protected BasePath getPathHelper(String[] paths) {
        if (paths == null || paths.length == 0) {
            throw new IllegalArgumentException("Arguments passed cannot be null or empty.");
        }
        return paths.length > 1 ? getPath(paths[0], Arrays.copyOfRange(paths, 1, paths.length))
                : getPath(paths[0]);
    }

    protected BasePath createPathFromIndexHelper(int beginIndex, int endIndex, List<String> names) {
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

        return createPath(this, null, subName);
    }

    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        int colonIndex = syntaxAndPattern.indexOf(':');
        if ((colonIndex <= 0) || (colonIndex == syntaxAndPattern.length() - 1)) {
            throw new IllegalArgumentException(
                    "syntaxAndPattern must have form \"syntax:pattern\" but was \"" + syntaxAndPattern + "\"");
        }

        String syntax = syntaxAndPattern.substring(0, colonIndex);
        String pattern = syntaxAndPattern.substring(colonIndex + 1);
        String expr;
        switch (syntax) {
            case "glob":
                expr = globToRegex(pattern);
                break;
            case "regex":
                expr = pattern;
                break;
            default:
                throw new UnsupportedOperationException("Unsupported path matcher syntax: \'" + syntax + "\'");
        }
        final Pattern regex = Pattern.compile(expr);
        return new PathMatcher() {
            @Override
            public boolean matches(Path path) {
                String str = path.toString();
                Matcher m = regex.matcher(str);
                return m.matches();
            }
        };
    }

    protected String globToRegex(String pattern) {
        StringBuilder sb = new StringBuilder(pattern.length());
        int inGroup = 0;
        int inClass = 0;
        boolean inQE = false;
        int firstIndexInClass = -1;
        char[] arr = pattern.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            char ch = arr[i];
            switch (ch) {
                case '\\':
                    if (++i >= arr.length) {
                        sb.append('\\');
                    } else {
                        char next = arr[i];
                        switch (next) {
                            case ',':
                                // escape not needed
                                break;
                            case 'Q':
                                inQE = true;
                                sb.append("\\");
                                break;
                            case 'E':
                                // extra escape needed
                                inQE = false;
                                sb.append("\\");
                                break;
                            default:
                                sb.append('\\');
                                break;
                        }
                        sb.append(next);
                    }
                    break;
                default:
                    if (inQE)
                        sb.append(ch);
                    else {
                        switch (ch) {

                            case '*':
                                sb.append(inClass == 0 ? ".*" : "*");
                                break;
                            case '?':
                                sb.append(inClass == 0 ? '.' : '?');
                                break;
                            case '[':
                                inClass++;
                                firstIndexInClass = i + 1;
                                sb.append('[');
                                break;
                            case ']':
                                inClass--;
                                sb.append(']');
                                break;
                            case '.':
                            case '(':
                            case ')':
                            case '+':
                            case '|':
                            case '^':
                            case '$':
                            case '@':
                            case '%':
                                if (inClass == 0 || (firstIndexInClass == i && ch == '^')) {
                                    sb.append('\\');
                                }
                                sb.append(ch);
                                break;
                            case '!':
                                sb.append(firstIndexInClass == i ? '^' : '!');
                                break;
                            case '{':
                                inGroup++;
                                sb.append('(');
                                break;
                            case '}':
                                inGroup--;
                                sb.append(')');
                                break;
                            case ',':
                                sb.append(inGroup > 0 ? '|' : ',');
                                break;
                            default:
                                sb.append(ch);
                        }
                    }
                    break;
            }
        }
        return sb.toString();
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
    private void checkURI(URI uri) {
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
    private void checkURI(URI uri, boolean needsPath) {
        requireNonNull(uri, "URI cannot be null.");

        var scheme = uri.getScheme();
        requireNonNull(scheme, "Scheme not found in URI.");
        if (!getScheme().equals(scheme)) {
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
