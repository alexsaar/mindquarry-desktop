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
package com.mindquarry.client.task.dialog;

import org.eclipse.swt.widgets.Shell;
import org.junit.Test;

import com.mindquarry.client.task.Task;

import junit.framework.TestCase;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class TaskDialogTest extends TestCase {
    @Test
    public void testTaskDialog() {
        Task task = new Task();
        task.setStatus("new");
        task.setTitle("new task");
        task.setSummary("summary of the task");
        
        Shell shell = new Shell();
        TaskDialog dlg = new TaskDialog(shell, task);
        dlg.open();
    }
}
