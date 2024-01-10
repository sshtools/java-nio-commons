package org.jadaptive.box.niofs.filesys;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jadaptive.box.niofs.filesysprovider.BoxFileSystemProvider;
import org.jadaptive.box.niofs.auth.api.client.BoxAPIClient;
import org.jadaptive.box.niofs.path.BoxPath;

import static java.util.Objects.*;

public class BoxFileSystem extends FileSystem {
	
	private static final String SEPARATOR = "/";
	
	private final BoxFileSystemProvider boxFileSystemProvider;
	private final BoxAPIClient boxAPIClient;
	
	public BoxFileSystem(BoxFileSystemProvider boxFileSystemProvider, BoxAPIClient boxAPIClient) {
		this.boxFileSystemProvider = boxFileSystemProvider;
		this.boxAPIClient = boxAPIClient;
	}

	@Override
	public FileSystemProvider provider() {
		return this.boxFileSystemProvider;
	}

	@Override
	public void close() throws IOException {
		
	}

	@Override
	public boolean isOpen() {
		return false;
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public String getSeparator() {
		return SEPARATOR;
	}

	@Override
	public Iterable<Path> getRootDirectories() {
		return Set.of(new BoxPath(this, null, List.of("/")));
	}

	@Override
	public Iterable<FileStore> getFileStores() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> supportedFileAttributeViews() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Path getPath(String first, String... more) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PathMatcher getPathMatcher(String syntaxAndPattern) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserPrincipalLookupService getUserPrincipalLookupService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WatchService newWatchService() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getNamesForPath(String path) {
		requireNonNull(path, "Path cannot be null");
		// if we don't remove first separator split will return first value as empty string ""
		// /get_started.pdf => "", "get_started.pdf"
		if (path.startsWith(SEPARATOR)) {
			path = path.substring(1);
		}
		return Arrays.stream(path.split(SEPARATOR)).collect(Collectors.toList());
	}
 }
