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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.mindquarry.desktop.util.TranslationMessageParser;

/**
 * Loads the translation for the current default locale from an XML
 * file in the classpath, falls back to English if there's no translation.
 * 
 * @author dnaber
 */
public class Messages {

    protected static final String BUNDLE_FILE_BASE = "/com/mindquarry/desktop/client/messages_"; //$NON-NLS-1$
    protected static final String BUNDLE_FILE_SUFFIX = ".xml"; //$NON-NLS-1$

    protected static Map<String, String> translationMap = null;

    public static String getString(String key) {
        return getString(key, (String[])null);
    }
    
    private static String getString(String key, String[] args) {
        try {
            if (translationMap == null) {
                initTranslationMap();
            }
            String translation = translationMap.get(key);
            if (translation == null) {
                return key;
            }
            return translation;
        } catch (Exception e) {
            throw new RuntimeException("No translation found for '" +key+ "': " 
                    + e.toString(), e);
        }
    }
    
    private static void initTranslationMap() throws IOException, SAXException, ParserConfigurationException {
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        parserFactory.setValidating(false);
        SAXParser parser = parserFactory.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        TranslationMessageParser translationParser = new TranslationMessageParser();
        reader.setContentHandler(translationParser);
        reader.setErrorHandler(translationParser);
        // TODO: use "xx_YY" if available, use "xx" otherwise:
        String transFile = BUNDLE_FILE_BASE + Locale.getDefault().getLanguage() + BUNDLE_FILE_SUFFIX;
        InputStream is = Messages.class.getResourceAsStream(transFile);
        if (is == null) {
            // no translation available for this language
            translationMap = new HashMap<String, String>();
            return;
        }
        reader.parse(new InputSource(is));
        translationMap = translationParser.getMap();
    }
        
}
