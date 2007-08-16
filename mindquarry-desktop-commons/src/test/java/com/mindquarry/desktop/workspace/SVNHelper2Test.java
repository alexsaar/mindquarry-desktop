package com.mindquarry.desktop.workspace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Before;
import org.junit.Test;
import org.tigris.subversion.javahl.Notify2;
import org.tigris.subversion.javahl.NotifyInformation;

public class SVNHelper2Test implements Notify2, ConflictHandler {
	private String repositoryURL = "https://secure.mindquarry.com/repos/test/trunk";

	private String localPath = "C:\\Dokumente und Einstellungen\\alexs\\Eigene Dateien\\tmp\\svn-test";

	private String username = "tester";

	private String password = "sec4561";

	private SVNHelper2 helper;

	@Before
	public void setUp() throws Exception {
		helper = new SVNHelper2(repositoryURL, localPath, username, password,
				this);
	}

	@Test
	public void testSynchronize() {
		helper.synchronize(this);
	}

	public void onNotify(NotifyInformation info) {
		System.out.println(SVNHelper2.notifyToString(info));
	}
	
	public String readLine() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try {
			return reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}

	public void visit(AddConflict conflict) throws CancelException {
		System.out.println("Rename locally added file to: ");
		// FIXME: check for non-existing file/foldername
		conflict.doRename(readLine());
	}
}
