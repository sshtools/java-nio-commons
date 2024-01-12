package org.jadaptive.box.niofs.api.client.locator;

import org.jadaptive.box.niofs.api.BoxRemoteAPI;

import java.util.Objects;

public class BoxConnectionAPILocator {

    private static final BoxConnectionAPILocator INSTANCE = new BoxConnectionAPILocator();

    private BoxRemoteAPI boxRemoteAPI;

    private BoxConnectionAPILocator() {}

    public static BoxRemoteAPI getBoxRemoteAPI() {
        Objects.requireNonNull(INSTANCE.boxRemoteAPI, "Box Remote Api is not set.");
        return INSTANCE.boxRemoteAPI;
    }

    public static void setBoxRemoteAPI(BoxRemoteAPI boxRemoteAPI) {
        INSTANCE.boxRemoteAPI = boxRemoteAPI;
    }
}
