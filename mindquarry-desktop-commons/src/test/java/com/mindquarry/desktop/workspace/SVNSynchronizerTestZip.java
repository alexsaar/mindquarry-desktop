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
	
	private void extractZip(String zipName, String destinationPath) {
	    File dest = new File(destinationPath);
	    // delete if test has failed and extracted dir is still present
	    deleteDir(dest);
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

	private boolean deleteDir(File dir) {
		// to see if this directory is actually a symbolic link to a directory,
		// we want to get its canonical path - that is, we follow the link to
		// the file it's actually linked to
		File candir;
		try {
			candir = dir.getCanonicalFile();
		} catch (IOException e) {
			return false;
		}

		// a symbolic link has a different canonical path than its actual path,
		// unless it's a link to itself
		if (!candir.equals(dir.getAbsoluteFile())) {
			// this file is a symbolic link, and there's no reason for us to
			// follow it, because then we might be deleting something outside of
			// the directory we were told to delete
			return false;
		}

		// now we go through all of the files and subdirectories in the
		// directory and delete them one by one
		File[] files = candir.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				File file = files[i];

				// in case this directory is actually a symbolic link, or it's
				// empty, we want to try to delete the link before we try
				// anything
				boolean deleted = file.delete();
				if (!deleted) {
					// deleting the file failed, so maybe it's a non-empty
					// directory
					if (file.isDirectory())
						deleteDir(file);

					// otherwise, there's nothing else we can do
				}
			}
		}

		// now that we tried to clear the directory out, we can try to delete it
		// again
		return dir.delete();
	}

	public SVNSynchronizer setupTest(String name) {
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
			    client.relocate(oldRepoUrl, repoUrl, path, false);
			}
		} catch (ClientException e) {
            throw new RuntimeException("could not setup test " + name, e);
		}

        SVNSynchronizer syncer = new SVNSynchronizer(repoUrl, wcPath, "", "", new AutomaticConflictHandler(wcPath));
        syncer.setNotifyListener(this);
        return syncer;
	}
	
	@Test
	public void testAddAddConflict() {
		SVNSynchronizer helper = setupTest("add_add_conflict");

		helper.synchronize();

		deleteDir(new File("target/add_add_conflict/"));
	}

	@Test
	public void testAddInDeletedConflict() {
		SVNSynchronizer helper = setupTest("add_in_deleted_conflict");

		helper.synchronize();

		deleteDir(new File("target/add_in_deleted_conflict/"));
	}

	public void onNotify(NotifyInformation info) {
		System.out.println("SVNKIT " + SVNSynchronizer.notifyToString(info));
	}
}
