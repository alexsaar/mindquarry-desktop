/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.xml;

import java.util.HashMap;

import org.dom4j.Node;

import dax.Path;
import dax.Transformer;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class TeamListTransformer extends Transformer {
    private HashMap<String, String> teamspaces;

    private String id = null;

    public TeamListTransformer(HashMap<String, String> teamspaces) {
        this.teamspaces = teamspaces;
    }

    @Override
    public void init() {
        teamspaces.clear();
    }

    @Path("//teamspace")
    public void teamspace(Node node) {
        applyTemplates(node);
    }

    @Path("id")
    public void id(Node node) {
        id = node.getStringValue().trim();
    }

    @Path("workspace")
    public void workspace(Node node) {
        teamspaces.put(id, node.getStringValue().trim());
    }
}
