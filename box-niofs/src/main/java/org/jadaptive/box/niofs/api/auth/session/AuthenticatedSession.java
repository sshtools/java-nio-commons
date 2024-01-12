package org.jadaptive.box.niofs.api.auth.session;

import com.box.sdk.BoxAPIConnection;

@FunctionalInterface
public interface AuthenticatedSession {

	BoxAPIConnection getBoxAPIConnection();
}
