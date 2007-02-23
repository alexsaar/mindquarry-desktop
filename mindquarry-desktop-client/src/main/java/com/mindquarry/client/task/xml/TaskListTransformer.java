/*
 * Copyright (C) 2006-2007 MindQuarry GmbH, All Rights Reserved
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
package com.mindquarry.client.task.xml;

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
public class TaskListTransformer extends Transformer {
    private List<String> taskURIs = new ArrayList<String>();

    @Override
    public void init() {
        taskURIs.clear();
    }

    @Path("//task")
    public void task(Node node) {
        if (node instanceof Element) {
            Element element = (Element) node;
            taskURIs.add(element.attribute("href").getStringValue()); //$NON-NLS-1$
        }
    }

    public List<String> getTaskURIs() {
        return taskURIs;
    }
}
