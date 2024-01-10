package org.jadaptive.box.niofs.auth.api.session.strategy;

import com.box.sdk.BoxAPIConnection;
import org.jadaptive.box.niofs.auth.api.session.AbstractAuthenticatedSession;

public class Oauth2AuthenticatedSession extends AbstractAuthenticatedSession {

	@Override
	protected void setUpBoxAPIConnection(BoxAPIConnection boxAPIConnection) {
		throw new UnsupportedOperationException();
	}

}
