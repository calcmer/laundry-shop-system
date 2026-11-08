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
import java.util.List;
import java.util.Arrays;

public class CustomersPanel extends JPanel {
    private final JFrame parent;
    private final CustomerManager customers;
    private final OrderManager orders;
    private final AccountManager accountManager;
    private final Account signedIn;
    private final List<Customer> visible = new ArrayList<>();
    private final CustomerTableModel model = new CustomerTableModel();
    private final JTable table = new JTable(model);
    private JTextField searchField;
    private JLabel resultsLabel;

    public CustomersPanel(JFrame parent, CustomerManager customers, OrderManager orders,
                          AccountManager accounts, Account session) {
        this.parent = parent;
        this.customers = customers;
        this.orders = orders;
        this.accountManager = accounts;
        this.signedIn = session;
        setLayout(new BorderLayout());
        setBackground(Theme.PAGE_BLUE);
        buildScreen();
        refreshCustomers();
    }

    private void buildScreen() {
        JPanel body = new JPanel(new BorderLayout(0, 20));
        body.setBackground(Theme.PAGE_BLUE);
        body.setBorder(new EmptyBorder(25, 30, 25, 30));
        add(body, BorderLayout.CENTER);

        JPanel header = new JPanel(new BorderLayout(10, 10));
        header.setOpaque(false);
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Customer Management");
        title.setFont(new Font("Arial", Font.BOLD, 29));
        title.setForeground(Theme.NAVY);
        heading.add(title);
        heading.add(Box.createVerticalStrut(5));
        header.add(heading, BorderLayout.WEST);

        JButton add = mainButton("+ Add Customer", Theme.BLUE, Theme.WHITE);
        add.setToolTipText("Register a new customer");
        add.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { addCustomer(); }
        });
        header.add(add, BorderLayout.EAST);
        body.add(header, BorderLayout.NORTH);

        RoundedPanel card = new RoundedPanel(20);
        card.setLayout(new BorderLayout(0, 17));
        card.setBackground(Theme.WHITE);
        card.setBorder(new EmptyBorder(22, 23, 20, 23));
        body.add(card, BorderLayout.CENTER);

        JPanel searchRow = new JPanel(new BorderLayout(12, 0));
        searchRow.setOpaque(false);
        RoundedPanel searchContainer = new RoundedPanel(16);
        searchContainer.setBackground(new Color(244, 248, 255));
        searchContainer.setOutlineColor(new Color(96, 135, 183));
        searchContainer.setLayout(new BorderLayout());
        searchContainer.setBorder(new EmptyBorder(8, 13, 8, 13));
        searchField = new HintTextField("Search customer name, ID, or contact...");
        searchField.setOpaque(false);
        searchField.setBorder(null);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setForeground(Theme.NAVY);
        searchField.setToolTipText("Search by customer ID, name or contact");
        searchField.setPreferredSize(new Dimension(350, 26));
        searchContainer.add(searchField, BorderLayout.CENTER);
        searchRow.add(searchContainer, BorderLayout.CENTER);
        resultsLabel = new JLabel("0 customers");
        resultsLabel.setForeground(Theme.MUTED_BLUE);
        resultsLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        searchRow.add(resultsLabel, BorderLayout.EAST);
        card.add(searchRow, BorderLayout.NORTH);

        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setForeground(Theme.NAVY);
        table.setBackground(Theme.WHITE);
        table.setRowHeight(51);
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
        centered.setBorder(new EmptyBorder(0, 7, 0, 7));
        for (int col = 0; col < 3; col++) table.getColumnModel().getColumn(col).setCellRenderer(centered);
        DefaultTableCellRenderer centeredHeader = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
        centeredHeader.setHorizontalAlignment(SwingConstants.CENTER);

        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(250);
        table.getColumnModel().getColumn(2).setPreferredWidth(190);
        table.getColumnModel().getColumn(3).setPreferredWidth(245);
        table.getColumnModel().getColumn(3).setCellRenderer(new CustomerActionsRenderer());
        table.getColumnModel().getColumn(3).setCellEditor(new CustomerActionsEditor());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(96, 135, 183), 2));
        scroll.getViewport().setBackground(Theme.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refreshCustomers(); }
            @Override public void removeUpdate(DocumentEvent e) { refreshCustomers(); }
            @Override public void changedUpdate(DocumentEvent e) { refreshCustomers(); }
        });
    }

    public void refreshCustomers() {
        if (searchField == null) return;
        if (table.isEditing()) table.getCellEditor().stopCellEditing();
        visible.clear();
        // used search
        visible.addAll(customers.search(searchField.getText()));
        model.fireTableDataChanged();
        resultsLabel.setText(visible.size() + " of " + customers.count() + " customers");
    }

    private JButton mainButton(String text, Color background, Color foreground) {
        RoundedButton button = new RoundedButton(text, 14);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBorder(new EmptyBorder(12, 21, 12, 21));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JPanel customerForm(JTextField name, JTextField contact) {
        JPanel form = DialogUI.column();
        DialogUI.field(name);
        DialogUI.field(contact);
        DialogUI.addField(form, "Customer Name", name);
        DialogUI.addField(form, "Contact Number", contact);
        return form;
    }

    private void addCustomer() {
        Customer added = CustomerRegistrationDialog.showDialog(parent, customers);
        if (added == null) return;
        refreshCustomers();
        JOptionPane.showMessageDialog(parent, added.getName() + " is now registered as "
                + added.getId() + ".", "Customer Saved", JOptionPane.INFORMATION_MESSAGE);
    }

    private void editCustomer(Customer customer) {
        if (!customer.isActive()) { refreshCustomers(); return; }
        JTextField name = new JTextField(customer.getName(), 24);
        JTextField contact = new JTextField(customer.getContactNumber(), 24);
        JPanel form = customerForm(name, contact);
        while (true) {
            int response = DialogUI.ask(parent, "Edit Customer - " + customer.getId(),
                    form, "Save Changes", false);
            if (response != JOptionPane.OK_OPTION) return;
            try {
                if (customers.hasAnotherActiveCustomerNamed(name.getText(), customer)) {
                    if (JOptionPane.showConfirmDialog(parent,
                            "Another customer already has this name. Continue?",
                            "Possible Duplicate", JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) continue;
                }
                customers.updateCustomer(customer, name.getText(), contact.getText());
                refreshCustomers();
                JOptionPane.showMessageDialog(parent, "Customer details updated.",
                        "Changes Saved", JOptionPane.INFORMATION_MESSAGE);
                return;
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(parent, ex.getMessage(),
                        "Check Customer Details", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void deleteCustomer(Customer customer) {
        if (!customer.isActive()) { refreshCustomers(); return; }
        if (!accountManager.isAdmin(signedIn)) {
            JOptionPane.showMessageDialog(parent, "Only administrators may delete customer records.",
                    "Access Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }
        boolean linked = orders.hasOrdersForCustomer(customer.getId());
        String text = "Delete " + customer.getDisplaySummary() + "?"
                + (linked ? "\nExisting orders will be preserved: profile archived." : "");
        JPasswordField adminPassword = new JPasswordField(22);
        JPanel form = DialogUI.column();
        JPanel warning = DialogUI.notice(text, true);
        warning.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(warning);
        form.add(Box.createVerticalStrut(16));
        DialogUI.field(adminPassword);
        DialogUI.addField(form, "Enter your administrator password", adminPassword);
        int choice = DialogUI.ask(parent, "Delete Customer", form, "Delete Customer", true);
        if (choice != JOptionPane.OK_OPTION) return;
        char[] pwd = adminPassword.getPassword();
        try {
            boolean archived = customers.deleteCustomer(customer,
                    accountManager, signedIn, pwd);
            refreshCustomers();
            JOptionPane.showMessageDialog(parent,
                    archived ? "Customer archived; linked orders were kept."
                            : "Customer deleted.",
                    "Customer Updated", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException | SecurityException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(),
                    "Cannot Delete Customer", JOptionPane.WARNING_MESSAGE);
        } finally {
            Arrays.fill(pwd, '\0');
            adminPassword.setText("");
        }
    }

    private class CustomerTableModel extends AbstractTableModel {
        private final String[] columns = {"Customer ID", "Name", "Contact Number", "Actions"};
        @Override public int getRowCount() { return visible.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int column) { return columns[column]; }
        @Override public Object getValueAt(int row, int column) {
            Customer c = visible.get(row);
            switch (column) {
                case 0: return c.getId();
                case 1: return c.getName();
                case 2: return c.getContactNumber();
                default: return "";
            }
        }
        @Override public boolean isCellEditable(int row, int column) { return column == 3; }
    }

    private JPanel actions(JButton edit, JButton delete) {
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 9, 7));
        controls.setOpaque(true);
        edit.setPreferredSize(new Dimension(80, 33));
        delete.setPreferredSize(new Dimension(88, 33));
        edit.setToolTipText("Edit this customer's name and contact");
        delete.setToolTipText("Admin password required to delete or archive");
        delete.setEnabled(accountManager.isAdmin(signedIn));
        controls.add(edit);
        controls.add(delete);
        return controls;
    }

    private class CustomerActionsRenderer implements TableCellRenderer {
        private final JPanel controls;
        CustomerActionsRenderer() {
            JButton edit = mainButton("Edit", new Color(228, 240, 255), Theme.BLUE);
            JButton delete = mainButton("Delete", new Color(255, 234, 234), new Color(169, 40, 40));
            controls = actions(edit, delete);
        }
        @Override public Component getTableCellRendererComponent(JTable table, Object value,
                                boolean selected, boolean focused, int row, int column) {
            controls.setBackground(selected ? table.getSelectionBackground() : Theme.WHITE);
            return controls;
        }
    }

    private class CustomerActionsEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel controls;
        private Customer current;
        CustomerActionsEditor() {
            JButton edit = mainButton("Edit", new Color(228, 240, 255), Theme.BLUE);
            JButton delete = mainButton("Delete", new Color(255, 234, 234), new Color(169, 40, 40));
            controls = actions(edit, delete);
            edit.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    final Customer selected = current;
                    fireEditingStopped();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override public void run() { editCustomer(selected); }
                    });
                }
            });
            delete.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    final Customer selected = current;
                    fireEditingStopped();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override public void run() { deleteCustomer(selected); }
                    });
                }
            });
        }
        @Override public Component getTableCellEditorComponent(JTable table, Object value,
                                   boolean selected, int row, int column) {
            current = visible.get(table.convertRowIndexToModel(row));
            controls.setBackground(table.getSelectionBackground());
            return controls;
        }
        @Override public Object getCellEditorValue() { return ""; }
    }
}
