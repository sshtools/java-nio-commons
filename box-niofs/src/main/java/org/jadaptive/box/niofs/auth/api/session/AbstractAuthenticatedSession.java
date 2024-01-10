package org.jadaptive.box.niofs.auth.api.session;

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
