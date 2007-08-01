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
package com.mindquarry.mylyn.repository.ui;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUiPlugin;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:saar@mindquarry.com">Alexander Saar</a>
 */
public class QueryWizard extends Wizard {
	private static final String TITLE = "Create Mindquarry Task Repository Query";

	private final TaskRepository repository;

	private QueryWizardPage queryPage;

	public QueryWizard(TaskRepository repository) {
		this.repository = repository;

		setNeedsProgressMonitor(true);
		setWindowTitle(TITLE);
	}

	@Override
	public void addPages() {
		queryPage = new QueryWizardPage(repository);
		queryPage.setWizard(this);
		addPage(queryPage);
	}

	@Override
	public boolean performFinish() {
		TasksUiPlugin.getTaskListManager().getTaskList().addQuery(
				queryPage.getQuery());
		return true;
	}

	@Override
	public boolean canFinish() {
		return true;
	}
}
