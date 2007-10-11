package com.mindquarry.desktop.workspace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.tigris.subversion.javahl.ClientException;
import org.tmatesoft.svn.core.SVNException;

import com.mindquarry.desktop.workspace.conflict.AddConflict;
import com.mindquarry.desktop.workspace.conflict.AutomaticConflictHandler;
import com.mindquarry.desktop.workspace.conflict.ConflictHandler;
import com.mindquarry.desktop.workspace.conflict.ContentConflict;
import com.mindquarry.desktop.workspace.conflict.DeleteWithModificationConflict;
import com.mindquarry.desktop.workspace.conflict.ObstructedConflict;
import com.mindquarry.desktop.workspace.conflict.PropertyConflict;
import com.mindquarry.desktop.workspace.conflict.ReplaceConflict;
import com.mindquarry.desktop.workspace.exception.CancelException;
import com.mindquarry.desktop.workspace.exception.SynchronizeException;

public class SVNSynchronizerTest extends SVNTestBase {
	@Test
    public void testMove() throws IOException, ClientException, SynchronizeException {
        setupZipTestWithFreshCheckout("move");

        // first create the files/dirs
        touch("file");
        mkdir("dir");
        touch("dir/file");
        mkdir("dir/subdir");
        touch("dir/subdir/file");
        
        // add the files
        add("file");
        add("dir");
        
        client.commit(new String[] { this.wcPath }, "initial add", true);
        
        // then move them
        move("file", "moved_file");
        move("dir", "moved_dir");
        
        SVNSynchronizer helper = setupSynchronizer(new EnsureNoConflictsConflictHandler());

        helper.synchronize();
        
        assertFileMissing("dir");
        assertFileExists("moved_dir");
        
        // TODO: check correct SVN state of all files
        
        cleanupTestOnlyRepo("move");
    }
    
    @Test
    public void testMoveAdded() throws IOException, ClientException, SynchronizeException {
        setupZipTestWithFreshCheckout("move");

        // first create the files/dirs
        touch("file");
        mkdir("dir");
        touch("dir/file");
        mkdir("dir/subdir");
        touch("dir/subdir/file");
        
        // add the files
        add("file");
        add("dir");
        
        // we don't commit but move the files immediately. this simulates a
        // situation with the desktop client when it crashed or stopped after
        // deleteMissingAndAddUnversioned() was done but before the added
        // elements were committed and the added flag transformed into normal
        
        // then move them
        move("file", "moved_file");
        move("dir", "moved_dir");
        
        SVNSynchronizer helper = setupSynchronizer(new EnsureNoConflictsConflictHandler());

        helper.synchronize();
        
        assertFileMissing("dir");
        assertFileExists("moved_dir");
        
        // TODO: check correct SVN state of all files
        
        cleanupTestOnlyRepo("move");
    }
    
    @Test
    public void testMissingDelete() throws SVNException, ClientException, IOException, SynchronizeException {
        setupCleanRepoWithFreshCheckout("missing_delete");
        
        touch("file");
        mkdir("dir");
        touch("dir/file");
        mkdir("dir/subdir");
        touch("dir/subdir/file");
        mkdir("dir/subdir/third");
        touch("dir/subdir/third/file");
        
        add("file");
        add("dir");

        client.commit(new String[] { this.wcPath }, "initial add", true);
        
        // this removes the files/folder but does not call a svn delete
        remove("file");
        remove("dir");
        
        SVNSynchronizer helper = setupSynchronizer(new EnsureNoConflictsConflictHandler());
        helper.synchronize();
        
        assertFileMissing("file");
        assertFileMissing("dir");
        assertFileMissing("dir/file");
        assertFileMissing("dir/subdir");
        assertFileMissing("dir/subdir/file");
        assertFileMissing("dir/subdir/third");
        assertFileMissing("dir/subdir/third/file");
        
        // TODO: check correct SVN state of all files
        
        cleanupTestOnlyRepo("missing_delete");
    }
    
    @Test
	public void testAddConflictDoRename() throws IOException, SynchronizeException {
		setupZipTest("add_add_conflict");
		SVNSynchronizer helper = setupSynchronizer(new AddConflictHandlerMock(wcPath, AddConflict.Action.RENAME));

        helper.synchronize();
		
		// TODO: we need to test the conflict objects: extend AutomaticConflictHandler
		// class with methods testing the fields of the conflict object

		// Test correct working copy contents
        assertFileExists("first");
        assertFileExists("first_renamed_0");
        assertFileExists("second");
        assertFileExists("second_renamed_2/");
        assertFileExists("second_renamed_2/file");
        assertFileExists("second_renamed_2/another_file");
        assertFileExists("third/");
        assertFileExists("third/first");
        assertFileExists("third/second");
        assertFileExists("third/second/first");
        assertFileExists("third_renamed_3");
        assertFileExists("fourth/");
        assertFileExists("fourth/first");
        assertFileExists("fourth/second/");
        assertFileExists("fourth/second/first");
        assertFileExists("fourth_renamed_1/");
        assertFileExists("fourth_renamed_1/file");
        assertFileExists("fourth_renamed_1/different_file");
		
		// TODO: here we have to test if the remote/localAdded fields contain
		// all files/folders of the test zip case
        // TODO: check correct SVN state of all files

        cleanupTest("add_add_conflict");
	}
    
    @Test
    public void testAddConflictDoReplace() throws IOException, SynchronizeException {
        setupZipTest("add_add_conflict");
        SVNSynchronizer helper = setupSynchronizer(new AddConflictHandlerMock(wcPath, AddConflict.Action.REPLACE));

        helper.synchronize();
        
        // TODO: we need to test the conflict objects: extend AutomaticConflictHandler
        // class with methods testing the fields of the conflict object

		// Test correct working copy contents
        assertFileExists("first");
        assertFileExists("second");
        assertFileExists("third/");
        assertFileExists("third/first");
        assertFileExists("third/second");
        assertFileExists("third/second/first");
        assertFileExists("fourth/");
        assertFileExists("fourth/first");
        assertFileExists("fourth/second/");
        assertFileExists("fourth/second/first");
        
        // TODO: here we have to test if the remote/localAdded fields contain
        // all files/folders of the test zip case
        // TODO: check correct SVN state of all files

        cleanupTest("add_add_conflict");
    }

	@Test
	public void testDeletedModifiedConflictDoRevertDelete() throws IOException, ClientException, SynchronizeException  {
		setupZipTest("deleted_modified_conflict");
        SVNSynchronizer helper = setupSynchronizer(new DeleteWithModificationConflictHandlerMock(wcPath, DeleteWithModificationConflict.Action.REVERTDELETE));
		
		// add file here, because having it in the zipped working copy breaks
		// relocating the directory '2 Two', as it does not exist in the
		// repository
		add("2 Two/Added.txt");

		helper.synchronize();

		// Test correct working copy contents
		assertFileExists ("1 One/");
		assertFileExists ("1 One/Added.txt");
		assertFileExists ("1 One/Existing.txt");
		assertFileExists ("1 One/Modified.txt");
		assertFileExists ("2 Two/");
		assertFileExists ("2 Two/Added.txt");
		assertFileExists ("2 Two/Existing.txt");
		assertFileExists ("2 Two/Modified.txt");
		assertFileExists ("3 Three.txt");
		assertFileExists ("4 Four.txt");
		assertFileExists ("Existing.txt");

		// Test correct file contents
		assertFileContains("1 One/Modified.txt", "Modified\r\nModified");
		assertFileContains("2 Two/Modified.txt", "Modified\r\nModified");
		assertFileContains("3 Three.txt", "Modified");
		assertFileContains("4 Four.txt",  "Modified");
		
		// TODO: check correct SVN state of all files

        cleanupTest("deleted_modified_conflict");
	}

    @Test
    public void testDeletedModifiedConflictDoRevertDeleteSvnRef() throws IOException, ClientException, SynchronizeException, SVNException  {
        setupCleanRepoWithFreshCheckout("deleted_modified_conflict2");
        
        mkdir("2 Two");
        touch("2 Two/file.txt");
        mkdir("2 Two/subdir");
        touch("2 Two/subdir/foobar.txt");
        
        add("2 Two");
        
        client.commit(new String[] { this.wcPath }, "initial add", true);
        
        // TODO: checkout subdir
        checkoutRemote("deleted_modified_conflict2");
        
        // remote setup: delete dir
        delRemote("2 Two");
        
        client.commit(new String[] { this.wcPathRemote }, "remote delete", true);
        
        // local setup: modify contents
        
        modify("2 Two/file.txt");
        touch("2 Two/another.txt");
        add("2 Two/another.txt");
        
        SVNSynchronizer helper = setupSynchronizer(new DeleteWithModificationConflictHandlerMock(wcPath, DeleteWithModificationConflict.Action.REVERTDELETE));
        helper.synchronize();
        
        // TODO: check file existence / non-existence
        // TODO: check correct SVN state of all files

        cleanupTestOnlyRepo("deleted_modified_conflict2");
    }
    
    @Test
    public void testDeletedModifiedConflictDoOnlyKeepModified() throws IOException, ClientException, SynchronizeException  {
        setupZipTest("deleted_modified_conflict");
        SVNSynchronizer helper = setupSynchronizer(new DeleteWithModificationConflictHandlerMock(wcPath, DeleteWithModificationConflict.Action.ONLYKEEPMODIFIED));

		// add file here, because having it in the zipped working copy breaks
		// relocating the directory '2 Two', as it does not exist in the
		// repository
        add("2 Two/Added.txt");

        helper.synchronize();

		// Test correct working copy contents
		assertFileExists ("1 One/");
		assertFileExists ("1 One/Added.txt");
		assertFileExists ("1 One/Existing.txt");
		assertFileExists ("1 One/Modified.txt");
		assertFileExists ("2 Two/");
		assertFileExists ("2 Two/Added.txt");
		assertFileMissing("2 Two/Existing.txt");
		assertFileExists ("2 Two/Modified.txt");
		assertFileExists ("3 Three.txt");
		assertFileExists ("4 Four.txt");
		assertFileExists ("Existing.txt");

		// Test correct file contents
		assertFileContains("1 One/Modified.txt", "Modified\r\nModified");
		assertFileContains("2 Two/Modified.txt", "Modified\r\nModified");
		assertFileContains("3 Three.txt", "Modified");
		assertFileContains("4 Four.txt",  "Modified");

		// TODO: check correct SVN state of all files

        cleanupTest("deleted_modified_conflict");
    }

    @Test
    public void testDeletedModifiedConflictDoDelete() throws IOException, ClientException, SynchronizeException  {
        setupZipTest("deleted_modified_conflict");
        SVNSynchronizer helper = setupSynchronizer(new DeleteWithModificationConflictHandlerMock(wcPath, DeleteWithModificationConflict.Action.DELETE));

		// add file here, because having it in the zipped working copy breaks
		// relocating the directory '2 Two', as it does not exist in the
		// repository
        add("2 Two/Added.txt");

        helper.synchronize();

		// Test correct working copy contents
		assertFileMissing("1 One/");
		assertFileMissing("1 One/Added.txt");
		assertFileMissing("1 One/Existing.txt");
		assertFileMissing("1 One/Modified.txt");
		assertFileMissing("2 Two/");
		assertFileMissing("2 Two/Added.txt");
		assertFileMissing("2 Two/Existing.txt");
		assertFileMissing("2 Two/Modified.txt");
		assertFileMissing("3 Three.txt");
		assertFileMissing("4 Four.txt");
		assertFileExists ("Existing.txt");

		// TODO: check correct SVN state of all files

        cleanupTest("deleted_modified_conflict");
    }
	
	private void prepareReplaceConflict() throws ClientException, IOException {
        // (things are already deleted, but not added)
        
        // files:
        
        // svn del first_file
        // svn add first_file
        replace("first_file", true);
        
        // vim second_file
        modify("second_file");
        
        // svn del third_file
        // svn add third_file
        replace("third_file", true);
        
        // dirs:
        
        // svn del first_dir
        // svn add first_dir
        replace("first_dir", false);
        // touch first_dir/neu
        touch("first_dir/neu");
        // svn add first_dir/neu
        add("first_dir/neu");
        
        // touch second_dir/neu.txt
        touch("second_dir/neu.txt");
        // svn add second_dir/neu.txt
        add("second_dir/neu.txt");
        // vim second_dir/file.txt
        modify("second_dir/file.txt");
        // svn del second_dir/subdir
        del("second_dir/subdir");
        
        // svn del third_dir
        // svn add third_dir
        replace("third_dir", false);
        // touch third_dir/neu
        touch("third_dir/neu");
        // svn add third_dir/neu
        add("third_dir/neu");
    }

    @Test
    public void testReplacedConflictsDoRename() throws IOException, ClientException, SynchronizeException {
        setupZipTest("replaced_conflict");
        SVNSynchronizer helper = setupSynchronizer(new ReplaceConflictHandlerMock(wcPath, ReplaceConflict.Action.RENAME));
        
        prepareReplaceConflict();
        
        helper.synchronize();

        // TODO: check file existence / non-existence
        // TODO: check correct SVN state of all files
        
        cleanupTest("replaced_conflict");
    }
    
    @Test
    public void testReplacedConflictsDoReplace() throws IOException, ClientException, SynchronizeException {
        setupZipTest("replaced_conflict");
        SVNSynchronizer helper = setupSynchronizer(new ReplaceConflictHandlerMock(wcPath, ReplaceConflict.Action.REPLACE));
        
        prepareReplaceConflict();
        
        helper.synchronize();

        // TODO: check file existence / non-existence
        // TODO: check correct SVN state of all files
        
        cleanupTest("replaced_conflict");
    }
    
    private void prepareObstructedAndConflicted() throws IOException {
        // rmdir obstructed_dir
        // touch obstructed_dir
        FileUtils.deleteDirectory(new File(wcPath + "/obstructed_dir"));
        touch("obstructed_dir");
    }
    
    @Test
    public void testObstructedAndConflictedDoRenameAndUseLocal() throws IOException, ClientException, SynchronizeException {
        setupZipTest("obstructed_and_conflicted");
        SVNSynchronizer helper = setupSynchronizer(new ObstructedConflictedHandlerMock(wcPath, ObstructedConflict.Action.RENAME, ContentConflict.Action.USE_LOCAL));
        
        prepareObstructedAndConflicted();
        
        helper.synchronize();
        
        // TODO: check file existence / non-existence
        // TODO: check correct SVN state of all files
        
        cleanupTest("obstructed_and_conflicted");
    }

    @Test
    public void testObstructedAndConflictedDoRenameAndUseRemote() throws IOException, ClientException, SynchronizeException {
        setupZipTest("obstructed_and_conflicted");
        SVNSynchronizer helper = setupSynchronizer(new ObstructedConflictedHandlerMock(wcPath, ObstructedConflict.Action.RENAME, ContentConflict.Action.USE_REMOTE));
        
        prepareObstructedAndConflicted();
        
        helper.synchronize();
        
        // TODO: check file existence / non-existence
        // TODO: check correct SVN state of all files
        
        cleanupTest("obstructed_and_conflicted");
    }

    /*
    @Test
    public void testObstructedAndConflictedDoRenameAndMerge() throws IOException, ClientException {
        setupTest("obstructed_and_conflicted");
        SVNSynchronizer helper = setupSynchronizer(new ObstructedConflictedHandlerMock(wcPath, ObstructedConflict.Action.RENAME, ContentConflict.Action.MERGE));
        
        prepareObstructedAndConflicted();
        
        helper.synchronize();
        
        // TODO: check file existence / non-existence
        // TODO: check correct SVN state of all files
        
        cleanupTest("obstructed_and_conflicted");
    }
    */

    @Test
    public void testObstructedAndConflictedDoRevertAndUseLocal() throws IOException, ClientException, SynchronizeException {
        setupZipTest("obstructed_and_conflicted");
        SVNSynchronizer helper = setupSynchronizer(new ObstructedConflictedHandlerMock(wcPath, ObstructedConflict.Action.REVERT, ContentConflict.Action.USE_LOCAL));
        
        prepareObstructedAndConflicted();
        
        helper.synchronize();
        
        // TODO: check file existence / non-existence
        // TODO: check correct SVN state of all files
        
        cleanupTest("obstructed_and_conflicted");
    }

    @Test
    public void testObstructedAndConflictedDoRevertAndUseRemote() throws IOException, ClientException, SynchronizeException {
        setupZipTest("obstructed_and_conflicted");
        SVNSynchronizer helper = setupSynchronizer(new ObstructedConflictedHandlerMock(wcPath, ObstructedConflict.Action.REVERT, ContentConflict.Action.USE_REMOTE));
        
        prepareObstructedAndConflicted();
        
        helper.synchronize();
        
        // TODO: check file existence / non-existence
        // TODO: check correct SVN state of all files
        
        cleanupTest("obstructed_and_conflicted");
    }

    /*
    @Test
    public void testObstructedAndConflictedDoRevertAndMerge() throws IOException, ClientException {
        setupTest("obstructed_and_conflicted");
        SVNSynchronizer helper = setupSynchronizer(new ObstructedConflictedHandlerMock(wcPath, ObstructedConflict.Action.REVERT, ContentConflict.Action.MERGE));
        
        prepareObstructedAndConflicted();
        
        helper.synchronize();
        
        // TODO: check file existence / non-existence
        // TODO: check correct SVN state of all files
        
        cleanupTest("obstructed_and_conflicted");
    }
    */

	// TODO: test ignore of Thumbs.db/.DS_Store
	// - simple test if it gets ignored (no ignored set previously)
	// - test with an svn:ignore property already set to check correct incremental setting of that property
    @Test
    public void testPropertyConflictDoUseLocalValue() throws IOException, ClientException, SynchronizeException {
        setupZipTest("property_conflict");
        SVNSynchronizer helper = setupSynchronizer(new PropertyConflictHandlerMock(wcPath, PropertyConflict.Action.USE_LOCAL_VALUE));

        helper.synchronize();
        
        String nl = System.getProperty("line.separator");
        assertEquals("first_local_value", client.propertyGet(wcPath, "mq:first").getValue());
        assertEquals("second_local_value", client.propertyGet(wcPath, "mq:second").getValue());
        assertEquals("temp"+nl+"*.bak"+nl+"*.log"+nl+"tmp"+nl+"bin"+nl, client.propertyGet(wcPath, "svn:ignore").getValue());
        
        cleanupTest("property_conflict");
    }
    
    @Test
    public void testPropertyConflictDoUseRemoteValue() throws IOException, ClientException, SynchronizeException {
        setupZipTest("property_conflict");
        SVNSynchronizer helper = setupSynchronizer(new PropertyConflictHandlerMock(wcPath, PropertyConflict.Action.USE_REMOTE_VALUE));

        helper.synchronize();

        String nl = System.getProperty("line.separator");
        assertEquals("first_remote_value", client.propertyGet(wcPath, "mq:first").getValue());
        assertEquals("second_remote_value", client.propertyGet(wcPath, "mq:second").getValue());
        assertEquals("temp"+nl+"*.bak"+nl+"*.log"+nl+"tmp"+nl+"bin"+nl, client.propertyGet(wcPath, "svn:ignore").getValue());
        
        cleanupTest("property_conflict");
    }
    
    @Test
    public void testPropertyConflictDoUseNewValue() throws IOException, ClientException, SynchronizeException {
        setupZipTest("property_conflict");
        SVNSynchronizer helper = setupSynchronizer(new PropertyConflictHandlerMock(wcPath, PropertyConflict.Action.USE_NEW_VALUE));

        helper.synchronize();

        String nl = System.getProperty("line.separator");
        assertTrue(client.propertyGet(wcPath, "mq:first").getValue().startsWith("shiny new value "));
        assertTrue(client.propertyGet(wcPath, "mq:second").getValue().startsWith("shiny new value "));
        assertEquals("temp"+nl+"*.bak"+nl+"*.log"+nl+"tmp"+nl+"bin"+nl, client.propertyGet(wcPath, "svn:ignore").getValue());
        
        cleanupTest("property_conflict");
    }
    
	private class AddConflictHandlerMock extends AutomaticConflictHandler {
        private AddConflict.Action action;
        
        private int uniqueCounter = 0;
        
	    public AddConflictHandlerMock(String wcPath, AddConflict.Action action) {
            super(wcPath);
            
            this.action = action;
        }
        
        public void handle(AddConflict conflict) {
            printer.printConflict(conflict);
            switch(action) {
            case RENAME: conflict.doRename(new File(conflict.getStatus().getPath()).getName() + "_renamed_" + uniqueCounter++); break;
            case REPLACE: conflict.doReplace(); break;
            }
        }
        
        public void handle(DeleteWithModificationConflict conflict) {
            throw new UnsupportedOperationException("not intended to be used with DeleteWithModificationConflict");
        }
        
        public void handle(ReplaceConflict conflict) {
            throw new UnsupportedOperationException("not intended to be used with ReplaceConflict");
        }
    }
    
    private class DeleteWithModificationConflictHandlerMock extends AutomaticConflictHandler {
        private DeleteWithModificationConflict.Action action;
        
        public DeleteWithModificationConflictHandlerMock(String wcPath, DeleteWithModificationConflict.Action action) {
            super(wcPath);
            
            this.action = action;
        }
        
        public void handle(AddConflict conflict) {
            throw new UnsupportedOperationException("not intended to be used with AddConflict");
        }
        
        public void handle(DeleteWithModificationConflict conflict) {
            printer.printConflict(conflict);
            switch(action) {
            case DELETE: conflict.doDelete(); break;
            case ONLYKEEPMODIFIED: conflict.doOnlyKeepModified(); break;
            case REVERTDELETE: conflict.doRevertDelete(); break;
            }
        }
        
        public void handle(ReplaceConflict conflict) {
            throw new UnsupportedOperationException("not intended to be used with ReplaceConflict");
        }
    }

    private class ReplaceConflictHandlerMock extends AutomaticConflictHandler {
        private ReplaceConflict.Action action;
        
        private int uniqueCounter = 0;

        public ReplaceConflictHandlerMock(String wcPath, ReplaceConflict.Action action) {
            super(wcPath);
            
            this.action = action;
        }
        
        public void handle(AddConflict conflict) {
            throw new UnsupportedOperationException("not intended to be used with AddConflict");
        }
        
        public void handle(ReplaceConflict conflict) {
            printer.printConflict(conflict);
            
            switch(action) {
            case RENAME: conflict.doRename(new File(conflict.getStatus().getPath()).getName() + "_renamed_" + uniqueCounter++); break;
            case REPLACE: conflict.doReplace(); break;
            }
        }
        
        public void handle(DeleteWithModificationConflict conflict) {
            throw new UnsupportedOperationException("not intended to be used with DeleteWithModificationConflict");
        }
    }

    private class PropertyConflictHandlerMock extends AutomaticConflictHandler {
        private PropertyConflict.Action action;
        
        private int uniqueCounter = 0;

        public PropertyConflictHandlerMock(String wcPath, PropertyConflict.Action action) {
            super(wcPath);
            
            this.action = action;
        }
        
        public void handle(AddConflict conflict) {
            throw new UnsupportedOperationException("not intended to be used with AddConflict");
        }
        
        public void handle(ReplaceConflict conflict) {
            throw new UnsupportedOperationException("not intended to be used with ReplaceConflict");
        }
        
        public void handle(PropertyConflict conflict) {
//            printer.printConflict(conflict);
            
            switch(action) {
            case RESOLVE_AUTOMATICALLY: throw new UnsupportedOperationException("method should not be called");
            case USE_LOCAL_VALUE: conflict.doUseLocalValue(); break;
            case USE_REMOTE_VALUE: conflict.doUseRemoteValue(); break;
            case USE_NEW_VALUE: conflict.doUseNewValue("shiny new value " + uniqueCounter++);
            }
        }
        
        public void handle(DeleteWithModificationConflict conflict) {
            throw new UnsupportedOperationException("not intended to be used with DeleteWithModificationConflict");
        }
    }

    private class ObstructedConflictedHandlerMock extends AutomaticConflictHandler {
        private ContentConflict.Action conflictAction;
        private ObstructedConflict.Action obAction;
        
        private int uniqueCounter = 0;

        public ObstructedConflictedHandlerMock(String wcPath, ObstructedConflict.Action obAction, ContentConflict.Action conflictAction) {
            super(wcPath);
            
            this.conflictAction = conflictAction;
            this.obAction = obAction;
        }
        
        public void handle(ContentConflict conflict) {
            printer.printConflict(conflict);
            
            switch(conflictAction) {
            case USE_LOCAL: conflict.doUseLocal(); break;
            case USE_REMOTE: conflict.doUseRemote(); break;
            case MERGE: conflict.doMerge(); break;
            case RENAME: conflict.doRename(new File(conflict.getStatus().getPath()).getName() + "_renamed_" + uniqueCounter++); break;
            }
        }
        
        public void handle(ObstructedConflict conflict) {
            printer.printConflict(conflict);
            
            switch(obAction) {
            case RENAME: conflict.doRename(new File(conflict.getStatus().getPath()).getName() + "_renamed_" + uniqueCounter++); break;
            case REVERT: conflict.doRevert(); break;
            }
        }
    }
    
    private class EnsureNoConflictsConflictHandler implements ConflictHandler {

        public void handle(ContentConflict conflict)
                throws CancelException {
            fail("detected conflict that should not be there: " + conflict);
        }

        public void handle(AddConflict conflict) throws CancelException {
            fail("detected conflict that should not be there: " + conflict);
        }

        public void handle(DeleteWithModificationConflict conflict)
                throws CancelException {
            fail("detected conflict that should not be there: " + conflict);
        }

        public void handle(ReplaceConflict conflict) throws CancelException {
            fail("detected conflict that should not be there: " + conflict);
        }

        public void handle(PropertyConflict conflict) throws CancelException {
            fail("detected conflict that should not be there: " + conflict);
        }

        public void handle(ObstructedConflict conflict)
                throws CancelException {
            fail("detected conflict that should not be there: " + conflict);
        }
        
    }

}
