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
package com.mindquarry.desktop;

import java.io.InputStream;
import java.util.Locale;

import org.apache.commons.i18n.MessageManager;
import org.apache.commons.i18n.MessageNotFoundException;
import org.apache.commons.i18n.XMLMessageProvider;

/**
 * Message provider that uses Jakarta Commons i18n for translation of
 * internationalized messages.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class Messages {
    private static final String BUNDLE_FILE = "/com/mindquarry/desktop/messages.xml"; //$NON-NLS-1$

    private static final String BUNDLE_NAME = "com.mindquarry.desktop.messages"; //$NON-NLS-1$

    static {
        InputStream is = Messages.class.getResourceAsStream(BUNDLE_FILE);
        XMLMessageProvider.install(BUNDLE_NAME, is);
    }

    public static String getString(Class caller, String key) {
        return getString(caller.getName(), key);
    }

    public static String getString(String id, String key) {
        return getString(id, key, new Object[0]);
    }

    public static String getString(Class caller, String key, Object[] args) {
        return getString(caller.getName(), key, args);
    }

    public static String getString(String id, String key, Object[] args) {
        String result = null;
        try {
            result = MessageManager.getText(id, key, args, Locale.getDefault());
        } catch (MessageNotFoundException e) {
            result = MessageManager.getText(id, key, args, Locale.ENGLISH);
        }
        return result;
    }
}
