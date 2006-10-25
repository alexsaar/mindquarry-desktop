/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.util;

import org.apache.commons.io.IOUtils;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class RegUtil {
    public static final String DOC_DIR_QUERY = "reg query \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders\" /v Personal"; //$NON-NLS-1$

    public static final String REGSTR_TOKEN = "REG_SZ"; //$NON-NLS-1$

    public static String getMyDocumentsFolder() {
        Process process;
        try {
            process = Runtime.getRuntime().exec(RegUtil.DOC_DIR_QUERY);
            process.waitFor();

            String result = IOUtils.toString(process.getInputStream());
            int p = result.indexOf(RegUtil.REGSTR_TOKEN);

            String workspaceDirName = result.substring(
                    p + RegUtil.REGSTR_TOKEN.length()).trim()
                    + "/MindClient"; //$NON-NLS-1$
            return (workspaceDirName);
        } catch (Exception e) {
            return null;
        }
    }
}
