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
import org.eclipse.swt.graphics.ImageData;

import com.mindquarry.mylyn.Plugin;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:saar@mindquarry.com">Alexander Saar</a>
 */
public class Images {
	private static final String OVERLAY_TASK_KEY = "task.png";
	public static final ImageDescriptor OVERLAY_TASK = getDescriptor(OVERLAY_TASK_KEY);

	private static ImageDescriptor getDescriptor(String key) {
		return Plugin.getDefault().getImageRegistry().getDescriptor(key);
	}

	public static final void fillRegistry(ImageRegistry registry) {
		InputStream stream = Plugin.getDefault().getClass()
				.getResourceAsStream(OVERLAY_TASK_KEY);
		ImageData data = new ImageData(stream);
		ImageDescriptor descriptor = ImageDescriptor.createFromImageData(data);
		registry.put(OVERLAY_TASK_KEY, descriptor);
	}
}
