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
import org.eclipse.mylyn.tasks.core.TaskRepository;

import com.mindquarry.desktop.model.task.Task;
import com.mindquarry.mylyn.repository.RepositoryConnector;

/**
 * Wrapper class that works as a delegate for integrating {@link Task}s.
 * 
 * @author <a hef="mailto:saar@mindquarry.com">Alexander Saar</a>
 */
public class TaskWrapper extends AbstractTask {
	public TaskWrapper(TaskRepository repository, Task task) {
		super(repository.getUrl(), calculateTaskId(task.getId()), task
				.getTitle());
		setUrl(task.getId());
		update(task);
	}

	/**
	 * @see org.eclipse.mylyn.tasks.core.AbstractTask#getConnectorKind()
	 */
	@Override
	public String getConnectorKind() {
		return RepositoryConnector.KIND;
	}

	/**
	 * @see org.eclipse.mylyn.tasks.core.AbstractTask#isLocal()
	 */
	@Override
	public boolean isLocal() {
		return false;
	}

	public void update(Task task) {
		if(task.getStatus().equals(Task.STATUS_DONE)) {
			setCompleted(true);
		} else {
			setCompleted(false);
		}
		if (task.getSummary() != null) {
			setNotes(task.getSummary());
		}
		if (task.getPriority() != null) {
			if (task.getPriority().equals(Task.PRIORITY_LOW)) {
				setPriority(PriorityLevel.P4.name());
			} else if (task.getPriority().equals(Task.PRIORITY_MEDIUM)) {
				setPriority(PriorityLevel.P3.name());
			} else if (task.getPriority().equals(Task.PRIORITY_IMPORTANT)) {
				setPriority(PriorityLevel.P2.name());
			} else if (task.getPriority().equals(Task.PRIORITY_CRITICAL)) {
				setPriority(PriorityLevel.P1.name());
			}
		} else {
			setPriority(PriorityLevel.P5.name());
		}
	}

	public static String calculateTaskId(String url) {
		return url.substring(url.lastIndexOf('/') + 1);
	}
}
