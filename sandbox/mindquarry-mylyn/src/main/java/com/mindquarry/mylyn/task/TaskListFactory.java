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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.mylyn.tasks.core.AbstractRepositoryQuery;
import org.eclipse.mylyn.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.AbstractTaskListFactory;
import org.w3c.dom.Element;

import com.mindquarry.mylyn.task.query.RepositoryQuery;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:saar@mindquarry.com">Alexander Saar</a>
 */
public class TaskListFactory extends AbstractTaskListFactory {
	private static final String KEY_MINDQUARRY = "Mindquarry";

	private static final String KEY_MINDQUARRY_TASK = KEY_MINDQUARRY
			+ AbstractTaskListFactory.KEY_TASK;

	private static final String KEY_MINDQUARRY_QUERY = KEY_MINDQUARRY
			+ AbstractTaskListFactory.KEY_QUERY;

	/**
	 * @see org.eclipse.mylyn.tasks.core.AbstractTaskListFactory#canCreate(org.eclipse.mylyn.tasks.core.AbstractTask)
	 */
	@Override
	public boolean canCreate(AbstractTask task) {
		return task instanceof TaskWrapper;
	}

	/**
	 * @see org.eclipse.mylyn.tasks.core.AbstractTaskListFactory#createTask(java.lang.String,
	 *      java.lang.String, java.lang.String, org.w3c.dom.Element)
	 */
	@Override
	public AbstractTask createTask(String repositoryUrl, String taskId,
			String summary, Element element) {
		return null;
//		return new Task(repositoryUrl, taskId, summary);
	}

	@Override
	public AbstractRepositoryQuery createQuery(String repositoryUrl,
			String queryString, String label, Element element) {
		return new RepositoryQuery(repositoryUrl, label);
	}

	/**
	 * @see org.eclipse.mylyn.tasks.core.AbstractTaskListFactory#getTaskElementName()
	 */
	@Override
	public String getTaskElementName() {
		return KEY_MINDQUARRY_TASK;
	}

	/**
	 * @see org.eclipse.mylyn.tasks.core.AbstractTaskListFactory#canCreate(org.eclipse.mylyn.tasks.core.AbstractRepositoryQuery)
	 */
	@Override
	public boolean canCreate(AbstractRepositoryQuery category) {
		return category instanceof RepositoryQuery;
	}

	/**
	 * @see org.eclipse.mylyn.tasks.core.AbstractTaskListFactory#getQueryElementName(org.eclipse.mylyn.tasks.core.AbstractRepositoryQuery)
	 */
	@Override
	public String getQueryElementName(AbstractRepositoryQuery query) {
		return query instanceof RepositoryQuery ? KEY_MINDQUARRY_QUERY : "";
	}

	/**
	 * @see org.eclipse.mylyn.tasks.core.AbstractTaskListFactory#getQueryElementNames()
	 */
	@Override
	public Set<String> getQueryElementNames() {
		Set<String> names = new HashSet<String>();
		names.add(KEY_MINDQUARRY_QUERY);
		return names;
	}
}
