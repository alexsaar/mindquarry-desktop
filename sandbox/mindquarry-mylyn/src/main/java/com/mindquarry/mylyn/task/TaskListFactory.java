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
package com.mindquarry.mylyn.task;

import org.eclipse.mylyn.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.AbstractTaskListFactory;
import org.w3c.dom.Element;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:saar@mindquarry.com">Alexander Saar</a>
 */
public class TaskListFactory extends AbstractTaskListFactory {
	private static final String TASK_ELEMENT_NAME = "task";

	/**
	 * @see org.eclipse.mylyn.tasks.core.AbstractTaskListFactory#canCreate(org.eclipse.mylyn.tasks.core.AbstractTask)
	 */
	@Override
	public boolean canCreate(AbstractTask task) {
		return task instanceof Task;
	}

	/**
	 * @see org.eclipse.mylyn.tasks.core.AbstractTaskListFactory#createTask(java.lang.String,
	 *      java.lang.String, java.lang.String, org.w3c.dom.Element)
	 */
	@Override
	public AbstractTask createTask(String repositoryUrl, String taskId,
			String summary, Element element) {
		Task task = new Task(repositoryUrl, taskId, summary);
		return task;
	}

	/**
	 * @see org.eclipse.mylyn.tasks.core.AbstractTaskListFactory#getTaskElementName()
	 */
	@Override
	public String getTaskElementName() {
		return TASK_ELEMENT_NAME;
	}
}
