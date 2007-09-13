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

import java.lang.reflect.Method;
import java.util.Set;
import java.util.prefs.Preferences;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tmatesoft.svn.core.internal.wc.SVNFileUtil;

/**
 * Set autostart registry entries on Windows platforms.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class AutostartUtilities {
    private static Log log = LogFactory.getLog(AutostartUtilities.class);

    private final static int KEY_ALL_ACCESS = 0xf003f;
    private final static String AUTOSTART_KEY = "Software\\Microsoft\\Windows\\CurrentVersion\\Run"; //$NON-NLS-1$
    private final static String AUTOSTART_ENTRY = "MQDESKTOPCLIENT"; //$NON-NLS-1$

    private AutostartUtilities() {
      // static methods only, no public constructor
    }
    
    public static void setAutostart(boolean autostart, Set<String> targetPatterns) {
        // check if we are on a Windows platform, otherwise skip processing
        boolean windows = SVNFileUtil.isWindows;
        if (!windows) {
        	log.debug("not setting autostart, os is not windows: " + 
        	        System.getProperty("os.name"));
            return;
        }
        log.debug("setting autostart, os: " + System.getProperty("os.name"));
        // registry variables

        // create registry method objects
        final Preferences userRoot = Preferences.userRoot();
        final Class clz = userRoot.getClass();

        try {
            // define methods for registry manipulation
            final Method mOpenKey = clz.getDeclaredMethod("openKey", //$NON-NLS-1$
                    new Class[] { byte[].class, int.class, int.class });
            mOpenKey.setAccessible(true);

            final Method mCloseKey = clz.getDeclaredMethod("closeKey", //$NON-NLS-1$
                    new Class[] { int.class });
            mCloseKey.setAccessible(true);

            final Method mWinRegSetValue = clz.getDeclaredMethod(
                    "WindowsRegSetValueEx", //$NON-NLS-1$
                    new Class[] { int.class, byte[].class, byte[].class });
            mWinRegSetValue.setAccessible(true);

            final Method mWinRegDeleteValue = clz.getDeclaredMethod(
                    "WindowsRegDeleteValue", //$NON-NLS-1$
                    new Class[] { int.class, byte[].class });
            mWinRegDeleteValue.setAccessible(true);

            // open registry key
            Integer hSettings = (Integer) mOpenKey.invoke(userRoot,
                    new Object[] { toByteArray(AUTOSTART_KEY),
                            new Integer(KEY_ALL_ACCESS),
                            new Integer(KEY_ALL_ACCESS) });

            // check autostart settings
            if (autostart) {
                // find classpath entry for desktop client JAR:
                String path = JarUtilities.getJar(targetPatterns);
                // write autostart value
                mWinRegSetValue.invoke(userRoot, new Object[] { hSettings,
                        toByteArray(AUTOSTART_ENTRY), toByteArray(path) });
            } else {
                // delete autostart entry
                mWinRegDeleteValue.invoke(userRoot, new Object[] { hSettings,
                        toByteArray(AUTOSTART_ENTRY) });
            }
            // close registry key
            mCloseKey
                    .invoke(Preferences.userRoot(), new Object[] { hSettings });
        } catch (Exception e) {
            // TODO: throw exception and catch it outside to display an error dialog
            log.error("Error while writing registry entries.", e); //$NON-NLS-1$
        }
    }

    private static byte[] toByteArray(String str) {
        byte[] result = new byte[str.length() + 1];
        for (int i = 0; i < str.length(); i++) {
            result[i] = (byte) str.charAt(i);
        }
        result[str.length()] = 0;
        return result;
    }
}
