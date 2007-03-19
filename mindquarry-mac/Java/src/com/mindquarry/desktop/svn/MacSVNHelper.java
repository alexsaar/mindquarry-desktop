package com.mindquarry.desktop.svn;

import org.tigris.subversion.javahl.Status;
import org.tigris.subversion.javahl.NotifyInformation;

import org.tigris.subversion.javahl.ClientException;

public class MacSVNHelper extends SVNHelper {
	
	public MacSVNHelper(String repositoryURL, String localPath, String username, String password) {
		super(repositoryURL, localPath, username, password);
	}
	
	public void onNotify(NotifyInformation info) {
//		System.out.println("mac notify: " + info.getPath());
	}
	
	protected int resolveConflict(Status status) {
		return CONFLICT_OVERRIDE_FROM_WC;
	}
	
	protected native String getCommitMessage();
	
}
