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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tigris.subversion.javahl.Status;
import org.tigris.subversion.javahl.StatusKind;
import org.tmatesoft.svn.core.javahl.SVNClientImpl;

public class Change {

    protected static Log log;
    protected SVNClientImpl client;
    protected Status status;
    protected File file;

    public Change(Status status) {
        this(status, new File(status.getPath()));
    }

    protected Change(Status status, File file) {
        log = LogFactory.getLog(getClass());
        this.status = status;
        this.file = file;
    }
    
    /**
     * This setter is ensured to be called before {@link accept()} so that an
     * implementation has full access to the svn client.
     */
    public void setSVNClient(SVNClientImpl client) {
        this.client = client;
    }

    /**
     * Returns the status (including local and remote) of the central object,
     * i.e. file or folder in the conflict.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Returns the File of the central object, i.e. file or folder in the
     * conflict.
     */
    public File getFile() {
        return file;
    }

    @Override
    public String toString() {
        return file.getName() + ": "
                + statusToString(status.getTextStatus()) + "/"
                + statusToString(status.getRepositoryTextStatus());
    }

    /**
     * Converts a status based on {@link StatusKind} to a String.
     * @param status Status to convert (e.g. StatusKind.added).
     * @return String representation of the status (e.g. "added").
     */
    public static String statusToString(int status) {
        switch (status) {
        // does not exist
        case StatusKind.none:
            return "none"; //$NON-NLS-1$

        // exists, but uninteresting
        case StatusKind.normal:
            return "normal"; //$NON-NLS-1$

        // text or props have been modified
        case StatusKind.modified:
            return "modified"; //$NON-NLS-1$

        // is scheduled for additon
        case StatusKind.added:
            return "added"; //$NON-NLS-1$

        // scheduled for deletion
        case StatusKind.deleted:
            return "deleted"; //$NON-NLS-1$

        // is not a versioned thing in this wc
        case StatusKind.unversioned:
            return "unversioned"; //$NON-NLS-1$

        // under v.c., but is missing
        case StatusKind.missing:
            return "missing"; //$NON-NLS-1$

        // was deleted and then re-added
        case StatusKind.replaced:
            return "replaced"; //$NON-NLS-1$

        // local mods received repos mods
        case StatusKind.merged:
            return "merged"; //$NON-NLS-1$

        // local mods received conflicting repos mods
        case StatusKind.conflicted:
            return "conflicted"; //$NON-NLS-1$

        // an unversioned resource is in the way of the versioned resource
        case StatusKind.obstructed:
            return "obstructed"; //$NON-NLS-1$

        // a resource marked as ignored
        case StatusKind.ignored:
            return "ignored"; //$NON-NLS-1$

        // a directory doesn't contain a complete entries list
        case StatusKind.incomplete:
            return "incomplete"; //$NON-NLS-1$

        // an unversioned path populated by an svn:externals property
        case StatusKind.external:
            return "external"; //$NON-NLS-1$
        }
        return "invalid (" + status + ")"; //$NON-NLS-1$
    }

}
