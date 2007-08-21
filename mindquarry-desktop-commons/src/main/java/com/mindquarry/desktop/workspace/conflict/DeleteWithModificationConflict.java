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
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.NodeKind;
import org.tigris.subversion.javahl.Status;

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
	
	public DeleteWithModificationConflict(boolean localDelete, Status status, List<Status> otherMods) {
		super(status);
		this.localDelete = localDelete;
		this.otherMods = otherMods;
	}

	public void beforeUpdate() throws ClientException {
        // NOTE: here we could implement a fast-path avoiding the download
        // of the new files by simply deleting the folder on the server
        // before we run the update
	    // if (action == Action.DELETE) {
        // nothing to do here, we have to wait for the update and delete
        // the folder again (svn will recreate it due to the remote mods)
	    // }

    	// TODO: check correctness
	    if (action == Action.REVERTDELETE) {	 
	    	if (localDelete) {   	
		    	// revert local delete
	    		client.revert(status.getPath(), true);
	    	} else {
	    		// copy A => B (TODO: unique name)
	    		File source      = new File(status.getPath());
	    		File destination = new File(source.getParent()+"/__"+source.getName());
	    		
	    		try {
					FileUtils.copyDirectory(source, destination);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// TODO: remove .svn directories from copy
				removeDotSVNDirectories(destination.getPath());
	    		
	    		// svn revert A
				client.revert(status.getPath(), true);
	    		
	    		// rm -rf A
				try {
					FileUtils.deleteDirectory(source);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
	        
	        // status probably missing (not svn deleted yet) because it's a conflict
	    }
	}

	private void removeDotSVNDirectories(String path) {
		File[] allDirs = new File(path).listFiles(new FileFilter() {
			public boolean accept(File arg0) {
				return arg0.isDirectory();
			}});
		for(File dir : allDirs) {
			if(dir.getName().compareTo(".svn") == 0) {
				// delete .svn directories
				try {
					FileUtils.deleteDirectory(dir);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				// recurse
				removeDotSVNDirectories(dir.getPath());
			}
		}
	}

	public void afterUpdate() throws ClientException {
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
			if (file.isDirectory()) {
				try {
    				FileUtils.deleteDirectory(file);
				} catch (IOException e) {
	    			log.error("deleting directory failed.");
	    			// TODO: callback for error handling
	    			System.exit(-1);
				}
			}
			else {
				if (!file.delete()) {
					log.error("deleting file failed.");
					// TODO: callback for error handling
					System.exit(-1);
				}
			}
            
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
            log.info("reverting delete: " + status.getPath());

            // TODO: revert deleted status
            // the following doesn't restore the already deleted files
//	    	// remote deletion => re-add files 
//	    	if (localDelete == false) {
//	    		client.add(status.getPath(), false);
//	    		for(Status mod : otherMods) {
//	    			client.add(mod.getPath(), false);
//	    		}
//	    	}
 
	    	if (localDelete == false) {
//	    		// Remote deletion of locally modified file: update removes file
//	    		// from versioning, so re-add it here
//	    		if (status.getNodeKind() == NodeKind.file)
//	    			client.add(status.getPath(), false);
	    		

	    		// TODO: svn copy --old-version A
	    		// TODO: copy -R B A
	    		// TODO: svn add A
	    	}
            
            break;
        }
	}

	public void accept(ConflictHandler handler) throws CancelException {
		handler.handle(this);
	}

	public void doDelete() {
		this.action = Action.DELETE;
	}
	
    public void doOnlyKeepModified() {
        this.action = Action.ONLYKEEPMODIFIED;
    }
    
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
}
