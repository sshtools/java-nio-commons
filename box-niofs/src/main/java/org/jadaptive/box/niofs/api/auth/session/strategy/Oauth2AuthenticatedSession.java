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
package org.jadaptive.box.niofs.api.auth.session.strategy;

import com.box.sdk.BoxAPIConnection;
import org.jadaptive.box.niofs.api.auth.session.AbstractAuthenticatedSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

public class Oauth2AuthenticatedSession extends AbstractAuthenticatedSession {

	private static final Logger logger = LoggerFactory.getLogger(Oauth2AuthenticatedSession.class);

	public Oauth2AuthenticatedSession(String clientId, String clientSecret, String authCode) {
		requireNonNull(clientId,"Provided client id cannot be null.");
		requireNonNull(clientSecret,"Provided client secret cannot be null.");
		requireNonNull(authCode,"Provided auth code cannot be null.");

		setUpBoxAPIConnection(new BoxAPIConnection(clientId,
				clientSecret, authCode));
	}

	@Override
	protected void setUpBoxAPIConnection(BoxAPIConnection boxAPIConnection) {
		logger.info("Setting up Box API Connection with Oauth2 Token.");
		requireNonNull(boxAPIConnection, "Passed Box API Connection is null.");
		super.defaultSetUpBoxAPIConnection(boxAPIConnection);
	}

	public String getRefreshToken() {
		return getBoxAPIConnection().getRefreshToken();
	}

	public String getAccessToken() {
		return getBoxAPIConnection().getAccessToken();
	}

	public static AuthUrlInfo getAuthUrlInfo(String clientId, URI redirectUri, String...scopes) {

		requireNonNull(clientId,"Provided client id cannot be null.");
		requireNonNull(redirectUri,"Provided redirect URI cannot be null.");
		requireNonNull(scopes,"Provided scopes cannot be null.");

		var scopeList = Arrays.asList(scopes);
		var state = UUID.randomUUID().toString();

		var authUrl = BoxAPIConnection.getAuthorizationURL(clientId,
				redirectUri, state, scopeList);

		return new AuthUrlInfo(authUrl, state);
	}

	public static class AuthUrlInfo {
		private final URL authUrl;

		private final String state;

		private AuthUrlInfo(URL authUrl, String state) {
			this.authUrl = authUrl;
			this.state = state;
		}

		public URL getAuthUrl() {
			return authUrl;
		}

		public String getState() {
			return state;
		}
	}
}
