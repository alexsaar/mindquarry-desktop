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

import com.mindquarry.desktop.I18N;

/**
 * Describes changes that add a file or directory locally.
 * 
 * @author <a href="mailto:christian(dot)richardt(at)mindquarry(dot)com">Christian Richardt</a>
 */
public class LocalAddition extends Change {
 
    public LocalAddition(File file, Status ancestorStatus) {
        super(ancestorStatus, file);
    }

    @Override
    public ChangeDirection getChangeDirection() {
        return ChangeDirection.TO_SERVER;
    }

    @Override
    public ChangeStatus getChangeStatus() {
        return ChangeStatus.ADDED;
    }

    @Override
    public String getLongDescription() {
        if(file.isDirectory())
            return I18N.get("This new directory will be uploaded to the server.");
        else
            return I18N.get("This new file will be uploaded to the server.");
    }

    @Override
    public String getShortDescription() {
        return I18N.get("Added locally");
    }

    @Override
    public String toString() {
        return file.getName() + ": LocalAddition";
    }
}
