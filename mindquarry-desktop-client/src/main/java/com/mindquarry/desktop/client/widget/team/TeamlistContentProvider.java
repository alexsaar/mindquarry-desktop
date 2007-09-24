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

package com.mindquarry.desktop.client.widget.team;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.mindquarry.desktop.model.team.Team;
import com.mindquarry.desktop.model.team.TeamList;

public class TeamlistContentProvider implements IStructuredContentProvider {
	public void dispose() {
	}

	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
	}

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof TeamList) {
			TeamList teamList = (TeamList) inputElement;
			return teamList.getTeams().toArray(new Team[0]);
		}
		return new Object[] {};
	}
}
