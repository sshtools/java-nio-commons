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

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Test;

public class SftpPathBuilderTest extends AbstractNioFsTest {
	@Test
	public void testDefaultOnly() {
		var path = SftpPathBuilder.create().build();
		assertEquals("sftp://guest@localhost/", path.toString());
	}

	@Test
	public void testHostnameOnly() {
		var path = SftpPathBuilder.create().
				withHost("somehost").build();
		assertEquals("sftp://guest@somehost/", path.toString());
	}
	
	@Test
	public void testHostnameAndPort() {
		var path = SftpPathBuilder.create().
				withPort(12345).
				withHost("somehost").build();
		assertEquals("sftp://guest@somehost:12345/", path.toString());
	}
	
	@Test
	public void testHostnamePortAndPath() {
		var path = SftpPathBuilder.create().
				withPort(12345).
				withPath("/home/xxxxx").
				withHost("somehost").build();
		assertEquals("sftp://guest@somehost:12345//home/xxxxx", path.toString());
	}
	
	@Test
	public void testHostnameUsernamePortAndPath() {
		var path = SftpPathBuilder.create().
				withPort(12345).
				withUsername("joeb").
				withPath("/home/xxxxx").
				withHost("somehost").build();
		assertEquals("sftp://joeb@somehost:12345//home/xxxxx", path.toString());
	}
	
	@Test
	public void testHostnameUsernamePasswordPortAndPath() {
		var path = SftpPathBuilder.create().
				withPort(12345).
				withPassword("QWErty123!\":@#").
				withUsername("joeb").
				withPath("/home/xxxxx").
				withHost("somehost").build();
		assertEquals("sftp://joeb:QWErty123!%22:%40%23@somehost:12345//home/xxxxx", path.toString());
	}
	
	@Test
	public void testCharArrayPassword() {
		var path = SftpPathBuilder.create().
				withPasswordCharacters("QWErty123!\":@#".toCharArray()).
				build();
		assertEquals("sftp://guest:QWErty123!%22:%40%23@localhost/", path.toString());
	}
	
	@Test
	public void testOptioanlPassword() {
		var path = SftpPathBuilder.create().
				withPassword(Optional.of("Qwerty")).
				build();
		assertEquals("sftp://guest:Qwerty@localhost/", path.toString());
	}
}
