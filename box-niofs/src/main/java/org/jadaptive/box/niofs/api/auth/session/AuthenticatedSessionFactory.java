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
package org.jadaptive.box.niofs.api.auth.session;

import org.jadaptive.box.niofs.api.auth.session.strategy.DeveloperTokenAuthenticatedSession;
import org.jadaptive.box.niofs.api.auth.session.type.AuthenticatedSessionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public class AuthenticatedSessionFactory {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticatedSessionFactory.class);

	public static final String DEVELOPER_TOKEN = "DEVELOPER_TOKEN";
	private AuthenticatedSessionFactory() {}

	public static AuthenticatedSession get(AuthenticatedSessionType type, Map<String, String> properties) {

		requireNonNull(properties, "Property map cannot be null.");

		logger.info("Processing Box Session type off {}", type);

		switch (type) {
			case TOKEN: {
				var developerToken = properties.get(DEVELOPER_TOKEN);
				if (developerToken == null || developerToken.isBlank()) {
					throw new IllegalArgumentException("No developer token found.");
				}
				return new DeveloperTokenAuthenticatedSession(developerToken);
			}
			case JWT:
            case OAuth2:
                throw new UnsupportedOperationException();
        }
		
		throw new IllegalStateException();
	}
}
