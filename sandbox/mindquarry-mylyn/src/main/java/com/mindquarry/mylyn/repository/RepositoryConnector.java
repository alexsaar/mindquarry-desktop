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
package com.mindquarry.mylyn.repository;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.tasks.core.AbstractAttachmentHandler;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryQuery;
import org.eclipse.mylyn.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.ITaskCollector;
import org.eclipse.mylyn.tasks.core.RepositoryTaskData;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import com.mindquarry.desktop.model.task.Task;
import com.mindquarry.desktop.model.task.TaskList;
import com.mindquarry.mylyn.Plugin;
import com.mindquarry.mylyn.task.TaskWrapper;

/**
 * Add summary documentation here.
 * 
 * @author <a hef="mailto:saar@mindquarry.com">Alexander Saar</a>
 */
public class RepositoryConnector extends AbstractRepositoryConnector {
	private static final String LABEL = "Mindquarry";

	public static final String KIND = "mindquarry";

	/**
	 * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#canCreateNewTask(org.eclipse.mylyn.tasks.core.TaskRepository)
	 */
	@Override
	public boolean canCreateNewTask(TaskRepository repository) {
		return false;
	}

	/**
	 * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#canCreateTaskFromKey(org.eclipse.mylyn.tasks.core.TaskRepository)
	 */
	@Override
	public boolean canCreateTaskFromKey(TaskRepository repository) {
		return false;
	}

	/**
	 * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#createTask(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	@Override
	public AbstractTask createTask(String repositoryUrl, String id,
			String summary) {
		return null;
	}

	/**
	 * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#getAttachmentHandler()
	 */
	@Override
	public AbstractAttachmentHandler getAttachmentHandler() {
		return null;
	}

	/**
	 * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#getConnectorKind()
	 */
	@Override
	public String getConnectorKind() {
		return KIND;
	}

	/**
	 * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#getLabel()
	 */
	@Override
	public String getLabel() {
		return LABEL;
	}

	/**
	 * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#getRepositoryUrlFromTaskUrl(java.lang.String)
	 */
	@Override
	public String getRepositoryUrlFromTaskUrl(String url) {
		return null;
	}

	/**
	 * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#getTaskDataHandler()
	 */
	@Override
	public AbstractTaskDataHandler getTaskDataHandler() {
		return null;
	}

	/**
	 * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#getTaskIdFromTaskUrl(java.lang.String)
	 */
	@Override
	public String getTaskIdFromTaskUrl(String url) {
		return url.substring(url.lastIndexOf('/'));
	}

	/**
	 * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#getTaskUrl(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public String getTaskUrl(String repositoryUrl, String taskId) {
		return repositoryUrl + "/" + taskId;
	}

	/**
	 * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#markStaleTasks(org.eclipse.mylyn.tasks.core.TaskRepository,
	 *      java.util.Set, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public boolean markStaleTasks(TaskRepository repository,
			Set<AbstractTask> tasks, IProgressMonitor monitor)
			throws CoreException {
		return false;
	}

	/**
	 * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#performQuery(org.eclipse.mylyn.tasks.core.AbstractRepositoryQuery,
	 *      org.eclipse.mylyn.tasks.core.TaskRepository,
	 *      org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.mylyn.tasks.core.ITaskCollector)
	 */
	@Override
	public IStatus performQuery(AbstractRepositoryQuery query,
			TaskRepository repository, IProgressMonitor monitor,
			ITaskCollector collector) {
		try {
			monitor.beginTask("Running query", IProgressMonitor.UNKNOWN);

			TaskList list = new TaskList(repository.getUrl() + "/tasks",
					repository.getUserName(), repository.getPassword());
			for (Task task : list.getTasks()) {
				collector.accept(new TaskWrapper(repository, task));
			}
			return Status.OK_STATUS;
		} catch (Exception e) {
			return new Status(IStatus.ERROR, Plugin.PLUGIN_ID, Status.ERROR,
					"Check repository configuration: " + e.getMessage(), e);
		} finally {
			monitor.done();
		}
	}

	/**
	 * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#updateAttributes(org.eclipse.mylyn.tasks.core.TaskRepository,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void updateAttributes(TaskRepository repository,
			IProgressMonitor monitor) throws CoreException {
		System.out.println("updating attributes");
	}

	/**
	 * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#updateTaskFromRepository(org.eclipse.mylyn.tasks.core.TaskRepository,
	 *      org.eclipse.mylyn.tasks.core.AbstractTask,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void updateTaskFromRepository(TaskRepository repository,
			AbstractTask task, IProgressMonitor monitor) throws CoreException {
		System.out.println();
	}

	/**
	 * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#updateTaskFromTaskData(org.eclipse.mylyn.tasks.core.TaskRepository,
	 *      org.eclipse.mylyn.tasks.core.AbstractTask,
	 *      org.eclipse.mylyn.tasks.core.RepositoryTaskData)
	 */
	@Override
	public void updateTaskFromTaskData(TaskRepository repository,
			AbstractTask task, RepositoryTaskData data) {
		System.out.println();
	}

	/**
	 * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#updateTaskFromQueryHit(org.eclipse.mylyn.tasks.core.TaskRepository,
	 *      org.eclipse.mylyn.tasks.core.AbstractTask,
	 *      org.eclipse.mylyn.tasks.core.AbstractTask)
	 */
	@Override
	public boolean updateTaskFromQueryHit(TaskRepository repository,
			AbstractTask task, AbstractTask task2) {
		return super.updateTaskFromQueryHit(repository, task, task2);
	}
}
