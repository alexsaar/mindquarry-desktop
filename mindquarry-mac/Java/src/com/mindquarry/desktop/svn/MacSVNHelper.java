package com.mindquarry.desktop.svn;

import org.tigris.subversion.javahl.Status;
import org.tigris.subversion.javahl.NotifyInformation;

import org.tigris.subversion.javahl.ClientException;

public class MacSVNHelper extends SVNHelper {
	
	public static void main(String[] args) throws ClientException {
		MacSVNHelper helper = new MacSVNHelper("file:///var/mindquarry/svn/gnaateam/trunk", "/Users/jonas/Desktop/MQSVN_new/First Team", null, null);
		helper.update();
		
		helper.getLocalChanges();
		
		helper.update();
	}
	
	public MacSVNHelper(String repositoryURL, String localPath, String username, String password) {
		super(repositoryURL, localPath, username, password);
	}
	
	public void onNotify(NotifyInformation info) {
		System.out.println("mac notify: " + info.getPath());
	}
	
	protected int resolveConflict(Status status) {
		return CONFLICT_OVERRIDE_FROM_WC;
	}
	
	protected String getCommitMessage() {
		return "generic commit message";
	}
	
}
