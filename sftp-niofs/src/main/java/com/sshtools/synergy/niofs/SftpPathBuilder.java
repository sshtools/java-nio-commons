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

import static com.sshtools.common.util.Utils.encodeUserInfo;

import java.net.URI;
import java.util.Optional;

import com.sshtools.common.util.Utils;

public  final class SftpPathBuilder {

	private Optional<String> username = Optional.empty();
	private Optional<char[]> password = Optional.empty();
	private Optional<String> host = Optional.empty();
	private Optional<Integer> port = Optional.empty();
	private Optional<String> path = Optional.empty();
	
	public static SftpPathBuilder create() {
		return new SftpPathBuilder();
	}
	
	private SftpPathBuilder() {
	}

	public final SftpPathBuilder withUsername(String username) {
		return withUsername(Utils.emptyOptionalIfBlank(username));
	}

	public final SftpPathBuilder withUsername(Optional<String> username) {
		this.username = username;
		return this;
	}

	public final SftpPathBuilder withPassword(String password) {
		return withPasswordCharacters(password.toCharArray());
	}

	public final SftpPathBuilder withPassword(Optional<String> password) {
		return withPasswordCharacters(password.map(p -> p.toCharArray()));
	}

	public final SftpPathBuilder withPasswordCharacters(char[] password) {
		return withPasswordCharacters(Utils.emptyOptionalIfBlank(password));
	}

	public final SftpPathBuilder withPasswordCharacters(Optional<char[]> password) {
		this.password = password;
		return this;
	}

	public final SftpPathBuilder withPath(String path) {
		this.path = Optional.ofNullable(path);
		return this;
	}

	public final SftpPathBuilder withHost(String host) {
		this.host = Optional.of(host);
		return this;
	}

	public final SftpPathBuilder withPort(int port) {
		this.port = Optional.of(port);
		return this;
	}

	public URI build() {
		var uriStr = new StringBuilder("sftp://");
		uriStr.append(encodeUserInfo( username.orElse("guest")));
		password.ifPresent(p -> {
			uriStr.append(":");
			uriStr.append(encodeUserInfo( new String(p)));
		});
		uriStr.append("@");
		uriStr.append(host.orElse("localhost"));
		port.ifPresent(p -> {
			if(p != 22) {
				uriStr.append(":");
				uriStr.append(p);
			}
		});
		uriStr.append("/" + path.orElse(""));
		return URI.create(uriStr.toString());

	}
}
