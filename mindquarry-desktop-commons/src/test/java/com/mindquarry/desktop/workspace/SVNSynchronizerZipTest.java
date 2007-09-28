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
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.wc.SVNAdminDirectoryLocator;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.javahl.SVNClientImpl;

import com.mindquarry.desktop.util.FileHelper;
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

public class SVNSynchronizerZipTest implements Notify2 {
	private SVNClientImpl client = SVNClientImpl.newInstance();
    
    private String wcPath;
    
    private String repoUrl;

    private String wcPathRemote;
	
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
    
	/**
	 * Creates repo and checkout based on zip file name.zip
	 */
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
    
    /**
     * Creates repo based on zip file name.zip. Checks out fresh manually.
     */
    public void setupTestOnlyRepo(String name) throws IOException, ClientException {
        System.out.println("Testing " + name + " ("
        		+ SVNAdminDirectoryLocator.SHALLOW_DIR_REF_FILENAME
        		+ ") ============================================================");
        String zipPath = name + ".zip";
        String targetPath = "target/" + name + "/";
        this.repoUrl = "file://" + new File(targetPath + "/repo").toURI().getPath();
        this.wcPath = targetPath + "wc";

        extractZip(zipPath, targetPath);

        client.checkout(repoUrl, wcPath, Revision.HEAD, true);
    }
    
    /**
     * Creates a fresh new repository without the need for a zip file and checks
     * out a working copy under "wc".
     */
    public void setupRepo(String name) throws SVNException, ClientException, IOException {
        System.out.println("Testing " + name + " ("
                + SVNAdminDirectoryLocator.SHALLOW_DIR_REF_FILENAME
                + ") ============================================================");
        String targetPath = "target/" + name + "/";
        
        File repo = new File(targetPath + "/repo").getAbsoluteFile();
        FileUtils.deleteDirectory(repo);
        this.repoUrl = "file://" + repo.toURI().getPath();
        SVNRepositoryFactory.createLocalRepository(repo, null, false, false, false);
        
        this.wcPath = targetPath + "wc";
        FileUtils.deleteDirectory(new File(wcPath));
        client.checkout(repoUrl, wcPath, Revision.HEAD, true);
    }
    
    /**
     * Checks out another working copy under "remote".
     */
    public void checkoutRemote(String name) throws SVNException, ClientException, IOException {
        String targetPath = "target/" + name + "/";
        this.wcPathRemote = targetPath + "remote";
        FileUtils.deleteDirectory(new File(wcPathRemote));
        client.checkout(repoUrl, wcPathRemote, Revision.HEAD, true);
    }
    
    public SVNSynchronizer setupSynchronizer(ConflictHandler conflictHandler) {
        SVNSynchronizer syncer = new SVNSynchronizer(repoUrl, wcPath, "", "", conflictHandler);
        syncer.setNotifyListener(this);
        
        return syncer;
    }

    /**
	 * Assert that a particular file or directory exists.
	 * @param path		Path to use, can be empty (e.g. /home/user/abc/) 
	 * @param relative	relative path including filename (e.g. dir/file.ext)
	 */
	protected void assertFileExists(String relative) {
		File file = new File(wcPath + "/" + relative);
		assertTrue("'" + relative + "' is expected to exist", file.exists());
	}
	
	/**
	 * Assert that a particular file or directory is missing.
	 * @param path		Path to use, can be empty (e.g. /home/user/abc/) 
	 * @param relative	relative path including filename (e.g. dir/file.ext)
	 */
	protected void assertFileMissing(String relative) {
		File file = new File(wcPath + "/" + relative);
		assertFalse("'" + relative + "' is expected to be missing", file.exists());
	}
	
	/**
	 * Assert that a particular file contains some substring.
	 * @param path		Path to use, can be empty (e.g. /home/user/abc/) 
	 * @param relative	relative path including filename (e.g. dir/file.ext)
	 */
	protected void assertFileContains(String relative,
			String substring) {
		File file = new File(wcPath + "/" + relative);
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
        Iterator iter = findFiles(dir,
        		SVNAdminDirectoryLocator.SHALLOW_DIR_REF_FILENAME);
        if (iter.hasNext()) {
            // failure
            assertTrue("found at least one "
					+ SVNAdminDirectoryLocator.SHALLOW_DIR_REF_FILENAME
					+ " at '" + iter.next() + "'", false);
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
    public void testMove() throws IOException, ClientException, SynchronizeException {
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
        
        client.commit(new String[] { this.wcPath }, "initial add", true);
        
        // then move them
        move("file", "moved_file");
        move("dir", "moved_dir");
        
        SVNSynchronizer helper = setupSynchronizer(new EnsureNoConflictsConflictHandler());

        helper.synchronize();
        
        assertFileMissing("dir");
        assertFileExists("moved_dir");
        
        cleanupTestOnlyRepo("move");
    }
    
    @Test
    public void testMoveAdded() throws IOException, ClientException, SynchronizeException {
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
        
        SVNSynchronizer helper = setupSynchronizer(new EnsureNoConflictsConflictHandler());

        helper.synchronize();
        
        assertFileMissing("dir");
        assertFileExists("moved_dir");
        
        cleanupTestOnlyRepo("move");
    }
    
    @Test
    public void testMissingDelete() throws SVNException, ClientException, IOException, SynchronizeException {
        setupRepo("missing_delete");
        
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
        
        cleanupTestOnlyRepo("missing_delete");
    }
    
    @Test
	public void testAddConflictDoRename() throws IOException, SynchronizeException {
		setupTest("add_add_conflict");
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

        cleanupTest("add_add_conflict");
	}
    
    @Test
    public void testAddConflictDoReplace() throws IOException, SynchronizeException {
        setupTest("add_add_conflict");
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

        cleanupTest("add_add_conflict");
    }

	@Test
	public void testDeletedModifiedConflictDoRevertDelete() throws IOException, ClientException, SynchronizeException  {
		setupTest("deleted_modified_conflict");
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
    public void testDeletedModifiedConflictDoRevertDelete2() throws IOException, ClientException, SynchronizeException, SVNException  {
        setupRepo("deleted_modified_conflict2");
        
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

        cleanupTestOnlyRepo("deleted_modified_conflict2");
    }
    
    @Test
    public void testDeletedModifiedConflictDoOnlyKeepModified() throws IOException, ClientException, SynchronizeException  {
        setupTest("deleted_modified_conflict");
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
        setupTest("deleted_modified_conflict");
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
	
	private void touch(String relativePath) throws IOException {
        FileUtils.touch(new File(wcPath + "/" + relativePath));
    }
	
    private void touchRemote(String relativePath) throws IOException {
        FileUtils.touch(new File(wcPathRemote + "/" + relativePath));
    }
    
	private void modify(String relativePath) throws IOException {
        FileUtils.writeStringToFile(new File(wcPath + "/" + relativePath), "time " + System.currentTimeMillis());
	}
    
    private void modifyRemote(String relativePath) throws IOException {
        FileUtils.writeStringToFile(new File(wcPathRemote + "/" + relativePath), "time " + System.currentTimeMillis());
    }
    
    private void replace(String relativePath, boolean isFile) throws ClientException, IOException {
        String path = wcPath + "/" + relativePath;
        client.remove(new String[] { path }, null, false);
        if (isFile) {
            touch(relativePath);
        }
        client.add(path, false);
    }
    
    private void replaceRemote(String relativePath, boolean isFile) throws ClientException, IOException {
        String path = wcPathRemote + "/" + relativePath;
        client.remove(new String[] { path }, null, false);
        if (isFile) {
            touchRemote(relativePath);
        }
        client.add(path, false);
    }
    
    private void add(String relativePath) throws ClientException {
        String path = wcPath + "/" + relativePath;
        client.add(path, true);
    }
    
    private void addRemote(String relativePath) throws ClientException {
        String path = wcPathRemote + "/" + relativePath;
        client.add(path, true);
    }
    
    private void del(String relativePath) throws ClientException {
        String path = wcPath + "/" + relativePath;
        client.remove(new String[] { path }, null, false);
    }
    
    private void delRemote(String relativePath) throws ClientException {
        String path = wcPathRemote + "/" + relativePath;
        client.remove(new String[] { path }, null, false);
    }
    
    private void move(String from, String to) throws IOException {
        File file = new File(wcPath, from);
        File toFile = new File(wcPath, to);
        if (!file.renameTo(toFile)) {
            throw new IOException("Could not rename '" + from + "' to '" + to + "'");
        }
    }
    
    private void moveRemote(String from, String to) throws IOException {
        File file = new File(wcPathRemote, from);
        File toFile = new File(wcPathRemote, to);
        if (!file.renameTo(toFile)) {
            throw new IOException("Could not rename '" + from + "' to '" + to + "'");
        }
    }
    
    private void remove(String relativePath) throws IOException {
        String path = wcPath + "/" + relativePath;
        File file = new File(path);
        if (file.isDirectory()) {
            FileUtils.deleteDirectory(file);
        } else {
            if (file.exists()) {
                FileHelper.delete(file);
            }
        }
    }
    
    private void removeRemote(String relativePath) throws IOException {
        String path = wcPathRemote + "/" + relativePath;
        File file = new File(path);
        if (file.isDirectory()) {
            FileUtils.deleteDirectory(file);
        } else {
            if (file.exists()) {
                FileHelper.delete(file);
            }
        }
    }
    
    private void mkdir(String relativePath) throws IOException {
        File file = new File(wcPath, relativePath);
        if (!file.mkdirs()) {
            throw new IOException("Could not mkdirs '" + file + "'");
        }
    }

    private void mkdirRemote(String relativePath) throws IOException {
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
    public void testReplacedConflictsDoRename() throws IOException, ClientException, SynchronizeException {
        setupTest("replaced_conflict");
        SVNSynchronizer helper = setupSynchronizer(new ReplaceConflictHandlerMock(wcPath, ReplaceConflict.Action.RENAME));
        
        prepareReplaceConflict();
        
        helper.synchronize();

        cleanupTest("replaced_conflict");
    }
    
    @Test
    public void testReplacedConflictsDoReplace() throws IOException, ClientException, SynchronizeException {
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
    public void testObstructedAndConflictedDoRenameAndUseLocal() throws IOException, ClientException, SynchronizeException {
        setupTest("obstructed_and_conflicted");
        SVNSynchronizer helper = setupSynchronizer(new ObstructedConflictedHandlerMock(wcPath, ObstructedConflict.Action.RENAME, ContentConflict.Action.USE_LOCAL));
        
        prepareObstructedAndConflicted();
        
        helper.synchronize();
        
        cleanupTest("obstructed_and_conflicted");
    }

    @Test
    public void testObstructedAndConflictedDoRenameAndUseRemote() throws IOException, ClientException, SynchronizeException {
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
    public void testObstructedAndConflictedDoRevertAndUseLocal() throws IOException, ClientException, SynchronizeException {
        setupTest("obstructed_and_conflicted");
        SVNSynchronizer helper = setupSynchronizer(new ObstructedConflictedHandlerMock(wcPath, ObstructedConflict.Action.REVERT, ContentConflict.Action.USE_LOCAL));
        
        prepareObstructedAndConflicted();
        
        helper.synchronize();
        
        cleanupTest("obstructed_and_conflicted");
    }

    @Test
    public void testObstructedAndConflictedDoRevertAndUseRemote() throws IOException, ClientException, SynchronizeException {
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
    public void testPropertyConflictDoUseLocalValue() throws IOException, ClientException, SynchronizeException {
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
    public void testPropertyConflictDoUseRemoteValue() throws IOException, ClientException, SynchronizeException {
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
    public void testPropertyConflictDoUseNewValue() throws IOException, ClientException, SynchronizeException {
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
