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

import java.io.File;

import org.tigris.subversion.javahl.Status;

/**
 * @author christian
 *
 */
public class LocalAddition extends Change {
 
    public LocalAddition(File file, Status ancestorStatus) {
        super(ancestorStatus, file);
    }

    @Override
    public String toString() {
        return file.getName() + ": LocalAddition";
    }
}
