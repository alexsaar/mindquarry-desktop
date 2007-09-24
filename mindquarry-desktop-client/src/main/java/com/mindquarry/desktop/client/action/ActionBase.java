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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;

import com.mindquarry.desktop.client.MindClient;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public abstract class ActionBase extends Action {
	protected static final String ICON_SIZE = "32x32";
	
	public static final String TEAM_ACTION_GROUP = "team-actions";
    public static final String TASK_ACTION_GROUP = "task-actions";
    public static final String STOP_ACTION_GROUP = "stop-actions";
    public static final String WORKSPACE_ACTION_GROUP = "workspace-actions";
    public static final String WORKSPACE_OPEN_GROUP = "workspace-open";
    public static final String MANAGEMENT_ACTION_GROUP = "management-actions";

	protected Log log = LogFactory.getLog(getClass());
	
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

	/**
	 * Whether the action should be enabled at startup.
	 */
    public boolean isEnabledByDefault() {
        return true;
    }

	public abstract String getGroup();
	public abstract boolean isToolbarAction();

}
