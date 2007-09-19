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

package com.mindquarry.desktop.client.widget.workspace;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.tigris.subversion.javahl.NodeKind;
import org.tigris.subversion.javahl.Status;
import org.tigris.subversion.javahl.StatusKind;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.workspace.conflict.Change;
import com.mindquarry.desktop.workspace.conflict.Conflict;
import com.mindquarry.desktop.workspace.conflict.LocalAddition;

/**
 * Image, text, and short text that describe a local or remote change so it
 * can be explained to the user.
 * 
 * @author dnaber
 */
public class ModificationDescription {
    private static Log log = LogFactory
        .getLog(ModificationDescription.class);

    private static final Image downloadImage = new Image(
            Display.getCurrent(), ModificationDescription.class
                    .getResourceAsStream("/com/mindquarry/icons/32x32/actions/synchronize-down.png")); //$NON-NLS-1$

    private static final Image uploadImage = new Image(
            Display.getCurrent(), ModificationDescription.class
                    .getResourceAsStream("/com/mindquarry/icons/32x32/actions/synchronize-up.png")); //$NON-NLS-1$

    private static final Image deleteImage = new Image(
            Display.getCurrent(), ModificationDescription.class
                    .getResourceAsStream("/org/tango-project/tango-icon-theme/32x32/status/user-trash-full.png")); //$NON-NLS-1$

    private static final Image conflictImage = new Image(
            Display.getCurrent(), ModificationDescription.class
                    .getResourceAsStream("/org/tango-project/tango-icon-theme/32x32/status/dialog-warning.png")); //$NON-NLS-1$

    private static final String CONFLICT_TEXT = 
        Messages.getString("This file has been modified both on the server " +
            "and locally. You will need to merge the changes.");
    private static final String CONFLICT_SHORT_TEXT = 
        Messages.getString("Conflict");

    private Image image;
    private String description;
    private String shortDescription;

    ModificationDescription(Image img, String description, String shortDescription) {
        this.image = img;
        this.description = description;
        this.shortDescription = shortDescription;
    }
    
    public Image getImage() {
        return image;
    }

    public String getDescription() {
        return description;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public static ModificationDescription getDescription(Change change) {
        // normal behavior:
        if(change == null)
            return getDescription(null, null);
        ModificationDescription oldMD = getDescription(change.getStatus(), change.getStatus());
        log.debug(change.getClass());
        return oldMD;

//        if (change == null) {
//            return new ModificationDescription(null, "", "");
//        }
//
//        // TODO: use class of change to infer description, e.g.
//        if (change instanceof Conflict) {
//            return new ModificationDescription(conflictImage, oldMD.getDescription()+"\n"+change.getClass()
//                    .toString(), "Conflict");
//        } else if (change instanceof LocalAddition) {
//            return new ModificationDescription(uploadImage, oldMD.getDescription()+"\n"+change.getClass()
//                    .toString(), "Added");
//        } else {
//            if (oldMD == null) {
//                return new ModificationDescription(null, "", "");
//            }
//            return new ModificationDescription(oldMD.getImage(), oldMD.getDescription()+"\n"+change.getClass()
//                    .toString(), oldMD.getShortDescription());
//        }
    }
    
    public static ModificationDescription getDescription(Status localStatusObj, Status remoteStatusObj) {
        int localStatus = -1;
        int remoteStatus = -1;
        if (localStatusObj != null) {
            localStatus = localStatusObj.getTextStatus();
        }
        if (remoteStatusObj != null) {
            remoteStatus = remoteStatusObj.getRepositoryTextStatus();
        }

        // occasionally, showing upload/download status on directories is
        // confusing, so need to know whether file/dir
        boolean isDir = (localStatusObj != null && localStatusObj.getNodeKind() == NodeKind.dir)
                || (remoteStatusObj != null && remoteStatusObj.getNodeKind() == NodeKind.dir);
        
        log.info("ModificationDescription -- status "+statusToString(localStatus)+"/"+statusToString(remoteStatus));
        switch (localStatus) {
            case StatusKind.obstructed:
                // FIXME: add question mark icon
                return new ModificationDescription(conflictImage,
                        Messages.getString("This file is obstructed."), // TODO: better description
                        Messages.getString("Obstructed"));
    
            case StatusKind.added:
            case StatusKind.unversioned:
                if (remoteStatus == StatusKind.added) {
                    // TODO: show conflict icon with "+" sign
                    return new ModificationDescription(conflictImage,
                            Messages.getString("This new item has also been added on the server. "
                                    +"You will nee to resolve the conflict."),
                                    Messages.getString("Conflict"));
                }
    
                // TODO: show upload icon with "+" sign
                return new ModificationDescription(uploadImage,
                        Messages.getString("This new item will be uploaded to the server."),
                        Messages.getString("Added"));
    
            case StatusKind.modified:
                if (remoteStatus == StatusKind.modified) {
                    // we cannot decide here if SVN can merge the changes for us,
                    // so show a conflict:
                    return new ModificationDescription(conflictImage, CONFLICT_TEXT,
                            CONFLICT_SHORT_TEXT);
                }
                return new ModificationDescription(uploadImage,
                        Messages.getString("Your changes of this item will be uploaded to the server."),
                        Messages.getString("Modified"));
                
            case StatusKind.deleted:
            case StatusKind.missing:
                return new ModificationDescription(deleteImage,
                        Messages.getString("This item has been deleted or moved locally. " +
                        		"It will be deleted on the server."),
                        		Messages.getString("Deleted"));
    
            case StatusKind.conflicted:
                return new ModificationDescription(conflictImage, CONFLICT_TEXT,
                        CONFLICT_SHORT_TEXT);
        }
        
        // checking remote status
        switch (remoteStatus) {
            case StatusKind.modified:
                if (isDir) { // don't show icon in this case, as it confuses the user
                    return new ModificationDescription(null, Messages.getString(
                            "Items in this folder have been modified or deleted on the server."), "");
                } else { // file
                    return new ModificationDescription(downloadImage, Messages.getString(
                            "This item has been modified on the server, the new version will be downloaded."),
                            Messages.getString("Modified"));
                }
    
            case StatusKind.added:
                return new ModificationDescription(downloadImage,
                        Messages.getString("This item is new on the server, " +
                        "it will be downloaded."),
                        Messages.getString("Added"));
                
            case StatusKind.deleted:
                return new ModificationDescription(deleteImage,
                        Messages.getString("This item has been deleted or moved on the server. " +
                        "It will be deleted locally."),
                        Messages.getString("Deleted"));
        }

        if (localStatus != -1 || remoteStatus != -1) {
            log.warn("Unhandled case for local/remote status "
                    + statusToString(localStatus) + "/"
                    + statusToString(remoteStatus));
        }
        return new ModificationDescription(null, "", "");
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
