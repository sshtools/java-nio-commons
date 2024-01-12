package org.jadaptive.box.niofs.path;

import org.jadaptive.box.niofs.filesys.BoxFileSystem;
import org.jadaptive.niofs.path.BasePath;

import java.io.IOException;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;

public class BoxPath extends BasePath {

	public BoxPath(BoxPathService boxPathService, String root, List<String> names) {
		super(boxPathService, root, names);
	}

	@Override
	public BoxFileSystem getFileSystem() {
		return getBasePathService().getFileSystem();
	}

	@Override
	public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	protected boolean isSameInstance(Object path) {
		return path instanceof BoxPath;
	}

	@Override
	protected BoxPathService getBasePathService() {
		return (BoxPathService) super.getBasePathService();
	}
}
