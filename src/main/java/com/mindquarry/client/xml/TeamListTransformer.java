/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.xml;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.Node;

import dax.Path;
import dax.Transformer;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class TeamListTransformer extends Transformer {
    private List<String> teamspaces = new ArrayList<String>();

    @Override
    public void init() {
        teamspaces.clear();
    }

    @Path("//teamspace")
    public void teamspace(Node node) {
        applyTemplates(node);
        if (node instanceof Element) {
            Element element = (Element) node;
            teamspaces.add(element.attribute("href").getStringValue());
        }
    }

    public List<String> getTeamspaces() {
        return teamspaces;
    }
}
