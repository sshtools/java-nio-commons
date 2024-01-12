package org.jadaptive.box.niofs.filesysprovider;

import org.jadaptive.box.niofs.api.client.locator.BoxConnectionAPILocator;
import org.jadaptive.box.niofs.filesys.BoxFileSystem;
import org.jadaptive.box.niofs.path.BoxPath;
import org.jadaptive.box.niofs.path.BoxPathService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class BoxFileSystemProvider extends FileSystemProvider {

	private static final Logger logger = LoggerFactory.getLogger(BoxFileSystemProvider.class);

	private volatile BoxFileSystem boxFileSystem;

	@Override
	public String getScheme() {
		return "box";
	}

	@Override
	public BoxFileSystem newFileSystem(URI uri, Map<String, ?> env) {
		// TODO we support such construction
		//checkURI(uri);
		// TODO check uri passed is at root i.e. '/', we do not support any other
		initBoxFileSystem();
		return boxFileSystem;
	}

	@Override
	public BoxFileSystem getFileSystem(URI uri) {
		// TODO we support such construction
		// TODO check uri is root '/' else throw error
		return newFileSystem(uri, Collections.emptyMap());
	}

	@Override
	public BoxPath getPath(URI uri) {
		if (boxFileSystem == null) {
			initBoxFileSystem();
		}
		return boxFileSystem.getPathService().getPath(uri);
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

	private void initBoxFileSystem() {
		if (boxFileSystem == null) {
			synchronized (BoxFileSystemProvider.class) {
				if (boxFileSystem == null) {
					logger.info("File system does not exists, creating new.");
					var boxRemoteAPI = BoxConnectionAPILocator.getBoxRemoteAPI();
					var pathService = new BoxPathService();
					boxFileSystem = new BoxFileSystem(this, pathService, boxRemoteAPI);
					pathService.setFileSystem(boxFileSystem);
				}
			}
		}
	}
}
