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

import org.tigris.subversion.javahl.NodeKind;
import org.tigris.subversion.javahl.Status;

import com.mindquarry.desktop.Messages;

/**
 * Describes changes that add a file or directory remotely.
 * 
 * @author <a href="mailto:christian(dot)richardt(at)mindquarry(dot)com">Christian Richardt</a>
 */
public class RemoteAddition extends Change {
 
    public RemoteAddition(File file, Status ancestorStatus) {
        super(ancestorStatus, file);
    }

    @Override
    public ChangeDirection getChangeDirection() {
        return ChangeDirection.FROM_SERVER;
    }

    @Override
    public ChangeStatus getChangeStatus() {
        return ChangeStatus.ADDED;
    }

    @Override
    public String getLongDescription() {
        if(status.getReposKind() == NodeKind.dir)
            return Messages.getString("This new directory was added on the server and will be downloaded.");
        else
            return Messages.getString("This new file was added on the server and will be downloaded.");
    }

    @Override
    public String getShortDescription() {
        return Messages.getString("Added remotely");
    }

    @Override
    public String toString() {
        return file.getName() + ": RemoteAddition";
    }
}
