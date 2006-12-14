/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.xml;

import org.dom4j.Element;
import org.dom4j.Node;

import com.mindquarry.client.task.Task;

import dax.Path;
import dax.Transformer;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class TaskTransformer extends Transformer {
    private Task task = null;
    
    @Override
    public void init() {
        task = new Task();
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

    public Task getTask() {
        return task;
    }
}
