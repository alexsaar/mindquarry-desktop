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
package com.mindquarry.desktop.client;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads the translation for the current default locale from an XML
 * file in the classpath, falls back to English if there's no translation.
 * 
 * @author dnaber
 */
public class Messages extends com.mindquarry.desktop.Messages {

    private static final String BUNDLE_FILE_BASE = "/com/mindquarry/desktop/client/messages_"; //$NON-NLS-1$
    private static final String BUNDLE_FILE_SUFFIX = ".xml"; //$NON-NLS-1$

    private static Map<String, String> translationMap = null;

    public static String getString(String key) {
        return getString(key, new String[]{});
    }
    
    public static String getString(String key, String... args) {
        if (translationMap == null) {
            translationMap = initTranslationMap(BUNDLE_FILE_BASE, BUNDLE_FILE_SUFFIX);
        }
        String translation = getTranslation(key, translationMap);
        int i = 0;
        // "{n}" can be used as a placeholder in the message, it refers
        // to the n-th argument (n starts at 0):
        while (true) {
          Pattern p = Pattern.compile("\\{"+i+"\\}");
          Matcher m = p.matcher(translation);
          if (!m.find()) {
            break;
          }
          translation = m.replaceAll(args[i]);
          i++;
        }
        return translation;
    }
            
}
