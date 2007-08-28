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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.mindquarry.desktop.model.ModelBase;
import com.mindquarry.desktop.util.NotAuthorizedException;

/**
 * @author <a href="mailto:lars(dot)trieloff(at)mindquarry(dot)com">Lars
 *         Trieloff</a>
 */
public class TaskList extends ModelBase {
	private List<Task> tasks;

	public TaskList(InputStream data, String login, String password) {
		super(data, new TaskListTransformer(login, password));
	}

	public TaskList(String url, String login, String password)
			throws NotAuthorizedException, Exception {
		super(url, login, password, new TaskListTransformer(login, password));
	}

	public TaskList() {
		super();
	}

	public TaskList(List<Task> tasks) {
		super();
		this.tasks = tasks;
	}

	@Override
	protected void initModel() {
		tasks = new ArrayList<Task>();
	}

	/**
	 * Getter for the list of tasks.
	 * 
	 * @return the list of tasks
	 */
	public List<Task> getTasks() {
		return tasks;
	}

	public void add(String url, String login, String password) {
		// check if some contant was received
		try {
			tasks.add(new Task(url, login, password));
		} catch (Exception e) {
			log.error("Error while loading task from " //$NON-NLS-1$
					+ url, e);
			return;
		}
	}
    
    /**
     * Replace a task by another one.
     * @param oldTask Task to be replaced.
     * @param newTask Task to replace with.
     */
    public void replace(Task oldTask, Task newTask) {
        int pos = tasks.indexOf(oldTask);
        if (pos >= 0) {
            tasks.remove(pos);
            tasks.add(pos, newTask);
        }
    }
}
