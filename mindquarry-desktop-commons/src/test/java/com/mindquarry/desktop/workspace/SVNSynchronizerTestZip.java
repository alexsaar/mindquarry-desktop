package com.mindquarry.desktop.workspace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import junit.framework.TestCase;

import org.junit.Test;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.Notify2;
import org.tigris.subversion.javahl.NotifyInformation;
import org.tigris.subversion.javahl.Status;
import org.tigris.subversion.javahl.StatusKind;
import org.tigris.subversion.javahl.Status.Kind;

import com.mindquarry.desktop.workspace.conflict.AddConflict;
import com.mindquarry.desktop.workspace.conflict.AddInDeletedConflict;
import com.mindquarry.desktop.workspace.conflict.ConflictHandler;
import com.mindquarry.desktop.workspace.conflict.DeleteWithModificationConflict;
import com.mindquarry.desktop.workspace.exception.CancelException;

public class SVNSynchronizerTestZip implements Notify2, ConflictHandler {
	private void extractZip(String zipName, String destinationPath) {
		new File(destinationPath).mkdirs();
		try {
			byte[] buf = new byte[1024];
			ZipEntry zipEntry;
			ZipInputStream zipInputStream = new ZipInputStream(
					new FileInputStream(
							"src/test/resources/com/mindquarry/desktop/workspace/"
									+ zipName));

			zipEntry = zipInputStream.getNextEntry();
			while (zipEntry != null) {
				// for each entry to be extracted
				String entryName = zipEntry.getName();
				File newFile = new File(entryName);

				String directory = newFile.getParent();
				if (directory == null) {
					if (newFile.isDirectory())
						break;
				}

				directory = (new File(destinationPath + entryName)).getParent();
				new File(directory).mkdirs();
				FileOutputStream fileOutputStream = new FileOutputStream(
						destinationPath + entryName);

				int n;
				while ((n = zipInputStream.read(buf, 0, 1024)) > -1)
					fileOutputStream.write(buf, 0, n);

				fileOutputStream.close();
				zipInputStream.closeEntry();
				zipEntry = zipInputStream.getNextEntry();

			}

			zipInputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		String zipPath = name + ".zip";
		String targetPath = "target/" + name + "/";
		String repoUrl = "file://" + targetPath + "/repo";
		String wcPath = targetPath + "wc";

		extractZip(zipPath, targetPath);

		return new SVNSynchronizer(repoUrl, wcPath, "", "", this, this);

	}
	
	@Test
	public void testRemoveNestedAdds() throws ClientException {
		SVNSynchronizer helper = setupTest("add_add_conflict");
		
		List<Status> localChanges = helper.getLocalChanges();
		Status addedStatus = null;
		
		for(Status s : localChanges) {
			if(s.getTextStatus() == StatusKind.added) {
				addedStatus = s;
				break;
			}
		}
		
		TestCase.assertNotNull(addedStatus);
		
		helper.removeNestedAdds(addedStatus, localChanges);
		
		TestCase.assertEquals(2, localChanges.size());
		
		deleteDir(new File("target/add_add_conflict/"));
	}

//	@Test
//	public void testAddAddConflict() {
//		SVNSynchronizer helper = setupTest("add_add_conflict");
//
//		helper.synchronize();
//
//		deleteDir(new File("target/add_add_conflict/"));
//	}
//
//	@Test
//	public void testAddInDeletedConflict() {
//		SVNSynchronizer helper = setupTest("add_in_deleted_conflict");
//
//		helper.synchronize();
//
//		deleteDir(new File("target/add_in_deleted_conflict/"));
//	}

	public void onNotify(NotifyInformation info) {
		System.out.println(SVNSynchronizer.notifyToString(info));
	}

	public void handle(AddConflict conflict) throws CancelException {
		System.out.print("Rename locally added file/folder to: ");
		// FIXME: check for non-existing file/foldername
		conflict.doRename("renamed_dir");
	}

	public void handle(AddInDeletedConflict conflict) throws CancelException {
		conflict.doReAdd();
	}

	public void handle(DeleteWithModificationConflict conflict)
			throws CancelException {
		for (Status s : conflict.getRemoteMods()) {
			System.out.println("remote "
					+ Kind.getDescription(s.getRepositoryTextStatus()) + " "
					+ s.getPath());
		}
		conflict.doKeepModified();
	}
}
