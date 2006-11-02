/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.task;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import com.mindquarry.client.MindClient;
import com.mindquarry.client.util.HttpUtil;
import com.mindquarry.client.util.MessageDialogUtil;
import com.mindquarry.client.xml.TaskListTransformer;

/**
 * @author <a href="mailto:lars(dot)trieloff(at)mindquarry(dot)com">Lars
 *         Trieloff</a>
 */
public class TaskManager {
    public static final String TITLE_COLUMN = "title"; //$NON-NLS-1$

    private List<Task> tasks = new ArrayList<Task>();

    private List<TaskListChangeListener> listeners = new Vector<TaskListChangeListener>();

    private final MindClient client;

    public TaskManager(final MindClient client) {
        this.client = client;
    }

    public void startTask(Task t) {
        for (Task task : tasks) {
            task.setActive(false);
        }
        t.setActive(true);
    }
    
    public void stopTask(Task t) {
        t.setActive(false);
    }
    
    public void setDone(Task task) {
        tasks.remove(task);
    }

    public void removeChangeListener(TaskListChangeListener provider) {
        this.listeners.remove(provider);
    }

    public void addChangeListener(TaskListChangeListener provider) {
        this.listeners.remove(provider);
    }

    public Task[] getTasks() {
        return tasks.toArray(new Task[] {});
    }

    public void refresh() {
        String content = null;
        try {
            content = HttpUtil.getContentAsXML(client.getOptions().getProperty(
                    MindClient.LOGIN_KEY), client.getOptions().getProperty(
                    MindClient.PASSWORD_KEY), client.getOptions().getProperty(
                    MindClient.ENDPOINT_KEY)
                    + "/tasks"); //$NON-NLS-1$
        } catch (Exception e) {
            MessageDialogUtil
                    .displaySyncErrorMsg("Could not retrieve list of tasks due to unexpected connection errors.");
        }
        // check if some contant was received
        if (content == null) {
            return;
        }
        SAXReader reader = new SAXReader();
        Document doc;
        try {
            doc = reader.read(new StringReader(content));
        } catch (DocumentException e) {
            e.printStackTrace();
            return;
        }
        // create a transformer
        TaskListTransformer t = new TaskListTransformer(tasks);

        // perform the transform
        t.execute(doc);
    }
}
