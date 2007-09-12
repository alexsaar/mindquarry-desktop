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
import java.util.ArrayList;
import java.util.List;

import com.mindquarry.desktop.model.ModelBase;
import com.mindquarry.desktop.util.NotAuthorizedException;

/**
 * @author <a href="mailto:lars(dot)trieloff(at)mindquarry(dot)com">Lars
 *         Trieloff</a>
 */
public class TeamList extends ModelBase {
    private List<Team> teams;

    public TeamList(InputStream data, String url, String login, String password) {
        super(data, new TeamListTransformer(url, login, password));
    }

    public TeamList(String url, String login, String password)
            throws NotAuthorizedException, Exception {
        super(url, login, password, new TeamListTransformer(url, login,
                password));
    }

    public TeamList() {
        super();
    }

    public TeamList(List<Team> teams) {
        super();
        this.teams = teams;
    }

    @Override
    protected void initModel() {
        teams = new ArrayList<Team>();
    }

    /**
     * Getter for the list of tasks.
     * 
     * @return the list of tasks
     */
    public List<Team> getTeams() {
        return teams;
    }

    public void add(String url, String login, String password) throws NotAuthorizedException {
        teams.add(new Team(url, login, password));
    }
}
