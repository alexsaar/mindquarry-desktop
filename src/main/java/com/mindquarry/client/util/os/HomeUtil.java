/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.util.os;

import org.apache.commons.io.IOUtils;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class HomeUtil {
    public static final String DOC_DIR_QUERY = "reg query \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders\" /v Personal"; //$NON-NLS-1$

    public static final String REGSTR_TOKEN = "REG_SZ"; //$NON-NLS-1$

    public static final String TEAMSPACE_FOLDER_NAME = "Teamspaces"; //$NON-NLS-1$

    public static String getTeamspaceFolderWindows() {
        Process process;
        try {
            process = Runtime.getRuntime().exec(HomeUtil.DOC_DIR_QUERY);
            process.waitFor();

            String result = IOUtils.toString(process.getInputStream());
            int p = result.indexOf(HomeUtil.REGSTR_TOKEN);

            String workspaceDirName = result.substring(
                    p + HomeUtil.REGSTR_TOKEN.length()).trim()
                    + "/" + TEAMSPACE_FOLDER_NAME; //$NON-NLS-1$
            return (workspaceDirName);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getTeamspaceFolder() {
        String home = System.getProperty("user.home"); //$NON-NLS-1$
        return home + "/Documents/" + TEAMSPACE_FOLDER_NAME; //$NON-NLS-1$
    }

    public static String getSettingsFolder() {
        String home = System.getProperty("user.home"); //$NON-NLS-1$
        return home + "/.mindclient"; //$NON-NLS-1$
    }
}
