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
package com.mindquarry.desktop.workspace.conflict;

import com.mindquarry.desktop.workspace.exception.CancelException;

/**
 * @author <a href="mailto:alexander(dot)klimetschek(at)mindquarry(dot)com">
 *         Alexander Klimetschek</a>
 *
 */
public class ConsoleConflictHandler implements ConflictHandler {
    
    private ConflictPrinter printer;

    public ConsoleConflictHandler(String workingCopyPath) {
        this.printer = new ConflictPrinter(workingCopyPath);
    }

    public void handle(AddConflict conflict) throws CancelException {
        printer.printConflict(conflict);

        System.out.println("Following options for local '" + printer.wcPath(conflict.getStatus()) + "': re(N)ame, (R)eplace: ");
        //option = readLine();
        System.out.println("Rename locally added file/folder to: ");
    }

    public void handle(DeleteWithModificationConflict conflict)
            throws CancelException {
        printer.printConflict(conflict);
        
        System.out.println("Following options (K)eep modified, (D)elete, (R)evert delete: ");
    }

    public void handle(ReplaceConflict conflict) throws CancelException {
        printer.printConflict(conflict);
        
    }


}
