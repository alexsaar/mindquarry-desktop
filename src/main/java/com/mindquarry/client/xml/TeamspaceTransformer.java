/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.xml;

import java.util.List;

import org.dom4j.Element;
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
