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
package com.mindquarry.desktop.preferences.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.bindings.keys.KeySequenceText;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/**
 * This class creates a preference page for shortcuts.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class ShortcutsPage extends PreferencePage {
    public static final String NAME = "shortcuts"; //$NON-NLS-1$
    
    public static final String SHORTCUT_KEY_BASE = "com.mindquarry.desktop.shortcut."; //$NON-NLS-1$

    private static final String CATEGORY_COL_ID = "category"; //$NON-NLS-1$

    private static final String ACTION_COL_ID = "action"; //$NON-NLS-1$

    private static final String SHORTCUT_COL_ID = "shortcut"; //$NON-NLS-1$

    private TableViewer viewer;

    private List<Shortcut> shortcuts;

    /**
     * ShortcutsPage default constructor
     */
    public ShortcutsPage() {
        super("Shortcuts");
        setDescription("Manage shortcuts of the application.");
        setImageDescriptor(ImageDescriptor
                .createFromImage(new Image(
                        null,
                        getClass()
                                .getResourceAsStream(
                                        "/com/mindquarry/icons/16x16/logo/mindquarry-icon.png"))));
        // init shortcuts
        shortcuts = new ArrayList<Shortcut>();
    }

    /**
     * Creates the controls for this page
     */
    @Override
    protected Control createContents(Composite parent) {
        loadStoredShortcuts();
        
        Table table = new Table(parent, SWT.BORDER | SWT.SINGLE
                | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn col = new TableColumn(table, SWT.NONE);
        col.setText("Category");
        col.setWidth(100);
        col = new TableColumn(table, SWT.NONE);
        col.setText("Action");
        col.setWidth(200);
        col = new TableColumn(table, SWT.NONE);
        col.setText("Shortcut");
        col.setWidth(100);

        CellEditor[] editors = new CellEditor[table.getColumnCount()];
        editors[2] = new KeySequenceTextCellEditor(table);

        TableViewer viewer = new TableViewer(table);
        viewer.setColumnProperties(new String[] { CATEGORY_COL_ID,
                ACTION_COL_ID, SHORTCUT_COL_ID });
        viewer.setCellEditors(editors);
        viewer.setCellModifier(new ShortcutCellModifier(viewer));
        viewer.setContentProvider(new ShortcutContentProvider());
        viewer.setLabelProvider(new ShortcutLabelProvider());
        viewer.setInput(shortcuts.toArray(new Shortcut[0]));
        
        return parent;
    }
    
    class KeySequenceTextCellEditor extends TextCellEditor {
        public KeySequenceTextCellEditor(Composite parent) {
            super(parent);
        }
        
        @Override
        protected Control createControl(Composite parent) {
            Text txt = (Text)super.createControl(parent);
            KeySequenceText kst = new KeySequenceText(txt);
            return txt;
        }
    }

    private void loadStoredShortcuts() {
        PreferenceStore store = (PreferenceStore)getPreferenceStore();
        HashMap<Integer, Shortcut> storedProfiles = new HashMap<Integer, Shortcut>();

        // load stored profiles
        String[] prefs = store.preferenceNames();
        for (String pref : prefs) {
            if (pref.startsWith(SHORTCUT_KEY_BASE)) {
                // analyze preference
                int nbr = Integer.valueOf(pref.substring(SHORTCUT_KEY_BASE
                        .length(), SHORTCUT_KEY_BASE.length() + 1));
                String prefName = pref.substring(SHORTCUT_KEY_BASE.length() + 2,
                        pref.length());

                // init profile
                Shortcut shortcut;
                if (storedProfiles.containsKey(nbr)) {
                    shortcut = storedProfiles.get(nbr);
                } else {
                    shortcut = new Shortcut("", //$NON-NLS-1$
                            "", //$NON-NLS-1$
                            ""); //$NON-NLS-1$
                    storedProfiles.put(nbr, shortcut);
                }
                // set shortcut values
                if (prefName.equals("category")) { //$NON-NLS-1$
                    shortcut.setCategory(store.getString(pref));
                } else if (prefName.equals("action")) { //$NON-NLS-1$
                    shortcut.setAction(store.getString(pref));
                } else if (prefName.equals("shortcut")) { //$NON-NLS-1$
                    shortcut.setShortcutIdentifier(store.getString(pref));
                }
            }
        }
        // set profile list
        Iterator<Integer> keyIter = storedProfiles.keySet().iterator();
        while (keyIter.hasNext()) {
            Shortcut shortcut = storedProfiles.get(keyIter.next());
            shortcuts.add(shortcut);
        }
    }

    @Override
    public boolean performOk() {
        IPreferenceStore store = getPreferenceStore();
        int pos = 0;

        // set properties from shortcuts
        for (Shortcut shortcut : shortcuts) {
            store.putValue(SHORTCUT_KEY_BASE + pos + ".category", //$NON-NLS-1$
                    shortcut.getCategory());
            store.putValue(SHORTCUT_KEY_BASE + pos + ".action", //$NON-NLS-1$
                    shortcut.getAction());
            store.putValue(SHORTCUT_KEY_BASE + pos + ".shortcut", //$NON-NLS-1$
                    shortcut.getShortcutIdentifier());
            pos++;
        }
        return true;
    }

    private class ShortcutCellModifier implements ICellModifier {
        private TableViewer viewer;

        public ShortcutCellModifier(TableViewer viewer) {
            this.viewer = viewer;
        }

        public boolean canModify(Object element, String property) {
            if (property.equals(SHORTCUT_COL_ID)) {
                return true;
            }
            return false;
        }

        public Object getValue(Object element, String property) {
            if (property.equals(SHORTCUT_COL_ID)) {
                return ((Shortcut) element).getShortcutIdentifier();
            }
            return null;
        }

        public void modify(Object element, String property, Object value) {
            if (property.equals(SHORTCUT_COL_ID)) {
                Item item = (Item) element;
                Shortcut shortcut = (Shortcut) item.getData();
                shortcut.setShortcutIdentifier((String) value);
                viewer.refresh(shortcut);
            }
        }
    }

    class Shortcut {
        private String category;

        private String action;

        private String shortcutId;

        public Shortcut(String category, String action, String shortcutId) {
            super();
            this.category = category;
            this.action = action;
            this.shortcutId = shortcutId;
        }

        /**
         * Getter for action.
         * 
         * @return the action
         */
        public String getAction() {
            return action;
        }

        /**
         * Setter for action.
         * 
         * @param action the action to set
         */
        public void setAction(String action) {
            this.action = action;
        }

        /**
         * Getter for category.
         * 
         * @return the category
         */
        public String getCategory() {
            return category;
        }

        /**
         * Setter for category.
         * 
         * @param category the category to set
         */
        public void setCategory(String category) {
            this.category = category;
        }

        /**
         * Getter for shortcut.
         * 
         * @return the shortcut
         */
        public String getShortcutIdentifier() {
            return shortcutId;
        }

        /**
         * Setter for shortcut.
         * 
         * @param shortcut the shortcut to set
         */
        public void setShortcutIdentifier(String shortcutId) {
            this.shortcutId = shortcutId;
        }
    }

    class ShortcutContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            return (Shortcut[]) inputElement;
        }

        public void dispose() {
            // nothing to do here
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // nothing to do here
        }
    }

    class ShortcutLabelProvider implements ITableLabelProvider {
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            Shortcut shortcut = (Shortcut) element;
            switch (columnIndex) {
            case 0:
                return shortcut.getCategory();
            case 1:
                return shortcut.getAction();
            case 2:
                return shortcut.getShortcutIdentifier();
            }
            return null;
        }

        public void addListener(ILabelProviderListener listener) {
            // nothing to do here
        }

        public void dispose() {
            // nothing to do here
        }

        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        public void removeListener(ILabelProviderListener listener) {
            // nothing to do here
        }
    }
}
