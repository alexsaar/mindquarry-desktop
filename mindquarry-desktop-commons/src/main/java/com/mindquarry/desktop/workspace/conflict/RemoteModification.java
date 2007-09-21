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
package com.mindquarry.desktop.workspace.conflict;

import java.io.File;

import org.tigris.subversion.javahl.Status;

/**
 * Describes changes that modify a file or directory remotely.
 * 
 * @author <a href="mailto:christian(dot)richardt(at)mindquarry(dot)com">Christian Richardt</a>
 */
public class RemoteModification extends Change {
 
    public RemoteModification(Status status) {
        super(status, new File(status.getPath()));
    }

    @Override
    public ChangeDirection getChangeDirection() {
        if(file.isDirectory()) // do not show direction icon for 'modified' dirs
            return ChangeDirection.NONE;
        else
            return ChangeDirection.FROM_SERVER;
    }

    @Override
    public ChangeStatus getChangeStatus() {
        return ChangeStatus.MODIFIED;
    }

    @Override
    public String getLongDescription() {
        if(file.isDirectory())
            return "Files or directories in this directory have been added or deleted on the server. " +
                    "These updated will be downloaded from the server.";
        else
            return "This remotely modified file will be downloaded from the server.";
    }

    @Override
    public String getShortDescription() {
        return "Modified remotely";
    }

    @Override
    public String toString() {
        return file.getName() + ": RemoteModified";
    }
}
