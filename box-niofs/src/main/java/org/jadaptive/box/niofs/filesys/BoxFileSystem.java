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
package org.jadaptive.box.niofs.filesys;

import org.jadaptive.box.niofs.api.BoxRemoteAPI;
import org.jadaptive.box.niofs.filesysprovider.BoxFileSystemProvider;
import org.jadaptive.box.niofs.path.BoxPathService;
import org.jadaptive.niofs.filesys.BaseFileSystem;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class BoxFileSystem extends BaseFileSystem {
	
	private static final String SEPARATOR = "/";
	private final BoxFileSystemProvider boxFileSystemProvider;
	private final BoxRemoteAPI boxRemoteAPI;

	
	public BoxFileSystem(BoxFileSystemProvider boxFileSystemProvider, BoxPathService boxPathService,
						 BoxRemoteAPI boxRemoteAPI) {
		super(boxPathService);
		this.boxFileSystemProvider = boxFileSystemProvider;
		this.boxRemoteAPI = boxRemoteAPI;
	}

	@Override
	public BoxFileSystemProvider provider() {
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
		return Set.of(getPathService().createRoot());
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
		return getPathService().getPath(first, more);
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
		if (path.startsWith(getPathService().getRootName())) {
			path = path.substring(1);
		}

		// a blank path value when split on separator would end up in collection as the only value
		// which is not desired, we need to return empty collection
		// special case for "/", above starts with check will convert "/" => "" i,e, an empty string
		if (path.isBlank()) {
			return Collections.emptyList();
		}

		return Arrays.stream(path.split(SEPARATOR)).collect(Collectors.toList());
	}

	@Override
	public BoxPathService getPathService() {
		return (BoxPathService) this.basePathService;
	}
}
