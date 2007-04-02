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
package com.mindquarry.desktop.pool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import com.mindquarry.desktop.preferences.PreferenceUtilities;

/**
 * Abstract base class for all desktop pools. Desktop pools are used for storing
 * entity that can not be stored due to connection problems and which have to be
 * synchronized later.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public abstract class PoolBase {
    protected Log log;
    
    public static final String POOL_FOLDER = "pool"; //$NON-NLS-1$

    protected String name;

    protected File poolFolder;

    public PoolBase(String name) {
        this.name = name;
        initPool();
    }

    private void initPool() {
        log = LogFactory.getLog(this.getClass());
        poolFolder = new File(PreferenceUtilities.SETTINGS_FOLDER + "/" //$NON-NLS-1$
                + POOL_FOLDER + "/" + name); //$NON-NLS-1$
        if (!poolFolder.exists()) {
            poolFolder.mkdirs();
        }
    }

    public void addEntry(String id, Document doc) throws IOException {
        File entryFile = new File(poolFolder.getAbsolutePath() + "/" + id); //$NON-NLS-1$
        FileOutputStream fos = new FileOutputStream(entryFile);
        fos.write(doc.asXML().getBytes());
        fos.flush();
        fos.close();
    }

    public Map<String, Document> getEntries() {
        Map<String, Document> results = new HashMap<String, Document>();

        File[] files = poolFolder.listFiles();
        for (File file : files) {
            String id = file.getName();

            try {
                StringBuffer contents = new StringBuffer();
                BufferedReader input = new BufferedReader(new FileReader(file));
                String line = null;

                while ((line = input.readLine()) != null) {
                    contents.append(line);
                }
                Document doc = DocumentHelper.parseText(contents.toString());
                results.put(id, doc);
            } catch (Exception e) {
                log.error("Error while fetching pool entries.", e); //$NON-NLS-1$
            }
        }
        return results;
    }
}
