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
package org.jadaptive.box.niofs.api;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BoxRemoteAPITest {

    private static String token;

    @BeforeAll
    static void init() throws IOException {
        token = Files.readString(Path.of("src/test/resources/config/dev_token"));
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalStateException("Box Developer Token is null or empty.");
        }
    }

    @Test
    @DisplayName("it should return user info.")
    void testUserInfo() {
        var api = new DeveloperTokenRemoteAPI(token);

        var boxUserInfo = api.getBoxUserInfo();

        assertNotNull(boxUserInfo);
        assertNotNull(boxUserInfo.getId());
        assertNotNull(boxUserInfo.getName());
        assertNotNull(boxUserInfo.getLogin());
        assertNotNull(boxUserInfo.getCreatedAt());

    }

}
