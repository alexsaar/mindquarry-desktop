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
import java.io.IOException;

import org.tigris.subversion.javahl.Status;
import org.tigris.subversion.javahl.Status.Kind;

import com.mindquarry.desktop.workspace.SVNSynchronizer;

/**
 * @author <a href="mailto:alexander(dot)klimetschek(at)mindquarry(dot)com">
 *         Alexander Klimetschek</a>
 *
 */
public class ConflictPrinter {

    private String workingCopyPath;

    public ConflictPrinter(String workingCopyPath) {
        try {
            this.workingCopyPath = new File(workingCopyPath).getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException("working copy path cannot be made absolute: " + workingCopyPath, e);
        }
    }

    public String wcPath(Status status) {
        return status.getPath().substring(workingCopyPath.length() + 1);
    }

    public void printConflict(AddConflict conflict) {
        Status status = conflict.getStatus();
        System.out.print("local " + SVNSynchronizer.nodeKindDesc(status.getNodeKind()) + " '" + wcPath(status) + "' conflicts with ");
        System.out.println("remote " + SVNSynchronizer.nodeKindDesc(status.getReposKind()) + " '" + wcPath(status) + "' by " + status.getReposLastCmtAuthor());
        for (Status s : conflict.getLocalAdded()) {
            System.out.println("  locally "
                    + Kind.getDescription(s.getTextStatus()) + " "
                    + SVNSynchronizer.nodeKindDesc(s.getNodeKind()) + " "
                    + "'" + wcPath(s) + "'");
        }
        for (Status s : conflict.getRemoteAdded()) {
            System.out.println("  remotely "
                    + Kind.getDescription(s.getRepositoryTextStatus()) + " "
                    + SVNSynchronizer.nodeKindDesc(s.getReposKind()) + " "
                    + "'" + wcPath(s) + "' by " + s.getReposLastCmtAuthor());
        }
    }

    public void printConflict(DeleteWithModificationConflict conflict) {
        Status status = conflict.getStatus();
        if (conflict.isLocalDelete()) {
            System.out.print("local deletion of '" + wcPath(status) + "' conflicts with ");
            System.out.println("remote modification of " + SVNSynchronizer.nodeKindDesc(status.getReposKind()) + " '" + wcPath(status) + "' by " + status.getReposLastCmtAuthor());
            for (Status s : conflict.getOtherMods()) {
                System.out.println("remote "
                        + Kind.getDescription(s.getRepositoryTextStatus()) + " "
                        + s.getPath());
            }
        } else {
            System.out.print("local modification of " + SVNSynchronizer.nodeKindDesc(status.getNodeKind()) + " '" + wcPath(status) + "' conflicts with ");
            System.out.println("remote deletion of '" + wcPath(status) + "' by " + status.getReposLastCmtAuthor());
            for (Status s : conflict.getOtherMods()) {
                System.out.println("local "
                        + Kind.getDescription(s.getRepositoryTextStatus()) + " "
                        + s.getPath());
            }
        }
    }

}
