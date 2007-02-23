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
package org.eclipse.swt.widgets;

import java.lang.reflect.Method;

import org.eclipse.swt.graphics.Point;

/**
 * TrayItem.getLocation() is package-private. This class helps to access this
 * information, since it is in the same package.
 * 
 * @author <a href="mailto:alexander(dot)klimetschek(at)mindquarry(dot)com">
 *         Alexander Klimetschek</a>
 * 
 */
public class GetTrayItemLocationHackMacOSX {

    public static Point getLocation(TrayItem trayItem) {
        try {
            Method m = TrayItem.class.getDeclaredMethod("getLocation", //$NON-NLS-1$
                    new Class[] {});
            return (Point) m.invoke(trayItem, new Object[] {});
        } catch (Exception e) {
            return new Point(0, 0);
        }
    }

    public static Point getAlignedLocation(TrayItem trayItem) {
        Point p = getLocation(trayItem);
        return new Point(p.x - 11, p.y + 4);
    }
}
