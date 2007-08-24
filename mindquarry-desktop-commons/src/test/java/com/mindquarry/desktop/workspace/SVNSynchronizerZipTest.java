package com.mindquarry.desktop.workspace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.Test;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.NodeKind;
import org.tigris.subversion.javahl.Notify2;
import org.tigris.subversion.javahl.NotifyInformation;
import org.tigris.subversion.javahl.Revision;
import org.tigris.subversion.javahl.Status;
import org.tigris.subversion.javahl.StatusKind;
import org.tmatesoft.svn.core.javahl.SVNClientImpl;

import com.mindquarry.desktop.workspace.conflict.AddConflict;
import com.mindquarry.desktop.workspace.conflict.AutomaticConflictHandler;
import com.mindquarry.desktop.workspace.conflict.ContentConflict;
import com.mindquarry.desktop.workspace.conflict.DeleteWithModificationConflict;
import com.mindquarry.desktop.workspace.conflict.ObstructedConflict;
import com.mindquarry.desktop.workspace.conflict.PropertyConflict;
import com.mindquarry.desktop.workspace.conflict.ReplaceConflict;

public class SVNSynchronizerZipTest implements Notify2 {
	private SVNClientImpl client = SVNClientImpl.newInstance();
    
    private String wcPath;
    
    private String repoUrl;
	
	private void extractZip(String zipName, String destinationPath) throws IOException {
	    File dest = new File(destinationPath);
	    // delete if test has failed and extracted dir is still present
	    FileUtils.deleteDirectory(dest);
	    dest.mkdirs();

		byte[] buffer = new byte[1024];
		ZipEntry zipEntry;
		ZipInputStream zipInputStream = new ZipInputStream(
				new FileInputStream(
						"src/test/resources/com/mindquarry/desktop/workspace/"
								+ zipName));

        while (null != (zipEntry = zipInputStream.getNextEntry())) {

            File zippedFile = new File(destinationPath + zipEntry.getName());

            if (zipEntry.isDirectory()) {
                zippedFile.mkdirs();
            } else {
                // ensure the parent directory exists
                zippedFile.getParentFile().mkdirs();
                
                OutputStream fileOutStream = new FileOutputStream(zippedFile);
                transferBytes(zipInputStream, fileOutStream, buffer);
                fileOutStream.close();
            }
        }

		zipInputStream.close();
	}

    private void transferBytes(InputStream in, OutputStream out, byte[] buffer)
    throws IOException {
        int nRead;
        while (-1 != (nRead = in.read(buffer, 0, buffer.length)))
            out.write(buffer, 0, nRead);
    }

	public void onNotify(NotifyInformation info) {
        System.out.println("SVNKIT " + SVNSynchronizer.notifyToString(info));
    }
    
	public void setupTest(String name) throws IOException {
	    System.out.println("Testing " + name + " ============================================================");
		String zipPath = name + ".zip";
		String targetPath = "target/" + name + "/";
		this.repoUrl = "file://" + new File(targetPath + "/repo").toURI().getPath();
		this.wcPath = targetPath + "wc";

		extractZip(zipPath, targetPath);
		
		try {
			String oldRepoUrl = client.info(wcPath).getRepository();
			
			List<String> relocatePaths = new ArrayList<String>();
			Status[] stati = client.status(wcPath, true, false, true);
			for (Status s : stati) {
			    // added directories cannot be relocated
			    if (s.getNodeKind() == NodeKind.dir && s.getTextStatus() != StatusKind.added) {
			        relocatePaths.add(s.getPath());
			    }
			}
			for (String path : relocatePaths) {
				try {
					client.relocate(oldRepoUrl, repoUrl, path, false);
				} catch (ClientException e) {
		            throw new RuntimeException("cannot relocate " + path, e);
				}
			}
		} catch (ClientException e) {
            throw new RuntimeException("could not setup test " + name, e);
		}
	}
    
    public void setupTestOnlyRepo(String name) throws IOException, ClientException {
        System.out.println("Testing " + name + " (.svnref) ============================================================");
        String zipPath = name + ".zip";
        String targetPath = "target/" + name + "/";
        this.repoUrl = "file://" + new File(targetPath + "/repo").toURI().getPath();
        this.wcPath = targetPath + "wc";

        extractZip(zipPath, targetPath);

        client.checkout(repoUrl, wcPath, Revision.HEAD, true);
    }
    
    public SVNSynchronizer setupSynchronizer(AutomaticConflictHandler conflictHandler) {
        SVNSynchronizer syncer = new SVNSynchronizer(repoUrl, wcPath, "", "", conflictHandler);
        syncer.setNotifyListener(this);
        
        return syncer;
    }

    /**
	 * Assert that a particular file or directory exists.
	 * @param path		Path to use, can be empty (e.g. /home/user/abc/) 
	 * @param relative	relative path including filename (e.g. dir/file.ext)
	 */
	protected void assertFileExists(String path, String relative) {
		File file = new File(path+relative);
		assertTrue("'" + relative + "' is expected to exist", file.exists());
	}
	
	/**
	 * Assert that a particular file or directory is missing.
	 * @param path		Path to use, can be empty (e.g. /home/user/abc/) 
	 * @param relative	relative path including filename (e.g. dir/file.ext)
	 */
	protected void assertFileMissing(String path, String relative) {
		File file = new File(path+relative);
		assertFalse("'" + relative + "' is expected to be missing", file.exists());
	}
	
	/**
	 * Assert that a particular file contains some substring.
	 * @param path		Path to use, can be empty (e.g. /home/user/abc/) 
	 * @param relative	relative path including filename (e.g. dir/file.ext)
	 */
	protected void assertFileContains(String path, String relative,
			String substring) {
		File file = new File(path+relative);
		String contents;
		try {
			contents = FileUtils.readFileToString(file);
			assertTrue("'" + relative + "' is expected to contain '" + substring
					+ "'", contents.contains(substring));
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Finds all files with a given pattern that can include wildcards. Will
	 * recurse into subdirectories. Similar to 'find DIR -name "PATTERN"' on a
	 * unix command line.
	 * 
	 * Example: findFiles(dir, "*.java")
	 * 
	 * @param dir the directory to search for (including all subdirectories)
	 * @param pattern a pattern that can contain "?" (single-char-wildcard) and
	 *                "*" (multi-char-wildcard) as wildcards
	 * @return an Iterator containing File objects for all found files
	 */
    public Iterator findFiles(File dir, String pattern) {
        return FileUtils.iterateFiles(dir, new WildcardFileFilter(pattern), TrueFileFilter.INSTANCE);
    }
    
    /**
     * Asserts that no .svnref file is present inside dir or its subfolders.
     */
    public void assertNoSVNRefFilePresent(File dir) {
        Iterator iter = findFiles(dir, ".svnref");
        if (iter.hasNext()) {
            // failure
            assertTrue("found at least one .svnref at '" + iter.next() + "'", false);
        }
    }
    
    /**
     * Cleans up the test at the end. Analogous to {@link setupTest()}. Will do
     * some sanity checks and delete the directory created from the zip file.
     * 
     * @param testName the test directory name (w/o "target/" at the beginning)
     * @throws IOException when the deletion failed
     */
    public void cleanupTest(String testName) throws IOException {
        File dir = new File("target/" + testName);
        assertNoSVNRefFilePresent(dir);
        FileUtils.deleteDirectory(dir);
    }

    /**
     * Cleans up the test at the end. Analogous to {@link setupTestOnlyRepo()}.
     * Will delete the directory created from the zip file.
     * 
     * @param testName the test directory name (w/o "target/" at the beginning)
     * @throws IOException when the deletion failed
     */
    public void cleanupTestOnlyRepo(String testName) throws IOException {
        File dir = new File("target/" + testName);
        FileUtils.deleteDirectory(dir);
    }
    
    @Test
    public void testMove() throws IOException, ClientException {
        setupTestOnlyRepo("move");

        // first create the files/dirs
        touch("file");
        mkdir("dir");
        touch("dir/file");
        mkdir("dir/subdir");
        touch("dir/subdir/file");
        
        // add the files
        add("file");
        add("dir");
        
        client.commit(new String[] { "target/move/wc" }, "initial add", true);
        
        // then move them
        move("file", "moved_file");
        move("dir", "moved_dir");
        
        SVNSynchronizer helper = setupSynchronizer(new AutomaticConflictHandler(wcPath));

        helper.synchronize();
        
        cleanupTestOnlyRepo("move");
    }
    
    @Test
    public void testMoveAdded() throws IOException, ClientException {
        setupTestOnlyRepo("move");

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
        
        SVNSynchronizer helper = setupSynchronizer(new AutomaticConflictHandler(wcPath));

        helper.synchronize();
        
        cleanupTestOnlyRepo("move");
    }
    
    @Test
	public void testAddConflictDoRename() throws IOException {
		setupTest("add_add_conflict");
		SVNSynchronizer helper = setupSynchronizer(new AddConflictHandlerMock(wcPath, AddConflict.Action.RENAME));

        helper.synchronize();
		
		// TODO: we need to test the conflict objects: extend AutomaticConflictHandler
		// class with methods testing the fields of the conflict object

		// Test correct working copy contents
		String localPath = helper.getLocalPath();
        assertFileExists(localPath, "/first");
        assertFileExists(localPath, "/first_renamed_0");
        assertFileExists(localPath, "/second");
        assertFileExists(localPath, "/second_renamed_2/");
        assertFileExists(localPath, "/second_renamed_2/file");
        assertFileExists(localPath, "/second_renamed_2/another_file");
        assertFileExists(localPath, "/third/");
        assertFileExists(localPath, "/third/first");
        assertFileExists(localPath, "/third/second");
        assertFileExists(localPath, "/third/second/first");
        assertFileExists(localPath, "/third_renamed_3");
        assertFileExists(localPath, "/fourth/");
        assertFileExists(localPath, "/fourth/first");
        assertFileExists(localPath, "/fourth/second/");
        assertFileExists(localPath, "/fourth/second/first");
        assertFileExists(localPath, "/fourth_renamed_1/");
        assertFileExists(localPath, "/fourth_renamed_1/file");
        assertFileExists(localPath, "/fourth_renamed_1/different_file");
		
		// TODO: here we have to test if the remote/localAdded fields contain
		// all files/folders of the test zip case

        cleanupTest("add_add_conflict");
	}
    
    @Test
    public void testAddConflictDoReplace() throws IOException {
        setupTest("add_add_conflict");
        SVNSynchronizer helper = setupSynchronizer(new AddConflictHandlerMock(wcPath, AddConflict.Action.REPLACE));

        helper.synchronize();
        
        // TODO: we need to test the conflict objects: extend AutomaticConflictHandler
        // class with methods testing the fields of the conflict object

		// Test correct working copy contents
		String localPath = helper.getLocalPath();
        assertFileExists(localPath, "/first");
        assertFileExists(localPath, "/second");
        assertFileExists(localPath, "/third/");
        assertFileExists(localPath, "/third/first");
        assertFileExists(localPath, "/third/second");
        assertFileExists(localPath, "/third/second/first");
        assertFileExists(localPath, "/fourth/");
        assertFileExists(localPath, "/fourth/first");
        assertFileExists(localPath, "/fourth/second/");
        assertFileExists(localPath, "/fourth/second/first");
        
        // TODO: here we have to test if the remote/localAdded fields contain
        // all files/folders of the test zip case

        cleanupTest("add_add_conflict");
    }

	@Test
	public void testDeletedModifiedConflictDoRevertDelete() throws IOException, ClientException  {
		setupTest("deleted_modified_conflict");
        SVNSynchronizer helper = setupSynchronizer(new DeleteWithModificationConflictHandlerMock(wcPath, DeleteWithModificationConflict.Action.REVERTDELETE));
		String localPath = helper.getLocalPath();
		
		// add file here, because having it in the zipped working copy breaks
		// relocating the directory '2 Two', as it does not exist in the
		// repository
		client.add(localPath + "/2 Two/Added.txt", false);

		helper.synchronize();

		// Test correct working copy contents
		assertFileExists (localPath, "/1 One/");
		assertFileExists (localPath, "/1 One/Added.txt");
		assertFileExists (localPath, "/1 One/Existing.txt");
		assertFileExists (localPath, "/1 One/Modified.txt");
		assertFileExists (localPath, "/2 Two/");
		assertFileExists (localPath, "/2 Two/Added.txt");
		assertFileExists (localPath, "/2 Two/Existing.txt");
		assertFileExists (localPath, "/2 Two/Modified.txt");
		assertFileExists (localPath, "/3 Three.txt");
		assertFileExists (localPath, "/4 Four.txt");
		assertFileExists (localPath, "/Existing.txt");

		// Test correct file contents
		assertFileContains(localPath, "/1 One/Modified.txt", "Modified\r\nModified");
		assertFileContains(localPath, "/2 Two/Modified.txt", "Modified\r\nModified");
		assertFileContains(localPath, "/3 Three.txt", "Modified");
		assertFileContains(localPath, "/4 Four.txt",  "Modified");
		
		// TODO: check correct SVN state of all files

        cleanupTest("deleted_modified_conflict");
	}

    @Test
    public void testDeletedModifiedConflictDoOnlyKeepModified() throws IOException, ClientException  {
        setupTest("deleted_modified_conflict");
        SVNSynchronizer helper = setupSynchronizer(new DeleteWithModificationConflictHandlerMock(wcPath, DeleteWithModificationConflict.Action.ONLYKEEPMODIFIED));
		String localPath = helper.getLocalPath();

		// add file here, because having it in the zipped working copy breaks
		// relocating the directory '2 Two', as it does not exist in the
		// repository
        client.add(localPath + "/2 Two/Added.txt", false);

        helper.synchronize();

		// Test correct working copy contents
		assertFileExists (localPath, "/1 One/");
		assertFileExists (localPath, "/1 One/Added.txt");
		assertFileExists (localPath, "/1 One/Existing.txt");
		assertFileExists (localPath, "/1 One/Modified.txt");
		assertFileExists (localPath, "/2 Two/");
		assertFileExists (localPath, "/2 Two/Added.txt");
		assertFileMissing(localPath, "/2 Two/Existing.txt");
		assertFileExists (localPath, "/2 Two/Modified.txt");
		assertFileExists (localPath, "/3 Three.txt");
		assertFileExists (localPath, "/4 Four.txt");
		assertFileExists (localPath, "/Existing.txt");

		// Test correct file contents
		assertFileContains(localPath, "/1 One/Modified.txt", "Modified\r\nModified");
		assertFileContains(localPath, "/2 Two/Modified.txt", "Modified\r\nModified");
		assertFileContains(localPath, "/3 Three.txt", "Modified");
		assertFileContains(localPath, "/4 Four.txt",  "Modified");

		// TODO: check correct SVN state of all files

        cleanupTest("deleted_modified_conflict");
    }

    @Test
    public void testDeletedModifiedConflictDoDelete() throws IOException, ClientException  {
        setupTest("deleted_modified_conflict");
        SVNSynchronizer helper = setupSynchronizer(new DeleteWithModificationConflictHandlerMock(wcPath, DeleteWithModificationConflict.Action.DELETE));
		String localPath = helper.getLocalPath();

		// add file here, because having it in the zipped working copy breaks
		// relocating the directory '2 Two', as it does not exist in the
		// repository
        client.add(localPath + "/2 Two/Added.txt", false);

        helper.synchronize();

		// Test correct working copy contents
		assertFileMissing(localPath, "/1 One/");
		assertFileMissing(localPath, "/1 One/Added.txt");
		assertFileMissing(localPath, "/1 One/Existing.txt");
		assertFileMissing(localPath, "/1 One/Modified.txt");
		assertFileMissing(localPath, "/2 Two/");
		assertFileMissing(localPath, "/2 Two/Added.txt");
		assertFileMissing(localPath, "/2 Two/Existing.txt");
		assertFileMissing(localPath, "/2 Two/Modified.txt");
		assertFileMissing(localPath, "/3 Three.txt");
		assertFileMissing(localPath, "/4 Four.txt");
		assertFileExists (localPath, "/Existing.txt");

		// TODO: check correct SVN state of all files

        cleanupTest("deleted_modified_conflict");
    }
	
	private void touch(String relativePath) throws IOException {
        FileUtils.touch(new File(wcPath + "/" + relativePath));
    }
	
	private void modify(String relativePath) throws IOException {
        FileUtils.writeStringToFile(new File(wcPath + "/" + relativePath), "time " + System.currentTimeMillis());
	}
    
    private void replace(String relativePath, boolean isFile) throws ClientException, IOException {
        String path = wcPath + "/" + relativePath;
        client.remove(new String[] { path }, null, false);
        if (isFile) {
            touch(relativePath);
        }
        client.add(path, false);
    }
    
    private void add(String relativePath) throws ClientException {
        String path = wcPath + "/" + relativePath;
        client.add(path, true);
    }
    
    private void del(String relativePath) throws ClientException {
        String path = wcPath + "/" + relativePath;
        client.remove(new String[] { path }, null, false);
    }
    
    private void move(String from, String to) throws IOException {
        File file = new File(wcPath, from);
        File toFile = new File(wcPath, to);
        if (!file.renameTo(toFile)) {
            throw new IOException("Could not rename '" + from + "' to '" + to + "'");
        }
    }
    
    private void mkdir(String relativePath) throws IOException {
        File file = new File(wcPath, relativePath);
        if (!file.mkdirs()) {
            throw new IOException("Could not mkdirs '" + file + "'");
        }
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
    public void testReplacedConflictsDoRename() throws IOException, ClientException {
        setupTest("replaced_conflict");
        SVNSynchronizer helper = setupSynchronizer(new ReplaceConflictHandlerMock(wcPath, ReplaceConflict.Action.RENAME));
        
        prepareReplaceConflict();
        
        helper.synchronize();

        cleanupTest("replaced_conflict");
    }
    
    @Test
    public void testReplacedConflictsDoReplace() throws IOException, ClientException {
        setupTest("replaced_conflict");
        SVNSynchronizer helper = setupSynchronizer(new ReplaceConflictHandlerMock(wcPath, ReplaceConflict.Action.REPLACE));
        
        prepareReplaceConflict();
        
        helper.synchronize();

        cleanupTest("replaced_conflict");
    }
    
    private void prepareObstructedAndConflicted() throws IOException {
        // rmdir obstructed_dir
        // touch obstructed_dir
        FileUtils.deleteDirectory(new File(wcPath + "/obstructed_dir"));
        touch("obstructed_dir");
    }
    
    @Test
    public void testObstructedAndConflictedDoRenameAndUseLocal() throws IOException, ClientException {
        setupTest("obstructed_and_conflicted");
        SVNSynchronizer helper = setupSynchronizer(new ObstructedConflictedHandlerMock(wcPath, ObstructedConflict.Action.RENAME, ContentConflict.Action.USE_LOCAL));
        
        prepareObstructedAndConflicted();
        
        helper.synchronize();
        
        cleanupTest("obstructed_and_conflicted");
    }

    @Test
    public void testObstructedAndConflictedDoRenameAndUseRemote() throws IOException, ClientException {
        setupTest("obstructed_and_conflicted");
        SVNSynchronizer helper = setupSynchronizer(new ObstructedConflictedHandlerMock(wcPath, ObstructedConflict.Action.RENAME, ContentConflict.Action.USE_REMOTE));
        
        prepareObstructedAndConflicted();
        
        helper.synchronize();
        
        cleanupTest("obstructed_and_conflicted");
    }

    /*
    @Test
    public void testObstructedAndConflictedDoRenameAndMerge() throws IOException, ClientException {
        setupTest("obstructed_and_conflicted");
        SVNSynchronizer helper = setupSynchronizer(new ObstructedConflictedHandlerMock(wcPath, ObstructedConflict.Action.RENAME, ContentConflict.Action.MERGE));
        
        prepareObstructedAndConflicted();
        
        helper.synchronize();
        
        cleanupTest("obstructed_and_conflicted");
    }
    */

    @Test
    public void testObstructedAndConflictedDoRevertAndUseLocal() throws IOException, ClientException {
        setupTest("obstructed_and_conflicted");
        SVNSynchronizer helper = setupSynchronizer(new ObstructedConflictedHandlerMock(wcPath, ObstructedConflict.Action.REVERT, ContentConflict.Action.USE_LOCAL));
        
        prepareObstructedAndConflicted();
        
        helper.synchronize();
        
        cleanupTest("obstructed_and_conflicted");
    }

    @Test
    public void testObstructedAndConflictedDoRevertAndUseRemote() throws IOException, ClientException {
        setupTest("obstructed_and_conflicted");
        SVNSynchronizer helper = setupSynchronizer(new ObstructedConflictedHandlerMock(wcPath, ObstructedConflict.Action.REVERT, ContentConflict.Action.USE_REMOTE));
        
        prepareObstructedAndConflicted();
        
        helper.synchronize();
        
        cleanupTest("obstructed_and_conflicted");
    }

    /*
    @Test
    public void testObstructedAndConflictedDoRevertAndMerge() throws IOException, ClientException {
        setupTest("obstructed_and_conflicted");
        SVNSynchronizer helper = setupSynchronizer(new ObstructedConflictedHandlerMock(wcPath, ObstructedConflict.Action.REVERT, ContentConflict.Action.MERGE));
        
        prepareObstructedAndConflicted();
        
        helper.synchronize();
        
        cleanupTest("obstructed_and_conflicted");
    }
    */

	// TODO: test ignore of Thumbs.db/.DS_Store
	// - simple test if it gets ignored (no ignored set previously)
	// - test with an svn:ignore property already set to check correct incremental setting of that property
    @Test
    public void testPropertyConflictDoUseLocalValue() throws IOException, ClientException {
        setupTest("property_conflict");
        SVNSynchronizer helper = setupSynchronizer(new PropertyConflictHandlerMock(wcPath, PropertyConflict.Action.USE_LOCAL_VALUE));

        helper.synchronize();
        
        String nl = System.getProperty("line.separator");
        assertEquals("first_local_value", client.propertyGet(wcPath, "mq:first").getValue());
        assertEquals("second_local_value", client.propertyGet(wcPath, "mq:second").getValue());
        assertEquals("temp"+nl+"*.bak"+nl+"*.log"+nl+"tmp"+nl+"bin"+nl, client.propertyGet(wcPath, "svn:ignore").getValue());
        
        cleanupTest("property_conflict");
    }
    
    @Test
    public void testPropertyConflictDoUseRemoteValue() throws IOException, ClientException {
        setupTest("property_conflict");
        SVNSynchronizer helper = setupSynchronizer(new PropertyConflictHandlerMock(wcPath, PropertyConflict.Action.USE_REMOTE_VALUE));

        helper.synchronize();

        String nl = System.getProperty("line.separator");
        assertEquals("first_remote_value", client.propertyGet(wcPath, "mq:first").getValue());
        assertEquals("second_remote_value", client.propertyGet(wcPath, "mq:second").getValue());
        assertEquals("temp"+nl+"*.bak"+nl+"*.log"+nl+"tmp"+nl+"bin"+nl, client.propertyGet(wcPath, "svn:ignore").getValue());
        
        cleanupTest("property_conflict");
    }
    
    @Test
    public void testPropertyConflictDoUseNewValue() throws IOException, ClientException {
        setupTest("property_conflict");
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
}
