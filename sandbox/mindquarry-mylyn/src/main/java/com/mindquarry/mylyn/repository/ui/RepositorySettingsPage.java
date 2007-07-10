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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.swt.widgets.Composite;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:saar@mindquarry.com">Alexander Saar</a>
 */
public class RepositorySettingsPage extends AbstractRepositorySettingsPage {
	private static final String TITLE = "Mindquarry Repository Settings";

	private static final String DESCRIPTION = "Please enter connection for your Mindquarry Collaboration Server (Example: https://www.mindquarry.org/tasks/project)";

	public RepositorySettingsPage(AbstractRepositoryConnectorUi repositoryUi) {
		super(TITLE, DESCRIPTION, repositoryUi);
	}

	/**
	 * @see org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage#createAdditionalControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createAdditionalControls(Composite parent) {
		// TODO create additional controls if needed
	}

	/**
	 * @see org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage#getValidator(org.eclipse.mylyn.tasks.core.TaskRepository)
	 */
	@Override
	protected Validator getValidator(TaskRepository repository) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage#isValidUrl(java.lang.String)
	 */
	@Override
	protected boolean isValidUrl(String name) {
		if (name.startsWith(URL_PREFIX_HTTPS)
				|| name.startsWith(URL_PREFIX_HTTP)) {
			try {
				new URL(name);
				return true;
			} catch (MalformedURLException e) {
			}
		}
		return false;
	}
}
