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

/**
 * Describes changes that delete a file or directory locally.
 * 
 * @author <a href="mailto:christian(dot)richardt(at)mindquarry(dot)com">Christian Richardt</a>
 */
public class LocalDeletion extends Change {
 
    public LocalDeletion(Status status) {
        super(status, new File(status.getPath()));
    }

    @Override
    public ChangeDirection getChangeDirection() {
        return ChangeDirection.TO_SERVER;
    }

    @Override
    public ChangeStatus getChangeStatus() {
        return ChangeStatus.DELETED;
    }

    @Override
    public String getLongDescription() {
        if(status.getNodeKind() == NodeKind.dir)
            return "This directory will be deleted on the server.";
        else
            return "This file will be deleted on the server.";
    }

    @Override
    public String getShortDescription() {
        return "Deleted locally";
    }

    @Override
    public String toString() {
        return file.getName() + ": LocalDeletion";
    }
}
