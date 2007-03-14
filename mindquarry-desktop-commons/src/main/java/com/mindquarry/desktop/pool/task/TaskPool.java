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
package com.mindquarry.desktop.pool.task;

import com.mindquarry.desktop.pool.PoolBase;

/**
 * Desktop pool implementation for tasks.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class TaskPool extends PoolBase {
    public static final String POOL_NAME = "tasks"; //$NON-NLS-1$

    private static TaskPool pool;

    private TaskPool(String name) {
        super(name);
    }

    public static TaskPool getInstance() {
        if (pool == null) {
            pool = new TaskPool(POOL_NAME);
        }
        return pool;
    }
}
