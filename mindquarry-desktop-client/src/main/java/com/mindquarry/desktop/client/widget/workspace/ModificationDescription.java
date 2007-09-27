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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.mindquarry.desktop.workspace.conflict.Change;
import com.mindquarry.desktop.workspace.conflict.ChangeDescriptor.ChangeDirection;
import com.mindquarry.desktop.workspace.conflict.ChangeDescriptor.ChangeStatus;

/**
 * Icons, long and short descriptions for a local or remote change so it can be
 * explained to the user.
 * 
 * @author dnaber
 * @author <a href="mailto:christian(dot)richardt(at)mindquarry(dot)com">Christian Richardt</a>
 */
public class ModificationDescription {
    private static Log log = LogFactory
        .getLog(ModificationDescription.class);

    // Direction icons:
    
    private static final Image DOWNLOAD_IMAGE = new Image(
            Display.getCurrent(), ModificationDescription.class
                    .getResourceAsStream("/com/mindquarry/icons/32x32/actions/synchronize-down.png")); //$NON-NLS-1$

    private static final Image UPLOAD_IMAGE = new Image(
            Display.getCurrent(), ModificationDescription.class
                    .getResourceAsStream("/com/mindquarry/icons/32x32/actions/synchronize-up.png")); //$NON-NLS-1$

    private static final Image CONFLICT_IMAGE = new Image(
            Display.getCurrent(), ModificationDescription.class
                    .getResourceAsStream("/org/tango-project/tango-icon-theme/32x32/status/dialog-warning.png")); //$NON-NLS-1$

    // Status icons:
    
    private static final Image ADDED_IMAGE = new Image(
            Display.getCurrent(), ModificationDescription.class
                    .getResourceAsStream("/com/mindquarry/icons/32x32/status/status-added.png")); //$NON-NLS-1$

    private static final Image MODIFIED_IMAGE = new Image(
            Display.getCurrent(), ModificationDescription.class
                    .getResourceAsStream("/com/mindquarry/icons/32x32/status/status-modified.png")); //$NON-NLS-1$

    private static final Image DELETED_IMAGE = new Image(
            Display.getCurrent(), ModificationDescription.class
                    .getResourceAsStream("/com/mindquarry/icons/32x32/status/status-deleted.png")); //$NON-NLS-1$

    private Image directionImage;
    private Image statusImage;
    private String longDescription;
    private String shortDescription;

    private static Map<ChangeDirection,Image> directionImageMap = null;
    private static Map<ChangeStatus,Image> statusImageMap = null;
    
    static {
        directionImageMap = new HashMap<ChangeDirection, Image>();
        directionImageMap.put(ChangeDirection.UNKNOWN, null);
        directionImageMap.put(ChangeDirection.NONE, null);
        directionImageMap.put(ChangeDirection.CONFLICT, CONFLICT_IMAGE);
        directionImageMap.put(ChangeDirection.TO_SERVER, UPLOAD_IMAGE);
        directionImageMap.put(ChangeDirection.FROM_SERVER, DOWNLOAD_IMAGE);

        statusImageMap = new HashMap<ChangeStatus, Image>();
        statusImageMap.put(ChangeStatus.UNKNOWN, null);
        statusImageMap.put(ChangeStatus.NONE, null);
        statusImageMap.put(ChangeStatus.ADDED, ADDED_IMAGE);
        statusImageMap.put(ChangeStatus.MODIFIED, MODIFIED_IMAGE);
        statusImageMap.put(ChangeStatus.DELETED, DELETED_IMAGE);
        statusImageMap.put(ChangeStatus.CONFLICTED, null);  // shown in direction already
        statusImageMap.put(ChangeStatus.REPLACED, null);
    }

    protected ModificationDescription(Image directionImage, Image statusOverlayImage,
            String longDescription, String shortDescription) {
        this.directionImage = directionImage;
        this.statusImage = statusOverlayImage;
        this.longDescription = longDescription;
        this.shortDescription = shortDescription;
    }
    
    public ModificationDescription(Change change) {
        this(null, null, "", "");
        if (change == null)
            return;

        log.debug(change.getShortDescription() + ", "
                + change.getChangeStatus() + "/" + change.getChangeDirection()
                + " (class " + change.getClass() + ")");

        // use change direction for choosing direction icon
        ChangeDirection changeDirection = change.getChangeDirection();
        if (directionImageMap.containsKey(changeDirection))
            directionImage = directionImageMap.get(changeDirection);

        // use change status for choosing status icon
        ChangeStatus changeStatus = change.getChangeStatus();
        if (statusImageMap.containsKey(changeStatus))
            statusImage = statusImageMap.get(changeStatus);

        this.longDescription = change.getLongDescription();
        this.shortDescription = change.getShortDescription();
    }
    
    public Image getDirectionImage() {
        return directionImage;
    }
    
    public Image getStatusImage() {
        return statusImage;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public String getShortDescription() {
        return shortDescription;
    }
}
