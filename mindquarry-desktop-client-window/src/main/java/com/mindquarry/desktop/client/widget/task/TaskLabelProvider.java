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
package com.mindquarry.desktop.client.widget.task;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.client.widget.util.ImageAdaptor;
import com.mindquarry.desktop.client.widget.util.ImageHelper;
import com.mindquarry.desktop.model.task.Task;

/**
 * Label provider for the Task table.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class TaskLabelProvider extends ColumnLabelProvider {
    private static final String TASK_PREFIX = "task-";

    private static final int ICON_SIZE = 32;
    private static final int OVERLAY_ICON_SIZE = 16;

    public Image getImage(Object element) {
        Task task = (Task) element;
        if (task.getStatus() == null) {
            return null;
        }
        return createOverlayIcon(task);
    }

    private Image createOverlayIcon(Task task) {
        ImageRegistry reg = JFaceResources.getImageRegistry();

        String id = TASK_PREFIX + task.getStatus();
        if (task.getPriority() != null) {
            id += "-" + task.getPriority();
        }

        Image result = reg.get(id);
        if (result == null) {
            String rID = "/com/mindquarry/icons/" + ICON_SIZE + "x" + ICON_SIZE
                    + "/status/task-" + task.getStatus() + ".png";
            InputStream is = TaskLabelProvider.class.getResourceAsStream(rID);

            if (task.getPriority() == null) {
                result = new Image(Display.getDefault(), is);
                reg.put(id, result);
            } else {
                try {
                    BufferedImage statusImg = ImageIO.read(is);

                    rID = "/com/mindquarry/icons/" + OVERLAY_ICON_SIZE + "x"
                            + OVERLAY_ICON_SIZE + "/emblems/task-"
                            + task.getPriority() + ".png";
                    is = TaskLabelProvider.class.getResourceAsStream(rID);
                    BufferedImage priorityImg = ImageIO.read(is);

                    int offset = ICON_SIZE - OVERLAY_ICON_SIZE;
                    ImageAdaptor combImg = ImageHelper.combine(statusImg,
                            priorityImg, offset, offset);

                    result = combImg.toSwtImage();
                    reg.put(id, result);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public String getText(Object element) {
        Task task = (Task) element;
        String text = task.getTitle();
        return text;
    }

    public String getToolTipText(Object element) {
        Task task = (Task) element;

        final int maxLength = 100;
        String text = ""; //$NON-NLS-1$
        String title = task.getTitle();
        if (title != null) {
            title = title.length() > maxLength ? title.substring(0, maxLength)
                    + "..." : title; //$NON-NLS-1$
        } else {
            title = "-"; //$NON-NLS-1$
        }
        text += Messages.getString("Title") //$NON-NLS-1$
                + ": " + title; //$NON-NLS-1$ 
        text += "\n" + Messages.getString("Status") //$NON-NLS-1$//$NON-NLS-2$
                + ": " + task.getStatus(); //$NON-NLS-1$ 
        String summary = task.getSummary();
        if (summary != null) {
            summary = summary.length() > maxLength ? summary.substring(0,
                    maxLength)
                    + "..." : summary; //$NON-NLS-1$
        } else {
            summary = "-"; //$NON-NLS-1$
        }
        text += "\n" + Messages.getString("Summary") //$NON-NLS-1$ //$NON-NLS-2$
                + ": " + summary; //$NON-NLS-1$ 
        return text;
    }

    public int getToolTipTimeDisplayed(Object object) {
        return 2000;
    }

    public int getToolTipDisplayDelayTime(Object object) {
        return 10;
    }
}
