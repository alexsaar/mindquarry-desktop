package com.mindquarry.desktop.workspace;


import org.tigris.subversion.javahl.NotifyInformation;
import org.tigris.subversion.javahl.Status;

import com.mindquarry.desktop.workspace.SVNHelper;

/**
 * Specilization of the {@link SVNHelper} for Mac OS related clients.
 * 
 * @author <a href="mailto:jonas(at)metaquark(dot)de">Jonas Witt</a>
 */
public class MacSVNHelper extends SVNHelper {
    public MacSVNHelper(String repositoryURL, String localPath,
            String username, String password) {
        super(repositoryURL, localPath, username, password);
    }

    public void onNotify(NotifyInformation info) {
        System.out.println("mac notify: " + info.getPath());
    }

    protected int resolveConflict(Status status) {
        return CONFLICT_OVERRIDE_FROM_WC;
    }

    protected native String getCommitMessage();

}
