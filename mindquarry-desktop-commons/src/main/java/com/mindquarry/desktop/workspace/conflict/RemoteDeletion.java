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

import com.mindquarry.desktop.Messages;

/**
 * Describes changes that delete a file or directory remotely.
 * 
 * @author <a href="mailto:christian(dot)richardt(at)mindquarry(dot)com">Christian Richardt</a>
 */
public class RemoteDeletion extends Change {
 
    public RemoteDeletion(Status status) {
        super(status, new File(status.getPath()));
    }

    @Override
    public ChangeDirection getChangeDirection() {
        return ChangeDirection.FROM_SERVER;
    }

    @Override
    public ChangeStatus getChangeStatus() {
        return ChangeStatus.DELETED;
    }

    @Override
    public String getLongDescription() {
        if(file.isDirectory())
            return Messages.getString("This directory was deleted on the server and will hence be deleted locally.");
        else
            return Messages.getString("This file was deleted on the server and will hence be deleted locally.");
    }

    @Override
    public String getShortDescription() {
        return Messages.getString("Deleted remotely");
    }

    @Override
    public String toString() {
        return file.getName() + ": RemoteDeletion";
    }
}
