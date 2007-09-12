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
package com.mindquarry.desktop.model.team;

import org.dom4j.Node;

import com.mindquarry.desktop.model.ModelBase;
import com.mindquarry.desktop.model.TransformerBase;

import dax.Path;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class TeamTransformer extends TransformerBase {
    private Team team = null;
    
    public TeamTransformer() {
    }

    @Override
    protected void handleModelPart(ModelBase model) {
        team = (Team) model;
    }

    @Path("/teamspace/workspace")
    public void workspace(Node node) {
        team.setWorkspaceURL(node.getStringValue().trim());
    }

    @Path("/teamspace/name")
    public void name(Node node) {
        team.setName(node.getStringValue().trim());
    }
    
    @Path("/teamspace/id")
    public void id(Node node) {
        team.setId(node.getStringValue().trim());
    }

    public Team getTeam() {
        return team;
    }
}
