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
package org.jadaptive.box.niofs.api;

import org.jadaptive.box.niofs.api.auth.session.strategy.Oauth2AuthenticatedSession;

public class Oauth2TokenRemoteAPI extends BaseBoxRemoteAPI {
    public Oauth2TokenRemoteAPI(String clientId, String clientSecret, String authCode) {
        super(new Oauth2AuthenticatedSession(clientId, clientSecret,authCode ));
    }

    public String getRefreshToken() {
        return ((Oauth2AuthenticatedSession) authenticatedSession).getRefreshToken();
    }

    public String getAccessToken() {
        return ((Oauth2AuthenticatedSession) authenticatedSession).getAccessToken();
    }
}
