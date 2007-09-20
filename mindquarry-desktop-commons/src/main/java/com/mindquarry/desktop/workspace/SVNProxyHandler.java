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
package com.mindquarry.desktop.workspace;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ini4j.Ini;
import org.tigris.subversion.javahl.ClientException;
import org.tmatesoft.svn.core.javahl.SVNClientImpl;

import com.mindquarry.desktop.preferences.profile.Profile;

/**
 * Utility class for handling Subversions proxy settings.
 * 
 * @author <a href="mailto:saar@mindquarry.com">Alexander Saar</a>
 */
public class SVNProxyHandler {
    public static final String GROUP_PREFIX = "mindquarry";

    public static final String PROXY_HOST = "http-proxy-host";
    public static final String PROXY_PORT = "http-proxy-port";
    public static final String PROXY_USERNAME = "http-proxy-username";
    public static final String PROXY_PASSWORD = "http-proxy-password";

    private static Log log = LogFactory.getLog(SVNProxyHandler.class);

    private static Ini getParser() throws Exception {
        SVNClientImpl client = SVNClientImpl.newInstance();
        Reader reader = new FileReader(client.getConfigDirectory() + "/servers");
        return new Ini(reader);
    }

    public static void applyProxySettings(List<Profile> profiles,
            String proxyURL, String proxyLogin, String proxyPwd)
            throws Exception {
        removeProxySettings();

        Ini ini = getParser();
        Ini.Section groups = ini.get("groups");

        int number = 0;
        for (Profile profile : profiles) {
            URI uri = new URI(profile.getServerURL());
            
            String groupID = GROUP_PREFIX + number++;
            groups.put(groupID, uri.getHost());

            uri = new URI(proxyURL);

            Ini.Section section = ini.add(groupID);
            section.put(PROXY_HOST, uri.getHost());
            if (uri.getPort() != -1) {
                section.put(PROXY_PORT, String.valueOf(uri.getPort()));
            }
            if (!proxyLogin.equals("")) {
                section.put(PROXY_USERNAME, proxyLogin);
            }
            if (!proxyPwd.equals("")) {
                section.put(PROXY_PASSWORD, proxyPwd);
            }
        }
        storeConfig(ini);
    }

    public static void removeProxySettings() throws Exception {
        Ini ini = getParser();
        Ini.Section groups = ini.get("groups");
        
        List<String> toBeRemoved = new ArrayList<String>();
        for (String group : groups.keySet()) {
            if (group.startsWith(GROUP_PREFIX)) {
                log.debug("Removing group: " + group);

                ini.remove(ini.get(group));
                toBeRemoved.add(group);
            }
        }
        for(String group : toBeRemoved) {
            groups.remove(group);
        }
        storeConfig(ini);
    }

    private static void storeConfig(Ini ini) throws IOException,
            ClientException {
        SVNClientImpl client = SVNClientImpl.newInstance();
        Writer writer = new FileWriter(client.getConfigDirectory() + "/servers");
        ini.store(writer);
    }
}
