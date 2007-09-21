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

/**
 * Provides information about a change or conflict, such as long and short
 * descriptions, as well as the direction or status of the change.
 * 
 *  @author <a href="mailto:christian(dot)richardt(at)mindquarry(dot)com">Christian Richardt</a>
 */
public interface ChangeDescriptor {

    /**
     * The direction of a change or conflict if both directions.
     */
    public enum ChangeDirection {
        UNKNOWN,    // unknown direction, i.e. not recognised
        NONE,       // no direction, e.g. unmodified (should not happen, but never know)
        FROM_SERVER,// incoming change from the server
        TO_SERVER,  // change to be sent to server
        CONFLICT    // file/dir is in conflict locally, so can't send/receive changes
    }

    /**
     * The status of a change. The local status is generally preferred and the
     * remote status used if the local status is normal.
     */
    public enum ChangeStatus {
        UNKNOWN,    // unknown status, i.e. not recognised
        NONE,       // no status, e.g. unmodified (should not happen, but never know)
        ADDED,      // added file/dir either locally or remotely
        MODIFIED,   // modified file/dir either locally or remotely
        DELETED,    // deleted file/dir either locally or remotely
        CONFLICTED  // file/dir is in conflict locally
    }
    
    /**
     * Returns the direction of a file/dir change.
     */
    public ChangeDirection getChangeDirection();

    /**
     * Returns the status of a file/dir change.
     */
    public ChangeStatus getChangeStatus();
    
    /**
     * Returns a long description of a file/dir change, to be shown as tooltips
     * in workspace changes dialog and in conflict dialogs if applicable.
     */
    public String getLongDescription();

    /**
     * Returns a short description of a file/dir change, to be used in the
     * commit dialog as a short summary of the longer description returned by
     * {@link ChangeDescriptor#getLongDescription()}.
     */
    public String getShortDescription();

}
