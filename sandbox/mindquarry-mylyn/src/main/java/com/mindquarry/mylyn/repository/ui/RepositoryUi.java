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

import java.io.InputStream;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryQuery;
import org.eclipse.mylyn.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.swt.graphics.ImageData;

import com.mindquarry.mylyn.Plugin;
import com.mindquarry.mylyn.repository.RepositoryConnector;
import com.mindquarry.mylyn.task.TaskWrapper;
import com.mindquarry.mylyn.task.TaskWrapper.Kind;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:saar@mindquarry.com">Alexander Saar</a>
 */
public class RepositoryUi extends AbstractRepositoryConnectorUi {
	private static final String OVERLAY_TASK_NEW = "/task-new.png";
	private static final String OVERLAY_TASK_RUNNING = "/task-running.png";
	private static final String OVERLAY_TASK_PAUSED = "/task-paused.png";
	private static final String OVERLAY_TASK_DONE = "/task-done.png";

	private ImageRegistry registry;

	public RepositoryUi() {
		registry = new ImageRegistry();

		InputStream stream = Plugin.getDefault().getClass()
				.getResourceAsStream(OVERLAY_TASK_NEW);
		ImageData data = new ImageData(stream);
		ImageDescriptor descriptor = ImageDescriptor.createFromImageData(data);
		registry.put(OVERLAY_TASK_NEW, descriptor);

		stream = Plugin.getDefault().getClass().getResourceAsStream(
				OVERLAY_TASK_RUNNING);
		data = new ImageData(stream);
		descriptor = ImageDescriptor.createFromImageData(data);
		registry.put(OVERLAY_TASK_RUNNING, descriptor);

		stream = Plugin.getDefault().getClass().getResourceAsStream(
				OVERLAY_TASK_PAUSED);
		data = new ImageData(stream);
		descriptor = ImageDescriptor.createFromImageData(data);
		registry.put(OVERLAY_TASK_PAUSED, descriptor);

		stream = Plugin.getDefault().getClass().getResourceAsStream(
				OVERLAY_TASK_DONE);
		data = new ImageData(stream);
		descriptor = ImageDescriptor.createFromImageData(data);
		registry.put(OVERLAY_TASK_DONE, descriptor);
	}

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
		return new QueryWizard(repository);
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

	/**
	 * @see org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi#getTaskKindOverlay(org.eclipse.mylyn.tasks.core.AbstractTask)
	 */
	@Override
	public ImageDescriptor getTaskKindOverlay(AbstractTask task) {
		Kind kind = Kind.fromString(task.getTaskKind());
		if (kind == Kind.TASK) {
			return registry.getDescriptor(OVERLAY_TASK_NEW);
		}
		return super.getTaskKindOverlay(task);
	}
}
