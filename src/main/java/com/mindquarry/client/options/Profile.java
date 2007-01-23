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

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class Profile {
    private String name;
    
    private String login;

    private String password;

    private String endpoint;

    private String location;

    public Profile() {
        
    }
    
    public Profile(String name, String login, String password, String endpoint,
            String location) {
        super();
        this.name = name;
        this.login = login;
        this.password = password;
        this.endpoint = endpoint;
        this.location = location;
    }

    /**
     * Getter for endpoint.
     * 
     * @return the endpoint
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for endpoint.
     * 
     * @param endpoint the endpoint to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Getter for endpoint.
     * 
     * @return the endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Setter for endpoint.
     * 
     * @param endpoint the endpoint to set
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Getter for location.
     * 
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Setter for location.
     * 
     * @param location the location to set
     */
    public void setLocation(String location) {
        this.location = location;
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
}
