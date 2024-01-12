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

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

public abstract class AbstractAuthenticatedSession implements AuthenticatedSession {

    private static final Logger logger = LoggerFactory.getLogger(AbstractAuthenticatedSession.class);

    protected String name;
    protected String login;
    protected String id;
    protected BoxAPIConnection boxAPIConnection;

    public String getName() {
        return name;
    }

    public String getLogin() {
        return login;
    }

    public String getId() {
        return id;
    }

    protected void setUpUserInfo() {

        requireNonNull(this.boxAPIConnection, "Seems Box API Connection is not created yet, is null.");

        logger.info("Setting up user information from box.");

        BoxUser.Info userInfo = BoxUser.getCurrentUser(this.boxAPIConnection).getInfo();

        this.name = userInfo.getName();
        this.login = userInfo.getLogin();
        this.id = userInfo.getID();

        logger.info("Box API Connection setup for user with name {}, login {} and id as {}.",
                    this.name, this.login, this.id);
    }

    public BoxAPIConnection getBoxAPIConnection() {
        requireNonNull(this.boxAPIConnection, "Seems Box API Connection is not created yet, is null.");
        return this.boxAPIConnection;
    }

    protected void defaultSetUpBoxAPIConnection(BoxAPIConnection boxAPIConnection) {
        requireNonNull(boxAPIConnection, "Passed Box API Connection is null.");
        this.boxAPIConnection = boxAPIConnection;
        this.setUpUserInfo();
    }

    protected abstract void setUpBoxAPIConnection(BoxAPIConnection boxAPIConnection);
}
