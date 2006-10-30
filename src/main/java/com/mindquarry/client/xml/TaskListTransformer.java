/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.xml;

import java.util.List;

import org.dom4j.Node;

import com.mindquarry.client.task.Task;

import dax.Path;
import dax.Transformer;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class TaskListTransformer extends Transformer {
    private List<Task> tasks;
    
    private String teamspaceID = null;
    
    private Task last = null;
    
    public TaskListTransformer(List<Task> tasks) {
        this.tasks = tasks;
    }

    @Override
    public void init() {
        tasks.clear();
    }

    @Path("//teamspace/id")
    public void teamspaceID(Node node) {
        teamspaceID = node.getStringValue().trim();
    }
    
    @Path("//task")
    public void task(Node node) {
        last = new Task();
        last.setTeamspace(teamspaceID);
        
        applyTemplates(node);
        
        tasks.add(last);
    }

    @Path("task/id")
    public void taskID(Node node) {
        last.setId(node.getStringValue().trim());
    }
    
    @Path("task/title")
    public void taskTitle(Node node) {
        last.setTitle(node.getStringValue().trim());
    }
    
    @Path("task/status")
    public void taskStatus(Node node) {
        last.setStatus(node.getStringValue());
    }

    public List<Task> getTasks() {
        return tasks;
    }
}
