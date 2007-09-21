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
package com.mindquarry.desktop.client.widget.util;

import java.io.File;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.tigris.subversion.javahl.NodeKind;
import org.tigris.subversion.javahl.Status;

/**
 * File icon provider. A display object must be created before you can access
 * this method.
 */
public class FileIconUtil {
    static ImageRegistry imageRegistry;

    private static final Image FOLDER_IMAGE = new Image(
            Display.getCurrent(),
            FileIconUtil.class
                    .getResourceAsStream("/org/tango-project/tango-icon-theme/16x16/places/folder.png")); //$NON-NLS-1$

    private static final Image FILE_IMAGE = new Image(
            Display.getCurrent(),
            FileIconUtil.class
                    .getResourceAsStream("/org/tango-project/tango-icon-theme/16x16/mimetypes/text-x-generic.png")); //$NON-NLS-1$

    private static final Image UNKNOWN_FILE_IMAGE = new Image(
            Display.getCurrent(),
            FileIconUtil.class
                    .getResourceAsStream("/org/tango-project/tango-icon-theme/16x16/mimetypes/text-x-generic-template.png")); //$NON-NLS-1$

    /**
     * Returns an icon representing the specified file.
     * 
     * @param file
     * @return
     */
    public static Image getIcon(File file) {
        if (file.isDirectory())
            return FOLDER_IMAGE;

        int lastDotPos = file.getName().indexOf('.');
        if (lastDotPos == -1)
            return FILE_IMAGE;

        Image image = getIcon(file.getName().substring(lastDotPos + 1));
        return image == null ? FILE_IMAGE : image;
    }

    /**
     * Returns an icon representing the specified file or remote svn file.
     * 
     * @param file
     * @param status
     * @return an swt Image, never null
     */
    public static Image getIcon(File file, Status status) {
        // if the file does not exist locally, it is probably a remote file
        // and we have to look inside the Status to know what type it is
        if (!file.exists()) {
            if (status.getReposKind() == NodeKind.dir) {
                return FOLDER_IMAGE;
            } else if (status.getReposKind() == NodeKind.file) {
                return getIconBasedOnFilename(file.getName());
            }
            
            return UNKNOWN_FILE_IMAGE;
        }
        
        if (file.isDirectory()) {
            return FOLDER_IMAGE;
        }
        
        return getIconBasedOnFilename(file.getName());
    }
    
    /**
     * Returns an 
     * @param name
     * @return an swt Image, never null
     */
    public static Image getIconBasedOnFilename(String name) {
        int lastDotPos = name.indexOf('.');
        if (lastDotPos == -1) {
            return FILE_IMAGE;
        }

        return getIcon(name.substring(lastDotPos + 1));
    }
    
    /**
     * Returns the icon for the file type with the specified extension.
     * 
     * @param extension
     * @return an image, never null (if nothing is found, it returns FILE_IMAGE)
     */
    private static Image getIcon(String extension) {
        if (imageRegistry == null) {
            imageRegistry = new ImageRegistry();
        }
        
        Image image = imageRegistry.get(extension);
        if (image != null) {
            return image;
        }

        Program program = Program.findProgram(extension);
        ImageData imageData = (program == null ? null : program.getImageData());
        if (imageData != null) {
            image = new Image(Display.getCurrent(), imageData);
            imageRegistry.put(extension, image);
        } else {
            image = FILE_IMAGE;
        }

        return image;
    }
}
