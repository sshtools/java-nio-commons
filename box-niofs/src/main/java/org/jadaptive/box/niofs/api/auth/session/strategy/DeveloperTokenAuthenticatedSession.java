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
