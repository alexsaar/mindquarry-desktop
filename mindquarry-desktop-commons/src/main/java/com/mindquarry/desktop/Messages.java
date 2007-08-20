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

    private static final String BUNDLE_FILE_BASE = "/com/mindquarry/desktop/messages_"; //$NON-NLS-1$
    private static final String BUNDLE_FILE_SUFFIX = ".xml"; //$NON-NLS-1$

    private static Map<String, String> translationMap = null;

    public static String getString(String key) {
        return getString(key, null);
    }
    
    private static String getString(String key, String[] args) {
        if (translationMap == null) {
            translationMap = initTranslationMap(BUNDLE_FILE_BASE, BUNDLE_FILE_SUFFIX);
        }
        String translation = translationMap.get(key);
        if (translation == null) {
            return key;
        }
        return translation;
    }
    
    protected static Map<String, String> initTranslationMap(String fileBase, String fileSuffix) {
        try {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parserFactory.setValidating(false);
            SAXParser parser = parserFactory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            TranslationMessageParser translationParser = new TranslationMessageParser();
            reader.setContentHandler(translationParser);
            reader.setErrorHandler(translationParser);
            // TODO: use "xx_YY" if available, use "xx" otherwise:
            String transFile = fileBase + Locale.getDefault().getLanguage() + fileSuffix;
            InputStream is = Messages.class.getResourceAsStream(transFile);
            if (is == null) {
                // no translation available for this language
                return new HashMap<String, String>();
            }
            reader.parse(new InputSource(is));
            return translationParser.getMap();
        } catch (Exception e) {
            throw new RuntimeException(e.toString(), e);
        }
    }
        
}
