package com.mindquarry.desktop.client.workspace;

import org.junit.Before;
import org.junit.Test;
import org.tigris.subversion.javahl.Status;
import org.tmatesoft.svn.core.javahl.SVNClientImpl;

public class SVNHelperTest {
	private String repositoryURL = "https://secure.mindquarry.com/repos/test/trunk";

	private String localPath = "C:\\Dokumente und Einstellungen\\alexs\\Eigene Dateien\\tmp\\svn-test";

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
			Status[] localStatus = svn.status(localPath, true, false, false);
			Status[] remoteStatus = svn.status(localPath, true, true, false);

			// check remote status
			for (Status remoteStat : remoteStatus) {
				for (Status localStat : localStatus) {
					String localUrl = localStat.getUrl();
					String remoteUrl = remoteStat.getUrl();

					if (localUrl.equals(remoteUrl)) {
						System.out.println("Local URL: " + localUrl);
						System.out.println("Remote URL: " + remoteUrl);
						System.out.println("Status: "
								+ Status.Kind.getDescription(remoteStat
										.getRepositoryTextStatus()));
						System.out.println("-----------");
						break;
					}
				}
			}
		} catch (Exception e) {
		}
	}
}
