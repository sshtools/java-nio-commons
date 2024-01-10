package org.jadaptive.box.niofs.path;

import org.jadaptive.box.niofs.filesys.BoxFileSystem;
import org.jadaptive.niofs.path.BasePath;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collections;
import java.util.List;

public class BoxPath extends BasePath {

	private static final String ROOT_NAME = "/";

	private final BoxFileSystem boxFileSystem;
	
	public BoxPath(BoxFileSystem boxFileSystem, String root, List<String> names) {
		super(root, names);
		this.boxFileSystem = boxFileSystem;
	}

	@Override
	public FileSystem getFileSystem() {
		return this.boxFileSystem;
	}

	@Override
	public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	protected String getRootName() {
		return ROOT_NAME;
	}

	@Override
	protected BoxPath createRoot() {
		return new BoxPath(this.boxFileSystem, getRootName(), Collections.emptyList());
	}

	@Override
	protected BasePath createPath(String root, List<String> names) {
		return new BoxPath(this.boxFileSystem, root, names);
	}

	@Override
	protected BoxPath createPathFromIndex(int beginIndex, int endIndex) {
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

		return new BoxPath(this.boxFileSystem, null, subName);
	}

	@Override
	protected boolean isSameInstance(Object path) {
		return path instanceof BoxPath;
	}
}
