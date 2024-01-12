package org.jadaptive.box.niofs.auth.api.client.locator;

import org.jadaptive.box.niofs.auth.api.client.BoxAPIClient;

import java.util.Objects;

public class BoxConnectionAPILocator {

    private static final BoxConnectionAPILocator INSTANCE = new BoxConnectionAPILocator();

    private BoxAPIClient boxAPIClient;

    private BoxConnectionAPILocator() {}

    public static BoxAPIClient getBoxAPIClient() {
        Objects.requireNonNull(INSTANCE.boxAPIClient, "Box Api Client is not set.");
        return INSTANCE.boxAPIClient;
    }

    public static void setBoxAPIClient(BoxAPIClient boxAPIClient) {
        INSTANCE.boxAPIClient = boxAPIClient;
    }
}
