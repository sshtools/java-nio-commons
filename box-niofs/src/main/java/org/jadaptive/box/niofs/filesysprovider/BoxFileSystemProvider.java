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

import org.jadaptive.box.niofs.api.client.locator.BoxConnectionAPILocator;
import org.jadaptive.box.niofs.attr.BoxNioFileAttributeView;
import org.jadaptive.box.niofs.filestore.BoxFileStore;
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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BoxFileSystemProvider extends FileSystemProvider {

	private static final Logger logger = LoggerFactory.getLogger(BoxFileSystemProvider.class);

	private volatile BoxFileSystem boxFileSystem;

	private final Lock lock = new ReentrantLock();

	@Override
	public String getScheme() {
		return "box";
	}

	@Override
	public BoxFileSystem newFileSystem(URI uri, Map<String, ?> env) {
		throw new UnsupportedOperationException();
	}

	@Override
	public BoxFileSystem getFileSystem(URI uri) {
		throw new UnsupportedOperationException();
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
		checkPath(path);
		return boxFileSystem.getBoxRemoteAPI().newByteChannel((BoxPath) path, options, attrs);
	}

	@Override
	public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) {
		checkPath(dir);
		return boxFileSystem.getBoxRemoteAPI().newDirectoryStream((BoxPath) dir, filter);
	}

	@Override
	public void createDirectory(Path path, FileAttribute<?>... attrs) {
		checkPath(path);
		boxFileSystem.getBoxRemoteAPI().createDirectory((BoxPath) path, attrs);
	}

	@Override
	public void delete(Path path) {
		checkPath(path);
		boxFileSystem.getBoxRemoteAPI().delete((BoxPath) path);
	}

	@Override
	public void copy(Path source, Path target, CopyOption... options) throws IOException {
		checkPath(source);
		checkPath(target);
		boxFileSystem.getBoxRemoteAPI().copy((BoxPath) source, (BoxPath) target, options);
		
	}

	@Override
	public void move(Path source, Path target, CopyOption... options) {
		checkPath(source);
		checkPath(target);
		boxFileSystem.getBoxRemoteAPI().move((BoxPath) source, (BoxPath) target, options);
	}

	@Override
	public boolean isSameFile(Path path1, Path path2) {
		if(path1 instanceof BoxPath && path2 instanceof BoxPath && Files.exists(path1) && Files.exists(path2)) {
			var full1 = path1.normalize().toAbsolutePath();
			var full2 = path2.normalize().toAbsolutePath();
			return full1.equals(full2);
		} else
			return false;
	}

	@Override
	public boolean isHidden(Path path) {
		throw new UnsupportedOperationException();
	}

	@Override
	public FileStore getFileStore(Path path) {
		checkPath(path);
		return ((BoxFileSystem) path.getFileSystem()).getBoxFileStore();
	}

	@Override
	public void checkAccess(Path path, AccessMode... modes) {
		checkPath(path);
		boxFileSystem.getBoxRemoteAPI().getBoxFileInfo((BoxPath) path);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
		checkPath(path);
		return (V) new BoxNioFileAttributeView(boxFileSystem.getBoxRemoteAPI(), (BoxPath) path);
	}

	@Override
	public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) {
		checkPath(path);
		return boxFileSystem.getBoxRemoteAPI().readAttributes((BoxPath) path, options);
	}

	@Override
	public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) {
		checkPath(path);
		return boxFileSystem.getBoxRemoteAPI().readAttributes((BoxPath) path, attributes, options);
	}

	@Override
	public void setAttribute(Path path, String attribute, Object value, LinkOption... options) {
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

	private void checkPath(Path dir) {
		if (!isSameInstance(dir)) {
			throw new IllegalArgumentException("Path is not an instance of BoxPath.");
		}
	}
	private boolean isSameInstance(Path path) {
		return path instanceof BoxPath;
	}
}
