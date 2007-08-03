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

import java.util.Collections;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.tasks.core.AbstractAttributeFactory;
import org.eclipse.mylyn.tasks.core.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.RepositoryTaskData;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import com.mindquarry.mylyn.repository.RepositoryConnector;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:saar@mindquarry.com">Alexander Saar</a>
 */
public class TaskDataHandler extends AbstractTaskDataHandler {
	private AbstractAttributeFactory attributeFactory = new AttributeFactory();

	private RepositoryConnector connector;

	public TaskDataHandler(RepositoryConnector connector) {
		this.connector = connector;
	}

	@Override
	public AbstractAttributeFactory getAttributeFactory(String repositoryUrl,
			String repositoryKind, String taskKind) {
		return attributeFactory;
	}

	@Override
	public AbstractAttributeFactory getAttributeFactory(
			RepositoryTaskData taskData) {
		return getAttributeFactory(taskData.getRepositoryUrl(), taskData
				.getRepositoryKind(), taskData.getTaskKind());
	}

	@Override
	public Set<String> getSubTaskIds(RepositoryTaskData taskData) {
		return Collections.emptySet();
	}

	@Override
	public RepositoryTaskData getTaskData(TaskRepository repository,
			String taskId, IProgressMonitor monitor) throws CoreException {
		RepositoryTaskData data = new RepositoryTaskData(attributeFactory,
				RepositoryConnector.KIND, repository.getUrl(), taskId);
		return data;
	}

	@Override
	public boolean initializeTaskData(TaskRepository repository,
			RepositoryTaskData data, IProgressMonitor monitor)
			throws CoreException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String postTaskData(TaskRepository repository,
			RepositoryTaskData taskData, IProgressMonitor monitor)
			throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}
}
