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

import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.Status;

import com.mindquarry.desktop.util.FileHelper;
import com.mindquarry.desktop.workspace.exception.CancelException;

/**
 * Represents the standard svn conflict for a file content's.
 * 
 * @author <a href="mailto:alexander(dot)klimetschek(at)mindquarry(dot)com">
 *         Alexander Klimetschek</a>
 *
 */
public class ContentConflict extends Conflict {

    private Action action = Action.UNKNOWN;
    private File conflictOldFile;
    private File conflictNewFile;
    private File conflictWorkingFile;
    
    public enum Action {
        /**
         * Indicating no conflict solution action was chosen yet.
         */
        UNKNOWN,
        /**
         * Use the local version (mine) of the file (loosing the server's
         * modifications).
         */
        USE_LOCAL,
        /**
         * Use the server version of the file (loosing the local modifications).
         */
        USE_REMOTE,
        /**
         * Call a merge program (ie. Word in merge-mode) to let the user handle
         * the conflicts.
         */
        MERGE;
    }
    
    
    public ContentConflict(Status status) {
        super(status);
        File parentDir = new File(status.getPath()).getParentFile();
        conflictOldFile = new File(parentDir, status.getConflictOld());
        conflictNewFile = new File(parentDir, status.getConflictNew());
        conflictWorkingFile = new File(parentDir, status.getConflictWorking());
    }

    @Override
    public void accept(ConflictHandler handler) throws CancelException {
        // rename conflicts files
        conflictOldFile = renameConflictFile(status.getConflictOld(), false);
        conflictNewFile = renameConflictFile(status.getConflictNew(), false);
        conflictWorkingFile = renameConflictFile(status.getConflictWorking(), false);
        
        try {
            handler.handle(this);
        } catch(CancelException e) {
            doCancel(); // cleaning up if there's an exception
        }
    }
    
    /**
     * Rename a file created in a conflict to make their name them useful for
     * users and vice versa, e.g. 'file.txt.r11' <=> 'file.r11.txt'. Essentially
     * swaps the last two extensions of the filename.
     * 
     * @param conflictedFilename
     *            Filename of the conflict file (e.g. file.txt.r11).
     * @param undo
     *            Use false for 'file.txt.r11' => 'file.r11.txt' and true for
     *            'file.txt.r11' <= 'file.r11.txt'.
     * @return the new file
     */
    private File renameConflictFile(String conflictedFilename, boolean undo) {
        File parentDir = new File(status.getPath()).getParentFile();
        int ext2pos = conflictedFilename.lastIndexOf('.');
        int ext1pos = conflictedFilename.lastIndexOf('.', ext2pos-1);
        
        // test if there's only one extension, and if so skip renaming
        if (ext1pos < 0) {
            log.info("Skip renaming file '" + conflictedFilename + "'");
            return new File(parentDir, conflictedFilename);
        }

        String name = conflictedFilename.substring(0, ext1pos);
        String ext1 = conflictedFilename.substring(ext1pos+1, ext2pos);
        String ext2 = conflictedFilename.substring(ext2pos+1);

        String newFilename = name+"."+ext2+"."+ext1;
        File conflictedFile = new File(parentDir, conflictedFilename);
        File newFile = new File(parentDir, newFilename);

        if (!undo) { // rename file.doc.r123 => file.r123.doc
            log.info("Renaming '" + conflictedFilename + "' => '" + newFilename + "'");
            conflictedFile.renameTo(newFile);
            return newFile;
        } else { // rename file.r123.doc => file.doc.r123
            log.info("Renaming '" + newFilename + "' => '" + conflictedFilename + "'");
            newFile.renameTo(conflictedFile);
            return conflictedFile;
        }
    }

    private void resolveConflict() throws ClientException, IOException {
        // rename conflicts files
        conflictOldFile = renameConflictFile(status.getConflictOld(), true);
        conflictNewFile = renameConflictFile(status.getConflictNew(), true);
        conflictWorkingFile = renameConflictFile(status.getConflictWorking(), true);
        
        // check for conflict resolve method
        switch (action) {
        case UNKNOWN:
            // client did not set a conflict resolution
            log.error("ContentConflict with no action set: " + status.getPath());
            return;
            
        case USE_REMOTE:
            // copy latest revision from repository to main file
            File parent = new File(status.getPath()).getParentFile();
            File remoteFile = new File(parent, status.getConflictNew());
            
            File conflictFile = new File(status.getPath());
            FileHelper.delete(conflictFile);
            FileHelper.renameTo(remoteFile, conflictFile);
            
            break;
            
        case USE_LOCAL:
            // just keep the local file
            parent = new File(status.getPath()).getParentFile();
            remoteFile = new File(parent, status.getConflictWorking());
            
            conflictFile = new File(status.getPath());
            FileHelper.delete(conflictFile);
            FileHelper.renameTo(remoteFile, conflictFile);
            break;
            
        case MERGE:
            // all work (except resolving) is done in the GUI class
        	break;
        }
        
        client.resolved(status.getPath(), false);
    }

    @Override
    public void beforeCommit() throws ClientException, IOException {
        resolveConflict();
    }

    @Override
    public void beforeRemoteStatus() throws ClientException, IOException {
        resolveConflict();
    }

    public String toString() {
        return "Content Conflict: " + status.getPath() + (action == Action.UNKNOWN ? "" : " " + action.name());
    }
    
    /**
     * Resolves the conflict by using the local content of the file. Note that
     * the local modifications will be lost!
     */
    public void doUseLocal() {
        this.action = Action.USE_LOCAL;
    }

    /**
     * Resolves the conflict by using the remote content of the file. The remote
     * modifications are not lost, they are already part of a version on the
     * server.
     */
    public void doUseRemote() {
        this.action = Action.USE_REMOTE;
    }

    /**
     * Resolves the conflict by merging - this actually does nothing except for
     * resolving the conflict for svn. This expects the file to contain the new
     * merged content - how this happens is up to the client code or user.
     */
    public void doMerge() {
        this.action = Action.MERGE;
    }

    public File getConflictNewFile() {
        return conflictNewFile;
    }

    public File getConflictOldFile() {
        return conflictOldFile;
    }

    public File getConflictWorkingFile() {
        return conflictWorkingFile;
    }

    public void doCancel() {
        // rename conflicts files
        conflictOldFile = renameConflictFile(status.getConflictOld(), true);
        conflictNewFile = renameConflictFile(status.getConflictNew(), true);
        conflictWorkingFile = renameConflictFile(status.getConflictWorking(), true);        
    }
}
