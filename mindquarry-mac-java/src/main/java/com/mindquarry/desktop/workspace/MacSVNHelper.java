package com.mindquarry.desktop.workspace;


import org.tigris.subversion.javahl.NotifyInformation;
import org.tigris.subversion.javahl.Status;

import com.mindquarry.desktop.workspace.deprecated.SVNHelper;

/**
 * Specilization of the {@link SVNHelper} for Mac OS related clients.
 * 
 * @author <a href="mailto:jonas(at)metaquark(dot)de">Jonas Witt</a>
 */
public class MacSVNHelper extends SVNHelper {
    public MacSVNHelper(String repositoryURL, String localPath,
            String username, String password) {
    super(repositoryURL, localPath, username, password);
        if (repositoryURL==null) {
          throw new RuntimeException("Repository URL cannot be null");
        }
        if (localPath==null) {
          throw new RuntimeException("localPath cannot be null");
        }
        if (username==null) {
          throw new RuntimeException("username cannot be null");
        }
        if (repositoryURL==null) {
          throw new RuntimeException("password cannot be null");
        }
    }

    public void onNotify(NotifyInformation info) {
        System.out.println("mac notify: " + info.getPath());
    }

    protected int resolveConflict(Status status) {
        return CONFLICT_OVERRIDE_FROM_WC;
    }

    protected native String getCommitMessage();

}
