package org.jadaptive.box.niofs.filesysprovider;

import org.jadaptive.box.niofs.auth.api.client.locator.BoxConnectionAPILocator;
import org.jadaptive.box.niofs.filesys.BoxFileSystem;
import org.jadaptive.box.niofs.path.BoxPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;

import static java.util.Objects.*;
import static java.lang.String.*;

public class BoxFileSystemProvider extends FileSystemProvider {

	private static final Logger logger = LoggerFactory.getLogger(BoxFileSystemProvider.class);

	private volatile BoxFileSystem boxFileSystem;

	@Override
	public String getScheme() {
		return "box";
	}

	@Override
	public BoxFileSystem newFileSystem(URI uri, Map<String, ?> env) {
		checkURI(uri);
		// TODO check uri passed is at root i.e. '/', we do not support any other
		initBoxFileSystem();
		return boxFileSystem;
	}

	@Override
	public BoxFileSystem getFileSystem(URI uri) {
		// TODO check uri is root '/' else throw error
		return newFileSystem(uri, Collections.emptyMap());
	}

	@Override
	public BoxPath getPath(URI uri) {
		checkURI(uri, true);

		if (boxFileSystem == null) {
			initBoxFileSystem();
		}

		var path = extractPathFromURI(uri);
		var names = boxFileSystem.getNamesForPath(path);

		// separator == root , need better way to communicate this
		return new BoxPath(boxFileSystem, boxFileSystem.getSeparator(), names);
	}

	@Override
	public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(Path path) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void copy(Path source, Path target, CopyOption... options) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void move(Path source, Path target, CopyOption... options) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isSameFile(Path path, Path path2) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isHidden(Path path) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public FileStore getFileStore(Path path) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void checkAccess(Path path, AccessMode... modes) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
		// TODO Auto-generated method stub
		
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

	private String extractPathFromURI(URI uri) {
		requireNonNull(uri, "URI cannot be null");
		var path = uri.getPath();
		requireNonNull(path, "Path of URI cannot be null");
		return path;
	}

	private void initBoxFileSystem() {
		if (boxFileSystem == null) {
			synchronized (BoxFileSystemProvider.class) {
				if (boxFileSystem == null) {
					logger.info("File system does not exists, creating new.");
					var boxAPIClient = BoxConnectionAPILocator.getBoxAPIClient();
					boxFileSystem = new BoxFileSystem(this, boxAPIClient);
				}
			}
		}
	}
}
