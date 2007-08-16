package com.mindquarry.desktop.workspace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Before;
import org.junit.Test;
import org.tigris.subversion.javahl.Notify2;
import org.tigris.subversion.javahl.NotifyInformation;

import com.mindquarry.desktop.workspace.conflict.AddConflict;
import com.mindquarry.desktop.workspace.conflict.AddInDeletedConflict;
import com.mindquarry.desktop.workspace.conflict.ConflictHandler;
import com.mindquarry.desktop.workspace.conflict.DeleteWithModificationConflict;
import com.mindquarry.desktop.workspace.exception.CancelException;

public class SVNSynchronizerTest implements Notify2, ConflictHandler {
	private String repositoryURL = "https://secure.mindquarry.com/repos/test/trunk";

	private String localPath = "C:\\Dokumente und Einstellungen\\alexs\\Eigene Dateien\\tmp\\svn-test";
    //private String localPath = "/Users/alex/Mindquarry/work/checkout/super/";

	private String username = "tester";
    //private String username = "admin";

	private String password = "sec4561";
    //private String password = "admin";

	private SVNSynchronizer helper;

	@Before
	public void setUp() throws Exception {
		helper = new SVNSynchronizer(repositoryURL, localPath, username, password,
				this);
	}

	@Test
	public void testSynchronize() {
		helper.synchronize(this);
	}

	public void onNotify(NotifyInformation info) {
		System.out.println(SVNSynchronizer.notifyToString(info));
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
		System.out.print("Rename locally added file/folder to: ");
		// FIXME: check for non-existing file/foldername
		conflict.doRename(readLine());
	}

	public void visit(AddInDeletedConflict conflict)
			throws CancelException {
		conflict.doReAdd();
	}

    public void visit(DeleteWithModificationConflict conflict)
            throws CancelException {
        conflict.doKeepAdded();
    }
}
