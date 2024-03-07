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

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import com.sshtools.client.SshClient;
import com.sshtools.client.sftp.SftpClient;
import com.sshtools.client.sftp.SftpClient.SftpClientBuilder;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.common.sftp.SftpStatusException;
import com.sshtools.common.ssh.SshException;
import com.sshtools.common.util.Utils;

/**
 * Convenience methods to create new SFTP {@link FileSystem} instances directly,
 * rather through resolution of {@link Path} through that standard libraries.
 * <p>
 * This allows you to easily do things such as re-use existing {@link SftpClient} instances
 * as more.
 *
 */
public class SftpFileSystems {
	
	private SftpFileSystems() {
	}

	/**
	 * Create a new file system given an existing {@link SshClient} and using
	 * the default directory (usually the users home directory) as the root 
	 * of the file system.
	 * 
	 * @param ssh ssh instance
	 * @return file system
	 * @throws IOException if file system cannot be created
	 */
	public static FileSystem newFileSystem(SshClient ssh) throws IOException {
		try {
			return newFileSystem(SftpClientBuilder.create().withClient(ssh).build());
		} catch (SshException | PermissionDeniedException e) {
			throw new IOException("Failed to create file system.", e);
		}
	}

	/**
	 * Create a new file system given an existing {@link SshClient} and using
	 * the default directory (usually the users home directory) as the root 
	 * of the file system.
	 * 
	 * @param ssh ssh instance
	 * @param path path
	 * @return file system
	 * @throws IOException if file system cannot be created
	 */
	public static FileSystem newFileSystem(SshClient ssh, Path path) throws IOException {
		try {
			return newFileSystem(SftpClientBuilder.create().withClient(ssh).build(), path);
		} catch (SshException | PermissionDeniedException e) {
			throw new IOException("Failed to create file system.", e);
		}
	}

	/**
	 * Create a new file system given an existing {@link SftpClient} and using
	 * the default directory (usually the users home directory) as the root 
	 * of the file system.
	 * 
	 * @param sftp sftp instance
	 * @return file system
	 * @throws IOException if file system cannot be created
	 */
	public static FileSystem newFileSystem(SftpClient sftp) throws IOException {
		try {
			return newFileSystem(sftp, Paths.get(sftp.pwd()));
		} catch (SftpStatusException | SshException e) {
			throw new IOException("Failed to create file system.", e);
		}
	}

	/**
	 * Create a new file system given an existing {@link SftpClient} and using
	 * a specified remote directory as the root of the file system. 
	 * 
	 * @param sftp sftp instance
	 * @param path path of remote root.
	 * @return file system
	 * @throws IOException if file system cannot be created
	 */
	public static FileSystem newFileSystem(SftpClient sftp, Path path) throws IOException {
		return newFileSystem(sftp, path.toString());
	}

	/**
	 * Create a new file system given an existing {@link SftpClient} and using
	 * a specified remote directory as the root of the file system. 
	 * 
	 * @param sftp sftp instance
	 * @param path path of remote root.
	 * @return file system
	 * @throws IOException if file system cannot be created
	 */
	public static FileSystem newFileSystem(SftpClient sftp, String path) throws IOException {
		var conx = sftp.getSubsystemChannel().getConnection();
		var uri = URI.create(String.format(
				"sftp://%s@%s%s%s", conx.getUsername(), 
					Utils.formatHostnameAndPort(
							conx.getRemoteIPAddress(), conx.getRemotePort()), path.equals("") ? "" : "/", path ));
		
		return new SftpFileSystemProvider().newFileSystem(uri, Map.of(SftpFileSystemProvider.SFTP_CLIENT, sftp));
	}

	/**
	 * Create a new file system given all configuration via the environment {@link Map}.
	 * 
	 * @param environment configuration of file system
	 * @return file system
	 * @throws IOException if file system cannot be created
	 */
	public static FileSystem newFileSystem(Map<String, ?> environment) throws IOException {
		return new SftpFileSystemProvider().newFileSystem(URI.create("sftp:////"), environment);
	}
}
