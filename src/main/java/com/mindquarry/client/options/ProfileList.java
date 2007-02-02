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
package com.mindquarry.client.options;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class ProfileList implements Serializable {
    private static final long serialVersionUID = 8321211437810319791L;

    private List<Profile> profiles = new ArrayList<Profile>();

    private Profile selected;

    private Integer selectionIndex = -1;
    
    /**
     * Default constructor.
     */
    public ProfileList() {
    }
    
    /**
     * Copy constructor.
     */
    public ProfileList(ProfileList list) {
        for(Profile old : list.getProfiles()) {
            addProfile(new Profile(old));
        }
    }

    /**
     * Getter for profiles.
     * 
     * @return the profiles
     */
    public Profile[] getProfiles() {
        return profiles.toArray(new Profile[0]);
    }

    /**
     * Setter for profiles.
     * 
     * @param profiles the profiles to set
     */
    public void setProfiles(Profile[] profiles) {
        this.profiles.clear();
        for (Profile profile : profiles) {
            this.profiles.add(profile);
        }
    }
    
    public Profile get(int index) {
        return profiles.get(index);
    }

    public void addProfile(Profile profile) {
        this.profiles.add(profile);
    }

    public void deleteProfile(Profile profile) {
        this.profiles.remove(profile);
        updateSelectionIndex();
    }

    public void deleteProfile(String name) {
        Profile toDelete = findByName(name);
        if (toDelete != null) {
            this.profiles.remove(toDelete);
        }
        updateSelectionIndex();
    }

    public Profile getProfileByName(String name) {
        return findByName(name);
    }

    public void select(Profile profile) {
        this.selected = profile;
        updateSelectionIndex();
    }

    public void select(String name) {
        Profile tmp = findByName(name);
        if (tmp != null) {
            selected = tmp;
        }
        updateSelectionIndex();
    }

    public Profile selectedProfile() {
        return this.selected;
    }

    public Integer getSelectionIndex() {
        return selectionIndex;
    }

    public void setSelectionIndex(Integer selectedIndex) {
        this.selectionIndex = selectedIndex;
    }

    private void updateSelectionIndex() {
        if(selected == null){
            selectionIndex = -1;
            return;
        }
        
        int index = -1;
        for(Profile profile : profiles) {
            if(profile.getName().equals(selected.getName())) {
                selectionIndex = index;
                return;
            }
        }
    }

    private Profile findByName(String name) {
        for (Profile profile : profiles) {
            if (profile.getName().equals(name)) {
                return profile;
            }
        }
        return null;
    }
    
    public int size() {
        return profiles.size();
    }
}
