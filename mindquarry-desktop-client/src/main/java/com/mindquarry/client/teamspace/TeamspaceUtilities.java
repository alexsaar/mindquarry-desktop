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
package com.mindquarry.client.teamspace;

import java.io.InputStream;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import com.mindquarry.client.util.network.HttpUtilities;
import com.mindquarry.client.workspace.xml.TeamListTransformer;
import com.mindquarry.desktop.preferences.profile.Profile;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class TeamspaceUtilities {
    public static List<String> getTeamspaceNamesForProfile(Profile profile)
            throws Exception {
        InputStream content = HttpUtilities.getContentAsXML(profile.getLogin(),
                profile.getPassword(), profile.getServerURL() + "/teams"); //$NON-NLS-1$
        // parse teamspace list
        SAXReader reader = new SAXReader();
        Document doc = reader.read(content);

        // create a transformer for teamspace list
        TeamListTransformer listTrans = new TeamListTransformer();
        listTrans.execute(doc);

        return listTrans.getTeamspaces();
    }
}
