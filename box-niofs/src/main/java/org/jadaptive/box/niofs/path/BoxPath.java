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
import org.jadaptive.niofs.watcher.BaseWatchService;

import java.io.IOException;
import java.nio.file.ProviderMismatchException;
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
		if (!(watcher instanceof BaseWatchService))
			throw new ProviderMismatchException();

		return ((BaseWatchService)watcher).register(this, events, modifiers);
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
