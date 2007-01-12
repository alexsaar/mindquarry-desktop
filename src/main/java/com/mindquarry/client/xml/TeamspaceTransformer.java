/*
 * Copyright (C) 2005-2006 MindQuarry GmbH, All Rights Reserved
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
package com.mindquarry.client.xml;

import org.dom4j.Node;

import dax.Path;
import dax.Transformer;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class TeamspaceTransformer extends Transformer {
    private String name = null;
    
    private String workspace = null;

    @Override
    public void init() {
        name = null;
        workspace = null;
    }

    @Path("//teamspace")
    public void teamspace(Node node) {
        applyTemplates(node);
    }
    
    @Path("workspace")
    public void workspace(Node node) {
        workspace = node.getStringValue().trim();
    }
    
    @Path("name")
    public void name(Node node) {
        name = node.getStringValue().trim();
    }

    public String getName() {
        return name;
    }

    public String getWorkspace() {
        return workspace;
    }
}
