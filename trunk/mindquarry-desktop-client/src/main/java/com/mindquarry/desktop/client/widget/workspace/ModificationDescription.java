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

import com.mindquarry.desktop.client.Messages;
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

    private static final Image downloadImage = new Image(
            Display.getCurrent(), ModificationDescription.class
                    .getResourceAsStream("/com/mindquarry/icons/32x32/actions/synchronize-down.png")); //$NON-NLS-1$

    private static final Image uploadImage = new Image(
            Display.getCurrent(), ModificationDescription.class
                    .getResourceAsStream("/com/mindquarry/icons/32x32/actions/synchronize-up.png")); //$NON-NLS-1$

    private static final Image conflictImage = new Image(
            Display.getCurrent(), ModificationDescription.class
                    .getResourceAsStream("/org/tango-project/tango-icon-theme/32x32/status/dialog-warning.png")); //$NON-NLS-1$

    private Image directionImage;
    private Image statusOverlayImage;
    private String longDescription;
    private String shortDescription;

    private static Map<ChangeDirection,Image> directionImageMap = null;
    private static Map<ChangeStatus,Image> statusOverlayImageMap = null;
    
    static {
        directionImageMap = new HashMap<ChangeDirection, Image>();
        directionImageMap.put(ChangeDirection.UNKNOWN, null);
        directionImageMap.put(ChangeDirection.NONE, null);
        directionImageMap.put(ChangeDirection.CONFLICT, conflictImage);
        directionImageMap.put(ChangeDirection.TO_SERVER, uploadImage);
        directionImageMap.put(ChangeDirection.FROM_SERVER, downloadImage);

        // TODO: add real overlay icons
        statusOverlayImageMap = new HashMap<ChangeStatus, Image>();
        statusOverlayImageMap.put(ChangeStatus.UNKNOWN, null);
        statusOverlayImageMap.put(ChangeStatus.NONE, null);
        statusOverlayImageMap.put(ChangeStatus.ADDED, null);
        statusOverlayImageMap.put(ChangeStatus.MODIFIED, null);
        statusOverlayImageMap.put(ChangeStatus.DELETED, null);
        statusOverlayImageMap.put(ChangeStatus.CONFLICTED, null);
    }

    protected ModificationDescription(Image directionImage, Image statusOverlayImage,
            String longDescription, String shortDescription) {
        this.directionImage = directionImage;
        this.statusOverlayImage = statusOverlayImage;
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

        // use change status for choosing status overlay icon
        ChangeStatus changeStatus = change.getChangeStatus();
        if (statusOverlayImageMap.containsKey(changeStatus))
            statusOverlayImage = statusOverlayImageMap.get(changeStatus);

        this.longDescription = Messages.getString(change.getLongDescription());
        this.shortDescription = Messages
                .getString(change.getShortDescription());
    }
    
    public Image getDirectionImage() {
        return directionImage;
    }
    
    public Image getStatusOverlayImage() {
        return statusOverlayImage;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public String getShortDescription() {
        return shortDescription;
    }
}