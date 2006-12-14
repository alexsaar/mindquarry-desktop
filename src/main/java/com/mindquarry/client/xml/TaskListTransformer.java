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
