package org.jadaptive.niofs.path;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.util.*;

import static java.util.Objects.requireNonNull;

public abstract class BasePath implements Path {

    protected final List<String> names;
    protected final String root;

    /**
     * Constructs a BasePath.
     *
     * The complete as we recognize in real world is
     *   root + names joined by path separator
     *  <br />
     *  <br />
     *  Given root as '/' and names as ["usr", "share", "mysql"] and separator as "/"
     *  <br/>
     *  The path is /usr/share/mysql
     *  <br />
     *  <br />
     *  Given root as 'C:/' and names as ["usr", "share", "mysql"] and separator as "/"
     *  <br/>
     *  The path is C:/usr/share/mysql
     *  <br />
     *  <br />
     *  Given root as null and names as ["usr", "share", "mysql"] and separator as "/"
     *  <br/>
     *  The path is usr/share/mysql
     *  <br />
     *  <br />
     *
     * @param root the root component of a filesystem
     * @param names that make up the complete path
     *
     */
    protected BasePath(String root, List<String> names) {
        requireNonNull(names, "Path names list cannot be null.");
        this.root = root;
        this.names = names;
    }

    protected abstract String getRootName();
    protected abstract BasePath createRoot();
    protected abstract BasePath createPathFromIndex(int beginIndex, int endIndex);
    protected abstract BasePath createPath(String root, List<String> names);
    protected BasePath createEmptyPath() {
        return createPath(null, List.of(""));
    }
    protected abstract boolean isSameInstance(Object path);

    protected boolean isNormal() {
        int count = getNameCount();
        if ((count == 0) || ((count == 1) && !isAbsolute())) {
            return true;
        }
        boolean foundNonParentName = isAbsolute(); // if there's a root, the path doesn't start with ..
        boolean normal = true;
        for (String name : names) {
            if (name.equals("..")) {
                if (foundNonParentName) {
                    normal = false;
                    break;
                }
            } else {
                if (name.equals(".")) {
                    normal = false;
                    break;
                }
                foundNonParentName = true;
            }
        }
        return normal;
    }

    public List<String> getNames() {
        return new ArrayList<>(names);
    }

    @Override
    public boolean isAbsolute() {
        // if starts with root name
        // if instance has root component
        return root != null;
    }

    @Override
    public BasePath getRoot() {

        if (root == null) {
            return null;
        }

        return createRoot();
    }

    @Override
    public BasePath getFileName() {
        // if no name return null
        // if single name and no root involved return this
        // else compute last name
        // /file.txt
        return names.isEmpty() ? null : getName(names.size() - 1);
    }

    @Override
    public BasePath getParent() {
        // if no name return null no parent
        // if only parent is root return it which may be null
        // else move back one step
        if (names.isEmpty() || (names.size() == 1 && root == null)) {
            return null;
        }

        return createPath(root, names.subList(0, names.size() - 1));

    }

    @Override
    public int getNameCount() {
        return names.size();
    }

    @Override
    public BasePath getName(int index) {
        if (index < 0 || index > names.size()) {
            throw new IllegalArgumentException("Index is out of bounds.");
        }
        return createPath(null, Collections.singletonList(names.get(index)));
    }

    @Override
    public BasePath subpath(int beginIndex, int endIndex) {
        return createPathFromIndex(beginIndex, endIndex);
    }

    @Override
    public boolean startsWith(Path other) {
        // idea is extract paths and to equals on list
        // this list sublist to size of the other
        // check both instance are same
        // check both have same filesystem

        if (checkCanBeCompared(other)) {
            var otherNames = ((BasePath) other).getNames();
            return startsWith(this.names, otherNames);
        }

        return false;
    }

    @Override
    public boolean endsWith(Path other) {
        // same as starts with just reverse the list
        if (checkCanBeCompared(other)) {
            var reversedOthers = reverseCollection(((BasePath) other).getNames());
            var reversedNames = reverseCollection(this.names);

            return startsWith(reversedNames, reversedOthers);
        }
        return false;
    }

    @Override
    public URI toUri() {
        var uri =  String.format("%s://%s", getFileSystem().provider().getScheme(), toAbsolutePath());
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Could not convert to URI.", e);
        }
    }

    @Override
    public Path toRealPath(LinkOption... options) {
        return toAbsolutePath();
    }

    @Override
    public Path toAbsolutePath() {
        if (isAbsolute()) {
            return this;
        }
        return createPath(getRootName(), this.names);
    }

    @Override
    public Path resolve(Path other) {
        if (!checkCanBeCompared(other)) {
            throw new ProviderMismatchException(other.toString());
        }

        if (isEmptyPath() || other.isAbsolute()) {
            return other;
        }

        if (((BasePath) other).isEmptyPath()) {
            return this;
        }

        var otherNames = ((BasePath) other).getNames();
        var copyNames = getNames();
        copyNames.addAll(otherNames);

        return createPath(root, copyNames);
    }

    /**
     * Basically idea is move up to root location of 'this' path and append the given path.
     *
     * <p>
     *  /c/d
     *  <br />
     * 	/a/b/c/d
     *  <br />
     * 	../../a/b/c/d
     * </p>
     * <br />
     * <p>
     *  /a/b/c/d
     *  <br />
     *  /c/d
     *  <br />
     *  ../../../../c/d
     * </p>
     *
     * @param other
     *          the path to relativize against this path
     *
     * @return
     */
    @Override
    public Path relativize(Path other) {

        if (!checkCanBeCompared(other)) {
            throw new ProviderMismatchException(other.toString());
        }

        if (!Objects.equals(getRoot(), other.getRoot())) {
            throw new IllegalArgumentException("Paths have different roots: " + this + ", " + other);
        }

        if (other.equals(this)) {
            return createEmptyPath();
        }

        if (isEmptyPath()) {
            return other;
        }

        BasePath otherBasePath = (BasePath) other;
        // Common subsequence
        int sharedSubsequenceLength = 0;
        for (int i = 0; i < Math.min(names.size(), other.getNameCount()); i++) {
            if (names.get(i).equals(other.getName(i))) {
                sharedSubsequenceLength++;
            } else {
                break;
            }
        }
        int extraNamesInThis = Math.max(0, names.size() - sharedSubsequenceLength);
        List<String> extraNamesInOther = (other.getNameCount() <= sharedSubsequenceLength)
                ? Collections.<String>emptyList()
                : otherBasePath.getNames().subList(sharedSubsequenceLength, other.getNameCount());
        List<String> parts = new ArrayList<>(extraNamesInThis + extraNamesInOther.size());
        // add .. for each extra name in this path
        parts.addAll(Collections.nCopies(extraNamesInThis, ".."));
        // add each extra name in the other path
        parts.addAll(extraNamesInOther);

        return createPath(null, parts);
    }

    @Override
    public Path normalize() {
        if (isNormal()) {
            return this;
        }

        Deque<String> newNames = new ArrayDeque<>();
        for (String name : names) {
            if (name.equals("..")) {
                String lastName = newNames.peekLast();
                if (lastName != null && !lastName.equals("..")) {
                    newNames.removeLast();
                } else if (!isAbsolute()) {
                    // if there's a root and we have an extra ".." that would go up above the root, ignore it
                    newNames.add(name);
                }
            } else if (!name.equals(".")) {
                newNames.add(name);
            }
        }

        return createPath(root, new ArrayList<>(newNames));
    }

    @Override
    public int compareTo(Path other) {
        if (!isSameInstance(other)) {
            throw new IllegalArgumentException("Cannot compare different instances of Path.");
        }
        var thisPath = toString();
        var otherPath = other.toString();
        return thisPath.compareTo(otherPath);
    }

    @Override
    public File toFile() {
        throw new UnsupportedOperationException("To file " + toAbsolutePath() + " N/A");
    }

    @Override
    public boolean equals(Object obj) {
        return isSameInstance(obj) && compareTo((BasePath) obj) == 0;
    }

    @Override
    public String toString() {
        var rootName = getRootName();
        // if is root return '/' unix style
        if (root != null && names.isEmpty()) {
            return rootName;
        }

        var path = String.join(getFileSystem().getSeparator(), this.names);

        return root != null
                ? root + path
                : path;
    }

    public boolean isEmptyPath() {
        return root == null && names.size() == 1 && names.get(0).isEmpty();
    }

    private boolean startsWith(List<?> list, List<?> other) {
        return list.size() >= other.size() && list.subList(0, other.size()).equals(other);
    }

    private boolean checkCanBeCompared(Path other) {
        return isSameInstance(other)
                && other.getFileSystem().equals(getFileSystem());
    }

    private <T> List<T> reverseCollection(List<T> otherNames) {
        var copy = new ArrayList<>(otherNames);
        Collections.reverse(copy);
        return copy;
    }
}
