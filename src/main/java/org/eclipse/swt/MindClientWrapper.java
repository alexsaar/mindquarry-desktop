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
package org.eclipse.swt;

import java.io.IOException;

import com.mindquarry.client.MindClient;

/**
 * For some weird reason SWT applications (swt 3.3M4) only work when the
 * class with the static main() method lies within the org.eclipse.swt
 * package. Maybe this has something to do with the load mechanism for the swt
 * native libraries under mac. Anyway, this removes those annoying
 * InvalidThreadExceptions.
 * 
 * @author <a href="mailto:alexander(dot)klimetschek(at)mindquarry(dot)com">
 *         Alexander Klimetschek</a>
 */
public class MindClientWrapper {
    public static void main(String[] args) throws IOException {
        // simply delegate, it only matters that the main() method
        // is inside the org.eclipse.swt package
        MindClient.main(args);
    }
}
