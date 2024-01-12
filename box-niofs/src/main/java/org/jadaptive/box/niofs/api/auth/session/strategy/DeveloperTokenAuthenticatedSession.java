package org.jadaptive.box.niofs.api.auth.session.strategy;

import com.box.sdk.BoxAPIConnection;
import org.jadaptive.box.niofs.api.auth.session.AbstractAuthenticatedSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

public class DeveloperTokenAuthenticatedSession extends AbstractAuthenticatedSession {

	private static final Logger logger = LoggerFactory.getLogger(DeveloperTokenAuthenticatedSession.class);

	public DeveloperTokenAuthenticatedSession(String developerToken) {
		setUpBoxAPIConnection(new BoxAPIConnection(developerToken));
	}

	@Override
	protected void setUpBoxAPIConnection(BoxAPIConnection boxAPIConnection) {
		logger.info("Setting up Box API Connection with Developer Token.");
		requireNonNull(boxAPIConnection, "Passed Box API Connection is null.");
		super.defaultSetUpBoxAPIConnection(boxAPIConnection);
	}
}
