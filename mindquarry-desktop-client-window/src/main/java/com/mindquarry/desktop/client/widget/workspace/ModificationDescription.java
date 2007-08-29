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
import org.tigris.subversion.javahl.StatusKind;

import com.mindquarry.desktop.client.Messages;

/**
 * Image and text that describes a local or remote change so it
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

    private Image image;
    private String description;

    ModificationDescription(Image img, String description) {
        this.image = img;
        this.description = description;
    }
    
    Image getImage() {
        return image;
    }

    String getDescription() {
        return description;
    }
    
    public static ModificationDescription getDescription(int localStatus, int remoteStatus) {
        // checking local status first
        switch (localStatus) {
        case StatusKind.obstructed:
            // FIXME: add question mark icon
            break;

        case StatusKind.added:
        case StatusKind.unversioned:
            if (remoteStatus == StatusKind.added) {
                // TODO: show upload icon with "+" sign
                return new ModificationDescription(conflictImage,
                        Messages.getString("This new item has also been added on the server. "
                                +"You will nee to resolve the conflict."));
            }

            // TODO: show upload icon with "+" sign
            return new ModificationDescription(uploadImage,
                    Messages.getString("This new item will be uploaded to the server."));

        case StatusKind.modified:
            if (remoteStatus == StatusKind.modified) {
                // we cannot decide here if SVN can merge the changes for us,
                // so show a conflict:
                return new ModificationDescription(conflictImage, CONFLICT_TEXT);
            }
            return new ModificationDescription(uploadImage,
                    Messages.getString("Your changes of this item will be uploaded to the server."));
            
        case StatusKind.deleted:
        case StatusKind.missing:
            return new ModificationDescription(deleteImage,
                    Messages.getString("This item has been deleted or moved locally. " +
                    		"It will be deleted on the server."));

        case StatusKind.conflicted:
            return new ModificationDescription(conflictImage, CONFLICT_TEXT);
        }
        
        // checking remote status
        switch (remoteStatus) {
        case StatusKind.modified:
            return new ModificationDescription(downloadImage,
                    Messages.getString("This item has been modified on the server, " +
                    "the new version will be downloaded."));

        case StatusKind.added:
            return new ModificationDescription(downloadImage,
                    Messages.getString("This item is new on the server, " +
                    "it will be downloaded."));
            
        case StatusKind.deleted:
            return new ModificationDescription(deleteImage,
                    Messages.getString("This item has been deleted or moved on the server. " +
                    "It will be deleted locally."));
        }

        if (localStatus != -1 || remoteStatus != -1) {
            log.warn("Unhandled case for local/remote status "
                    + localStatus + "/" + remoteStatus);
        }
        return new ModificationDescription(null, "");
    }

}
