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
package org.jadaptive.api.user;

import java.util.Date;

public class FileSysUserInfo {

    private final String id;

    private final String name;

    private final String login;

    private final long spaceAmount;

    private final long spaceUsed;

    private final Date createdAt;

    public FileSysUserInfo(String id, String name, String login,
                           long spaceAmount, long spaceUsed, Date createdAt) {
        this.id = id;
        this.name = name;
        this.login = login;
        this.spaceAmount = spaceAmount;
        this.spaceUsed = spaceUsed;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLogin() {
        return login;
    }

    public long getSpaceAmount() {
        return spaceAmount;
    }

    public long getSpaceUsed() {
        return spaceUsed;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "BoxUserInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", login='" + login + '\'' +
                ", spaceAmount=" + spaceAmount +
                ", spaceUsed=" + spaceUsed +
                ", createdAt=" + createdAt +
                '}';
    }
}
