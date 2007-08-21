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
package com.mindquarry.desktop.workspace.conflict;

import com.mindquarry.desktop.workspace.exception.CancelException;

/**
 * Callback handler the client of {@link SVNSynchronizer} has to implement (ie.
 * the graphical user interface). Callback methods are called to handle
 * synchronization conflicts. Callback handler has to call
 * {@link Conflict#accept(ConflictHandler)} to indicate that it is able to
 * handle a conflict.
 * 
 * @author <a href="mailto:saar@mindquarry.com">Alexander Saar</a>
 * @author <a href="mailto:victor.saar@mindquarry.com">Victor Saar</a>
 * @author <a href="mailto:klimetschek@mindquarry.com">Alexander Klimetschek</a>
 */
public interface ConflictHandler {
    
	public void handle(AddConflict conflict)
	        throws CancelException;

	public void handle(DeleteWithModificationConflict conflict)
			throws CancelException;

    public void handle(ReplaceConflict conflict)
            throws CancelException;
}
