package com.mindquarry.desktop.client.workspace;

import org.junit.Before;
import org.junit.Test;
import org.tigris.subversion.javahl.Status;
import org.tmatesoft.svn.core.javahl.SVNClientImpl;

public class SVNHelperTest {
	private String repositoryURL = "https://secure.mindquarry.com/repos/test/trunk";

    private String localPath = "/home/vsaar/work/test";

    private String username = "tester";

    private String password = "sec4561";

    private SVNClientImpl svn;

	@Before
	public void setUp() throws Exception {
		svn = SVNClientImpl.newInstance();
		
		svn.username(username);
		svn.password(password);
	}
	
	@Test
	public void testPathMatching() {
		try {
			Status[] localStatus = svn.status(localPath, true, false, true);
			Status[] remoteStatus = svn.status(localPath, true, true, false);
			
			for (Status remoteStat : remoteStatus) {
				System.out.println(remoteStat.getUrl());
				System.out.println(remoteStat.getRepositoryTextStatus());
				
				for(Status localStat : localStatus) {
					String localUrl = localStat.getUrl();
					String remoteUrl = remoteStat.getUrl();
					if(localUrl.equals(remoteUrl)) {
						System.err.println(localStat.getUrl());
						break;
					}
				}
			}
		}
		catch(Exception e) {
		}
	}
}
