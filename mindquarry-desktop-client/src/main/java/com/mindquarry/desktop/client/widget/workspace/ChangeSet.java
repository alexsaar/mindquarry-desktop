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

package com.mindquarry.desktop.client.widget.workspace;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.mindquarry.desktop.model.team.Team;
import com.mindquarry.desktop.workspace.conflict.Change;

/**
 * A set of changed files and their status (per team).
 * 
 * @author <a href="naber(at)mindquarry(dot)com">Daniel Naber</a>
 */
public class ChangeSet {

    private Team team;
    private Map<File, Change> changes = new HashMap<File, Change>();
    
    public ChangeSet(Team team) {
        this.team = team;
    }
    
    public Team getTeam() {
        return team;
    }
    
    public void addChange(Change change) {
        changes.put(change.getFile(), change);
    }

    public Map<File, Change> getChanges() {
        return changes;
    }

    public Set<File> getFiles() {
        return changes.keySet();
    }

    public String toString() {
        // Status doesn't offer a useful toString() anyway, so print 
        // just the file names:
        return team + ": " + changes.values();
    }

}
