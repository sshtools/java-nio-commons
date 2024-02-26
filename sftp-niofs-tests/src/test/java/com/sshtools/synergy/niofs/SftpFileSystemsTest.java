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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.Files;
import java.util.Map;

import org.junit.Test;

import com.sshtools.client.SshClient.SshClientBuilder;
import com.sshtools.client.sftp.SftpClient.SftpClientBuilder;

public class SftpFileSystemsTest extends AbstractNioFsTest {

	@Test
	public void testNewSftpFileSystemFromExistingClient() throws Exception {
		try (var ssh = SshClientBuilder.create().
				withTarget("localhost", port).
				withUsername("test").
				withPassword("test")
				.build()) {
			try (var sftp = SftpClientBuilder.create().withClient(ssh).build()) {
				try(var fs = SftpFileSystems.newFileSystem(sftp)) {
					var path = fs.getPath("testFile");
					System.out.println("path: " + path + " abspath: " + path.toAbsolutePath().toString());
					Files.createFile(path);
				}
			}
		}
		assertTrue("testFile must exist", Files.exists(tmpDir.resolve("testFile")));
	}

	@Test(expected = IOException.class)
	public void testFailNewSftpFileSystemFromExistingClient() throws Exception {
		try (var ssh = SshClientBuilder.create().
				withTarget("localhost", port).
				withUsername("test").
				withPassword("test").
				build()) {
			var sftp = SftpClientBuilder.create().withClient(ssh).build();
			sftp.close();
			try(var fs = SftpFileSystems.newFileSystem(sftp)) {
				Files.createFile(fs.getPath("testFile"));
			}
		}
		assertTrue("testFile must exist", Files.exists(tmpDir.resolve("testFile")));
	}

	@Test(expected = ConnectException.class)
	public void testFailNewSftpFileSystemFromMap() throws Exception {
		try(var fs = SftpFileSystems.newFileSystem(Map.of(
				SftpFileSystemProvider.USERNAME, "test",
			    SftpFileSystemProvider.PASSWORD, "test",
			    SftpFileSystemProvider.HOSTNAME, "localhost",
			    SftpFileSystemProvider.PORT, 3333
				))) {
			Files.createFile(fs.getPath("testFile"));
		}
		assertTrue("testFile must exist", Files.exists(tmpDir.resolve("testFile")));
	}

	@Test(expected = Exception.class)
	public void testFailNewSftpFileSystemFromMapDefaultPort() throws Exception {
		try(var fs = SftpFileSystems.newFileSystem(Map.of(
				SftpFileSystemProvider.USERNAME, "XXXXXXXXXXXXXXXXXXXXXXXXXXX",
			    SftpFileSystemProvider.PASSWORD, "XXXXXXXXXXXXXXXXXXXXXXXXXXX",
			    SftpFileSystemProvider.HOSTNAME, "localhost",
			    SftpFileSystemProvider.PORT, 22
				))) {
			Files.createFile(fs.getPath("testFile"));
		}
		assertTrue("testFile must exist", Files.exists(tmpDir.resolve("testFile")));
	}

	@Test
	public void testNewSftpFileSystemFromMap() throws Exception {
		try(var fs = SftpFileSystems.newFileSystem(Map.of(
				SftpFileSystemProvider.USERNAME, "test",
			    SftpFileSystemProvider.PASSWORD, "test",
			    SftpFileSystemProvider.HOSTNAME, "localhost",
			    SftpFileSystemProvider.PORT, port
				))) {
			Files.createFile(fs.getPath("testFile"));
		}
		assertTrue("testFile must exist", Files.exists(tmpDir.resolve("testFile")));
	}
}
