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

import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.Status;

import com.mindquarry.desktop.workspace.exception.CancelException;

/**
 * Represents the standard svn conflict for a file content's.
 * 
 * @author <a href="mailto:alexander(dot)klimetschek(at)mindquarry(dot)com">
 *         Alexander Klimetschek</a>
 *
 */
public class ContentConflict extends Conflict {

    public ContentConflict(Status status) {
        super(status);
    }

    @Override
    public void accept(ConflictHandler handler) throws CancelException {
        handler.handle(this);
    }

    @Override
    public void afterUpdate() throws ClientException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void beforeUpdate() throws ClientException {
        // TODO Auto-generated method stub
        
    }

    public String toString() {
        return "Content Conflict: " + status.getPath();
        //return "Content Conflict: " + status.getPath() + (action == Action.UNKNOWN ? "" : " " + action.name());
    }
}
