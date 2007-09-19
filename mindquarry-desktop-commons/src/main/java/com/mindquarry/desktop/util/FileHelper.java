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

package com.mindquarry.desktop.util;

import java.io.File;

import java.io.IOException;

/**
 * Some File helper tools.
 * 
 * @author dnaber
 */
public class FileHelper {
    
    private FileHelper() {
        // static methods only
    }

    public static void renameTo(File from, File to) throws IOException {
        if (!from.renameTo(to)) {
            throw new IOException("Could not rename '" + from.getCanonicalPath() + "' to '" + to.getCanonicalPath() + "'");
        }
    }

    public static void delete(File file) throws IOException {
        if (!file.delete()) {
            throw new IOException("Could not delete '" + file.getCanonicalPath() + "'");
        }
    }

    public static void mkdir(File dir) throws IOException {
        if (!dir.mkdir()) {
            throw new IOException("Could not create dir '" + dir.getCanonicalPath() + "'");
        }
    }

    public static void mkdirs(File dir) throws IOException {
        if (!dir.mkdirs()) {
            throw new IOException("Could not create dirs '" + dir.getCanonicalPath() + "'");
        }
    }

    /**
     * Checks if the file path 'parentPath' is a filesystem parent of the path
     * 'childPath'. Uses the java File api to check that correctly.
     */
    public static boolean isParent(String parentPath, String childPath) {
        File parent = new File(parentPath);
        File child = new File(childPath);
    
        while ((child = child.getParentFile()) != null) {
            if (child.equals(parent)) {
                return true;
            }
        }
        return false;
    }
}
