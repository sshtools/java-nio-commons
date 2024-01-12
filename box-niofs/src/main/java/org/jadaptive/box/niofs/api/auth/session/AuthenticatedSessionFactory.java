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
