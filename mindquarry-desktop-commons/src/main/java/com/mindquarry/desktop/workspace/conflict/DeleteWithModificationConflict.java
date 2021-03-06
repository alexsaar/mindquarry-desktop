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
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.tigris.subversion.javahl.ChangePath;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.Info;
import org.tigris.subversion.javahl.LogMessage;
import org.tigris.subversion.javahl.Revision;
import org.tigris.subversion.javahl.Status;
import org.tigris.subversion.javahl.StatusKind;
import org.tigris.subversion.javahl.Revision.Number;
import org.tmatesoft.svn.core.internal.util.SVNEncodingUtil;
import org.tmatesoft.svn.core.internal.wc.SVNAdminDirectoryLocator;

import com.mindquarry.desktop.I18N;
import com.mindquarry.desktop.util.FileHelper;
import com.mindquarry.desktop.workspace.exception.CancelException;

/**
 * Local delete (of folder or file) conflicts with remote modification of
 * file or contents of the folder - or a remote delete conflicts with local
 * modification of a file or contents of a folder.
 * 
 * @author <a href="mailto:saar@mindquarry.com">Alexander Saar</a>
 * @author <a href="mailto:victor.saar@mindquarry.com">Victor Saar</a>
 * @author <a href="mailto:klimetschek@mindquarry.com">Alexander Klimetschek</a>
 */
public class DeleteWithModificationConflict extends Conflict {

    // Some prefix to make sure the file prefix isn't too short for
    // File.createTempFile(). Use "mindquarry_" so we can easily spot
    // the files created by ourselfes:
	private static final String TEMP_FILE_PREFIX = "mindquarry_";
	
    private Action action = Action.UNKNOWN;
	
	public enum Action {
	    /**
	     * Indicating no conflict solution action was chosen yet.
	     */
		UNKNOWN,
		/**
		 * Really delete, throwing away the modifications or adds.
		 */
		DELETE,
		/**
		 * Keep the modified files, only deleting the untouched stuff.
		 */
		ONLYKEEPMODIFIED,
		/**
		 * Completely reverting the delete to the local (or remote) state.
		 */
		REVERTDELETE;
	}
	
	private List<Status> otherMods;
    private boolean localDelete;
	private File tempCopy;
	
	public DeleteWithModificationConflict(boolean localDelete, Status status, List<Status> otherMods) {
		super(status);
		this.localDelete = localDelete;
		this.otherMods = otherMods;
	}
	
	/**
	 * Creates a temporary directory similarly to to the way
	 * File.createTempFile(String,String,File) works. Creates an empty directory
	 * in the specified directory, using the given prefix and suffix strings to
	 * generate its name.
	 * 
	 * @param prefix
	 *            The prefix string to be used in generating the directory's
	 *            name; must be at least three characters long.
	 * @param suffix
	 *            The suffix string to be used in generating the directory's
	 *            name; may be null, in which case the suffix ".tmp" will be
	 *            used.
	 * @param directory
	 *            The directory in which the directory is to be created, or null
	 *            if the default temporary directory is to be used.
	 * @return
	 * @throws IOException
	 *            If creation of the temporary directory fails.
	 */
	private static File createTempDir(String prefix, String suffix, File directory)
	        throws IOException {
	    log.debug("createTempDir, directory: " + directory.getAbsolutePath());
        // add prefix, as name must be at least 3 characters
        prefix = TEMP_FILE_PREFIX + prefix;

		// create and immediately delete temporary file using library function
		File file = File.createTempFile(prefix, suffix, directory);
        log.debug("created file, will delete it again: " + file.getAbsolutePath());
		FileHelper.delete(file);
		
        // create directory with the same unique name
		FileHelper.mkdir(file);
		
		return file;
	}

	public void beforeUpdate() throws ClientException, IOException {
        // NOTE: here we could implement a fast-path avoiding the download
        // of the new files by simply deleting the folder on the server
        // before we run the update
	    // if (action == Action.DELETE) {
        // nothing to do here, we have to wait for the update and delete
        // the folder again (svn will recreate it due to the remote mods)
	    // }

	    if (action == Action.REVERTDELETE) {	 
	    	if (localDelete) {   	
		    	// revert local delete
	    	    log.info("beforeUpdate (localDelete), reverting " + status.getPath());
	    		client.revert(status.getPath(), true);
	    	} else {
	    		// make a local copy of the file/dir
	    		File source = new File(status.getPath());
	    		
	    		// temp file prefix is required to be at least 3 chars long, so add another prefix:
	    		String tmpPrefix = TEMP_FILE_PREFIX + source.getName();
				if (source.isFile()) {
	    			tempCopy = File.createTempFile(tmpPrefix, null, source.getParentFile());
					FileUtils.copyFile(source, tempCopy);
				} else {
	    			tempCopy = createTempDir(tmpPrefix, null, source.getParentFile());
					FileUtils.copyDirectory(source, tempCopy);
				}
				
				// remove .svn directories from copy (if there are any)
				removeDotSVNDirectories(tempCopy.getPath());
	    		
	    		// revert all local changes to file/dir
				log.info("beforeUpdate, reverting " + status.getPath());
				client.revert(status.getPath(), true);
				
				// TODO: really delete folder before svn copy
				// We will do a svn copy of an old version into this folder
				// after the update. To do this, the folder must not exist
				// anymore locally, and should not be seen as 'missing'.
				// Otherwise svn is forced to overwrite some existing, versioned
				// folder, which fails with errors like:
				// - 'Revision 1 doesn't match existing revision 2'
				//     if the folder is removed ('missing')
				// - 'path refers to directory or read access is denied'
				//     if the folder exists, but is 'unversioned'
				
				// it has to be in normal state, so that during the update it
				// gets deleted through the remote changes - the deletion will
				// then be reverted through the svn copy from the last version
				// before the deletion
	    		
				// TODO: this is wrong, only delete unversioned files
	    		// Delete complete file/dir as the update operation will leave
				// unversioned copies of files that were locally modified.
				FileUtils.forceDelete(source);
	    	}
	    }
	}

	public void afterUpdate() throws ClientException, IOException {
        switch (action) {
        case UNKNOWN:
            // client did not set a conflict resolution
            log.error("DeleteWithModificationConflict with no action set: " + status.getPath());
            break;
            
        case DELETE:
            log.info("now deleting again " + status.getPath());

            // Delete file or directory -- works in all cases:
            // 1. Local container delete with remote updates:
            //    Update operation makes sure that the remote updates are
            //    adopted locally, re-creating the affected files if necessary.
            //    So they need to be deleted again. Commit deletes remote
            //    directory.
            //
            // 2. Remote container delete with local updates:
            //    Update operation deletes everything in the remotely deleted
            //    directory apart from files affected by local changes, which
            //    are left unversioned and need to be deleted locally.
            //
            // 3. Remotely modified file deleted locally:
            //    Update operation makes sure that the remote update is adopted
            //    locally, re-creating the affected file if necessary. Hence
            //    need to delete the local file again.
            //
            // 4. Locally modified file deleted remotely:
            //    Update operation leaves the affected file as unversioned which
            //    is therefore deleted.
			File file = new File(status.getPath());
            FileUtils.forceDelete(file);
            
            break;
            
        case ONLYKEEPMODIFIED:
            log.info("keeping added/modified from remote: " + status.getPath());
            
            if (localDelete) {
            	// revert deletion status for local deletions
            	client.revert(status.getPath(), true);
            } else {
            	// only re-add files/directories that were added/modified
        		client.add(status.getPath(), false);
            	if (otherMods != null) {
            		for (Status s : otherMods) {
	            		client.add(s.getPath(), false);
            		}
            	}
            }
            
            break;
            
        case REVERTDELETE:
	    	if (localDelete == false) {
	            log.info("reverting remote delete: " + status.getPath());
	    		// Revert remote deletes in three steps:
	    		// 1. Restore last revision before deletion (incl. history)
	    		// 2. Redo changes by replacing with local files
	    		// 3. Re-add locally added files
	    		
	    		// 1. Restore last revision before deletion (incl. history)
				File targetFile = new File(status.getPath());
	    		String parent = targetFile.getParent();
				Number restoreRev = null;
				String delFile = null;

				// Get all log messages since last update
				LogMessage[] logMessages = client.logMessages(parent,
						Revision.BASE, Revision.HEAD, false, true);
				
				// need to translate svn URLs from ChangePath to wc paths, so we
				// have to find out about the working copy root to see what the
				// URL prefix of the checkout is
				File wcDir = new File(status.getPath());
				if (tempCopy.isFile()) {
				    wcDir = wcDir.getParentFile();
				}
				File wcRoot = SVNAdminDirectoryLocator.findWCRoot(wcDir);
				Info info = client.info(wcRoot.getPath());
                
                // eg. http://svn.server.com/team1/trunk/my%20path
                String wcRootURL = info.getUrl();
                // eg. http://svn.server.com/team1
                String repoRootURL = info.getRepository();
                // eg. /trunk/my%20path
                String checkoutPrefixURL = wcRootURL.substring(repoRootURL.length());
                // eg. /trunk/my path
                String checkoutPrefix = SVNEncodingUtil.uriDecode(checkoutPrefixURL);
                
                // eg. /home/peter/checkout/team1
                String wcRootPath = wcRoot.getPath();
                // eg. /home/peter/checkout/team1/dir/file.txt
                String currentPath = status.getPath();
                // eg. /dir/file.txt (the one to look for in the ChangePaths below)
                String relativePath = currentPath.substring(wcRootPath.length());
                
				// Look for revision in which the file/dir was (last) deleted
				for (LogMessage logMessage : logMessages) {
					for (ChangePath changePath : logMessage.getChangedPaths()) {
					    
					    // eg. /trunk/my path/dir/file.txt
					    String cp = changePath.getPath();
                        // eg. /dir/file.txt
					    String relativeChangePath = cp.substring(checkoutPrefix.length());
					    
					    if (relativePath.equals(relativeChangePath) && changePath.getAction() == 'D') {
                            // want to restore last revision before deletion
                            restoreRev = new Number(logMessage.getRevision().getNumber()-1);
                            delFile    = cp;
                            log.debug("found revision of deletion: '" + changePath.getPath()
                                    + "' was deleted in revision "
                                    + logMessage.getRevision().toString());
					    }
					}
				}
				
				// Restore deleted version with version history
				if(restoreRev != null) {
				    log.debug("copy " + repoRootURL + delFile + " to " + status.getPath() +
				            ", restoreRev " + restoreRev);
				    
					client.copy(repoRootURL + delFile, status.getPath(), null, restoreRev);
				} else {
					log.error("Failed to restore deleted version of " + status.getPath());
				}

	    		// 2. Redo changes by replacing with local files (move B => A)
	    		File destination = new File(status.getPath());
    			if (tempCopy.isFile()) {
    			    log.debug("copy file from " + tempCopy + " to " + destination);
    				FileUtils.copyFile(tempCopy, destination);
    				tempCopy.delete();
    			} else {
                    log.debug("copy dir from " + tempCopy + " to " + destination);
    				FileUtils.copyDirectory(tempCopy, destination);
    				FileUtils.deleteDirectory(tempCopy);
    			}

	    		// 3. Re-add locally added files
	    		// Cannot just add directories recursively since the copy
				// operation above automatically adds them and adding them twice
				// results in an error. So just add files that were added
				// locally.
	    		if(otherMods != null) {
	    			for(Status s : otherMods) {
	    				if(s.getTextStatus() == StatusKind.added) {
	    				    log.debug("re-adding " + s.getPath());
	    					client.add(s.getPath(), false);
	    				}
	    			}
	    		}
	    	}
            
            break;
        }
	}

	public void accept(ConflictHandler handler) throws CancelException {
		handler.handle(this);
	}

	/**
	 * Resolve the conflict by deleting the resource and throwing away the
	 * modifications, whether the modifications were made locally and the
	 * deletion was done remotely or vice-versa. Note that the modifications
	 * will be lost!
	 */
	public void doDelete() {
		this.action = Action.DELETE;
	}

	/**
	 * Resolve the conflict by deleting only the parts of the affected directory
	 * that were not modified and by keeping the modified stuff. None of the
	 * modifications will be lost but the directory might look quite empty now.
	 */
    public void doOnlyKeepModified() {
        this.action = Action.ONLYKEEPMODIFIED;
    }
    
    /**
     * Resolve the conflict by reverting the delete completely. The
     * modifications will be kept and all other files and directories that were
     * already deleted are restored.
     */
    public void doRevertDelete() {
        this.action = Action.REVERTDELETE;
    }
    
	public String toString() {
		return "Delete/Modification Conflict: " + status.getPath() + (action == Action.UNKNOWN ? "" : " " + action.name());
	}

    public List<Status> getOtherMods() {
        return otherMods;
    }

    /**
     * True if status refers to a local delete and getOtherMods() to remote
     * modifications.
     */
    public boolean isLocalDelete() {
        return localDelete;
    }

    /**
     * True if status refers to a remote delete and getOtherMods() to local
     * modifications.
     */
    public boolean isRemoteDelete() {
        return !localDelete;
    }

    @Override
    public ChangeStatus getChangeStatus() {
        if(localDelete)
            return ChangeStatus.DELETED;
        else
            return ChangeStatus.MODIFIED;
    }

    @Override
    public String getLongDescription() {
        if (otherMods == null) { // only single files involved
            if (localDelete) { // local DELETED, remote MODIFIED
                return I18N.get("This remotely modified file was deleted locally. " +
                        "You will need to resolve the conflict.");
            } else { // local MODIFIED, remote DELETED
                return I18N.get("This remotely deleted file was modified locally. " +
                        "You will need to resolve the conflict.");
            }
        } else { // directories involved
            if (localDelete) { // local dir DELETED, remote MODIFIED
                return I18N.get("This locally deleted directory contains remote changes. " +
                        "You will need to resolve the conflict.");
            } else { // local MODIFIED, remote dir DELETED
                return I18N.get("This remotely deleted directory contains local changes. " +
                        "You will need to resolve the conflict.");
            }
        }
    }
}
