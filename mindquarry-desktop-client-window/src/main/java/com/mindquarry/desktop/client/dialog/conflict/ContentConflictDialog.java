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
package com.mindquarry.desktop.client.dialog.conflict;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.tigris.subversion.javahl.Status;
import org.tmatesoft.svn.core.internal.wc.SVNFileUtil;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.util.FileHelper;
import com.mindquarry.desktop.workspace.conflict.ContentConflict;

/**
 * Dialog for resolving replace conflicts.
 * 
 * @author <a href="mailto:victor(dot)saar(at)mindquarry(dot)com">Victor Saar</a>
 * @author <a href="mailto:christian(dot)richardt(at)mindquarry(dot)com">Christian Richardt</a>
 */
public class ContentConflictDialog extends AbstractConflictDialog {

    private static Log log = LogFactory.getLog(ContentConflictDialog.class);

    private static final String MERGE_USING_WORD_HELP = Messages
            .getString("After starting MS Word, merge both versions "
                    + "and save the result. Then click the 'Done' button that "
                    + "will appear and click 'OK'."); //$NON-NLS-1$
    private static final String MERGE_MANUALLY_HELP = Messages.getString(
            "Please use the various file version at the top to merge the "
            + "changes into the target file. Click 'Finished Merging' and then "
            + "'OK' when done."); //$NON-NLS-1$

    private ContentConflict conflict;
    private ContentConflict.Action resolveMethod = ContentConflict.Action.MERGE;

    private File mergedVersion = null;
    private File mergedVersionTarget = null;
    private Button mergeButton = null;
    private Button mergeOptionButton;
    private Label mergeHelpLabel;

    // files used when merging versions with MS Word, will
    // may be deleted at the end:
    private List<File> tempFiles = new ArrayList<File>();

    public ContentConflictDialog(ContentConflict conflict, Shell shell) {
        super(shell);
        this.conflict = conflict;
    }

    /**
     * Shows the filename of the affected file and displays buttons for directly
     * opening the various files created by the conflict.
     */
    protected void showFileInformation(Composite composite) {
        Label name = new Label(composite, SWT.READ_ONLY);
        name.setText(Messages.getString("Filename(s)") + ": "
                + conflict.getStatus().getPath());
        
        Composite fileButtonBar = new Composite(composite, SWT.NONE);
        fileButtonBar.setLayout(new RowLayout(SWT.HORIZONTAL));
        
        // Button 1: old revision which both the local and the remote files are based on
        Button openOldFileButton = new Button(fileButtonBar, SWT.BUTTON1);
        openOldFileButton.setText(Messages.getString("Open original file")); //$NON-NLS-1$
        openOldFileButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event arg0) {
                Program.launch(conflict.getConflictOldFile().getAbsolutePath());
            }
        });

        // Button 2: locally modified version of the file
        Button openMyFileButton = new Button(fileButtonBar, SWT.BUTTON1);
        openMyFileButton.setText(Messages.getString("Open my local file")); //$NON-NLS-1$
        openMyFileButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event arg0) {
                Program.launch(conflict.getConflictWorkingFile().getAbsolutePath());
            }
        });

        // Button 3: new revision from server which contains the remote changes
        Button openServerFileButton = new Button(fileButtonBar, SWT.BUTTON1);
        openServerFileButton.setText(Messages.getString("Open updated file from server")); //$NON-NLS-1$
        openServerFileButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event arg0) {
                Program.launch(conflict.getConflictNewFile().getAbsolutePath());
            }
        });

        // Button 4: automatically created file using text-based merging
        Button openMergedFileButton = new Button(fileButtonBar, SWT.BUTTON1);
        openMergedFileButton.setText(Messages.getString("Target file (automatically merged)")); //$NON-NLS-1$
        openMergedFileButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event arg0) {
                Program.launch(conflict.getStatus().getPath());
            }
        });
    }

    @Override
    protected String getMessage() {
        // TODO: move the information about which user made the change to
        // AbstractConflictDialog so (almost) all other dialogs can show it,
        // too:
        return Messages
                .getString("The file you are trying to synchronize was modified on the server. "
                        + "Please select a resolution method. ")
                + Messages
                        .getString("Latest change on the server was done by user: ")
                + conflict.getStatus().getLastCommitAuthor(); // TODO: show 'User Name' rather than 'user'
    }

    @Override
    protected void createLowerDialogArea(Composite composite) {
        Composite subComposite = new Composite(composite, SWT.NONE);
        subComposite.setLayout(new RowLayout(SWT.VERTICAL));

        // TODO:
        // Composite mergeComposite = new Composite(subComposite, SWT.NONE);
        // mergeComposite.setLayout(new RowLayout(SWT.HORIZONTAL));

        // TODO: make this use the mimetype and make it extensible
        // to other file types:
        boolean isWordDocument = false;
        String lcFilename = conflict.getStatus().getPath();
        if (lcFilename != null) {
            lcFilename = lcFilename.toLowerCase();
            if (lcFilename.endsWith(".doc") || lcFilename.endsWith(".docx")) {
                isWordDocument = true;
            }
        }
        // the current merge solution for MS Word is based on a script that
        // works only on Windows:
        boolean isWindows = SVNFileUtil.isWindows;
        boolean offerMSWordMerge = isWordDocument && isWindows;

        // Option 1: use locally modified file
        Button button1 = makeRadioButton(subComposite, Messages
                .getString("Use your local version of the file"), //$NON-NLS-1$
                ContentConflict.Action.USE_LOCAL, false);
        button1.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                enableButtons(true, false);
            }
        });

        // Option 2: use remotely modified file
        Button button2 = makeRadioButton(subComposite, Messages
                .getString("Use the file from the server"), //$NON-NLS-1$
                ContentConflict.Action.USE_REMOTE, false);
        button2.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                enableButtons(true, false);
            }
        });

        // Option 3: merge manually (recommended)
        mergeOptionButton = makeRadioButton(subComposite, Messages
                .getString("Manually merge both files (recommended)"), //$NON-NLS-1$
                ContentConflict.Action.MERGE, false);

        mergeHelpLabel = new Label(subComposite, SWT.WRAP);
        RowData rowData = new RowData();
        rowData.width = 500;
        mergeHelpLabel.setLayoutData(rowData);
        mergeHelpLabel.setEnabled(false);

        mergeButton = new Button(subComposite, SWT.BUTTON1);

        if (offerMSWordMerge) { // merge using Microsoft Word
            mergeOptionButton.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    enableButtons(false, true);
                }
            });

            mergeHelpLabel.setText(MERGE_USING_WORD_HELP);

            mergeButton.setText(Messages.getString("Start MS Word")); //$NON-NLS-1$
            mergeButton.addListener(SWT.Selection,
                    new MergeButtonListener());
        } else { // manual merge
            mergeOptionButton.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    enableButtons(false, true);
                }
            });

            mergeHelpLabel.setText(MERGE_MANUALLY_HELP);

            mergeButton.setText(Messages.getString("Finished Merging")); //$NON-NLS-1$
            mergeButton.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event arg0) {
                    enableButtons(true, false);
                }
            });
        }
        mergeButton.setEnabled(false);
    }
    
    /**
     * Enables the OK button and the merge options.
     * @param okButton Enable the OK button.
     * @param mergeButton Enable the merge button and the merge help text.
     */
    private void enableButtons(boolean okButton, boolean mergeButton) {
        if(this.okButton != null) {
            this.okButton.setEnabled(okButton);
        }
        if(this.mergeButton != null) {
            this.mergeButton.setEnabled(mergeButton);
        }
        if(this.mergeHelpLabel != null) {
            this.mergeHelpLabel.setEnabled(mergeButton);
        }
    }

    protected String getHelpURL() {
        // TODO fix help URL
        return "http://www.mindquarry.com/";
    }

    private void mergeWordDocumentsManually(Status status, String basePath)
            throws IOException {
        File localVersion = new File(status.getPath());
        File serverVersion = new File(basePath, status.getConflictNew());
        log.debug("merge: serverVersion: " + serverVersion);
        log.debug("merge: localVersion: " + localVersion);

        // use the correct filename suffix:
        String suffix = FilenameUtils.getExtension(localVersion.getName());
        File tmpServerVersion = File.createTempFile("mindquarry-merge-server",
                "." + suffix);
        FileUtils.copyFile(serverVersion, tmpServerVersion);
        tempFiles.add(tmpServerVersion);

        mergedVersion = File.createTempFile("mindquarry-merge-local", "."
                + suffix);
        FileUtils.copyFile(localVersion, mergedVersion);
        tempFiles.add(mergedVersion);

        File tmpScriptFile = File.createTempFile("mindquarry-merge-script",
                ".js");
        tempFiles.add(tmpScriptFile);

        //
        // FIXME: delete temp files also in case of 'cancel'
        //

        // load script from JAR and save as temp file to avoid path problems:
        InputStream is = getClass()
                .getResourceAsStream("/scripts/merge-doc.js");
        String script = loadInputStream(is);
        FileWriter fw = new FileWriter(tmpScriptFile);
        fw.write(script);
        fw.close();
        String mergeScript = tmpScriptFile.getAbsolutePath();

        String[] cmdArray = new String[] { "wscript", mergeScript,
                mergedVersion.getAbsolutePath(),
                tmpServerVersion.getAbsolutePath() };
        log.debug("Calling merge script: " + Arrays.toString(cmdArray));

        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(cmdArray);
        int exitValue = -1;
        try {
            exitValue = proc.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e.toString(), e);
        }
        log.debug("Exit value " + exitValue);
        if (exitValue != 0) {
            mergeOptionButton.setEnabled(false);
            mergeButton.setEnabled(false);
            okButton.setEnabled(true); // let user continue with other option
            MessageDialog.openError(getShell(), Messages
                    .getString("Error executing MS Word"), Messages
                    .getString("The script used to merge documents "
                            + "using MS Word could not be started. The exit "
                            + "code was ")
                    + exitValue);
        }
    }

    public static String loadInputStream(InputStream inputStream)
            throws IOException {
        byte[] buffer = new byte[0];
        byte[] tmpbuf = new byte[9];
        while (true) {
            int len = inputStream.read(tmpbuf);
            if (len <= 0) {
                break;
            }
            byte[] newbuf = new byte[buffer.length + len];
            System.arraycopy(buffer, 0, newbuf, 0, buffer.length);
            System.arraycopy(tmpbuf, 0, newbuf, buffer.length, len);
            buffer = newbuf;
        }
        return new String(buffer);
    }

    protected Button makeRadioButton(Composite composite, String text,
            final ContentConflict.Action action, boolean selected) {
        final Button button = new Button(composite, SWT.RADIO);
        button.setText(text);
        button.setSelection(selected);
        button.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                // we get two events on click, so only react on the real
                // selection:
                if (button.getSelection()) {
                    resolveMethod = action;
                }
            }
        });
        return button;
    }

    public ContentConflict.Action getResolveMethod() {
        return resolveMethod;
    }

    class MergeButtonListener implements Listener {
        private boolean buttonStatusDone = false;

        public void handleEvent(Event event) {
            if (buttonStatusDone) {
                okButton.setEnabled(true);
                mergeButton.setEnabled(false);
                try {
                    FileUtils.copyFile(mergedVersion, mergedVersionTarget);
                    for (File tempFile : tempFiles) {
                        FileHelper.delete(tempFile);
                    }
                    tempFiles = new ArrayList<File>();
                } catch (IOException e) {
                    throw new RuntimeException(e.toString(), e);
                }
            } else {
                okButton.setEnabled(false);
                buttonStatusDone = true;
                mergeButton.setText(Messages.getString("Done")); //$NON-NLS-1$
                Status status = conflict.getStatus();
                String parentDir = new File(status.getPath()).getParent();
                try {
                    mergeWordDocumentsManually(status, parentDir);
                    mergedVersionTarget = new File(status.getPath());
                } catch (IOException e) {
                    throw new RuntimeException(e.toString(), e);
                }
            }
        }
    }
}
