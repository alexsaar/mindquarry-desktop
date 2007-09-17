package com.mindquarry.desktop.workspace.conflict;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.Status;

/**
 * Base for all conflicts that have a rename action.
 * 
 * @author <a href="mailto:victor(dot)saar(at)mindquarry(dot)com">Victor Saar</a>
 */
public abstract class RenamingConflict extends Conflict {
    /**
     * The folder in which the added object (status) lies in. Needed to check
     * for rename conflicts.
     */
    protected File folder;
    /**
     * All objects that are added remotely in the same folder as the object to
     * be able to check for rename conflicts.
     */
    protected List<String> remoteAddedInFolder = null;
    
    public RenamingConflict(Status status) {
        super(status);
        this.folder = new File(status.getPath()).getParentFile();
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
        // new file name must be available locally
        if (new File(folder, newName).exists()) {
            System.out.println("Cannot rename to '" + newName + "' (exists locally)");
            return false;
        }

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
        
        // such a file must not be added during the next update (that would be
        // another conflict then)        
        if (remoteAddedInFolder.contains(newName)) {
            System.out.println("Cannot rename to '" + newName + "' (exists on the server)");
            return false;
        }

        return true;
    }
}
