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

    private static final Image conflictImage = new Image(
            Display.getCurrent(), ModificationDescription.class
                    .getResourceAsStream("/org/tango-project/tango-icon-theme/32x32/status/dialog-warning.png")); //$NON-NLS-1$

    private Image image;
    private String description;
    private String shortDescription;

    private static Map<ChangeDirection,Image> imageMap = null;
    
    static {
        imageMap = new HashMap<ChangeDirection, Image>();
        imageMap.put(ChangeDirection.CONFLICT, conflictImage);
        imageMap.put(ChangeDirection.TO_SERVER, uploadImage);
        imageMap.put(ChangeDirection.FROM_SERVER, downloadImage);
    }

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
        if (change == null)
            return new ModificationDescription(null, "", "");

        log.debug(">>\n  Class: " + change.getClass() + "\n" + "  Status: "
                + change.getChangeStatus() + "\n" + "  Direction: "
                + change.getChangeDirection() + "\n" + "  Long Description: "
                + change.getLongDescription() + "\n" + "  Short Description: "
                + change.getShortDescription() + "\n");

        // use change direction for choosing icon
        Image dirImage = null;
        ChangeDirection changeDirection = change.getChangeDirection();
        if (imageMap.containsKey(changeDirection))
            dirImage = imageMap.get(changeDirection);

        return new ModificationDescription(dirImage, Messages.getString(change
                .getLongDescription()), change.getShortDescription());
    }
}
