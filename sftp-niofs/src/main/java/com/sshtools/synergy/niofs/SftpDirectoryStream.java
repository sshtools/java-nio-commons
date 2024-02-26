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
package com.sshtools.synergy.niofs;

import static com.sshtools.synergy.niofs.SftpFileSystem.toAbsolutePathString;
import static com.sshtools.synergy.niofs.SftpFileSystemProvider.translateException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.ClosedDirectoryStreamException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class SftpDirectoryStream implements DirectoryStream<Path> {
	private final DirectoryStream.Filter<? super Path> filter;
	private volatile Iterator<Path> iterator;
	private volatile boolean open = true;
	private final Path path;

	SftpDirectoryStream(SftpPath sftpPath, DirectoryStream.Filter<? super Path> filter) throws IOException {
		this.path = sftpPath.normalize();
		this.filter = filter;
		if (Files.exists(path)) {
			if (!Files.isDirectory(path)) {
				throw new NotDirectoryException(sftpPath.toString());
			}
		} else
			throw new NoSuchFileException(sftpPath.toString());
	}

	@Override
	public synchronized void close() throws IOException {
		open = false;
	}

	@Override
	public synchronized Iterator<Path> iterator() {
		if (!open)
			throw new ClosedDirectoryStreamException();
		if (iterator != null)
			throw new IllegalStateException();
		try {
			var sftpPath = (SftpPath) path;
			try {
				var fs = sftpPath.getFileSystem();
				var pstr = toAbsolutePathString(path);
				var sftp = fs.getSftp();
				var it = sftp.lsIterator(pstr);

				iterator = new Iterator<>() {

					Path next = null;

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}

					@Override
					public boolean hasNext() {
						if (!open)
							return false;
						checkNext();
						return next != null;
					}

					@Override
					public Path next() {
						if (!open)
							throw new NoSuchElementException();
						try {
							checkNext();
							if (next == null) {
								throw new NoSuchElementException();
							}
							return next;
						} finally {
							next = null;
						}
					}

					private void checkNext() {
						if (next == null) {
							while (true) {
								var hasNext = it.hasNext();
								if (hasNext) {
									var nextFile = it.next();
									/* TODO: check this will never actual happen */
									/*if (nextFile.getFilename().equals(".") || nextFile.getFilename().equals(".."))
										continue; */
									var p = path.resolve(nextFile.getFilename());
									try {
										if (filter == null || filter.accept(p)) {
											next = p;
											return;
										}
									} catch (IOException ioe) {
										throw new UncheckedIOException(ioe);
									}
								} else
									return;
							}
						}
					}

				};
			} catch (Exception e) {
				throw translateException(e);
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return iterator;
	}

}
