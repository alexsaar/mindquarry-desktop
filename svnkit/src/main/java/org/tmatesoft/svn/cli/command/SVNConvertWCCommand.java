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
package org.tmatesoft.svn.cli.command;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

import org.tmatesoft.svn.cli.SVNCommand;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.wc.SVNAdminDirectoryLocator;

/**
 * @author <a href="mailto:alexander(dot)klimetschek(at)mindquarry(dot)com">
 *         Alexander Klimetschek</a>
 *
 */
public class SVNConvertWCCommand extends SVNCommand {

    @Override
    public void run(PrintStream out, PrintStream err) throws SVNException {
        if (getCommandLine().getPathCount() > 0) {
            final String absolutePath = getCommandLine().getPathAt(0);
            matchTabsInPath(absolutePath, err);
            //out.println("converting " + absolutePath);
            SVNAdminDirectoryLocator.convert(new File(absolutePath));
        }
    }

    @Override
    public void run(InputStream in, PrintStream out, PrintStream err)
            throws SVNException {
        run(out, err);
    }

}
