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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.tigris.subversion.javahl.Status;

import com.mindquarry.desktop.workspace.conflict.Change;

/**
 * A list of change sets (just a List with some convenience methods),
 * one changeset per team.
 * 
 * @author <a href="naber(at)mindquarry(dot)com">Daniel Naber</a>
 */
public class ChangeSets {

    private List<ChangeSet> changeSets = new ArrayList<ChangeSet>();
    
    public ChangeSets() {
    }
    
    public void add(ChangeSet changeSet) {
        changeSets.add(changeSet);
    }

    public List<ChangeSet> getList() {
        return changeSets;
    }

    /**
     * Get all files from all changesets, ie form all teams.
     */
    public List<File> getFiles() {
        List<File> files = new ArrayList<File>();
        for (ChangeSet changeSet : changeSets) {
            files.addAll(changeSet.getFiles());
        }
        return files;
    }

    /**
     * Get the Change item of a file (no matter what team it is in).
     */
    public Change getChange(File file) {
        for (ChangeSet changeSet : changeSets) {
            for (File tmpFile : changeSet.getFiles()) {
                if (tmpFile.equals(file)) {
                    return changeSet.getChanges().get(file);
                }
            }
        }
        return null;
    }

    /**
     * Get the status of a file (no matter what team it is in). Convenience method.
     */
    public Status getStatus(File file) {
        Change change = getChange(file);
        if (change != null) {
            return change.getStatus();
        }
        return null;
    }

    public int size() {
        return changeSets.size();
    }

    /**
     * Get the IDs of all teams.
     */
    public Set<String> getTeamIds() {
        Set<String> teamIds = new HashSet<String>();
        for (ChangeSet changeSet : changeSets) {
            teamIds.add(changeSet.getTeam().getId());
        }
        return teamIds;
    }

    public String toString() {
        return changeSets.toString();
    }
    
}
