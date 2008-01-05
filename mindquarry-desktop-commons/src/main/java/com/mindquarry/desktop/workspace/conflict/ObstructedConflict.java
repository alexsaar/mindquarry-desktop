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

import com.mindquarry.desktop.I18N;
import com.mindquarry.desktop.util.FileHelper;
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
    public void beforeRemoteStatus() throws ClientException, IOException {
        File file = new File(status.getPath());
        switch (action) {
        case UNKNOWN:
            // client did not set a conflict resolution
            log.error("ObstructedConflict with no action set: " + status.getPath());
            //throw new CancelException("ObstructedConflict with no action set: " + status.getPath());
            break;
            
        case RENAME:
            log.info("renaming " + file.getAbsolutePath() + " to " + newName);

            File destination = new File(file.getParentFile(), newName);
            FileHelper.renameTo(file, destination);
            
            client.add(destination.getPath(), true, true);
            
            // restore file or folder (updating to working copy base revision)
            client.update(status.getPath(), Revision.BASE, true);
            
            break;
            
        case REVERT:
            log.info("reverting obstructed file/folder: " + status.getPath());
            
            FileUtils.forceDelete(file);
            
            // restore file or folder (updating to working copy base revision)
            client.update(status.getPath(), Revision.BASE, true);
            
            break;
        }
    }

    public String toString() {
        return "Obstructed Conflict: " + status.getPath() + (action == Action.UNKNOWN ? "" : " " + action.name());
    }

    /**
     * Resolve the conflict by renaming the new file or dir and restoring the
     * old one.
     * 
     * Make sure you have called isRenamePossible(newName) and it returned true
     * before calling doRename(newName).
     * 
     * @param newName the new name of the file to rename to. Must be just a
     *                filename but not a full path name.
     */
    public void doRename(String newName) {
        this.newName = newName;
        this.action = Action.RENAME;
    }
    
    /**
     * Resolve the conflict by reverting the new file or folder to the older
     * variant. Note that the new content will be lost!
     */
    public void doRevert() {
        this.action = Action.REVERT;
    }

    @Override
    public String getLongDescription() {
        if (file.isDirectory()) {
            return I18N.get("This is a new directory, but on the " +
            	    "server there still exists a file of the same name.");
        } else {
            return I18N.get("This is a new file, but on the " +
                    "server there still exists a directory of the same name.");
        }
    }

}
