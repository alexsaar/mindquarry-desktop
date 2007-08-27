/*
 * ====================================================================
 * Copyright (c) 2004-2007 TMate Software Ltd.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://svnkit.com/license.html.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package org.tmatesoft.svn.core.internal.wc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;


/**
 * @version 1.0
 * @author  TMate Software Ltd.
 */
public class SVNAdminDirectoryLocator {
    /**
     * Name of the hidden directory inside the user's home directory that
     * contains all shallow working copy directories.
     */
    public static String SHALLOW_BASEDIR_NAME = ".svndata";
    
    /**
     * File inside the shallow working copy dir that contains the full path
     * to the working copy to check for copies and moves.
     */
    public static String WC_PATH_FILENAME = ".wcpath";
    
    /**
     * File inside the working copy root which contains the name of the
     * directory inside the shallowWorkingCopyBaseDir
     */
    public static String SHALLOW_DIR_REF_FILENAME = ".svnref";

    /**
     * For configuring a differently named .svn file (embedded case)
     */
    private static String ourEmbeddedAdminDirectoryName;

    /**
     * Checks for /.svn and subfiles like /.svn/some/file
     */
    public static boolean isAdminFile(File file) {
        String path = file.getAbsolutePath().replace(File.separatorChar, '/');
        String adminDir = "/" + SVNAdminDirectoryLocator.getAdminDirectoryName();
        return path.lastIndexOf(adminDir + "/") > 0 || path.endsWith(adminDir);
    }

    /**
     * Checks for .svn or .svnref at the end
     */
    public static boolean isAdminResource(String path) {
        return SVNAdminDirectoryLocator.isAdminResource(new File(path));
    }

    /**
     * Checks for .svn or .svnref at the end
     */
    public static boolean isAdminResource(File file) {
        if (file == null) {
            return false;
        }
        
        return SVNAdminDirectoryLocator.getAdminDirectoryName().equals(file.getName()) ||
            SHALLOW_DIR_REF_FILENAME.equals(file.getName());
    }

    /**
     * Checks if the base paths contains an .svn directory
     */
    private static boolean hasEmbeddedAdminDirectory(File base) {
        if (base == null) {
            return false;
        }
        File adminDir = new File(base, SVNAdminDirectoryLocator.getAdminDirectoryName());
        return (adminDir.exists() && adminDir.isDirectory());
    }

    /**
     * Returns the name of the admin sub-directory, typically ".svn". It contains
     * the admin area, thus all information about the living working copy.
     */
    public static String getAdminDirectoryName() {
        if (ourEmbeddedAdminDirectoryName == null) {
            String defaultAdminDir = ".svn";
            if (SVNFileUtil.getEnvironmentVariable("SVN_ASP_DOT_NET_HACK") != null){
                defaultAdminDir = "_svn";
            }
            ourEmbeddedAdminDirectoryName = System.getProperty("svnkit.admindir", System.getProperty("javasvn.admindir", defaultAdminDir));
            if (ourEmbeddedAdminDirectoryName == null || "".equals(ourEmbeddedAdminDirectoryName.trim())) {
                ourEmbeddedAdminDirectoryName = defaultAdminDir;
            }
        }
        return ourEmbeddedAdminDirectoryName;
    }

    public static void setEmbeddedAdminDirectoryName(String name) {
        ourEmbeddedAdminDirectoryName = name;
    }
    
    /**
     * This will either return the embedded .svn directory as File object or 
     * the shallow admin directory located in the user's home dir. The latter
     * will do a parent directory traversal to find the working copy root, in
     * which the .svnref file pointing to the shallow directory is located.
     * @param dir the working copy dir for which the admin directory should be
     *            found
     * @param create if true, will create the shallow admin directory in case it
     *               is not present yet - otherwise it will return null
     */
    public static File getAdminDirectory(File dir, boolean create) {
        // we need absolute path handling since we might go up to the filesystem root
        if (dir == null) {
            dir = new File("");
        }
        dir = dir.getAbsoluteFile();
        
        // some requests directly target the .svn sub directory
        // (SVNAdminArea.getVersion() implementations)
        if (isAdminResource(dir)) {
            return dir;
        }
        
        // check for old style .svn embedded directory
        if (hasEmbeddedAdminDirectory(dir)) {
            return new File(dir, getAdminDirectoryName()); 
        }
        
        // new style .svnref -> shallow admin directory in home dir
        WCRootInfo wcRootInfo = findWCRoot(dir);
        File adminSubDir;
        
        if (wcRootInfo.found) {
            // if this is a correct working copy, pass the reference to the admin dir
            adminSubDir = new File(
                    getShallowWorkingCopyBaseDir() + "/" +
                    wcRootInfo.shallowWorkingCopyDir + "/" +
                    wcRootInfo.wcRelativePath + "/" +
                    getAdminDirectoryName()
                );
        } else {
            if (!create) {
                return null;
            }
            // this can either be a checkout (then use the new .svnref)
            // or an add in the old .svn (we did not find a .svnref above)
            
            // check for a .svn-based wc (the parent would contain a .svn)
            if (hasEmbeddedAdminDirectory(dir.getParentFile())) {
                // add in an embedded .svn case
                adminSubDir = new File(dir, getAdminDirectoryName());
            } else {
                // new checkout, the shallow wc dir must be created
                adminSubDir = new File(createShallowWorkingCopyBaseDir(dir), getAdminDirectoryName());
            }
        }
        
        // ensure the admin directory exists
        if (create && !adminSubDir.exists()) {
            adminSubDir.mkdirs();
        }
        
        return adminSubDir;
    }

    /**
     * Returns for a normal file in the working copy the path relative to the
     * folder in which there is a corresponding wc admin directory available.
     * This is typically just the parent folder in the case of a file (not dir).
     * For example:
     *     file         = "/checkouts/foobar/folder/myfile.txt"
     *     wc admin dir = "/checkouts/foobar/folder/.svn"
     *     result       =                          "myfile.txt"
     *     
     *     (Note that wc admin dir might be a shallow working copy.)
     */
    public static String getRelativePathToClosestVersionedDir(File file) {
        // the admin dir indicating versioning will be located in or at the
        // same level as the file's parent
        File base = file.getParentFile();
        
        // first try to find the "closest versioned dir" by looking for
        // a corresponding shallow working copy dir
        SVNAdminDirectoryLocator.WCRootInfo wcRoot = findWCRoot(base);
        if (wcRoot.found) {
            final File shallowAdminBaseDir = new File(
                    getShallowWorkingCopyBaseDir() + "/" +
                    wcRoot.shallowWorkingCopyDir
                );
            
            File fullAdminSubDir = new File(
                    shallowAdminBaseDir,
                    wcRoot.wcRelativePath
                );
            // record the relative path as we go up
            String relativePath = "";
            
            // stop when we are at the root of the working copy
            while (!fullAdminSubDir.equals(shallowAdminBaseDir)) {
                // if the directory exists in the shallow working copy
                // (including an admin directory), we have reached the
                // "closest versioned dir" and calculate the relative path to it
                if (fullAdminSubDir.exists() && fullAdminSubDir.isDirectory()
                        && new File(fullAdminSubDir, getAdminDirectoryName()).exists()) {
                    
                    return relativePath + file.getName();
                }
                // go upwards
                relativePath = fullAdminSubDir.getName() + "/" + relativePath;
                fullAdminSubDir = fullAdminSubDir.getParentFile();
            }
            
            // ok, there seems to be only a trunk present
            return wcRoot.wcRelativePath;
        }
        
        // look out for old style .svn dirs
        while (base != null) {
            if (base.isDirectory()) {
                if (SVNAdminDirectoryLocator.hasEmbeddedAdminDirectory(base)) {
                    break;
                }
            }
            base = base.getParentFile();
        }
        String path = file.getAbsolutePath();
        if (base != null) {
            path = path.substring(base.getAbsolutePath().length());
        }
        path = path.replace(File.separatorChar, '/');
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }
    
    /**
     * Returns the base directory for the shallow working copy directories
     * inside the user's home dir.
     */
    public static String getShallowWorkingCopyBaseDir() {
        return System.getProperty("user.home") + "/" + SVNAdminDirectoryLocator.SHALLOW_BASEDIR_NAME;
    }    

    private static boolean warningPrinted = false;
    
    /**
     * Does a sanity check to detect corrupting moves or copies of the working
     * directory.
     */
    public static void checkShallowWorkingCopyDir(String shallowWorkingCopyDir, File wcRoot) {
        File wcPathFile = new File(
                getShallowWorkingCopyBaseDir() + "/" +
                shallowWorkingCopyDir + "/" +
                SVNAdminDirectoryLocator.WC_PATH_FILENAME
            );
        
        if (!wcPathFile.exists() || !wcPathFile.isFile()) {
            // TODO: throw svn exception
            System.out.println("Shallow working copy is corrupt");
            Thread.dumpStack();
            System.exit(-1);
        }
        
        try {
            File expectedWC = new File(readWorkingCopyPathFile(wcPathFile)).getCanonicalFile();
            wcRoot = wcRoot.getCanonicalFile();
            if (!expectedWC.equals(wcRoot)) {
                // TODO: this case forbids the move of a working copy
                // there is only a problem if the user copies the working copy
                // so that there are now two of them pointing to the same
                // shallow wc. if he now separately works in both wcs, it will
                // all mess up. but this is probably a very seldom case, so we
                // don't exit here.
                if (!warningPrinted) {
                    warningPrinted = true;
                    System.out.println("Warning: Shallow working copy '" + wcRoot.getPath() + "' was copied or moved from '" + expectedWC.getPath() + "'.");
                }
//                Thread.dumpStack();
//                System.exit(-1);            
            }
        } catch (IOException ioe) {
            // TODO: throw svn exception
            ioe.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Uses unique file creation mechanism to create a unique file under
     * <code>~/.svndata/</code>
     * @param prefix 
     */
    private static File generateUniqueShallowWorkingCopyDir(String prefix) {
        try {
            File uniqueDir = SVNFileUtil.createUniqueFile(
                    new File(getShallowWorkingCopyBaseDir()),
                    prefix, ""
                );
            
            uniqueDir.mkdirs();
            return uniqueDir;
        } catch (SVNException e) {
            // TODO: throw svn exception
            e.printStackTrace();
            System.exit(-1);
        }
        return null;
    }
    
    /**
     * Writes the .svnref file.
     */
    private static void writeShallowWorkingCopyDirReference(File wcDir, File shallowWorkingCopyDir) {
        File shallowRefFile = new File(wcDir, SVNAdminDirectoryLocator.SHALLOW_DIR_REF_FILENAME);
        
        FileOutputStream out;
        try {
            out = new FileOutputStream(shallowRefFile);
            PrintStream p = new PrintStream(out);
            p.println(shallowWorkingCopyDir.getName());
            p.close();

            // Hide file in Windows
            SVNFileUtil.setHidden(shallowRefFile, true);
        } catch (FileNotFoundException e) {
            // TODO: throw svn exception
            e.printStackTrace();
            System.exit(-1);
        }
    }
    
    /**
     * Reads the .svnref file.
     */
    private static String readShallowWorkingCopyDirReference(File refFile) {
        FileInputStream in;
        try {
            in = new FileInputStream (refFile);
            String shallowWorkingCopyDir = new BufferedReader(new InputStreamReader(in)).readLine();
            in.close();
            return shallowWorkingCopyDir;
        } catch (IOException e) {
            // TODO: throw svn exception
            e.printStackTrace();
            System.exit(-1);
        }
        
        return null;
    }
    
    /**
     * Writes the .wcpath sanity check file.
     */
    private static void writeWorkingCopyPathFile(File wcDir, File shallowWorkingCopyDir) {
        File wcPathFile = new File(shallowWorkingCopyDir, SVNAdminDirectoryLocator.WC_PATH_FILENAME);
        FileOutputStream out;
        try {
            out = new FileOutputStream(wcPathFile);
            PrintStream p = new PrintStream(out);
            p.println(wcDir.getAbsolutePath());
            p.close();
        } catch (FileNotFoundException e) {
            // TODO: throw svn exception
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Reads the .wcpath sanity check file.
     */
    private static String readWorkingCopyPathFile(File wcPathFile) {
        FileInputStream in;
        try {
            in = new FileInputStream (wcPathFile);
            String wcPath = new BufferedReader(new InputStreamReader(in)).readLine();
            in.close();
            return wcPath;
        } catch (IOException e) {
            // TODO: throw svn exception
            e.printStackTrace();
            System.exit(-1);
        }
        
        return null;
    }
    
    /**
     * Sets up .svndata with a unique wc.XYZ subdir for the shallow working copy
     * directory, containing a .wcpath sanity check file and creates the
     * .svnref file in the working copy root.
     */
    private static File createShallowWorkingCopyBaseDir(File wcDir) {
        File shallowWorkingCopyDir = generateUniqueShallowWorkingCopyDir(wcDir.getName());
        writeWorkingCopyPathFile(wcDir, shallowWorkingCopyDir);
        // during a checkout the working copy might not yet be created
        // FIXME: this might break the atomicity of a checkout (created just at
        // the beginning, if something fails, it will be there) ???
        if (!wcDir.exists()) {
            wcDir.mkdirs();
        }
        writeShallowWorkingCopyDirReference(wcDir, shallowWorkingCopyDir);
        return shallowWorkingCopyDir;
    }
    
    /**
     * Helper class for multi-return value containing the information for
     * an .svnref-style working copy root.
     */
    private class WCRootInfo {
        public boolean found = false;
        public String wcRelativePath = "";
        public String shallowWorkingCopyDir = "";
        public File wcRoot = null;
    }
    
    /**
     * Searches for the working copy root that contains a .svnref file pointing
     * to the shallow admin directory. If found, it returns both the relative
     * path from the wc root to the dir and the reference contained within
     * the ref file.
     */
    private static WCRootInfo findWCRoot(File dir) {
        WCRootInfo result = new SVNAdminDirectoryLocator().new WCRootInfo();
        
        // walk up the parents
        while (true) {
            // in each dir, look for the .svnref file
            File svnRef = new File(dir, SVNAdminDirectoryLocator.SHALLOW_DIR_REF_FILENAME);
            if (svnRef.exists() && svnRef.isFile()) {
                // if there is an .svnref, we have found the wc root 
                result.shallowWorkingCopyDir = readShallowWorkingCopyDirReference(svnRef);
                result.wcRoot = dir;
                // do sanity checks
                checkShallowWorkingCopyDir(result.shallowWorkingCopyDir, dir);
                break;
            }
            
            // if we walk up, we add one step more to the relative path
            result.wcRelativePath = dir.getName() + "/" + result.wcRelativePath;
            dir = dir.getParentFile();
            
            // check if the directory exists
            if (dir == null || !dir.exists()) {
                // we have reached the filesystem root without finding a .svnref
                return new SVNAdminDirectoryLocator().new WCRootInfo();
            }
        }
        
        result.found = true;
        return result;
    }
    
    /**
     * Looks for the working copy root of a wc with embedded .svn directories.
     * 
     * @param dir the directory to start
     * @return the working copy root or null if it could not be found
     */
    private static File findWCRootForEmbeddedAdminDir(File dir) {
        // if we start in a dir without a .svn, we aren't inside a wc at all
        if (!hasEmbeddedAdminDirectory(dir)) {
            return null;
        }
        
        // walk up the parents until we find a dir without an embedded .svn dir
        while (true) {
            File parent = dir.getParentFile();
            // if it is null or does not exist, we are at the root of the file system
            if (parent == null || !parent.exists() || !hasEmbeddedAdminDirectory(parent)) {
                // we found it
                break;
            }
            
            dir = parent;
        }
        return dir;
    }
    
    /**
     * Converts the working copy containing the given dir from the variant with
     * embedded .svn directories into the variant with the shallow wc. The
     * currently used variant is automatically detected.
     * 
     * @param dir   can be any dir inside the working copy; the entire working
     *              copy will be converted
     * @throws SVNException if something went wrong
     */
    public static void convert(File dir) throws SVNException {
        try {
            // make sure we have an absolute file name
            dir = dir.getCanonicalFile();
            
            System.out.println("Working dir: " + new File(".").getAbsolutePath());
            if (hasEmbeddedAdminDirectory(dir)) {
                // embedded .svn dirs
                System.out.println("Found embedded .svn dir, converting to shallow working copy.");
                
                File wcRoot = findWCRootForEmbeddedAdminDir(dir);
                System.out.println("Found wc root at '" + wcRoot.getCanonicalPath() + "'");
                convertEmbedded2Shallow(wcRoot);
            } else {
                // try to find wc root
                WCRootInfo wcRootInfo = findWCRoot(dir);
                if (!wcRootInfo.found) {
                    // TODO: throw SVNException
                    System.out.println("Cannot convert, '" + dir + "' is not a working copy.");
                    System.exit(-1);
                }
                System.out.println("Found wc root at '" + wcRootInfo.wcRoot.getCanonicalPath() + "'");
                System.out.println("Found shallow working copy '" + wcRootInfo.shallowWorkingCopyDir + "', converting to embedded .svn dir.");
                convertShallow2Embedded(wcRootInfo.wcRoot, new File(getShallowWorkingCopyBaseDir(), wcRootInfo.shallowWorkingCopyDir));
            }
        } catch (IOException e) {
            throw new SVNException(SVNErrorMessage.create(SVNErrorCode.IO_ERROR, "file problem during convert"), e);
        }
    }

    /**
     * Converts a working copy with embedded .svn directories into the variant
     * with a shallow working copy inside .svndata.
     * 
     * @param wcRoot the root of the working copy
     * @throws SVNException 
     */
    private static void convertEmbedded2Shallow(File wcRoot) throws SVNException {
        // create shallow wc (with .svnref)
        File shallowWC = createShallowWorkingCopyBaseDir(wcRoot);
        
        File svnRef = new File(wcRoot, SHALLOW_DIR_REF_FILENAME);
        System.out.println("created '" + svnRef + "'");
        
        try {
            // copy dir structure and move .svn dirs
            copySVNDirs(wcRoot, shallowWC);
        } catch (SVNException e) {
            // if copying failed, delete the shallow wc and stop
            SVNFileUtil.deleteAll(shallowWC, true);
            throw e;
        }
        // delete .svn dirs only after successful copying
        deleteEmbeddedSVNDirs(wcRoot);
        
    }

    /**
     * Deletes all .svn directories in the dir and below.
     */
    private static void deleteEmbeddedSVNDirs(File dir) {
        File adminDir = new File(dir, getAdminDirectoryName());
        if (adminDir.exists() && adminDir.isDirectory()) {
            System.out.println("removing '" + adminDir + "'");
            SVNFileUtil.deleteAll(adminDir, true);
        }
        // walk down the child directories
        File[] children = dir.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory() && !isAdminResource(child)) {
                    deleteEmbeddedSVNDirs(child);
                }
            }
        }
    }

    /**
     * Method that does recursive copying of .svn dirs.
     * 
     * @param fromDir  the source directory (must not be a file)
     * @param toDir    the target directory (can be non-existent)
     * @throws SVNException 
     */
    private static void copySVNDirs(File fromDir, File toDir) throws SVNException {
        File adminDir = new File(fromDir, getAdminDirectoryName());
        if (adminDir.exists() && adminDir.isDirectory()) {
            // copy over the .svn directory
            File toAdminDir = new File(toDir, getAdminDirectoryName());
            System.out.println("copying '" + adminDir + "' to '" + toAdminDir + "'");
            SVNFileUtil.copyDirectory(adminDir, toAdminDir, true, null);
        }
        
        // walk down the child directories
        File[] children = fromDir.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory() && !isAdminResource(child)) {
                    copySVNDirs(child, new File(toDir, child.getName()));
                }
            }
        }
    }

    private static void convertShallow2Embedded(File wcRoot, File shallowDir) throws SVNException {
        // copy .svn dirs from shallow to wc
        copySVNDirs(shallowDir, wcRoot);
        
        // remove .svnref
        File svnRef = new File(wcRoot, SHALLOW_DIR_REF_FILENAME);
        System.out.println("removing '" + svnRef + "'");
        SVNFileUtil.deleteFile(svnRef);
        
        // remove shallow wc
        System.out.println("removing '" + shallowDir + "'");
        SVNFileUtil.deleteAll(shallowDir, true);
    }


    /**
     * When using shallow working copies, this function deletes the shallow
     * directory corresponding to a given directory in the working copy. For
     * example, calling with "wc/A/B/" will delete the directory
     * ".svndata/repos/A/B" and all its sub-directories.
     * 
     * @param deletedDir
     *            Directory that was deleted in the working copy.
     */
    public static void cleanupDirDueToDeletion(File deletedDir) {
        File adminDir = getAdminDirectory(deletedDir, false);
        if (adminDir == null) {
            return;
        }
        File deleteDir = adminDir.getParentFile();

        // if using shallow working copies, the parent directory contains no
        // ".svn" directory
        if (!hasEmbeddedAdminDirectory(deletedDir.getParentFile())) {
            // TODO: remove comment
            System.out.println("Deleting '" + deleteDir.getPath() + "' ...");
            SVNFileUtil.deleteAll(deleteDir, true);
        }
    }
}
