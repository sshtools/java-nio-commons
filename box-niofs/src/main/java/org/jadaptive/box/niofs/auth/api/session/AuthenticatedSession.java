package org.jadaptive.box.niofs.auth.api.session;

import com.box.sdk.BoxAPIConnection;

@FunctionalInterface
public interface AuthenticatedSession {

	BoxAPIConnection getBoxAPIConnection();
}
