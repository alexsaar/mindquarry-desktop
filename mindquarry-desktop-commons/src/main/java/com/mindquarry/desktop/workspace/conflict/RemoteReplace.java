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

import com.mindquarry.desktop.I18N;

/**
 * Describes a change where a file/directory has been deleted and re-added
 * remotely (can only happen using other SVN clients).
 *  
 * @author <a href="naber(at)mindquarry(dot)com">Daniel Naber</a>
 */
public class RemoteReplace extends Change {

    public RemoteReplace(Status status) {
        super(status, new File(status.getPath()));
    }

    @Override
    public ChangeDirection getChangeDirection() {
        return ChangeDirection.FROM_SERVER;
    }

    @Override
    public ChangeStatus getChangeStatus() {
        return ChangeStatus.REPLACED;
    }

    @Override
    public String getLongDescription() {
        if(status.getReposKind() == NodeKind.dir)
            return I18N.get("This directory was replaced on the server and will be downloaded.");
        else
            return I18N.get("This file was replaced on the server and will be downloaded.");
    }

    @Override
    public String getShortDescription() {
        return I18N.get("Replaced remotely");
    }

    @Override
    public String toString() {
        return file.getName() + ": RemoteReplace";
    }

}
