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
        if (SVNAdminDirectoryLocator.isAdminResource(dir)) {
            return dir;
        }
        
        // check for old style .svn embedded directory
        if (SVNAdminDirectoryLocator.hasEmbeddedAdminDirectory(dir)) {
            return new File(dir, SVNAdminDirectoryLocator.getAdminDirectoryName()); 
        }
        
        // new style .svnref -> shallow admin directory in home dir
        SVNAdminDirectoryLocator.WCRootInfo wcRoot = findWCRoot(dir);
        File shallowAdminSubDir;
        
        if (wcRoot.found) {
            // if this is a correct working copy, pass the reference to the admin dir
            shallowAdminSubDir = new File(
                    getShallowWorkingCopyBaseDir() + "/" +
                    wcRoot.shallowWorkingCopyDir + "/" +
                    wcRoot.wcRelativePath + "/" +
                    getAdminDirectoryName()
                );
        } else {
            if (!create) {
                return null;
            }
            // upon checkout, the shallow wc dir must be created
            shallowAdminSubDir = new File(createShallowWorkingCopyBaseDir(dir), SVNAdminDirectoryLocator.getAdminDirectoryName());
        }
        
        // ensure the admin directory exists
        if (!shallowAdminSubDir.exists()) {
            shallowAdminSubDir.mkdirs();
        }
        
        return shallowAdminSubDir;
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
                // TODO: throw svn exception
                System.out.println("Shallow working copy '" + wcRoot.getPath() + "' was copied or moved from '" + expectedWC.getPath() + "'. Cannot proceed.");
                Thread.dumpStack();
                System.exit(-1);            
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
}
