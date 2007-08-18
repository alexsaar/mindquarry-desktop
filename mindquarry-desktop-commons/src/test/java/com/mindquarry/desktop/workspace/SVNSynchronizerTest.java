package com.mindquarry.desktop.workspace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Before;
import org.junit.Test;
import org.tigris.subversion.javahl.Notify2;
import org.tigris.subversion.javahl.NotifyInformation;
import org.tigris.subversion.javahl.Status;
import org.tigris.subversion.javahl.Status.Kind;

import com.mindquarry.desktop.workspace.conflict.AddConflict;
import com.mindquarry.desktop.workspace.conflict.ModificationInDeletedConflict;
import com.mindquarry.desktop.workspace.conflict.ConflictHandler;
import com.mindquarry.desktop.workspace.conflict.DeleteWithModificationConflict;
import com.mindquarry.desktop.workspace.exception.CancelException;

public class SVNSynchronizerTest implements Notify2, ConflictHandler {
	private String repositoryURL = "https://secure.mindquarry.com/repos/test/trunk";

	//private String localPath = "C:\\Dokumente und Einstellungen\\alexs\\Eigene Dateien\\tmp\\svn-test";
    private String localPath = "/Users/alex/Mindquarry/work/checkout/super/";

	//private String username = "tester";
    private String username = "admin";

	//private String password = "sec4561";
    private String password = "admin";

	private SVNSynchronizer helper;

	@Before
	public void setUp() throws Exception {
		helper = new SVNSynchronizer(repositoryURL, localPath, username, password, this);
		helper.setNotifyListener(this);
	}

	@Test
	public void testSynchronize() {
		helper.synchronize();
	}

	public void onNotify(NotifyInformation info) {
        System.out.println("SVNKIT " + SVNSynchronizer.notifyToString(info));
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

	public void handle(AddConflict conflict) throws CancelException {
        System.out.println("Following options re(N)ame, (R)eplace: ");
        for (Status s : conflict.getLocalAdded()) {
            System.out.println("local added: " + s.getPath());
        }
        for (Status s : conflict.getRemoteAdded()) {
            System.out.println("remote added: " + s.getPath());
        }
		System.out.print("Rename locally added file/folder to: ");
		// FIXME: check for non-existing file/foldername
		conflict.doRename(readLine());
	}

	public void handle(ModificationInDeletedConflict conflict)
			throws CancelException {
        System.out.println("Following options (R)eadd, (D)elete, [(M)ove]: ");
		conflict.doReAdd();
	}

    public void handle(DeleteWithModificationConflict conflict)
            throws CancelException {
        for (Status s : conflict.getOtherMods()) {
            System.out.println("remote " + Kind.getDescription(s.getRepositoryTextStatus()) + " " + s.getPath());
        }
        System.out.println("Following options (K)eep modified, (D)elete, (R)evert delete: ");
        conflict.doOnlyKeepModified();
    }
}