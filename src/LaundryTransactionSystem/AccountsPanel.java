package LaundryTransactionSystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AccountsPanel extends JPanel {
    private final JFrame parent;
    private final AccountManager manager;
    private final Account signedIn;
    private final List<Account> visible = new ArrayList<>();
    private final AccountsTableModel model = new AccountsTableModel();
    private final JTable table = new JTable(model);
    private HintTextField search;
    private JLabel resultCount;

    public AccountsPanel(JFrame parent, AccountManager manager, Account signedIn) {
        this.parent = parent;
        this.manager = manager;
        this.signedIn = signedIn;
        if (!manager.isAdmin(signedIn)) throw new SecurityException("Accounts page is for administrators only.");
        setLayout(new BorderLayout());
        setBackground(Theme.PAGE_BLUE);
        build();
        refreshAccounts();
    }

    private void build() {
        JPanel body = new JPanel(new BorderLayout(0, 20));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(25, 30, 25, 30));
        add(body, BorderLayout.CENTER);

        JPanel top = new JPanel(new BorderLayout(12, 10));
        top.setOpaque(false);
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Account Management");
        title.setForeground(Theme.NAVY);
        title.setFont(new Font("Arial", Font.BOLD, 29));
        heading.add(title);
        top.add(heading, BorderLayout.WEST);
        JButton add = button("+ Add Account", Theme.BLUE, Color.WHITE);
        add.addActionListener(e -> addAccount());
        top.add(add, BorderLayout.EAST);
        body.add(top, BorderLayout.NORTH);

        RoundedPanel card = new RoundedPanel(20);
        card.setLayout(new BorderLayout(0, 17));
        card.setBackground(Theme.WHITE);
        card.setBorder(new EmptyBorder(22, 23, 20, 23));
        body.add(card, BorderLayout.CENTER);

        JPanel searchRow = new JPanel(new BorderLayout(12, 0));
        searchRow.setOpaque(false);
        RoundedPanel searchBox = new RoundedPanel(16);
        searchBox.setLayout(new BorderLayout());
        searchBox.setBackground(new Color(244, 248, 255));
        searchBox.setOutlineColor(new Color(96, 135, 183));
        searchBox.setBorder(new EmptyBorder(8, 13, 8, 13));
        search = new HintTextField("Search user ID, name, or username...");
        search.setOpaque(false);
        search.setBorder(null);
        search.setFont(new Font("Arial", Font.PLAIN, 14));
        search.setForeground(Theme.NAVY);
        search.setPreferredSize(new Dimension(360, 26));
        searchBox.add(search);
        searchRow.add(searchBox, BorderLayout.CENTER);
        resultCount = new JLabel();
        resultCount.setForeground(Theme.MUTED_BLUE);
        resultCount.setFont(new Font("Arial", Font.PLAIN, 13));
        searchRow.add(resultCount, BorderLayout.EAST);
        card.add(searchRow, BorderLayout.NORTH);

        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setForeground(Theme.NAVY);
        table.setBackground(Theme.WHITE);
        table.setRowHeight(53);
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(151, 178, 210));
        table.setSelectionBackground(new Color(229, 240, 255));
        table.setSelectionForeground(Theme.NAVY);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setPreferredSize(new Dimension(0, 47));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setForeground(Theme.NAVY);
        table.getTableHeader().setBackground(new Color(240, 246, 255));
        table.getTableHeader().setReorderingAllowed(false);
        DefaultTableCellRenderer centered = new DefaultTableCellRenderer();
        centered.setHorizontalAlignment(SwingConstants.CENTER);
        centered.setBorder(new EmptyBorder(0, 6, 0, 6));
        for (int i = 0; i < 5; i++) table.getColumnModel().getColumn(i).setCellRenderer(centered);
        DefaultTableCellRenderer status = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean selected, boolean focus, int row, int column) {
                super.getTableCellRendererComponent(t, value, selected, focus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!selected) setForeground("Blocked".equals(value)
                        ? new Color(176, 40, 40) : new Color(32, 135, 70));
                return this;
            }
        };
        table.getColumnModel().getColumn(4).setCellRenderer(status);
        table.getTableHeader().setDefaultRenderer(centeredHeader());
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(170);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(105);
        table.getColumnModel().getColumn(5).setPreferredWidth(260);
        table.getColumnModel().getColumn(5).setCellRenderer(new ActionsRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ActionsEditor());
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(96, 135, 183), 2));
        scroll.getViewport().setBackground(Theme.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        JLabel note = new JLabel("Accounts is available to administrators only!");
        note.setFont(new Font("Arial", Font.PLAIN, 12));
        note.setForeground(Theme.MUTED_BLUE);
        card.add(note, BorderLayout.SOUTH);

        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refreshAccounts(); }
            @Override public void removeUpdate(DocumentEvent e) { refreshAccounts(); }
            @Override public void changedUpdate(DocumentEvent e) { refreshAccounts(); }
        });
    }

    private javax.swing.table.TableCellRenderer centeredHeader() {
        DefaultTableCellRenderer header = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean selected, boolean focus, int row, int column) {
                super.getTableCellRendererComponent(t, value, selected, focus, row, column);
                setFont(new Font("Arial", Font.BOLD, 13));
                return this;
            }
        };
        header.setHorizontalAlignment(SwingConstants.CENTER);
        header.setForeground(Theme.NAVY);
        header.setBackground(new Color(240, 246, 255));
        header.setFont(new Font("Arial", Font.BOLD, 13));
        header.setOpaque(true);
        return header;
    }
    private JButton button(String name, Color back, Color front) {
        RoundedButton b = new RoundedButton(name, 14);
        b.setBackground(back);
        b.setForeground(front);
        b.setFont(new Font("Arial", Font.BOLD, 13));
        b.setBorder(new EmptyBorder(10, 16, 10, 16));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
    public void refreshAccounts() {
        if (!manager.isAdmin(signedIn)) return;
        if (search == null) return;
        if (table.isEditing()) table.getCellEditor().stopCellEditing();
        visible.clear();
        visible.addAll(manager.search(search.getText()));
        model.fireTableDataChanged();
        resultCount.setText(visible.size() + " of " + manager.count() + " accounts");
    }
    private JPanel form(JTextField name, JTextField username, JComboBox<String> role,
                        JPasswordField password, JPasswordField confirm) {
        JPanel form = DialogUI.column();
        DialogUI.field(name);
        DialogUI.field(username);
        DialogUI.combo(role);
        DialogUI.addField(form, "Full Name", name);
        DialogUI.addField(form, "Username", username);
        DialogUI.addField(form, "Access Level", role);
        if (password != null) {
            DialogUI.field(password);
            DialogUI.field(confirm);
            DialogUI.addField(form, "Password (at least 6 characters)", password);
            DialogUI.addField(form, "Confirm Password", confirm);
        }
        return form;
    }
    private JTextField registrationField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Arial", Font.PLAIN, 15));
        field.setBackground(new Color(248, 250, 252));
        field.setForeground(new Color(23, 43, 77));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                new EmptyBorder(0, 12, 0, 12)));
        field.setPreferredSize(new Dimension(390, 43));
        return field;
    }

    private JPasswordField registrationPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Arial", Font.PLAIN, 15));
        field.setBackground(new Color(248, 250, 252));
        field.setForeground(new Color(23, 43, 77));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                new EmptyBorder(0, 12, 0, 12)));
        field.setPreferredSize(new Dimension(390, 43));
        return field;
    }

    private void addRegistrationField(JPanel form, int index, String caption, JComponent field) {
        GridBagConstraints labelPosition = new GridBagConstraints();
        labelPosition.gridx = 0;
        labelPosition.gridy = index * 2;
        labelPosition.weightx = 1;
        labelPosition.fill = GridBagConstraints.HORIZONTAL;
        labelPosition.anchor = GridBagConstraints.WEST;
        labelPosition.insets = new Insets(0, 0, 5, 0);
        JLabel label = new JLabel(caption);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(23, 43, 77));
        form.add(label, labelPosition);

        GridBagConstraints fieldPosition = new GridBagConstraints();
        fieldPosition.gridx = 0;
        fieldPosition.gridy = index * 2 + 1;
        fieldPosition.weightx = 1;
        fieldPosition.fill = GridBagConstraints.HORIZONTAL;
        fieldPosition.insets = new Insets(0, 0, 14, 0);
        form.add(field, fieldPosition);
    }

    private void addAccount() {
        final Color darkBlue = new Color(11, 75, 163);
        final Color darkText = new Color(23, 43, 77);
        final Color yellow = new Color(226, 233, 85);
        final Color white = Color.WHITE;

        JDialog dialog = new JDialog(parent, "Create New Account", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(530, 745);
        dialog.setMinimumSize(new Dimension(490, 690));
        dialog.setLocationRelativeTo(parent);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(white);
        dialog.setContentPane(outer);

        JPanel header = new JPanel();
        header.setBackground(darkBlue);
        header.setBorder(new EmptyBorder(19, 18, 18, 18));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel brand = new JLabel("JOCASHA'S LAUNDRY SHOP", SwingConstants.CENTER);
        brand.setFont(new Font("Sylfaen", Font.BOLD, 20));
        brand.setForeground(white);
        brand.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel title = new JLabel("Create New Account", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 27));
        title.setForeground(white);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel subtitle = new JLabel("Register an employee or administrator", SwingConstants.CENTER);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(new Color(223, 235, 255));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(brand);
        header.add(Box.createVerticalStrut(11));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        outer.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 6));
        content.setBackground(white);
        content.setBorder(new EmptyBorder(21, 38, 24, 38));
        outer.add(content, BorderLayout.CENTER);

        JTextField name = registrationField();
        JTextField username = registrationField();
        JComboBox<String> role = new JComboBox<>(new String[]{"Employee", "Admin"});
        role.setFont(new Font("Arial", Font.PLAIN, 15));
        role.setBackground(new Color(248, 250, 252));
        role.setForeground(darkText);
        role.setPreferredSize(new Dimension(390, 43));
        role.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        JPasswordField password = registrationPasswordField();
        JPasswordField confirm = registrationPasswordField();

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(white);
        addRegistrationField(fields, 0, "Full Name", name);
        addRegistrationField(fields, 1, "Username", username);
        addRegistrationField(fields, 2, "Access Level", role);
        addRegistrationField(fields, 3, "Password (at least 6 characters)", password);
        addRegistrationField(fields, 4, "Confirm Password", confirm);

        GridBagConstraints showPosition = new GridBagConstraints();
        showPosition.gridx = 0;
        showPosition.gridy = 10;
        showPosition.anchor = GridBagConstraints.WEST;
        showPosition.fill = GridBagConstraints.HORIZONTAL;
        showPosition.weightx = 1;
        JCheckBox showPassword = new JCheckBox("Show password");
        showPassword.setBackground(white);
        showPassword.setForeground(darkText);
        showPassword.setFont(new Font("Arial", Font.BOLD, 13));
        showPassword.setFocusPainted(false);
        final char mask = password.getEchoChar();
        showPassword.addActionListener(e -> {
            char echo = showPassword.isSelected() ? (char) 0 : mask;
            password.setEchoChar(echo);
            confirm.setEchoChar(echo);
        });
        fields.add(showPassword, showPosition);
        content.add(fields, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(0, 8));
        footer.setBackground(white);
        JTextArea errorMessage = new JTextArea(" ", 2, 1);
        errorMessage.setFont(new Font("Arial", Font.PLAIN, 12));
        errorMessage.setForeground(new Color(178, 45, 45));
        errorMessage.setBackground(white);
        errorMessage.setLineWrap(true);
        errorMessage.setWrapStyleWord(true);
        errorMessage.setEditable(false);
        errorMessage.setFocusable(false);
        footer.add(errorMessage, BorderLayout.NORTH);

        JPanel actionButtons = new JPanel(new GridLayout(1, 2, 12, 0));
        actionButtons.setBackground(white);
        JButton cancel = button("Cancel", new Color(234, 241, 251), darkBlue);
        JButton save = button("Save Account", yellow, darkText);
        cancel.setFont(new Font("Arial", Font.BOLD, 15));
        save.setFont(new Font("Arial", Font.BOLD, 15));
        cancel.setPreferredSize(new Dimension(190, 47));
        save.setPreferredSize(new Dimension(190, 47));
        actionButtons.add(cancel);
        actionButtons.add(save);
        footer.add(actionButtons, BorderLayout.SOUTH);
        content.add(footer, BorderLayout.SOUTH);

        cancel.addActionListener(e -> dialog.dispose());
        save.addActionListener(e -> {
            char[] pwd = password.getPassword();
            char[] confirmation = confirm.getPassword();
            try {
                Account added = manager.createAccount(signedIn,
                        name.getText(), username.getText(),
                        (String) role.getSelectedItem(), pwd, confirmation);
                refreshAccounts();
                dialog.dispose();
                JOptionPane.showMessageDialog(parent,
                        added.getFullName() + " registered as " + added.getId() + ".",
                        "Account Added", JOptionPane.INFORMATION_MESSAGE);
            } catch (IllegalArgumentException | SecurityException ex) {
                errorMessage.setText(ex.getMessage());
                password.setText("");
                confirm.setText("");
                password.requestFocusInWindow();
            } finally {
                Arrays.fill(pwd, '\0');
                Arrays.fill(confirmation, '\0');
            }
        });
        dialog.getRootPane().setDefaultButton(save);
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.setVisible(true);
    }

    private void editAccount(Account target) {
        JTextField name = new JTextField(target.getFullName(), 24), username = new JTextField(target.getUsername(), 24);
        JComboBox<String> role = new JComboBox<>(new String[]{"Employee", "Admin"});
        role.setSelectedItem(target.getRole());
        JPanel form = form(name, username, role, null, null);
        while (true) {
            int answer = DialogUI.ask(parent, "Edit Account - " + target.getId(),
                    form, "Save Changes", false);
            if (answer != JOptionPane.OK_OPTION) return;
            try {
                manager.editAccount(signedIn, target, name.getText(), username.getText(),
                        (String) role.getSelectedItem());
                refreshAccounts();
                JOptionPane.showMessageDialog(parent, "Account information updated.", "Changes Saved", JOptionPane.INFORMATION_MESSAGE);
                return;
            } catch (IllegalArgumentException | SecurityException ex) {
                JOptionPane.showMessageDialog(parent, ex.getMessage(), "Cannot Edit Account", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    private void deleteAccount(Account target) {
        if (target == signedIn) {
            JOptionPane.showMessageDialog(parent, "You cannot delete the account you are signed in with.",
                    "Delete Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JPanel warning = DialogUI.column();
        JPanel warningNote = DialogUI.notice("Delete " + target.getFullName()
                + " (" + target.getId() + ")?\nThis cannot be undone.", true);
        warningNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        warning.add(warningNote);
        warning.add(Box.createVerticalStrut(14));
        JLabel passwordNote = DialogUI.label("Administrator password will be required.");
        passwordNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        warning.add(passwordNote);
        int answer = DialogUI.ask(parent, "Confirm Account Deletion", warning, "Continue", true);
        if (answer != JOptionPane.OK_OPTION) return;
        JPasswordField password = new JPasswordField(20);
        DialogUI.field(password);
        JPanel passwordPanel = DialogUI.column();
        DialogUI.addField(passwordPanel, "Enter your administrator password to confirm deletion", password);
        int confirmation = DialogUI.ask(parent, "Verify Your Administrator Password",
                passwordPanel, "Verify", false);
        if (confirmation != JOptionPane.OK_OPTION) return;
        char[] pwd = password.getPassword();
        try {
            manager.deleteAccount(signedIn, target, pwd);
            refreshAccounts();
            JOptionPane.showMessageDialog(parent, "Account deleted. Its User ID will not be reused.",
                    "Account Deleted", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException | SecurityException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Cannot Delete Account", JOptionPane.WARNING_MESSAGE);
        } finally { Arrays.fill(pwd, '\0'); password.setText(""); }
    }
    private void unblock(Account target) {
        if (!target.isBlocked()) return;
        int answer = JOptionPane.showConfirmDialog(parent,
                "Unblock " + target.getFullName() + " (" + target.getUsername() + ")?",
                "Unblock Account", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) return;
        try {
            manager.unblockAccount(signedIn, target);
            refreshAccounts();
        } catch (SecurityException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Cannot Unblock", JOptionPane.WARNING_MESSAGE);
        }
    }
    private class AccountsTableModel extends AbstractTableModel {
        private final String[] headers = {"User ID", "Full Name", "Username", "Role", "Status", "Actions"};
        @Override public int getRowCount() { return visible.size(); }
        @Override public int getColumnCount() { return headers.length; }
        @Override public String getColumnName(int col) { return headers[col]; }
        @Override public Object getValueAt(int row, int col) {
            Account a = visible.get(row);
            switch (col) {
                case 0: return a.getId();
                case 1: return a.getFullName();
                case 2: return a.getUsername();
                case 3: return a.getRole();
                case 4: return a.getStatus();
                default: return "";
            }
        }
        @Override public boolean isCellEditable(int row, int col) { return col == 5; }
    }
    private JPanel actionRow(JButton edit, JButton delete, JButton unblock) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 9));
        row.add(edit); row.add(delete); row.add(unblock);
        edit.setPreferredSize(new Dimension(63, 33));
        delete.setPreferredSize(new Dimension(76, 33));
        unblock.setPreferredSize(new Dimension(87, 33));
        return row;
    }
    private JButton small(String label, boolean red) {
        return button(label, red ? new Color(255, 234, 234) : new Color(228, 240, 255),
                red ? new Color(169, 40, 40) : Theme.BLUE);
        
        
    }
    
    // interface for the table
    private class ActionsRenderer implements TableCellRenderer {
        private final JButton edit = small("Edit", false), delete = small("Delete", true), unblock = small("Unblock", false);
        private final JPanel controls = actionRow(edit, delete, unblock);
        @Override public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                                                                   boolean focus, int row, int col) {
            Account a = visible.get(t.convertRowIndexToModel(row));
            delete.setVisible(a != signedIn);
            unblock.setVisible(a.isBlocked());
            controls.setBackground(selected ? t.getSelectionBackground() : Theme.WHITE);
            return controls;
        }
    }
    
    // interface para ma edit yung table -- adding action buttons
    private class ActionsEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton edit = small("Edit", false), delete = small("Delete", true), unblock = small("Unblock", false);
        private final JPanel controls = actionRow(edit, delete, unblock);
        private Account current;
        ActionsEditor() {
            edit.addActionListener(e -> { Account target = current; fireEditingStopped(); SwingUtilities.invokeLater(() -> editAccount(target)); });
            delete.addActionListener(e -> { Account target = current; fireEditingStopped(); SwingUtilities.invokeLater(() -> deleteAccount(target)); });
            unblock.addActionListener(e -> { Account target = current; fireEditingStopped(); SwingUtilities.invokeLater(() -> unblock(target)); });
        }
        @Override public Component getTableCellEditorComponent(JTable t, Object value, boolean selected, int row, int col) {
            current = visible.get(t.convertRowIndexToModel(row));
            delete.setVisible(current != signedIn);
            unblock.setVisible(current.isBlocked());
            controls.setBackground(t.getSelectionBackground());
            return controls;
        }
        @Override public Object getCellEditorValue() { return ""; }
    }
}
