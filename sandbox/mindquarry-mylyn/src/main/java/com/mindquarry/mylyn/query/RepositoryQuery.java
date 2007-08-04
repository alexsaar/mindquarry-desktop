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
package com.mindquarry.mylyn.query;

import org.eclipse.mylyn.tasks.core.AbstractRepositoryQuery;

import com.mindquarry.mylyn.repository.RepositoryConnector;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:saar@mindquarry.com">Alexander Saar</a>
 */
public class RepositoryQuery extends AbstractRepositoryQuery {
	private boolean showMyTasksOnly = false;
	private String status = "all";

	public RepositoryQuery(String repositoryURL, String description,
			String status, boolean myTasks) {
		super(description);

		this.status = status;
		this.showMyTasksOnly = myTasks;
		this.setRepositoryUrl(repositoryURL);
	}

	public RepositoryQuery(String repositoryURL, String description) {
		super(description);

		this.setRepositoryUrl(repositoryURL);
	}

	/**
	 * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryQuery#getRepositoryKind()
	 */
	@Override
	public String getRepositoryKind() {
		return RepositoryConnector.KIND;
	}

	public boolean isShowMyTasksOnly() {
		return showMyTasksOnly;
	}

	public String getStatus() {
		return status;
	}
}
