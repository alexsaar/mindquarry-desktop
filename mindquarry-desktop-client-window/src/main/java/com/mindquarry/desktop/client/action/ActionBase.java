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
package com.mindquarry.desktop.client.action;

import org.eclipse.jface.action.Action;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.client.MindClient;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public abstract class ActionBase extends Action {
	protected static final String ICON_SIZE = "16x16";

	protected final String TEXT = Messages.getString(getClass(), "text"); //$NON-NLS-1$
	protected final String TOOLTIP = Messages.getString(getClass(), "tooltip"); //$NON-NLS-1$
	
	protected MindClient client;

	public ActionBase(MindClient client) {
		this.client = client;
	}

	/**
	 * @see org.eclipse.jface.action.Action#getId()
	 */
	public String getId() {
		return getClass().getName();
	}
}
