/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.task;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.DocumentSource;
import org.dom4j.io.SAXReader;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.mindquarry.client.MindClient;
import com.mindquarry.client.util.network.HttpUtil;
import com.mindquarry.client.util.widgets.MessageDialogUtil;
import com.mindquarry.client.util.widgets.UpdateComposite;
import com.mindquarry.client.xml.TaskListTransformer;
import com.mindquarry.client.xml.TaskTransformer;

/**
 * @author <a href="mailto:lars(dot)trieloff(at)mindquarry(dot)com">Lars
 *         Trieloff</a>
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class TaskManager {
    public static final String TITLE_COLUMN = "title"; //$NON-NLS-1$

    private List<Task> tasks = new ArrayList<Task>();

    private List<TaskListChangeListener> listeners = new Vector<TaskListChangeListener>();

    private final MindClient client;

    private final Button doneButton;

    private final TaskManager myself = this;

    private Composite refreshWidget;

    private Table taskTable = null;

    private TableViewer taskTableViewer = null;

    private static final StreamSource taskDoneXSL = new StreamSource(
            TaskManager.class.getResourceAsStream("/xslt/taskDone.xsl")); //$NON-NLS-1$

    public TaskManager(final MindClient client, final TableViewer taskTableViewer,
            final Button doneButton) {
        this.client = client;
        this.taskTableViewer = taskTableViewer;
        this.doneButton = doneButton;
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
        taskTableViewer.refresh();

        // set task content to status "done"
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        Source xmlSource = new DocumentSource(task.getContent());

        TransformerFactory transFact = TransformerFactory.newInstance();
        try {
            Transformer trans = transFact.newTransformer(taskDoneXSL);
            trans.transform(xmlSource, new StreamResult(result));

            HttpUtil.putAsXML(client.getOptions().getProperty(
                    MindClient.LOGIN_KEY), client.getOptions().getProperty(
                    MindClient.PASSWORD_KEY), task.getId(), result
                    .toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // must disable doneButton explicitly, because removing tasks does
        // not fire a selection changed event
        doneButton.setEnabled(false);
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

    /**
     * Runs task update in a separate thread, so that GUI can continue
     * processing. Thus this method returns immediatly. While updating tasks the
     * Task Manager will show an update widget instead of the task table.
     */
    public void asyncRefresh() {
    	System.out.println("refereshing");
		new Thread(new Runnable() {
			public void run() {
				refresh();
			}
		}).start();
    }

    private void refresh() {
        //setRefreshing(true);
    	System.out.println("calling http server for task list");
        InputStream content = null;
        try {
            content = HttpUtil.getContentAsXML(client.getOptions().getProperty(
                    MindClient.LOGIN_KEY), client.getOptions().getProperty(
                    MindClient.PASSWORD_KEY), client.getOptions().getProperty(
                    MindClient.ENDPOINT_KEY)
                    + "/tasks"); //$NON-NLS-1$
        } catch (Exception e) {
            MessageDialogUtil
                    .displaySyncErrorMsg(Messages.getString("TaskManager.0")); //$NON-NLS-1$
        }
        System.out.println("got some http content");
        // check if some contant was received
        if (content == null) {
        	System.out.println("no content received...");
            //setRefreshing(false);
        } else {
	        SAXReader reader = new SAXReader();
	        Document doc;
	        try {
	            doc = reader.read(content);
	        } catch (DocumentException e) {
	            e.printStackTrace();
	            return;
	        }
	        // create and execute transformer for tasklist description
	        TaskListTransformer tlTransformer = new TaskListTransformer();
	        tlTransformer.execute(doc);
	        System.out.println("getting task descriptions");
	        // get task descriptions
	        for (String taskURI : tlTransformer.getTaskURIs()) {
	            InputStream taskcontent = null;
	            try {
	                taskcontent = HttpUtil.getContentAsXML(client.getOptions()
	                        .getProperty(MindClient.LOGIN_KEY), client.getOptions()
	                        .getProperty(MindClient.PASSWORD_KEY), client
	                        .getOptions().getProperty(MindClient.ENDPOINT_KEY)
	                        + "/tasks/" + taskURI); //$NON-NLS-1$
	            } catch (Exception e) {
	                MessageDialogUtil
	                        .displaySyncErrorMsg(Messages.getString("TaskManager.1")); //$NON-NLS-1$
	            }
	            try {
	                doc = reader.read(taskcontent);
	                
	                TaskTransformer tTransformer = new TaskTransformer();
	                tTransformer.execute(doc);
	
	                // get initialized teask and set content
	                Task newTask = tTransformer.getTask();
	                newTask.setContent(doc);
	
	                System.out.println("Task: " + taskURI + " " + newTask.getStatus());
	                // add task to internal list of tasks, if it does not yet exist
	                if ((!tasks.contains(newTask))
	                        && (!newTask.getStatus().equals("done"))) { //$NON-NLS-1$
	                    tasks.add(newTask);
	                    this.updateTaskList();
	                }
	            } catch (DocumentException e) {
	                e.printStackTrace();
	            }
	        }
        }
        updateTaskList();
		
    }

	private void updateTaskList() {
		// update task table
        MindClient.getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				System.out.println("updateTaskList: " + Thread.currentThread().getName());
				if ((taskTableViewer!=null)&&(!taskTable.isDisposed())) {
					taskTableViewer.setInput(myself);
				}
			}
		});
	}

    public TableViewer getTaskTableViewer() {
        return taskTableViewer;
    }
}
