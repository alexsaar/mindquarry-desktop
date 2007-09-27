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
 * Describes a change where a file/directory has been deleted and re-added
 * locally (can only happen using other SVN clients).
 *  
 * @author <a href="naber(at)mindquarry(dot)com">Daniel Naber</a>
 */
public class LocalReplace extends Change {

    public LocalReplace(File file, Status status) {
        super(status, file);
    }

    @Override
    public ChangeDirection getChangeDirection() {
        return ChangeDirection.TO_SERVER;
    }

    @Override
    public ChangeStatus getChangeStatus() {
        return ChangeStatus.REPLACED;
    }

    @Override
    public String getLongDescription() {
        if(file.isDirectory())
            return Messages.getString("This directory was deleted and re-added, it will be uploaded to the server.");
        else
            return Messages.getString("This file was deleted and re-added, it will be uploaded to the server.");
    }

    @Override
    public String getShortDescription() {
        return Messages.getString("Replaced locally");
    }

    @Override
    public String toString() {
        return file.getName() + ": LocalReplace";
    }

}
