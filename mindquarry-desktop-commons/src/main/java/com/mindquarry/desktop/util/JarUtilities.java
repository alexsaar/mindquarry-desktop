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
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * Helper method to work with JAR files.
 * 
 * @author dnaber
 */
public class JarUtilities {

    private JarUtilities() {
      // static methods only, no public constructor
    }

    /**
     * Get the full path to the first JAR in classpath that contains 
     * <tt>pattern</tt> as a substring.
     * @throws IOException if pattern cannot be found
     */
    public static String getJar(String pattern) throws IOException {
      Set<String> patterns = new HashSet<String>();
      patterns.add(pattern);
      return getJar(patterns);
    }
    
    /**
     * Get the full path to the first JAR in classpath that contains 
     * one of <tt>patterns</tt> as a substring.
     * @throws IOException if no pattern can be found
     */
    public static String getJar(Set<String> patterns) throws IOException {
        String path = null;
        String classpath = System.getProperty("java.class.path");  //$NON-NLS-1$
        String[] cpEntries = classpath.split(File.pathSeparator);
        for (String cpEntry : cpEntries) {
          for (String targetPattern : patterns) {
            if (cpEntry.contains(targetPattern)) {
                path = cpEntry;
                break;
            }
          }
        }
        if (path == null) {
          throw new IOException("Could not find JAR in classpath. " +
            "Expected one of these JARs: " + patterns +
            ", Classpath: " + classpath);
        }
        return path;
    }

    /**
     * Return the "Version" entry from the given JAR's manifest file.
     * @throws IOException
     */
    public static String getVersion(String jarFileName) throws IOException {
      JarFile jarFile = new JarFile(jarFileName);
      return jarFile.getManifest().getMainAttributes().getValue("Version");
    }

    /**
     * Return the "buildDate" entry from the given JAR's manifest file.
     * @throws IOException
     */
    public static String getBuildDate(String jarFileName) throws IOException {
      JarFile jarFile = new JarFile(jarFileName);
      return jarFile.getManifest().getMainAttributes().getValue("buildDate");
    }

}
