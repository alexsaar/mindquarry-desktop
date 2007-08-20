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
package com.mindquarry.desktop.model.team;

import java.io.InputStream;

import com.mindquarry.desktop.model.ModelBase;
import com.mindquarry.desktop.util.NotAuthorizedException;

/**
 * @author <a href="mailto:lars(dot)trieloff(at)mindquarry(dot)com">Lars
 *         Trieloff</a>
 */
public class Team extends ModelBase {
    private String id;

    private String name;

    private String workspaceURL;

    public Team(InputStream data) {
        super(data, new TeamTransformer());
    }

    public Team() {
        super();
    }

    public Team(String url, String login, String password)
            throws NotAuthorizedException, Exception {
        super(url, login, password, new TeamTransformer());
    }

    /**
     * Getter for id.
     * 
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Setter for id.
     * 
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter for workspaceURL.
     * 
     * @return the workspaceURL
     */
    public String getWorkspaceURL() {
        return workspaceURL;
    }

    /**
     * Setter for workspaceURL.
     * 
     * @param workspaceURL
     *            the workspaceURL to set
     */
    public void setWorkspaceURL(String workspaceURL) {
        this.workspaceURL = workspaceURL;
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
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return id;
    }
}
