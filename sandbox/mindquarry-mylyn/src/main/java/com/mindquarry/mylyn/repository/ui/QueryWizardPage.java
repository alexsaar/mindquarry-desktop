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

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.search.AbstractRepositoryQueryPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.mindquarry.mylyn.task.query.RepositoryQuery;

/**
 * Add summary documentation here.
 *
 * @author <a href="mailto:saar@mindquarry.com">Alexander Saar</a>
 */
public class QueryWizardPage extends AbstractRepositoryQueryPage {
	private static final String TITLE = "Create Mindquarry Task Repository Query";

	private static final String DESCRIPTION = "Select the fields that should be contained in your query.";

	public QueryWizardPage(TaskRepository repository) {
		super(TITLE);
		setTitle(TITLE);
		setDescription(DESCRIPTION);
		setImageDescriptor(TasksUiImages.BANNER_REPOSITORY);
		
		this.repository = repository;
	}

	public void createControl(Composite parent) {
		super.createControl(parent);
				
//		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
//		gridData.grabExcessVerticalSpace = false;
		
		Composite composite = (Composite)parent.getChildren()[0];
//		composite.setLayoutData(gridData);
//		composite.setLayout(new GridLayout(1, false));

		Label label = new Label(composite, SWT.LEFT);
		label.setText("Status:");
		
		Combo status = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		status.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		status.add("new");
		status.add("running");
		status.add("paused");
		status.add("done");
		status.select(0);
		
		setPageComplete(true);
		setControl(composite);
	}

	@Override
	public IWizardPage getNextPage() {
		return null;
	}

	@Override
	public AbstractRepositoryQuery getQuery() {
		return new RepositoryQuery(repository.getUrl(), "new query");
	}
}
