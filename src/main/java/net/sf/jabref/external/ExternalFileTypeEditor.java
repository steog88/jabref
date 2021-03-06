/*  Copyright (C) 2003-2011 JabRef contributors.
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package net.sf.jabref.external;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import net.sf.jabref.GUIGlobals;
import net.sf.jabref.Globals;
import net.sf.jabref.JabRefFrame;
import net.sf.jabref.MnemonicAwareAction;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.ButtonStackBuilder;

/**
 * Editor for external file types.
 */
public class ExternalFileTypeEditor extends JDialog {

    private JFrame frame = null;
    private JDialog dialog = null;
    private ArrayList<ExternalFileType> fileTypes;
    private JTable table;
    private ExternalFileTypeEntryEditor entryEditor = null;
    private FileTypeTableModel tableModel;
    private final JButton ok = new JButton(Globals.lang("Ok"));
    private final JButton cancel = new JButton(Globals.lang("Cancel"));
    private final JButton add = new JButton(GUIGlobals.getImage("add"));
    private final JButton remove = new JButton(GUIGlobals.getImage("remove"));
    private final JButton edit = new JButton(GUIGlobals.getImage("edit"));
    private final JButton toDefaults = new JButton(Globals.lang("Default"));
    private final EditListener editListener = new EditListener();


    private ExternalFileTypeEditor(JFrame frame) {
        super(frame, Globals.lang("Manage external file types"), true);
        this.frame = frame;
        init();
    }

    private ExternalFileTypeEditor(JDialog dialog) {
        super(dialog, Globals.lang("Manage external file types"), true);
        this.dialog = dialog;
        init();
    }

    /**
     * Update the editor to show the current settings in Preferences.
     */
    private void setValues() {
        fileTypes.clear();
        ExternalFileType[] types = Globals.prefs.getExternalFileTypeSelection();
        for (ExternalFileType type : types) {

            fileTypes.add(type.copy());
        }
        Collections.sort(fileTypes);
    }

    /**
     * Store the list of external entry types to Preferences.
     */
    private void storeSettings() {
        Globals.prefs.setExternalFileTypes(fileTypes);
    }

    private void init() {

        ok.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                storeSettings();
                dispose();
            }
        });
        AbstractAction cancelAction = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };
        cancel.addActionListener(cancelAction);
        // The toDefaults resets the entire list to its default values.
        toDefaults.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                /*int reply = JOptionPane.showConfirmDialog(ExternalFileTypeEditor.this,
                        Globals.lang("All custom file types will be lost. Proceed?"),
                        Globals.lang("Reset file type definitons"), JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);*/
                //if (reply == JOptionPane.YES_OPTION) {
                java.util.List<ExternalFileType> list = Globals.prefs.getDefaultExternalFileTypes();
                fileTypes.clear();
                fileTypes.addAll(list);
                Collections.sort(fileTypes);
                //Globals.prefs.resetExternalFileTypesToDefault();
                //setValues();
                tableModel.fireTableDataChanged();
                //}
            }
        });

        add.addActionListener(new AddListener());
        remove.addActionListener(new RemoveListener());
        edit.addActionListener(editListener);
        fileTypes = new ArrayList<ExternalFileType>();
        setValues();

        tableModel = new FileTypeTableModel();
        table = new JTable(tableModel);
        table.setDefaultRenderer(ImageIcon.class, new IconRenderer());
        table.addMouseListener(new TableClickListener());

        table.getColumnModel().getColumn(0).setMaxWidth(24);
        table.getColumnModel().getColumn(0).setMinWidth(24);
        table.getColumnModel().getColumn(1).setMinWidth(170);
        table.getColumnModel().getColumn(2).setMinWidth(60);
        table.getColumnModel().getColumn(3).setMinWidth(100);
        table.getColumnModel().getColumn(0).setResizable(false);

        JScrollPane sp = new JScrollPane(table);

        JPanel upper = new JPanel();
        upper.setLayout(new BorderLayout());
        upper.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        upper.add(sp, BorderLayout.CENTER);
        getContentPane().add(upper, BorderLayout.CENTER);

        ButtonStackBuilder bs = new ButtonStackBuilder();
        bs.addButton(add);
        bs.addButton(remove);
        bs.addButton(edit);
        bs.addRelatedGap();
        bs.addButton(toDefaults);
        upper.add(bs.getPanel(), BorderLayout.EAST);

        ButtonBarBuilder bb = new ButtonBarBuilder();
        bb.addGlue();
        bb.addButton(ok);
        bb.addButton(cancel);
        bb.addGlue();
        bb.getPanel().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        getContentPane().add(bb.getPanel(), BorderLayout.SOUTH);
        pack();

        // Key bindings:
        ActionMap am = upper.getActionMap();
        InputMap im = upper.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        im.put(Globals.prefs.getKey("Close dialog"), "close");
        am.put("close", cancelAction);
        am = bb.getPanel().getActionMap();
        im = bb.getPanel().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        im.put(Globals.prefs.getKey("Close dialog"), "close");
        am.put("close", cancelAction);

        if (frame != null) {
            setLocationRelativeTo(frame);
        } else {
            setLocationRelativeTo(dialog);
        }
    }

    private ExternalFileTypeEntryEditor getEditor(ExternalFileType type) {
        if (entryEditor == null) {
            entryEditor = new ExternalFileTypeEntryEditor(ExternalFileTypeEditor.this, type);
        } else {
            entryEditor.setEntry(type);
        }
        return entryEditor;
    }

    /**
     * Get an AbstractAction for opening the external file types editor.
     * @param frame The JFrame used as parent window for the dialog.
     * @return An Action for opening the editor.
     */
    public static AbstractAction getAction(JabRefFrame frame) {
        return new EditExternalFileTypesAction(frame);
    }

    /**
     * Get an AbstractAction for opening the external file types editor.
     * @param dialog The JDialog used as parent window for the dialog.
     * @return An Action for opening the editor.
     */
    public static AbstractAction getAction(JDialog dialog) {
        return new EditExternalFileTypesAction(dialog);
    }


    private class AddListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // Generate a new file type:
            ExternalFileType type = new ExternalFileType("", "", "", "", "new");
            // Show the file type editor:
            getEditor(type).setVisible(true);
            if (entryEditor.okPressed()) {
                // Ok was pressed. Add the new file type and update the table:
                fileTypes.add(type);
                tableModel.fireTableDataChanged();
            }
        }
    }

    private class RemoveListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            int[] rows = table.getSelectedRows();
            if (rows.length == 0) {
                return;
            }
            for (int i = rows.length - 1; i >= 0; i--) {
                fileTypes.remove(rows[i]);
            }
            tableModel.fireTableDataChanged();
            if (fileTypes.size() > 0) {
                int row = Math.min(rows[0], fileTypes.size() - 1);
                table.setRowSelectionInterval(row, row);
            }
        }
    }

    class EditListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            int[] rows = table.getSelectedRows();
            if (rows.length != 1) {
                return;
            }
            getEditor(fileTypes.get(rows[0])).setVisible(true);
            if (entryEditor.okPressed()) {
                tableModel.fireTableDataChanged();
            }
        }
    }

    class IconRenderer implements TableCellRenderer {

        final JLabel lab = new JLabel();


        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            lab.setText(null);
            lab.setIcon((ImageIcon) value);
            return lab;
        }
    }

    private class FileTypeTableModel extends AbstractTableModel {

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public int getRowCount() {
            return fileTypes.size();
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
            case 0:
                return " ";
            case 1:
                return Globals.lang("Name");
            case 2:
                return Globals.lang("Extension");
            case 3:
                return Globals.lang("MIME type");
            case 4:
                return Globals.lang("Application");
            default:
                return null;
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return ImageIcon.class;
            } else {
                return String.class;
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ExternalFileType type = fileTypes.get(rowIndex);
            switch (columnIndex) {
            case 0:
                return type.getIcon();
            case 1:
                return type.getName();
            case 2:
                return type.getExtension();
            case 3:
                return type.getMimeType();
            case 4:
                return type.getOpenWith();
            default:
                return null;
            }
        }
    }

    class TableClickListener extends MouseAdapter {

        private void handleClick(MouseEvent e) {
            if (e.getClickCount() == 2) {
                editListener.actionPerformed(null);
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            handleClick(e);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            handleClick(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            handleClick(e);
        }
    }

    public static class EditExternalFileTypesAction extends MnemonicAwareAction {

        private JabRefFrame frame = null;
        private JDialog dialog = null;
        ExternalFileTypeEditor editor = null;


        public EditExternalFileTypesAction(JabRefFrame frame) {
            super();
            putValue(Action.NAME, "Manage external file types");
            this.frame = frame;
        }

        public EditExternalFileTypesAction(JDialog dialog) {
            super();
            putValue(Action.NAME, "Manage external file types");
            this.dialog = dialog;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (editor == null) {
                if (frame != null) {
                    editor = new ExternalFileTypeEditor(frame);
                } else {
                    editor = new ExternalFileTypeEditor(dialog);
                }
            }
            editor.setValues();
            editor.setVisible(true);
            if (frame != null) {
                if (frame.basePanel() != null) {
                    frame.basePanel().mainTable.repaint();
                }
            }
        }
    }

}
