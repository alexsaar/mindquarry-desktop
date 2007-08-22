package com.mindquarry.desktop.workspace;

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
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.NodeKind;
import org.tigris.subversion.javahl.Notify2;
import org.tigris.subversion.javahl.NotifyInformation;
import org.tigris.subversion.javahl.Status;
import org.tigris.subversion.javahl.StatusKind;
import org.tmatesoft.svn.core.javahl.SVNClientImpl;

import com.mindquarry.desktop.workspace.conflict.AddConflict;
import com.mindquarry.desktop.workspace.conflict.AutomaticConflictHandler;
import com.mindquarry.desktop.workspace.conflict.Conflict;
import com.mindquarry.desktop.workspace.conflict.ConflictPrinter;
import com.mindquarry.desktop.workspace.conflict.DeleteWithModificationConflict;
import com.mindquarry.desktop.workspace.conflict.ReplaceConflict;

public class SVNSynchronizerTestZip implements Notify2 {
	private SVNClientImpl client = SVNClientImpl.newInstance();
    
    private String wcPath;
    
    private String repoUrl;
	
	private void extractZip(String zipName, String destinationPath) throws IOException {
	    File dest = new File(destinationPath);
	    // delete if test has failed and extracted dir is still present
	    FileUtils.deleteDirectory(dest);
	    dest.mkdirs();
		try {
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
		} catch (Exception e) {
			e.printStackTrace();
		}
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

	@Test
	public void testAddConflictDoRename() throws IOException {
		setupTest("add_add_conflict");
		SVNSynchronizer helper = setupSynchronizer(new AddConflictHandlerMock(wcPath, AddConflict.Action.RENAME));

        helper.synchronize();
		
		// TODO: we need to test the conflict objects: extend AutomaticConflictHandler
		// class with methods testing the fields of the conflict object
		
		// TODO: here we have to test if the remote/localAdded fields contain
		// all files/folders of the test zip case

		FileUtils.deleteDirectory(new File("target/add_add_conflict/"));
	}
    
    @Test
    public void testAddConflictDoReplace() throws IOException {
        setupTest("add_add_conflict");
        SVNSynchronizer helper = setupSynchronizer(new AddConflictHandlerMock(wcPath, AddConflict.Action.REPLACE));

        helper.synchronize();
        
        // TODO: we need to test the conflict objects: extend AutomaticConflictHandler
        // class with methods testing the fields of the conflict object
        
        // TODO: here we have to test if the remote/localAdded fields contain
        // all files/folders of the test zip case

        FileUtils.deleteDirectory(new File("target/add_add_conflict/"));
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

		FileUtils.deleteDirectory(new File("target/deleted_modified_conflict/"));
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

        FileUtils.deleteDirectory(new File("target/deleted_modified_conflict/"));
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

        FileUtils.deleteDirectory(new File("target/deleted_modified_conflict/"));
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
        client.add(path, false);
    }
    
    private void del(String relativePath) throws ClientException {
        String path = wcPath + "/" + relativePath;
        client.remove(new String[] { path }, null, false);
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
    }

    @Test
    public void testReplacedConflictsDoReplace() throws IOException, ClientException {
        setupTest("replaced_conflict");
        SVNSynchronizer helper = setupSynchronizer(new ReplaceConflictHandlerMock(wcPath, ReplaceConflict.Action.REPLACE));
        
        prepareReplaceConflict();
        
        helper.synchronize();
    }
    
	// TODO: test ignore of Thumbs.db/.DS_Store
	// - simple test if it gets ignored (no ignored set previously)
	// - test with an svn:ignore property already set to check correct incremental setting of that property
	
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
            throw new UnsupportedOperationException("not intended to be used with ReplaceConflict");
        }
    }
}
