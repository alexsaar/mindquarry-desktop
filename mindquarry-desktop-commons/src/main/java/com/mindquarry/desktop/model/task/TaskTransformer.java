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
package com.mindquarry.desktop.model.task;

import org.dom4j.Element;
import org.dom4j.Node;

import com.mindquarry.desktop.model.ModelBase;
import com.mindquarry.desktop.model.TransformerBase;

import dax.Path;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class TaskTransformer extends TransformerBase {
    private Task task;

    /**
     * {@inheritJavaDoc}
     * 
     * @see com.mindquarry.minutes.editor.model.tranformer.TransformerBase#handleModelPart(com.mindquarry.minutes.editor.model.ModelBase)
     */
    @Override
    protected void handleModelPart(ModelBase model) {
        task = (Task) model;
    }

    @Path("/task")
    public void task(Node node) {
        if (node instanceof Element) {
            Element element = (Element) node;
            task.setId(element.attribute("base").getStringValue()); //$NON-NLS-1$
        }
        applyTemplates(node);
    }

    @Path("title")
    public void title(Node node) {
        task.setTitle(node.getStringValue().trim());
    }

    @Path("status")
    public void status(Node node) {
        task.setStatus(node.getStringValue().trim());
    }

    @Path("priority")
    public void priority(Node node) {
        task.setPriority(node.getStringValue().trim());
    }

    @Path("summary")
    public void summary(Node node) {
        task.setSummary(node.getStringValue().trim());
    }

    @Path("description")
    public void description(Node node) {
        task.setDescription(node.getStringValue().trim());
    }

    @Path("date")
    public void date(Node node) {
        task.setDate(node.getStringValue().trim());
    }
}
