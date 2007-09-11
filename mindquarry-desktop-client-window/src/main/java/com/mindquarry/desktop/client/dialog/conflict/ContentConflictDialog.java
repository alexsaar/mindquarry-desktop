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
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.tigris.subversion.javahl.Status;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.util.FileHelper;
import com.mindquarry.desktop.workspace.conflict.ContentConflict;

/**
 * Dialog for resolving replace conflicts.
 * 
 * @author <a href="mailto:victor(dot)saar(at)mindquarry(dot)com">Victor Saar</a>
 */
public class ContentConflictDialog extends AbstractConflictDialog {
    private static Log log = LogFactory.getLog(ContentConflictDialog.class);

    private ContentConflict conflict;
    private ContentConflict.Action resolveMethod;

    private static final ContentConflict.Action DEFAULT_RESOLUTION = ContentConflict.Action.USE_LOCAL;
    private static final String MANUALLY_MERGE_TEXT = Messages
            .getString("Manually merge both files");
    private static final String MANUALLY_MERGE_HELP = Messages
            .getString("After starting MS Word, merge both versions "
                    + "and save the result.\nThen click the 'Done' button that "
                    + "will appear and click 'OK'.");

    private File mergedVersion = null;
    private File mergedVersionTarget = null;
    private Button startMergeButton = null;

    // files used when merging versions with MS Word, will
    // may be deleted at the end:
    private List<File> tempFiles = new ArrayList<File>();

    private Button mergeButton;

    public ContentConflictDialog(ContentConflict conflict, Shell shell) {
        super(shell);
        this.conflict = conflict;
        resolveMethod = DEFAULT_RESOLUTION;
    }

    protected void showFileInformation(Composite composite) {
        Label name = new Label(composite, SWT.READ_ONLY);
        name.setText(Messages.getString("Filename(s)") + ": "
                + conflict.getStatus().getPath());
        
        Composite fileButtonBar = new Composite(composite, SWT.NONE);
        fileButtonBar.setLayout(new RowLayout(SWT.HORIZONTAL));
        
        final Status status = conflict.getStatus();
        final File parentDir = new File(status.getPath()).getParentFile();
        
        Button openOldFileButton = new Button(fileButtonBar, SWT.BUTTON1);
        openOldFileButton.setText(Messages.getString("Open original file")); //$NON-NLS-1$
        openOldFileButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event arg0) {
                File file = new File(parentDir, status.getConflictOld());
                Program.launch(file.getAbsolutePath());
            }
        });

        Button openMyFileButton = new Button(fileButtonBar, SWT.BUTTON1);
        openMyFileButton.setText(Messages.getString("Open my local file")); //$NON-NLS-1$
        openMyFileButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event arg0) {
                File file = new File(parentDir, status.getConflictWorking());
                Program.launch(file.getAbsolutePath());
            }
        });
        
        Button openServerFileButton = new Button(fileButtonBar, SWT.BUTTON1);
        openServerFileButton.setText(Messages.getString("Open updated file from server")); //$NON-NLS-1$
        openServerFileButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event arg0) {
                File file = new File(parentDir, status.getConflictNew());
                Program.launch(file.getAbsolutePath());
            }
        });

        Button openMergedFileButton = new Button(fileButtonBar, SWT.BUTTON1);
        openMergedFileButton.setText(Messages.getString("Open automatically merged file")); //$NON-NLS-1$
        openMergedFileButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event arg0) {
                Program.launch(status.getPath());
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
                        + "Please select the version that should be treated as the current version. ")
                + Messages
                        .getString("Latest change on the server was done by user: ")
                + conflict.getStatus().getLastCommitAuthor();
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
        String system = System.getProperty("os.name");
        // the current merge solution for MS Word is based on a script that
        // works only on Windows:
        boolean isWindows = true;
        if (system != null) {
            isWindows = system.startsWith("Windows");
        }
        boolean offerMSWordMerge = isWordDocument && isWindows;

        Button button1 = makeRadioButton(subComposite, Messages
                .getString("Use your local version of the file"), //$NON-NLS-1$
                ContentConflict.Action.USE_LOCAL);
        button1.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                okButton.setEnabled(true);
                if(startMergeButton != null) {
                    startMergeButton.setEnabled(false);
                }
            }
        });

        Button button2 = makeRadioButton(subComposite, Messages
                .getString("Use the file from the server"), //$NON-NLS-1$
                ContentConflict.Action.USE_REMOTE);
        button2.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                okButton.setEnabled(true);
                if(startMergeButton != null) {
                    startMergeButton.setEnabled(false);
                }
            }
        });

        if (offerMSWordMerge) {
            mergeButton = makeRadioButton(subComposite, MANUALLY_MERGE_TEXT, //$NON-NLS-1$
                    ContentConflict.Action.MERGE);
            mergeButton.setText(MANUALLY_MERGE_TEXT);
            mergeButton.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    okButton.setEnabled(false);
                    startMergeButton.setEnabled(true);
                }
            });

            Label mergeHelp = new Label(subComposite, SWT.NONE);
            mergeHelp.setText(MANUALLY_MERGE_HELP);

            startMergeButton = new Button(subComposite, SWT.BUTTON1);
            startMergeButton.setText(Messages.getString("Start MS Word")); //$NON-NLS-1$
            startMergeButton.setEnabled(false);
            startMergeButton.addListener(SWT.Selection,
                    new MergeButtonListener());
        } else { // manual merge
            Button button3 = makeRadioButton(subComposite, Messages
                    .getString("Merge manually, continue only if done"), //$NON-NLS-1$
                    ContentConflict.Action.MERGE);
            button3.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    okButton.setEnabled(true);
                    if(startMergeButton != null) {
                        startMergeButton.setEnabled(false);
                    }
                }
            });
        }

    }

    protected String getHelpURL() {
        // TODO fix help URL
        return "http://www.mindquarry.com/";
    }

    private void mergeManually(Status status, String basePath)
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
            mergeButton.setEnabled(false);
            startMergeButton.setEnabled(false);
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
            final ContentConflict.Action action) {
        final Button button = new Button(composite, SWT.RADIO);
        button.setText(text);
        if (action == DEFAULT_RESOLUTION) {
            button.setSelection(true);
        }
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
                startMergeButton.setEnabled(false);
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
                startMergeButton.setText(Messages.getString("Done")); //$NON-NLS-1$
                Status status = conflict.getStatus();
                String parentDir = new File(status.getPath()).getParent();
                try {
                    mergeManually(status, parentDir);
                    mergedVersionTarget = new File(status.getPath());
                } catch (IOException e) {
                    throw new RuntimeException(e.toString(), e);
                }
            }
        }
    }
}
