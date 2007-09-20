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
package com.mindquarry.desktop.preferences;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;

import com.mindquarry.desktop.preferences.pages.GeneralSettingsPage;
import com.mindquarry.desktop.preferences.pages.NetworkSettingsPage;
import com.mindquarry.desktop.preferences.pages.ProxySettingsPage;
import com.mindquarry.desktop.preferences.pages.ServerProfilesPage;

/**
 * Constants for preference management.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class PreferenceUtilities {
    public static final String SETTINGS_FOLDER = System
            .getProperty("user.home") //$NON-NLS-1$
            + "/.mindquarry"; //$NON-NLS-1$

    private static PreferenceManager mgr;
    
    public static boolean checkPreferenceFile(File prefFile) {
        if (!prefFile.exists()) {
            try {
                prefFile.getParentFile().mkdirs();
                prefFile.createNewFile();
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    public static PreferenceManager getDefaultPreferenceManager() {
        if (mgr == null) {
            mgr = new PreferenceManager();

            GeneralSettingsPage general = new GeneralSettingsPage();
            mgr
                    .addToRoot(new PreferenceNode(GeneralSettingsPage.NAME,
                            general));

            NetworkSettingsPage network = new NetworkSettingsPage();
            PreferenceNode networkNode = new PreferenceNode(
                    ServerProfilesPage.NAME, network);
            mgr.addToRoot(networkNode);

            ServerProfilesPage profiles = new ServerProfilesPage();
            PreferenceNode profilesNode = new PreferenceNode(
                    ServerProfilesPage.NAME, profiles);
            mgr.addTo(networkNode.getId(), profilesNode);
            
            ProxySettingsPage proxy = new ProxySettingsPage();
            mgr.addTo(networkNode.getId(), new PreferenceNode(
                    ProxySettingsPage.NAME, proxy));
        }
        return mgr;
    }
}
