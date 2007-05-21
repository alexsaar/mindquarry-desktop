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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.dom4j.Node;

import com.mindquarry.desktop.model.ModelBase;
import com.mindquarry.desktop.model.TransformerBase;

import dax.Path;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class TaskListTransformer extends TransformerBase {
    private Log log;

    private TaskList taskList = null;

    private String baseURL;

    private String login;

    private String password;

    public TaskListTransformer(String login, String password) {
        this.login = login;
        this.password = password;

        log = LogFactory.getLog(TaskListTransformer.class);
    }

    @Override
    protected void handleModelPart(ModelBase model) {
        taskList = (TaskList) model;
    }

    @Path("/tasks")
    public void tasks(Node node) {
        log.info("Tasks element found. Trying to evaluate children."); //$NON-NLS-1$
        if (node instanceof Element) {
            Element element = (Element) node;
            baseURL = element.attribute("base").getStringValue(); //$NON-NLS-1$
        }
        applyTemplates();
    }

    @Path("task")
    public void task(Node node) {
        log.info("Found new task element."); //$NON-NLS-1$
        if (node instanceof Element) {
            Element element = (Element) node;

            log.info("Trying to add task from '" //$NON-NLS-1$
                    + element.attribute("href").getStringValue() //$NON-NLS-1$
                    + "'."); //$NON-NLS-1$
            taskList.add(baseURL + element.attribute("href").getStringValue(), //$NON-NLS-1$ 
                    login, password);
        }
    }

    public TaskList getTaskURIs() {
        return taskList;
    }
}
