/*
 * Copyright (C) 2006-2007 Mindquarry GmbH, All Rights Reserved
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 */
package com.mindquarry.desktop.event.network;

import com.mindquarry.desktop.event.EventBase;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com"> Alexander
 *         Saar</a>
 */
public class ProxySettingsChangedEvent extends EventBase {
    private boolean enabled;

    private String url;
    private String pwd;
    private String login;

    public ProxySettingsChangedEvent(Object source, boolean enabled,
            String url, String pwd, String login) {
        super(source);
        this.enabled = enabled;
        this.url = url;
        this.pwd = pwd;
        this.login = login;
    }

    public boolean isProxyEnabled() {
        return enabled;
    }

    public String getUrl() {
        return url;
    }

    public String getPwd() {
        return pwd;
    }

    public String getLogin() {
        return login;
    }
}
