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
package com.mindquarry.desktop.client;

import java.io.IOException;


/**
 * @author <a href="mailto:alexander(dot)klimetschek(at)mindquarry(dot)com">
 *         Alexander Klimetschek</a>
 *
 */
public class MindClientMac {

    public static void main(String[] args) throws IOException {
        String[] cmdArray = new String[6];
        cmdArray[0] = "java";
        cmdArray[1] = "-XstartOnFirstThread";
        cmdArray[2] = "-Xdock:name=Mindquarry Desktop Client";
        cmdArray[3] = "-Dapple.laf.useScreenMenuBar=true";
        cmdArray[4] = "-jar";
        cmdArray[5] = "mindquarry-desktop-client-macosx.jar";
        Runtime.getRuntime().exec(cmdArray);
    }

}
