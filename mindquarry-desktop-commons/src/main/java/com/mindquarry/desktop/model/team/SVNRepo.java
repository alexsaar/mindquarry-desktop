package com.mindquarry.desktop.model.team;

import java.io.File;

import com.mindquarry.desktop.preferences.profile.Profile;
import com.mindquarry.desktop.workspace.SVNSynchronizer;
import com.mindquarry.desktop.workspace.conflict.ConflictHandler;


public class SVNRepo extends Team {

	private String id;
	private String svnURL;
	private String localPath;
	private String username;
	private String password;

	public SVNRepo(String id, String svnURL, String localPath, String username, String password) {
		this.id = id;
		this.svnURL = svnURL;
		this.localPath = localPath;
		this.username = username;
		this.password = password;
	}

	@Override
    public boolean dirExists(Profile profile) {
    	File teamDir = new File(getLocalPath());
    	return teamDir.exists();
    }
    
	@Override
    public SVNSynchronizer createSynchronizer(Profile profile, ConflictHandler handler) {
    	return new SVNSynchronizer(
    			getWorkspaceURL(),
    			getLocalPath(),
    			getUsername(),
    			getPassword(),
    			handler
    		);
    }

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return id;
	}

	@Override
	public String getWorkspaceURL() {
		return svnURL;
	}

	@Override
	public String toString() {
		return id;
	}

	public String getLocalPath() {
		return localPath;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

}
