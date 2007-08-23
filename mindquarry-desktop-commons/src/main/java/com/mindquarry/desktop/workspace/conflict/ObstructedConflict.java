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
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.Revision;
import org.tigris.subversion.javahl.Status;

import com.mindquarry.desktop.workspace.exception.CancelException;

/**
 * @author <a href="mailto:alexander(dot)klimetschek(at)mindquarry(dot)com">
 *         Alexander Klimetschek</a>
 *
 */
public class ObstructedConflict extends RenamingConflict {

    private Action action = Action.UNKNOWN;
    private String newName;
    
    public enum Action {
        /**
         * Indicating no conflict solution action was chosen yet.
         */
        UNKNOWN,
        /**
         * Rename the new directory or file and revert the old one.
         */
        RENAME,
        /**
         * Undo the obstructed file or dir to the original state.
         */
        REVERT;
    }
    
    public ObstructedConflict(Status status) {
        super(status);
    }

    @Override
    public void accept(ConflictHandler handler) throws CancelException {
        handler.handle(this);
    }

    @Override
    public void beforeRemoteStatus() throws ClientException {
        File file = new File(status.getPath());
        switch (action) {
        case UNKNOWN:
            // client did not set a conflict resolution
            log.error("ObstructedConflict with no action set: " + status.getPath());
            //throw new CancelException("ObstructedConflict with no action set: " + status.getPath());
            break;
            
        case RENAME:
            log.info("renaming to " + newName);

            if (!file.renameTo(new File(file.getParentFile(), newName))) {
                log.error("rename to " + newName + " failed.");
                // TODO: callback for error handling
                System.exit(-1);
            }
            
            // restore file or folder (updating to working copy base revision)
            client.update(status.getPath(), Revision.BASE, true);
            
            break;
            
        case REVERT:
            log.info("reverting obstructed file/folder: " + status.getPath());
            
            try {
                FileUtils.forceDelete(file);
            } catch (IOException e) {
                log.error("deleting failed.");
                // TODO: callback for error handling
                System.exit(-1);
            }
            
            // restore file or folder (updating to working copy base revision)
            client.update(status.getPath(), Revision.BASE, true);
            
            break;
        }
    }

    public String toString() {
        return "Obstructed Conflict: " + status.getPath() + (action == Action.UNKNOWN ? "" : " " + action.name());
    }

    public void doRename(String newName) {
        this.newName = newName;
        this.action = Action.RENAME;
    }
    
    public void doRevert() {
        this.action = Action.REVERT;
    }
}
