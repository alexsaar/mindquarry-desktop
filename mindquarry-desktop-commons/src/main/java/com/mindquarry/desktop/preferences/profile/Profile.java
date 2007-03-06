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
package com.mindquarry.desktop.preferences.profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.preference.PreferenceStore;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class Profile {
    public static final String PREF_WORKSPACES = "workspaces"; //$NON-NLS-1$

    public static final String PREF_SERVER_URL = "url"; //$NON-NLS-1$

    public static final String PREF_PASSWORD = "password"; //$NON-NLS-1$

    public static final String PREF_LOGIN = "login"; //$NON-NLS-1$

    public static final String PREF_NAME = "name"; //$NON-NLS-1$

    public static final String PROFILE_KEY_BASE = "com.mindquarry.server.profile."; //$NON-NLS-1$

    public static final String PROFILE_SELECTED = "com.mindquarry.server.selected"; //$NON-NLS-1$

    private String name;

    private String login;

    private String password;

    private String serverURL;

    private String workspaceFolder;

    /**
     * Default constructor
     */
    public Profile() {
    }

    /**
     * Copy constructor.
     */
    public Profile(Profile old) {
        name = new String(old.getName());
        login = new String(old.getLogin());
        password = new String(old.getPassword());
        serverURL = new String(old.getServerURL());
        workspaceFolder = new String(old.getWorkspaceFolder());
    }

    /**
     * Constructor that initializes all fields.
     */
    public Profile(String name, String login, String password,
            String serverURL, String workspaceFolder) {
        super();
        this.name = name;
        this.login = login;
        this.password = password;
        this.serverURL = serverURL;
        this.workspaceFolder = workspaceFolder;
    }

    /**
     * Getter for name.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for name.
     * 
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for serverURL.
     * 
     * @return the serverURL
     */
    public String getServerURL() {
        return serverURL;
    }

    /**
     * Setter for serverURL.
     * 
     * @param url the serverURL to set
     */
    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }

    /**
     * Getter for workspaceFolder.
     * 
     * @return the workspaceFolder
     */
    public String getWorkspaceFolder() {
        return workspaceFolder;
    }

    /**
     * Setter for location.
     * 
     * @param folder the location to set
     */
    public void setWorkspaceFolder(String workspaceFolder) {
        this.workspaceFolder = workspaceFolder;
    }

    /**
     * Getter for login.
     * 
     * @return the login
     */
    public String getLogin() {
        return login;
    }

    /**
     * Setter for login.
     * 
     * @param login the login to set
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * Getter for password.
     * 
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Setter for password.
     * 
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    public static List<Profile> loadProfiles(PreferenceStore store) {
        HashMap<Integer, Profile> storedProfiles = new HashMap<Integer, Profile>();

        // load stored profiles
        String[] prefs = store.preferenceNames();
        for (String pref : prefs) {
            if (pref.startsWith(PROFILE_KEY_BASE)) {
                // analyze preference
                int nbr = Integer.valueOf(pref.substring(PROFILE_KEY_BASE
                        .length(), PROFILE_KEY_BASE.length() + 1));
                String prefName = pref.substring(PROFILE_KEY_BASE.length() + 2,
                        pref.length());

                // init profile
                Profile profile;
                if (storedProfiles.containsKey(nbr)) {
                    profile = storedProfiles.get(nbr);
                } else {
                    profile = new Profile("", //$NON-NLS-1$
                            "", //$NON-NLS-1$
                            "", //$NON-NLS-1$
                            "", //$NON-NLS-1$
                            ""); //$NON-NLS-1$
                    storedProfiles.put(nbr, profile);
                }
                setProfileAttribute(store, profile, pref, prefName);
            }
        }
        // set profile list
        List<Profile> profiles = new ArrayList<Profile>();
        Iterator<Integer> keyIter = storedProfiles.keySet().iterator();
        while (keyIter.hasNext()) {
            Profile profile = storedProfiles.get(keyIter.next());
            profiles.add(profile);
        }
        return profiles;
    }

    public static void storeProfiles(PreferenceStore store,
            List<Profile> profiles) {
        // set properties from profiles
        int pos = 0;
        for (Profile profile : profiles) {
            store.putValue(Profile.PROFILE_KEY_BASE + pos + "." //$NON-NLS-1$
                    + Profile.PREF_NAME, profile.getName());
            store.putValue(Profile.PROFILE_KEY_BASE + pos + "." //$NON-NLS-1$
                    + Profile.PREF_LOGIN, profile.getLogin());
            store.putValue(Profile.PROFILE_KEY_BASE + pos + "." //$NON-NLS-1$
                    + Profile.PREF_PASSWORD, profile.getPassword());
            store.putValue(Profile.PROFILE_KEY_BASE + pos + "." //$NON-NLS-1$
                    + Profile.PREF_SERVER_URL, profile.getServerURL());
            store.putValue(Profile.PROFILE_KEY_BASE + pos + "." //$NON-NLS-1$
                    + Profile.PREF_WORKSPACES, profile.getWorkspaceFolder());
            pos++;
        }
    }

    public static boolean addProfile(PreferenceStore store, Profile toBeStored) {
        List<Profile> profiles = loadProfiles(store);
        for (Profile profile : profiles) {
            if (profile.getName().equals(toBeStored.getName())) {
                return false;
            }
        }
        profiles.add(toBeStored);
        storeProfiles(store, profiles);
        return true;
    }

    public static void selectProfile(PreferenceStore store, String name) {
        String[] prefs = store.preferenceNames();
        for (String pref : prefs) {
            if (pref.startsWith(PROFILE_KEY_BASE)) {
                // analyze preference
                int nbr = Integer.valueOf(pref.substring(PROFILE_KEY_BASE
                        .length(), PROFILE_KEY_BASE.length() + 1));
                String prefName = pref.substring(PROFILE_KEY_BASE.length() + 2,
                        pref.length());

                // check if we found the select profile
                if ((prefName.equals(PREF_NAME))
                        && (store.getString(pref).equals(name))) {
                    store.putValue(PROFILE_SELECTED, String.valueOf(nbr));
                    return;
                }
            }
        }
    }

    public static Profile getSelectedProfile(PreferenceStore store) {
        return getSelectedProfile(store, store.getInt(PROFILE_SELECTED));
    }

    private static Profile getSelectedProfile(PreferenceStore store, int id) {
        Profile profile = null;

        String[] prefs = store.preferenceNames();
        for (String pref : prefs) {
            if (pref.startsWith(PROFILE_KEY_BASE)) {
                // analyze preference
                int nbr = Integer.valueOf(pref.substring(PROFILE_KEY_BASE
                        .length(), PROFILE_KEY_BASE.length() + 1));
                String prefName = pref.substring(PROFILE_KEY_BASE.length() + 2,
                        pref.length());

                // init profile if not done already
                if (profile == null) {
                    profile = new Profile("", //$NON-NLS-1$
                            "", //$NON-NLS-1$
                            "", //$NON-NLS-1$
                            "", //$NON-NLS-1$
                            ""); //$NON-NLS-1$
                }

                // set profile values
                if (nbr == id) {
                    setProfileAttribute(store, profile, pref, prefName);
                }
            }
        }
        return profile;
    }

    private static void setProfileAttribute(PreferenceStore store,
            Profile profile, String pref, String prefName) {
        if (prefName.equals(PREF_NAME)) {
            profile.setName(store.getString(pref));
        } else if (prefName.equals(PREF_LOGIN)) {
            profile.setLogin(store.getString(pref));
        } else if (prefName.equals(PREF_PASSWORD)) {
            profile.setPassword(store.getString(pref));
        } else if (prefName.equals(PREF_SERVER_URL)) {
            profile.setServerURL(store.getString(pref));
        } else if (prefName.equals(PREF_WORKSPACES)) {
            profile.setWorkspaceFolder(store.getString(pref));
        }
    }
}
