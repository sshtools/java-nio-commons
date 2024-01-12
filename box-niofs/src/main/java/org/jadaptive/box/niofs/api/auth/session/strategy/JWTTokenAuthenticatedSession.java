package org.jadaptive.box.niofs.api.auth.session.strategy;

import com.box.sdk.BoxAPIConnection;
import org.jadaptive.box.niofs.api.auth.session.AbstractAuthenticatedSession;

public class JWTTokenAuthenticatedSession extends AbstractAuthenticatedSession {

	@Override
	protected void setUpBoxAPIConnection(BoxAPIConnection boxAPIConnection) {
		throw new UnsupportedOperationException();
	}

}
