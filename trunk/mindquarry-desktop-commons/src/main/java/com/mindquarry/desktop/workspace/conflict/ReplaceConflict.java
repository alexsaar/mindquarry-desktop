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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.Status;
import org.tigris.subversion.javahl.StatusKind;
import org.tigris.subversion.javahl.Status.Kind;

import com.mindquarry.desktop.workspace.exception.CancelException;

/**
 * Represents the following conflicts replaced/modified, modified/replaced and
 * replaced/replaced.
 * 
 * @author <a href="mailto:victor.saar@mindquarry.com">Victor Saar</a>
 * @author <a href="mailto:klimetschek@mindquarry.com">Alexander Klimetschek</a>
 */
public class ReplaceConflict extends Conflict {
	private Action action = Action.UNKNOWN;
	
	public enum Action {
        /**
         * Indicating no conflict solution action was chosen yet.
         */
		UNKNOWN,
		/**
		 * Rename locally to a different file/folder name to avoid the conflict.
		 */
		RENAME,
		/**
		 * Replace the local file with the remotely added one, overwriting the local data.
		 */
		REPLACE;
	}
	
	private String newName;
    private List<Status> localChildren;
    private List<Status> remoteChildren;
    
    /**
     * The folder in which the added object (status) lies in. Needed to check
     * for rename conflicts.
     */
    private File folder;
    /**
     * All objects that are added remotely in the same folder as the object to
     * be able to check for rename conflicts.
     */
    private List<String> remoteAddedInFolder = null;
	
	public ReplaceConflict(Status status, List<Status> localChildren, List<Status> remoteChildren) {
		super(status);
		this.localChildren = localChildren;
		this.remoteChildren = remoteChildren;
		
		this.folder = new File(status.getPath()).getParentFile();
	}

	public void beforeUpdate() throws ClientException {
		File file = new File(status.getPath());
		
		switch (action) {
		case UNKNOWN:
			// client did not set a conflict resolution
			log.error("AddConflict with no action set: " + status.getPath());
			break;
			
		case RENAME:
			log.info("renaming to " + newName);
			
            for (Status s : getLocalChildren()) {
                log.debug("****** " + Kind.getDescription(s.getTextStatus())  + " " + s.getPath());
            }
            
            File source      = new File(status.getPath());
            File destination = new File(source.getParent() + "/" + newName);
            
            try {
                if(source.isDirectory()) {
                    FileUtils.copyDirectory(source, destination);

                    removeDotSVNDirectories(destination.getPath());
                } else {
                    FileUtils.copyFile(source, destination);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            client.add(destination.getPath(), true, true);
            client.remove(new String[] { file.getPath() }, null, true);
			
			break;
			
		case REPLACE:
			log.info("replacing with new file/folder from server: " + status.getPath());
			
            client.revert(file.getPath(), true);
            
            if (status.getRepositoryTextStatus() == StatusKind.replaced) {
                try {
                    FileUtils.forceDelete(file);
                } catch (IOException e) {
                    log.error("deleting failed.");
                    // TODO: callback for error handling
                    System.exit(-1);
                }
            }
            
			break;
		}
	}

    private void removeDotSVNDirectories(String path) {
        File[] allDirs = new File(path).listFiles(new FileFilter() {
            public boolean accept(File arg0) {
                return arg0.isDirectory();
            }});
        
        if (allDirs != null) {
            for(File dir : allDirs) {
                if(dir.getName().compareTo(".svn") == 0) {
                    // delete .svn directories
                    try {
                        FileUtils.forceDelete(dir);
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
    }

	public void afterUpdate() {
		// nothing to do here
	}

	public void accept(ConflictHandler handler) throws CancelException {
		handler.handle(this);
	}
	
	/**
	 * Call this during conflict resolving before a call to doRename() to check
	 * if the given name is possible without another conflict. Not possible when
	 * another file or folder exists in the same directory or when a file or
	 * folder with that name will be added during the next update.
	 * 
	 * @param newName the new name for the conflicted file/folder to check for
	 * @return true if the name can be used, false if not
	 * @throws ClientException this method must check the svn client for info
	 * about newly added files in the next update; simply catch that exception
	 * and throw a CancelException in the handle method
	 */
	public boolean isRenamePossible(String newName) throws ClientException {
	    if (remoteAddedInFolder == null) {
	        // lazily retrieve possible conflicts with other remotely added files
	        remoteAddedInFolder = new ArrayList<String>();
	        for (Status s : client.status(folder.getAbsolutePath(), true, true, false)) {
	            // simply add all files that are either locally or remotely in
	            // a non-normal state - this will include delete cases, but
	            // those files exist locally anyway until the update is done
	            remoteAddedInFolder.add(new File(s.getPath()).getName());
	        }
	    }
	    
	    // TODO: remember all choosen newNames for a certain directory in a
	    // static list and check against them (see above when renameTo fails)
	    
	    // TODO: check for relative path names (eg. ../../newfile) and return false
	    
	    // such a file must not exist locally yet and it must not be added
	    // during the next update (that would be another conflict then)
	    boolean result = !(new File(folder, newName).exists() || remoteAddedInFolder.contains(newName));
	    
	    if (!result) {
	        System.out.println("Cannot rename to '" + newName + "'");
	    }
	    return result;
	}

	public void doRename(String newName) {
	    // TODO: double check here? what if it fails? throw exception? return false?
//	    if (!isRenamePossible(newName)) {
//	        
//	    }
		this.action = Action.RENAME;
		this.newName = newName;
	}
	
	public void doReplace() {
		this.action = Action.REPLACE;
	}
	
	public String toString() {
		return "Replaced/Modified/Replaced Conflict: " + status.getPath() + (action == Action.UNKNOWN ? "" : " " + action.name());
	}

    public List<Status> getLocalChildren() {
        return localChildren;
    }

    public List<Status> getRemoteChildren() {
        return remoteChildren;
    }
}
