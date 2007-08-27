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
        if (localStatus == StatusKind.obstructed) {
            // FIXME: add question mark icon
        } else if (localStatus == StatusKind.added
                || localStatus == StatusKind.unversioned) {
            // TODO: show upload icon with "+" sign
            return new ModificationDescription(uploadImage, 
                    Messages.getString("This new file/directory will be uploaded to the server."));
        } else if (localStatus == StatusKind.modified && remoteStatus == StatusKind.modified) {
            // we cannot decide here if SVN can merge the changes for us,
            // so show a conflict:
            return new ModificationDescription(conflictImage,
                    CONFLICT_TEXT);
        } else if (localStatus == StatusKind.modified) {
            return new ModificationDescription(uploadImage,
                    Messages.getString("Your changes of this file will be uploaded to the server."));
        } else if (remoteStatus == StatusKind.modified) {
            return new ModificationDescription(downloadImage,
                    Messages.getString("This file has been modified on the server. " +
                    		"The new version will be downloaded."));
        } else if (remoteStatus == StatusKind.added) {
            return new ModificationDescription(downloadImage,
                    Messages.getString("This file is new on the server. " +
                    		"The file will be downloaded."));
        } else if (localStatus == StatusKind.deleted || localStatus == StatusKind.missing) {
            return new ModificationDescription(deleteImage,
                    Messages.getString("This file has been deleted locally. " +
                    		"It will be deleted on the server."));
        } else if (remoteStatus == StatusKind.deleted) {
            return new ModificationDescription(deleteImage,
                    Messages.getString("This file has been deleted on the server. " +
                    		"It will be deleted locally."));
        } else if (localStatus == StatusKind.conflicted) {
            return new ModificationDescription(conflictImage, CONFLICT_TEXT);
        } else if (localStatus != -1 || remoteStatus != -1) {
            log.warn("No icon set for local/remote status " + localStatus 
                    + "/" + remoteStatus);
        }
        return new ModificationDescription(null, "");
    }

}
