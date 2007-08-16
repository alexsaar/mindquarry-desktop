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
 * Callback handler the client of SVNSynchronizer has to implement (ie. GUI). 
 *
 * @author <a href="mailto:saar@mindquarry.com">Alexander Saar</a>
 *
 */
public interface ConflictHandler {
    
	public void visit(AddConflict conflict) throws CancelException;

	public void visit(AddInDeletedConflict conflict) throws CancelException;

    public void visit(DeleteWithModificationConflict conflict) throws CancelException;
}
