package com.mindquarry.desktop.model.team;

import java.util.ArrayList;
import java.util.List;

import com.mindquarry.desktop.preferences.profile.Profile.SVNRepoData;

/**
 * Maps a list of plain SVN repos onto the TeamList interface.
 * Does not use the ModelBase class functionality it only returns
 * SVNRepo classes from getTeams().
 * 
 * @author alex
 *
 */
public class SVNRepoList extends TeamList {

	private List<Team> svnRepos;

	public SVNRepoList(List<SVNRepoData> svnRepoDatas) {
		svnRepos = new ArrayList<Team>();
		// convert from SVNRepoData to SVNRepo
		for (SVNRepoData data : svnRepoDatas) {
			svnRepos.add(new SVNRepo(data.id, data.svnURL, data.localPath, data.username, data.password));
		}
	}

	@Override
	public List<Team> getTeams() {
		return svnRepos;
	}
}
