/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.xml;

import java.util.HashMap;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class TeamlistContentHandler extends DefaultHandler {
    private final HashMap<String, String> workspaces;

    private boolean inWorkspace = false;

    private StringBuffer workspace = null;

    private boolean inWorkspaceID = false;

    private StringBuffer workspaceID = null;

    public TeamlistContentHandler(HashMap<String, String> workspaces) {
        this.workspaces = workspaces;
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        if (qName.equals("id")) {
            inWorkspaceID = true;
            workspaceID = new StringBuffer();
        }

        if (qName.equals("workspace")) {
            inWorkspace = true;
            workspace = new StringBuffer();
        }
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (qName.equals("id")) {
            inWorkspaceID = false;
        } else if (qName.equals("workspace")) {
            workspaces.put(workspaceID.toString().trim(), workspace.toString()
                    .trim());

            inWorkspace = false;
            workspace = null;

            workspaceID = null;
        }
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (inWorkspaceID && (workspaceID != null)) {
            workspaceID.append(new String(ch, start, length));
        }

        if (inWorkspace && (workspace != null)) {
            workspace.append(new String(ch, start, length));
        }
    }
}
