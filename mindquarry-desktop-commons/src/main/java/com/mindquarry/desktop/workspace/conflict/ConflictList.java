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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mindquarry.desktop.workspace.exception.CancelException;

/**
 * Special ArrayList for Conflicts that calls the ConflictHandler upon each
 * add of a new Conflict object.
 * 
 * @author <a href="mailto:alexander(dot)klimetschek(at)mindquarry(dot)com">
 *         Alexander Klimetschek</a>
 *
 */
public class ConflictList<T extends Conflict> extends ArrayList<T> {

    private static final long serialVersionUID = -8916078254426759711L;
    
    private ConflictHandler handler;

    private static Log log = LogFactory.getLog(ConflictList.class);
    
    public ConflictList(ConflictHandler handler) {
        this.handler = handler;
    }

    public boolean addConflict(T conflict) throws CancelException {
        log.info("## Found conflict: " + conflict.toString());
        conflict.accept(handler);
        return super.add(conflict);
    }
    
    /**
     * @deprecated use addConflict() instead
     */
    @Override
    public boolean add(T o) {
        throw new UnsupportedOperationException("please use ConflictList.addConflict() instead");
    }

    /**
     * @deprecated use addConflict() instead
     */
    @Override
    public void add(int index, T element) {
        throw new UnsupportedOperationException("please use ConflictList.addConflict() instead");
    }

    /**
     * @deprecated use addConflict() instead
     */
    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException("please use ConflictList.addConflict() instead");
    }

    /**
     * @deprecated use addConflict() instead
     */
    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException("please use ConflictList.addConflict() instead");
    }

}
