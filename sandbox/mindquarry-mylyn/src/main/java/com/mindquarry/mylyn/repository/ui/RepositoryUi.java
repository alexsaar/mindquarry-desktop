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

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;

import com.mindquarry.mylyn.repository.RepositoryConnector;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:saar@mindquarry.com">Alexander Saar</a>
 */
public class RepositoryUi extends AbstractRepositoryConnectorUi {
	/**
	 * @see org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi#getConnectorKind()
	 */
	@Override
	public String getConnectorKind() {
		return RepositoryConnector.KIND;
	}

	/**
	 * @see org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi#getNewTaskWizard(org.eclipse.mylyn.tasks.core.TaskRepository)
	 */
	@Override
	public IWizard getNewTaskWizard(TaskRepository repository) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi#getQueryWizard(org.eclipse.mylyn.tasks.core.TaskRepository,
	 *      org.eclipse.mylyn.tasks.core.AbstractRepositoryQuery)
	 */
	@Override
	public IWizard getQueryWizard(TaskRepository repository,
			AbstractRepositoryQuery query) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi#getSettingsPage()
	 */
	@Override
	public AbstractRepositorySettingsPage getSettingsPage() {
		return new RepositorySettingsPage(this);
	}

	/**
	 * @see org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi#hasSearchPage()
	 */
	@Override
	public boolean hasSearchPage() {
		return false;
	}
}
