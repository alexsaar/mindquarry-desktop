package com.mindquarry.desktop.util;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parse Qt's "*.ts" files, building an "English text -> translated text" map.
 * 
 * @author dnaber
 */
public class TranslationMessageParser extends DefaultHandler {
    
    private Map<String, String> translationMap = new HashMap<String, String>();
    
    private boolean inSource = false;
    private boolean inTranslation = false;
    private StringBuilder sourceSB = new StringBuilder();
    private StringBuilder translationSB = new StringBuilder();
    
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("source".equals(qName)) {
            inSource = true;
        } else if ("translation".equals(qName)) {
            inTranslation = true;
        }
    }
    
    public Map<String, String> getMap() {
        return translationMap;
    }
    
    public void endElement(String uri, String localName, String qName) {
        if ("source".equals(qName)) {
            inSource = false;
        } else if ("translation".equals(qName)) {
            translationMap.put(sourceSB.toString().trim(), translationSB.toString().trim());
            sourceSB = new StringBuilder();
            translationSB = new StringBuilder();
            inSource = false;
            inTranslation = false;
        }        
    }
    
    public void characters(char[] ch, int start, int length) {
        if (inSource) {
            sourceSB.append(ch, start, length);
        } else if (inTranslation) {
            translationSB.append(ch, start, length);
        }
    }
    
    public void error(SAXParseException e) throws SAXException {
        throw e;
    }
    
    public void fatalError(SAXParseException e) throws SAXException {
        throw e;
    }
    
}
