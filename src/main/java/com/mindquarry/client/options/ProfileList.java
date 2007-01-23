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

import java.util.ArrayList;
import java.util.List;

/**
 * Add summary documentation here.
 *
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class ProfileList {
    private List<Profile> profiles = new ArrayList<Profile>();
    
    private Profile selected;

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
        for(Profile profile : profiles) {
            this.profiles.add(profile);
        }
    }
    
    public void addProfile(Profile profile) {
        this.profiles.add(profile);
    }
    
    public void deleteProfile(Profile profile) {
        this.profiles.remove(profile);
    }
    
    public void deleteProfile(String name) {
        for(Profile profile : this.profiles) {
            if(profile.getName().equals(name)) {
                this.profiles.remove(profile);
            }
        }
    }
    
    public Profile getProfileByName(String name) {
        for(Profile profile : this.profiles) {
            if(profile.getName().equals(name)) {
                return profile;
            }
        }
        return null;
    }
    
    public void select(Profile profile) {
        this.selected = profile;
    }
    
    public Profile selectedProfile() {
        return this.selected;
    }
}
