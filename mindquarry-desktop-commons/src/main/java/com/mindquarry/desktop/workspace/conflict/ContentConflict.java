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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    
    private static Log log = LogFactory.getLog(ContentConflict.class);

    private Action action = Action.UNKNOWN;
    private File conflictServerFile;
    private File conflictLocalFile;
    
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
        conflictServerFile = new File(parentDir, status.getConflictNew());

        // FIXME: handle binary files correctly
        if (status.getConflictWorking().length() == 0) { // binary file
            conflictLocalFile = new File(status.getPath());
        } else { // plain text files
            conflictLocalFile = new File(parentDir, status.getConflictWorking());
        }
    }

    @Override
    public void accept(ConflictHandler handler) throws CancelException {
        // rename conflicts files
        conflictServerFile = renameConflictFile(getConflictServerFile());
        conflictLocalFile = renameConflictFile(getConflictLocalFile());
        
        try {
            handler.handle(this);
        } catch(Exception e) {
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
     * @return the new file
     */
    protected static File renameConflictFile(File conflictedFile) {
        if(conflictedFile == null) {
            log.warn("renameConflictFile: conflictedFile is null");
            return null;
        }

        File parentDir = conflictedFile.getParentFile();
        String conflictedFilename = conflictedFile.getName(); 
        int ext2pos = conflictedFilename.lastIndexOf('.');
        int ext1pos = conflictedFilename.lastIndexOf('.', ext2pos-1);
        
        // test if there are less than two extension, and if so skip renaming
        if (ext1pos < 0 || ext2pos < 0) {
            log.info("Skip renaming file '" + conflictedFilename + "'");
            return conflictedFile;
        }

        String name = conflictedFilename.substring(0, ext1pos);
        String ext1 = conflictedFilename.substring(ext1pos+1, ext2pos);
        String ext2 = conflictedFilename.substring(ext2pos+1);

        String newFilename = name+"."+ext2+"."+ext1;
        File newFile = new File(parentDir, newFilename);

        log.info("Renaming '" + conflictedFilename + "' => '" + newFilename + "'");
        conflictedFile.renameTo(newFile);
        return newFile;
    }

    private void resolveConflict() throws ClientException, IOException {
        // rename conflicts files
        conflictServerFile = renameConflictFile(getConflictServerFile());
        conflictLocalFile = renameConflictFile(getConflictLocalFile());
        
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

    public File getConflictServerFile() {
        return conflictServerFile;
    }

    public File getConflictLocalFile() {
        return conflictLocalFile;
    }

    public File getConflictTargetFile() {
        return new File(status.getPath());
    }

    public void doCancel() {
        // rename conflicts files
        conflictServerFile = renameConflictFile(getConflictServerFile());
        conflictLocalFile = renameConflictFile(getConflictLocalFile());      
    }
}
