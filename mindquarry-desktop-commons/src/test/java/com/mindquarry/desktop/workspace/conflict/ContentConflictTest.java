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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

/**
 * Tests renaming various conflict files (i.e. file.r12, file.r13, file.mine).
 * 
 * @author <a href="mailto:christian(dot)richardt(at)mindquarry(dot)com">
 *         Christian Richardt</a>
 * 
 */
public class ContentConflictTest {

    /**
     * Test method for
     * {@link com.mindquarry.desktop.workspace.conflict.ContentConflict#renameConflictFile(java.io.File)}.
     * 
     * @throws IOException
     */
    @Test
    public void testRenameConflictFile() throws IOException {
        // find temp dir
        File tempFile = File.createTempFile("prefix", "suffix");
        File temp = tempFile.getParentFile();

        File conflictFile = null;
        File renamedFile = null;

        // Case 1: file => no rename
        conflictFile = setupConflictFile(temp, "file");
        renamedFile = ContentConflict.renameConflictFile(conflictFile);
        assertNoRename(conflictFile, renamedFile, "file");
        cleanupFiles(conflictFile, renamedFile);

        // Case 2: file.ext => no rename
        conflictFile = setupConflictFile(temp, "file.ext");
        renamedFile = ContentConflict.renameConflictFile(conflictFile);
        assertNoRename(conflictFile, renamedFile, "file.ext");
        cleanupFiles(conflictFile, renamedFile);

        // Case 3: .file => no rename
        conflictFile = setupConflictFile(temp, ".file");
        renamedFile = ContentConflict.renameConflictFile(conflictFile);
        assertNoRename(conflictFile, renamedFile, ".file");
        cleanupFiles(conflictFile, renamedFile);

        // Case 4: file. => no rename
        conflictFile = setupConflictFile(temp, "file.");
        renamedFile = ContentConflict.renameConflictFile(conflictFile);
        assertNoRename(conflictFile, renamedFile, "file.");
        cleanupFiles(conflictFile, renamedFile);

        // Case 5: file.ext1.ext2 => do rename
        conflictFile = setupConflictFile(temp, "file.ext1.ext2");
        renamedFile = ContentConflict.renameConflictFile(conflictFile);
        assertRename(conflictFile, renamedFile, "file.ext2.ext1");
        cleanupFiles(conflictFile, renamedFile);

        // Case 6: .file.ext => do rename
        conflictFile = setupConflictFile(temp, ".file.ext");
        renamedFile = ContentConflict.renameConflictFile(conflictFile);
        assertRename(conflictFile, renamedFile, ".ext.file");
        cleanupFiles(conflictFile, renamedFile);

        // Case 7: file..ext => do rename
        conflictFile = setupConflictFile(temp, "file..ext");
        renamedFile = ContentConflict.renameConflictFile(conflictFile);
        assertRename(conflictFile, renamedFile, "file.ext.");
        cleanupFiles(conflictFile, renamedFile);

        // Case 8: .file. => do rename
        conflictFile = setupConflictFile(temp, ".file.");
        renamedFile = ContentConflict.renameConflictFile(conflictFile);
        assertRename(conflictFile, renamedFile, "..file");
        cleanupFiles(conflictFile, renamedFile);

        // Case 9: ..file => do rename
        conflictFile = setupConflictFile(temp, "..file");
        renamedFile = ContentConflict.renameConflictFile(conflictFile);
        assertRename(conflictFile, renamedFile, ".file.");
        cleanupFiles(conflictFile, renamedFile);

        // Case 10: file.. => do rename (but no effect)
        conflictFile = setupConflictFile(temp, "file..");
        renamedFile = ContentConflict.renameConflictFile(conflictFile);
        assertNoRename(conflictFile, renamedFile, "file..");
        cleanupFiles(conflictFile, renamedFile);

        // Case 11: file.ext1.ext2.ext3 => do rename
        conflictFile = setupConflictFile(temp, "file.ext1.ext2.ext3");
        renamedFile = ContentConflict.renameConflictFile(conflictFile);
        assertRename(conflictFile, renamedFile, "file.ext1.ext3.ext2");
        cleanupFiles(conflictFile, renamedFile);
    }

    private void cleanupFiles(File conflictFile, File renamedFile) {
        if (conflictFile.exists()) {
            conflictFile.delete();
            conflictFile = null;
        }
        if (renamedFile.exists()) {
            renamedFile.delete();
            renamedFile = null;
        }
    }

    private File setupConflictFile(File temp, String filename)
            throws IOException {
        File conflictFile = new File(temp, filename);
        conflictFile.createNewFile();
        return conflictFile;
    }

    private void assertNoRename(File conflictFile, File renamedFile,
            String filename) {
        assertEquals("Conflict and renamed file identical",
                conflictFile, renamedFile);
        assertEquals("Same file name like before",
                conflictFile.getName(), filename);
        assertEquals("Same file name like conflict file",
                renamedFile.getName(), filename);
        assertTrue("Conflict file exists", conflictFile.exists());
        assertTrue("Renamed file exists", renamedFile.exists());
    }

    private void assertRename(File conflictFile, File renamedFile,
            String filename) {
        assertEquals("Conflict and renamed file in same directory",
                conflictFile.getParentFile(), renamedFile.getParentFile());
        assertEquals("Correctly renamed", renamedFile.getName(), filename);
        assertFalse("Conflict file is missing", conflictFile.exists());
        assertTrue("Renamed file exists", renamedFile.exists());
    }

}
