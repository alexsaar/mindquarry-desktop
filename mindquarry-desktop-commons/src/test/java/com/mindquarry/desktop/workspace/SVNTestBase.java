/*
 * Copyright (C) 2006-2007 Mindquarry GmbH, All Rights Reserved
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 */
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
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
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
import com.mindquarry.desktop.workspace.conflict.ConflictHandler;

/**
 * Base class for doing svn tests. Provides zip-extraction with repo and working
 * copies prepared and also manual creation of repository and checkout for
 * creating test scenarios (the latter is recommended).
 * 
 * @author <a href="mailto:alexander(dot)klimetschek(at)mindquarry(dot)com">
 *         Alexander Klimetschek</a>
 *
 */
public class SVNTestBase implements Notify2 {

    protected SVNClientImpl client = SVNClientImpl.newInstance();
    protected String wcPath; // working dir for sync, created by
    protected String wcPathRemote;
    protected String repoUrl;

    /**
     * Extracts the zip file <code>zipName</code> into the folder
     * <code>destinationPath</code>.
     */
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

    /**
     * Helper method that simply copies over the bytes from the InputStream to
     * the Outputstream by using the given byte array buffer.
     */
    private void transferBytes(InputStream in, OutputStream out, byte[] buffer)
            throws IOException {
        int nRead;
        while (-1 != (nRead = in.read(buffer, 0, buffer.length))) {
            out.write(buffer, 0, nRead);
        }
    }

    /**
     * Print out all messages from the svn client on sysout.
     */
    public void onNotify(NotifyInformation info) {
        System.out.println("SVNKIT " + SVNSynchronizer.notifyToString(info));
    }

    /**
     * Creates repo and checkout based on zip file name.zip
     */
    public void setupZipTest(String name) throws IOException {
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
    public void setupZipTestWithFreshCheckout(String name) throws IOException,
            ClientException {
        System.out.println("Testing "
                        + name
                        + " ("
                        + SVNAdminDirectoryLocator.SHALLOW_DIR_REF_FILENAME
                        + ") ============================================================");
        String zipPath = name + ".zip";
        String targetPath = "target/" + name + "/";
        this.repoUrl = "file://"
                + new File(targetPath + "/repo").toURI().getPath();
        this.wcPath = targetPath + "wc";

        extractZip(zipPath, targetPath);

        client.checkout(repoUrl, wcPath, Revision.HEAD, true);
    }

    /**
     * Creates a fresh new repository without the need for a zip file and checks
     * out a working copy under "wc".
     */
    public void setupCleanRepoWithFreshCheckout(String name) throws SVNException, ClientException,
            IOException {
        System.out.println("Testing "
                        + name
                        + " ("
                        + SVNAdminDirectoryLocator.SHALLOW_DIR_REF_FILENAME
                        + ") ============================================================");
        String targetPath = "target/" + name + "/";

        File repo = new File(targetPath + "/repo").getAbsoluteFile();
        FileUtils.deleteDirectory(repo);
        this.repoUrl = "file://" + repo.toURI().getPath();
        SVNRepositoryFactory.createLocalRepository(repo, null, false, false,
                false);

        this.wcPath = targetPath + "wc";
        FileUtils.deleteDirectory(new File(wcPath));
        client.checkout(repoUrl, wcPath, Revision.HEAD, true);
    }

    /**
     * Checks out another working copy under "remote".
     */
    public void checkoutRemote(String name) throws SVNException,
            ClientException, IOException {
        String targetPath = "target/" + name + "/";
        this.wcPathRemote = targetPath + "remote";
        FileUtils.deleteDirectory(new File(wcPathRemote));
        client.checkout(repoUrl, wcPathRemote, Revision.HEAD, true);
    }

    /**
     * Sets up the SVNSynchronizer for a test to run on the local working copy
     * (wcPath) and to use the given ConflictHandler for dealing with conflicts.
     */
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
    protected void assertFileContains(String relative, String substring) {
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
    @SuppressWarnings("unchecked")
    public Iterator findFiles(File dir, String pattern) {
        return FileUtils.iterateFiles(dir, new WildcardFileFilter(pattern), TrueFileFilter.INSTANCE);
    }

    /**
     * Asserts that no .svnref file is present inside dir or its subfolders.
     */
    @SuppressWarnings("unchecked")
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
    
    
    // Working copy file / directory manipulation methods

    /**
     * Creates a file inside the working copy (wcPath).
     */
    protected void touch(String relativePath) throws IOException {
        FileUtils.touch(new File(wcPath + "/" + relativePath));
    }

    /**
     * Creates a file inside the 'remote' working copy (wcPathRemote).
     */
    private void touchRemote(String relativePath) throws IOException {
        FileUtils.touch(new File(wcPathRemote + "/" + relativePath));
    }

    /**
     * Modifies a file inside the working copy (wcPath) by writing the current time into it.
     */
    protected void modify(String relativePath) throws IOException {
        FileUtils.writeStringToFile(new File(wcPath + "/" + relativePath), "time " + System.currentTimeMillis());
    }

    /**
     * Modifies a file inside the 'remote' working copy (wcPathRemote) by writing the current time into it.
     */
    protected void modifyRemote(String relativePath) throws IOException {
        FileUtils.writeStringToFile(new File(wcPathRemote + "/" + relativePath), "time " + System.currentTimeMillis());
    }

    /**
     * Replaces a file or directory inside the working copy (wcPath) (svn del + svn add).
     */
    protected void replace(String relativePath, boolean isFile)
            throws ClientException, IOException {
        String path = wcPath + "/" + relativePath;
        client.remove(new String[] { path }, null, false);
        if (isFile) {
            touch(relativePath);
        }
        client.add(path, false);
    }

    /**
     * Replaces a file or directory inside the 'remote' working copy (wcPathRemote) (svn del + svn add).
     */
    protected void replaceRemote(String relativePath, boolean isFile)
            throws ClientException, IOException {
        String path = wcPathRemote + "/" + relativePath;
        client.remove(new String[] { path }, null, false);
        if (isFile) {
            touchRemote(relativePath);
        }
        client.add(path, false);
    }

    /**
     * Schedules an existing file or directory for addition to svn inside the working copy (wcPath) (svn add).
     */
    protected void add(String relativePath) throws ClientException {
        String path = wcPath + "/" + relativePath;
        client.add(path, true);
    }

    /**
     * Schedules an existing file or directory for addition to svn inside the 'remote' working copy (wcPathRemote) (svn add).
     */
    protected void addRemote(String relativePath) throws ClientException {
        String path = wcPathRemote + "/" + relativePath;
        client.add(path, true);
    }

    /**
     * Schedules a file or directory for deletion from svn inside the working copy (wcPath) (svn del).
     */
    protected void del(String relativePath) throws ClientException {
        String path = wcPath + "/" + relativePath;
        client.remove(new String[] { path }, null, false);
    }

    /**
     * Schedules a file or directory for deletion from svn inside the 'remote' working copy (wcPathRemote) (svn del).
     */
    protected void delRemote(String relativePath) throws ClientException {
        String path = wcPathRemote + "/" + relativePath;
        client.remove(new String[] { path }, null, false);
    }

    /**
     * Renames an existing file or directory inside the working copy (wcPath).
     */
    protected void move(String from, String to) throws IOException {
        File file = new File(wcPath, from);
        File toFile = new File(wcPath, to);
        if (!file.renameTo(toFile)) {
            throw new IOException("Could not rename '" + from + "' to '" + to + "'");
        }
    }

    /**
     * Renames an existing file or directory inside the 'remote' working copy (wcPathRemote).
     */
    protected void moveRemote(String from, String to) throws IOException {
        File file = new File(wcPathRemote, from);
        File toFile = new File(wcPathRemote, to);
        if (!file.renameTo(toFile)) {
            throw new IOException("Could not rename '" + from + "' to '" + to + "'");
        }
    }

    /**
     * Deletes an existing file or directory inside the working copy (wcPath) (physically removed).
     */
    protected void remove(String relativePath) throws IOException {
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

    /**
     * Deletes an existing file or directory inside the 'remote' working copy (wcPathRemote) (physically removed).
     */
    protected void removeRemote(String relativePath) throws IOException {
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

    /**
     * Creates a directory (full path) inside the working copy (wcPath) (mkdir -p).
     */
    protected void mkdir(String relativePath) throws IOException {
        File file = new File(wcPath, relativePath);
        if (!file.mkdirs()) {
            throw new IOException("Could not mkdirs '" + file + "'");
        }
    }

    /**
     * Deletes an existing file or directory inside the 'remote' working copy (wcPathRemote) (physically removed).
     */
    protected void mkdirRemote(String relativePath) throws IOException {
        File file = new File(wcPath, relativePath);
        if (!file.mkdirs()) {
            throw new IOException("Could not mkdirs '" + file + "'");
        }
    }

}