package com.mindquarry.desktop.workspace;

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

import com.mindquarry.desktop.workspace.conflict.AutomaticConflictHandler;

public class SVNSynchronizerTestZip implements Notify2 {
	private SVNClientImpl client = SVNClientImpl.newInstance();
	
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
    
	public SVNSynchronizer setupTest(String name) throws IOException {
	    System.out.println("Testing " + name + " =======================");
		String zipPath = name + ".zip";
		String targetPath = "target/" + name + "/";
		String repoUrl = "file://" + new File(targetPath + "/repo").toURI().getPath();
		String wcPath = targetPath + "wc";

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

        SVNSynchronizer syncer = new SVNSynchronizer(repoUrl, wcPath, "", "", new AutomaticConflictHandler(wcPath));
        syncer.setNotifyListener(this);
        return syncer;
	}
	
	@Test
	public void testAddAddConflict() throws IOException {
		SVNSynchronizer helper = setupTest("add_add_conflict");

		helper.synchronize();
		
		// TODO: we need to test the conflict objects: extend AutomaticConflictHandler
		// class with methods testing the fields of the conflict object
		
		// TODO: here we have to test if the remote/localAdded fields contain
		// all files/folders of the test zip case

		FileUtils.deleteDirectory(new File("target/add_add_conflict/"));
	}

	@Test
	public void testDeletedModifiedConflict() throws IOException, ClientException  {
		SVNSynchronizer helper = setupTest("deleted_modified_conflict");
		
		client.add(helper.getLocalPath() + "/2 Two/Added.txt", false);

		helper.synchronize();

		FileUtils.deleteDirectory(new File("target/deleted_modified_conflict/"));
	}
	
	// TODO: test ignore of Thumbs.db/.DS_Store
	// - simple test if it gets ignored (no ignored set previously)
	// - test with an svn:ignore property already set to check correct incremental setting of that property
	
	
}
