package LaundryTransactionSystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class NewOrderPanel extends JPanel {
    private final JFrame parent;
    private final OrderManager orderManager;
    private final CustomerManager customerManager;
    private final Runnable goToOrders;
    private final Account signedIn;
    private Customer selectedCustomer;

    private JLabel customerNameValue;
    private JLabel customerContactValue;
    private JComboBox<String> serviceBox;
    private JTextField weightField;
    private JTextArea instructionsArea;
    private JLabel rateValue;
    private JTextField pickupField;
    private JTextField paymentField;
    private JLabel totalValue;
    private JLabel changeValue;
    private JLabel summaryCustomer, summaryContact, summaryService, summaryWeight;
    private JLabel summaryRate, summaryPickup, summaryStatus;
    private JLabel summaryTotal, summaryTendered, summaryChange;
    private JTextArea summaryInstructions;
    private JLabel customerIdValue;
    private JScrollPane formScroll;

    public NewOrderPanel(JFrame parent, OrderManager orderManager,
                         CustomerManager customerManager, Account signedIn, Runnable goToOrders) {
        this.parent = parent;
        this.orderManager = orderManager;
        this.customerManager = customerManager;
        this.goToOrders = goToOrders;
        this.signedIn = signedIn;
        setLayout(new BorderLayout());
        setBackground(Theme.PAGE_BLUE);
        buildScreen();
        clearValues();
    }

    private void buildScreen() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Theme.PAGE_BLUE);
        body.setBorder(new EmptyBorder(20, 28, 24, 28));

        JLabel title = new JLabel("New Laundry Order");
        title.setFont(new Font("Arial", Font.BOLD, 29));
        title.setForeground(Theme.NAVY);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 18, 0);
        body.add(title, c);

        JPanel customerCard = createCard("Customer Information");
        buildCustomerSection(customerCard);
        JPanel laundryCard = createCard("Laundry Details");
        buildLaundrySection(laundryCard);
        JPanel paymentCard = createCard("Payment Details");
        buildPaymentSection(paymentCard);
        JPanel summaryCard = createCard("Order Summary");
        buildSummarySection(summaryCard);

        c.gridwidth = 1; c.gridy = 1; c.gridx = 0;
        c.weightx = 0.43; c.weighty = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 0, 15, 9);
        body.add(customerCard, c);

        c.gridx = 1; c.weightx = 0.57;
        c.insets = new Insets(0, 9, 15, 0);
        body.add(laundryCard, c);

        c.gridx = 0; c.gridy = 2; c.weightx = 0.43;
        c.insets = new Insets(0, 0, 0, 9);
        body.add(paymentCard, c);

        c.gridx = 1; c.weightx = 0.57;
        c.insets = new Insets(0, 9, 0, 0);
        body.add(summaryCard, c);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        JButton cancel = button("Cancel", Theme.WHITE, Theme.NAVY);
        JButton clear = button("Clear Form", Theme.WHITE, Theme.BLUE);
        JButton confirm = button("Confirm Order", Theme.BLUE, Theme.WHITE);
        buttons.add(cancel);
        buttons.add(clear);
        buttons.add(confirm);
        c.gridx = 0; c.gridy = 3; c.gridwidth = 2; c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(17, 0, 0, 0);
        body.add(buttons, c);

        cancel.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if (isDirty()) {
                    int choice = JOptionPane.showConfirmDialog(parent,
                            "Discard this unfinished order?", "Cancel New Order",
                            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (choice != JOptionPane.YES_OPTION) return;
                }
                clearValues();
                goToOrders.run();
            }
        });
        clear.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if (JOptionPane.showConfirmDialog(parent,
                        "Clear all entered order details?", "Clear Form",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) clearValues();
            }
        });
        confirm.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { confirmOrder(); }
        });

        JPanel scrollContent = new JPanel(new BorderLayout());
        scrollContent.setBackground(Theme.PAGE_BLUE);
        scrollContent.add(body, BorderLayout.NORTH);
        formScroll = new JScrollPane(scrollContent);
        formScroll.setBorder(null);
        formScroll.getVerticalScrollBar().setUnitIncrement(18);
        formScroll.getViewport().setBackground(Theme.PAGE_BLUE);
        add(formScroll, BorderLayout.CENTER);
    }

    public void scrollToTop() {
        if (selectedCustomer != null) {
            if (selectedCustomer.isActive()) chooseCustomer(selectedCustomer);
            else {
                selectedCustomer = null;
                customerNameValue.setText("No customer selected");
                customerContactValue.setText("-");
                customerIdValue.setText("-");
                refreshCalculations();
            }
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() { formScroll.getVerticalScrollBar().setValue(0); }
        });
    }

    private JPanel createCard(String title) {
        RoundedPanel card = new RoundedPanel(20);
        card.setBackground(Theme.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(19, 21, 20, 21));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Arial", Font.BOLD, 19));
        heading.setForeground(Theme.NAVY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(heading);
        card.add(Box.createVerticalStrut(16));
        return card;
    }

    private void buildCustomerSection(JPanel card) {
        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.setPreferredSize(new Dimension(320, 43));
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
        JButton search = button("Search Customer", Theme.BLUE, Theme.WHITE);
        JButton add = button("+ New Customer", Theme.PEACH, Theme.NAVY);
        actions.add(search);
        actions.add(add);
        card.add(actions);
        card.add(Box.createVerticalStrut(20));

        JPanel customerDetails = new JPanel();
        customerDetails.setOpaque(false);
        customerDetails.setAlignmentX(Component.LEFT_ALIGNMENT);
        customerDetails.setLayout(new BoxLayout(customerDetails, BoxLayout.Y_AXIS));
        customerDetails.add(fieldLabel("Customer Name"));
        customerNameValue = displayField("No customer selected");
        customerNameValue.setFont(new Font("Arial", Font.BOLD, 17));
        customerDetails.add(customerNameValue);
        customerDetails.add(Box.createVerticalStrut(15));
        customerDetails.add(fieldLabel("Contact Number"));
        customerContactValue = displayField("-");
        customerDetails.add(customerContactValue);
        customerDetails.add(Box.createVerticalStrut(15));
        customerDetails.add(fieldLabel("Customer ID"));
        customerIdValue = displayField("-");
        customerDetails.add(customerIdValue);
        card.add(customerDetails);

        search.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { openCustomerSearch(); }
        });
        add.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { registerCustomer(); }
        });
    }

    private void buildLaundrySection(JPanel card) {
        card.add(fieldLabel("Service Type"));
        serviceBox = new JComboBox<>(new String[]{
            "Wash, Dry & Fold", "Dry Cleaning", "Express Laundry"
        });
        styleCombo(serviceBox);
        card.add(serviceBox);
        card.add(Box.createVerticalStrut(11));

        JPanel weightAndRate = new JPanel(new GridLayout(1, 2, 14, 0));
        weightAndRate.setOpaque(false);
        weightAndRate.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel weightColumn = new JPanel();
        weightColumn.setOpaque(false);
        weightColumn.setLayout(new BoxLayout(weightColumn, BoxLayout.Y_AXIS));
        weightColumn.add(fieldLabel("Weight (kg)"));
        weightField = inputField("e.g., 3.5");
        weightColumn.add(weightField);
        JPanel rateColumn = new JPanel();
        rateColumn.setOpaque(false);
        rateColumn.setLayout(new BoxLayout(rateColumn, BoxLayout.Y_AXIS));
        rateColumn.add(fieldLabel("Price per Kilogram"));
        rateValue = displayField("-");
        rateValue.setFont(new Font("Arial", Font.BOLD, 16));
        rateValue.setForeground(Theme.BLUE);
        rateColumn.add(rateValue);
        weightAndRate.add(weightColumn);
        weightAndRate.add(rateColumn);
        card.add(weightAndRate);
        card.add(Box.createVerticalStrut(11));

        card.add(fieldLabel("Expected Pickup Date (YYYY-MM-DD)"));
        pickupField = inputField("e.g., 2026-10-21");
        card.add(pickupField);
        card.add(Box.createVerticalStrut(11));

        card.add(fieldLabel("Special Instructions (Optional)"));
        instructionsArea = new JTextArea(2, 20);
        instructionsArea.setFont(new Font("Arial", Font.PLAIN, 15));
        instructionsArea.setForeground(Theme.NAVY);
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);
        instructionsArea.setToolTipText("Laundry requests, up to 250 characters");
        instructionsArea.setBorder(new EmptyBorder(8, 10, 8, 10));
        JScrollPane instructionsScroll = new JScrollPane(instructionsArea);
        instructionsScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        instructionsScroll.setBorder(BorderFactory.createLineBorder(new Color(201, 215, 230)));
        instructionsScroll.setPreferredSize(new Dimension(300, 64));
        instructionsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        card.add(instructionsScroll);
        card.add(Box.createVerticalStrut(10));

        JLabel statusHint = new JLabel("Initial order status: Pending");
        statusHint.setForeground(Theme.MUTED_BLUE);
        statusHint.setFont(new Font("Arial", Font.PLAIN, 12));
        statusHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(statusHint);

        serviceBox.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { refreshCalculations(); }
        });
        watch(weightField);
        watch(pickupField);
        instructionsArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refreshCalculations(); }
            @Override public void removeUpdate(DocumentEvent e) { refreshCalculations(); }
            @Override public void changedUpdate(DocumentEvent e) { refreshCalculations(); }
        });
    }

    private void buildPaymentSection(JPanel card) {
        card.add(fieldLabel("Total Amount"));
        totalValue = displayField(OrderManager.pesos(0));
        totalValue.setFont(new Font("Arial", Font.BOLD, 32));
        totalValue.setForeground(Theme.BLUE);
        card.add(totalValue);
        card.add(Box.createVerticalStrut(23));

        card.add(fieldLabel("Amount Tendered"));
        paymentField = inputField("e.g., 500.00");
        card.add(paymentField);
        card.add(Box.createVerticalStrut(23));

        card.add(fieldLabel("Change"));
        changeValue = displayField(OrderManager.pesos(0));
        changeValue.setFont(new Font("Arial", Font.BOLD, 27));
        changeValue.setForeground(new Color(0, 137, 91));
        card.add(changeValue);
        watch(paymentField);
    }

    private void buildSummarySection(JPanel card) {
        JPanel rows = new JPanel(new GridBagLayout());
        rows.setOpaque(false);
        rows.setAlignmentX(Component.LEFT_ALIGNMENT);
        summaryCustomer = addSummaryRow(rows, 0, "Customer");
        summaryContact = addSummaryRow(rows, 1, "Contact");
        summaryService = addSummaryRow(rows, 2, "Service");
        summaryWeight = addSummaryRow(rows, 3, "Weight");
        summaryRate = addSummaryRow(rows, 4, "Rate / kg");
        summaryPickup = addSummaryRow(rows, 5, "Pickup Date");
        summaryStatus = addSummaryRow(rows, 6, "Status");
        card.add(rows);
        card.add(Box.createVerticalStrut(11));
        JSeparator divider = new JSeparator();
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(divider);
        card.add(Box.createVerticalStrut(11));

        JPanel amounts = new JPanel(new GridLayout(1, 3, 10, 0));
        amounts.setOpaque(false);
        amounts.setAlignmentX(Component.LEFT_ALIGNMENT);
        summaryTotal = addSummaryAmount(amounts, "TOTAL", Theme.BLUE);
        summaryTendered = addSummaryAmount(amounts, "TENDERED", Theme.NAVY);
        summaryChange = addSummaryAmount(amounts, "CHANGE", new Color(0, 137, 91));
        card.add(amounts);
        card.add(Box.createVerticalStrut(13));

        card.add(fieldLabel("Special Instructions"));
        summaryInstructions = new JTextArea(2, 10);
        summaryInstructions.setEditable(false);
        summaryInstructions.setFocusable(false);
        summaryInstructions.setOpaque(false);
        summaryInstructions.setFont(new Font("Arial", Font.PLAIN, 14));
        summaryInstructions.setForeground(Theme.NAVY);
        summaryInstructions.setLineWrap(true);
        summaryInstructions.setWrapStyleWord(true);
        summaryInstructions.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(summaryInstructions);
    }

    private JLabel addSummaryRow(JPanel rows, int row, String name) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 7, 20);
        JLabel label = new JLabel(name);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setForeground(Theme.MUTED_BLUE);
        rows.add(label, c);

        JLabel value = new JLabel("-");
        value.setFont(new Font("Arial", Font.BOLD, 14));
        value.setForeground(Theme.NAVY);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 7, 0);
        rows.add(value, c);
        return value;
    }

    private JLabel addSummaryAmount(JPanel amounts, String name, Color color) {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(name);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(Theme.MUTED_BLUE);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel value = new JLabel(OrderManager.pesos(0));
        value.setFont(new Font("Arial", Font.BOLD, 20));
        value.setForeground(color);
        value.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(label);
        box.add(Box.createVerticalStrut(6));
        box.add(value);
        amounts.add(box);
        return value;
    }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Theme.NAVY);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(0, 0, 5, 0));
        return label;
    }

    private JLabel displayField(String value) {
        JLabel label = new JLabel(value);
        label.setFont(new Font("Arial", Font.PLAIN, 15));
        label.setForeground(Theme.NAVY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        return label;
    }

    private JTextField inputField(String tooltip) {
        JTextField field = new JTextField();
        field.setFont(new Font("Arial", Font.PLAIN, 15));
        field.setForeground(Theme.NAVY);
        field.setToolTipText(tooltip);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(201, 215, 230)),
                new EmptyBorder(9, 10, 9, 10)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    private void styleCombo(JComboBox<String> combo) {
        combo.setFont(new Font("Arial", Font.PLAIN, 15));
        combo.setForeground(Theme.NAVY);
        combo.setBackground(Theme.WHITE);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private JButton button(String label, Color background, Color foreground) {
        RoundedButton btn = new RoundedButton(label, 14);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setBackground(background);
        btn.setForeground(foreground);
        btn.setPreferredSize(new Dimension(150, 41));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void watch(JTextField field) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refreshCalculations(); }
            @Override public void removeUpdate(DocumentEvent e) { refreshCalculations(); }
            @Override public void changedUpdate(DocumentEvent e) { refreshCalculations(); }
        });
    }

    private void chooseCustomer(Customer customer) {
        selectedCustomer = customer;
        customerNameValue.setText(customer.getName());
        customerContactValue.setText(customer.getContactNumber());
        customerIdValue.setText(customer.getId());
        refreshCalculations();
    }

    private void registerCustomer() {
        Customer added = CustomerRegistrationDialog.showDialog(parent, customerManager);
        if (added == null) return;
        chooseCustomer(added);
        JOptionPane.showMessageDialog(parent,
                "Customer registered and selected.", "Customer Added",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void openCustomerSearch() {
        JDialog dialog = new JDialog(parent, "Search Existing Customer", true);
        dialog.getContentPane().setBackground(Color.WHITE);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel searchBar = new JPanel(new BorderLayout(0, 8));
        searchBar.setBackground(Color.WHITE);
        searchBar.setBorder(new EmptyBorder(18, 18, 5, 18));
        searchBar.add(DialogUI.label("Search by customer ID, name, or contact number:"), BorderLayout.NORTH);
        JTextField filter = new JTextField();
        DialogUI.field(filter);
        searchBar.add(filter, BorderLayout.CENTER);
        dialog.add(searchBar, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(new String[]{"Customer ID", "Customer Name", "Contact Number"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable results = new JTable(model);
        results.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        results.setForeground(Theme.NAVY);
        results.setBackground(Color.WHITE);
        results.setRowHeight(38);
        results.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        results.setSelectionBackground(new Color(222, 235, 253));
        results.setSelectionForeground(Theme.NAVY);
        results.setGridColor(new Color(158, 187, 223));
        results.setShowGrid(true);
        results.setIntercellSpacing(new Dimension(1, 1));
        results.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        results.getTableHeader().setForeground(Theme.NAVY);
        results.getTableHeader().setBackground(new Color(231, 241, 253));
        results.getTableHeader().setPreferredSize(new Dimension(0, 40));
        results.getTableHeader().setReorderingAllowed(false);
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int col = 0; col < results.getColumnCount(); col++) {
            results.getColumnModel().getColumn(col).setCellRenderer(center);
        }
        JScrollPane tableScroll = new JScrollPane(results);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(91, 134, 187), 2));
        JPanel tableSpace = new JPanel(new BorderLayout());
        tableSpace.setBackground(Color.WHITE);
        tableSpace.setBorder(new EmptyBorder(5, 18, 4, 18));
        tableSpace.add(tableScroll, BorderLayout.CENTER);
        dialog.add(tableSpace, BorderLayout.CENTER);

        JLabel count = DialogUI.label("");
        JPanel bottom = new JPanel(new BorderLayout(12, 0));
        bottom.setBackground(new Color(245, 249, 255));
        bottom.setBorder(new EmptyBorder(11, 18, 13, 18));
        bottom.add(count, BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton close = DialogUI.button("Close", false, false);
        JButton select = DialogUI.button("Select Customer", true, false);
        actions.add(close);
        actions.add(select);
        bottom.add(actions, BorderLayout.EAST);
        dialog.add(bottom, BorderLayout.SOUTH);
        final java.util.ArrayList<Customer> shown = new java.util.ArrayList<>();
        Runnable updateResults = new Runnable() {
            @Override public void run() {
                shown.clear();
                // used search
                shown.addAll(customerManager.search(filter.getText()));
                model.setRowCount(0);
                for (Customer customer : shown) {
                    model.addRow(new Object[]{customer.getId(), customer.getName(), customer.getContactNumber()});
                }
                count.setText(shown.size() + " customer(s)");
            }
        };
        filter.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateResults.run(); }
            @Override public void removeUpdate(DocumentEvent e) { updateResults.run(); }
            @Override public void changedUpdate(DocumentEvent e) { updateResults.run(); }
        });
        updateResults.run();

        Runnable selectCustomer = new Runnable() {
            @Override public void run() {
                int row = results.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(dialog,
                            "Select a customer row first.", "No Selection",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                chooseCustomer(shown.get(results.convertRowIndexToModel(row)));
                dialog.dispose();
            }
        };
        select.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { selectCustomer.run(); }
        });
        results.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) selectCustomer.run();
            }
        });
        close.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { dialog.dispose(); }
        });
        dialog.getRootPane().setDefaultButton(select);
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.setSize(665, 470);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private double parsePositive(String value, String label) {
        try {
            double amount = Double.parseDouble(value.trim());
            if (!Double.isFinite(amount) || amount <= 0) throw new NumberFormatException();
            return amount;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " must be a positive number.");
        }
    }

    private LocalDate parsePickupDate() {
        try { return LocalDate.parse(pickupField.getText().trim()); }
        catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Enter pickup date as YYYY-MM-DD.");
        }
    }

    private void refreshCalculations() {
        if (serviceBox == null || rateValue == null || totalValue == null
                || changeValue == null || summaryCustomer == null) return;
        String service = (String) serviceBox.getSelectedItem();
        double rate = OrderManager.serviceRate(service);
        rateValue.setText(OrderManager.pesos(rate) + " / kg");
        double weight = 0;
        double tendered = 0;
        try {
            weight = Double.parseDouble(weightField.getText().trim());
            if (!Double.isFinite(weight) || weight <= 0) weight = 0;
        } catch (NumberFormatException ex) { weight = 0; }
        try {
            tendered = Double.parseDouble(paymentField.getText().trim());
            if (!Double.isFinite(tendered) || tendered < 0) tendered = 0;
        } catch (NumberFormatException ex) { tendered = 0; }
        double total = Math.round(weight * rate * 100.0) / 100.0;
        totalValue.setText(OrderManager.pesos(total));
        changeValue.setText(OrderManager.pesos(tendered >= total && total > 0 ? tendered - total : 0));
        String name = selectedCustomer == null ? "Not selected" : selectedCustomer.getName();
        String contact = selectedCustomer == null ? "-" : selectedCustomer.getContactNumber();
        summaryCustomer.setText(name);
        summaryContact.setText(contact);
        summaryService.setText(service);
        summaryWeight.setText(weight > 0 ? weight + " kg" : "-");
        summaryRate.setText(OrderManager.pesos(rate) + " / kg");
        summaryPickup.setText(pickupField.getText().trim().isEmpty()
                ? "-" : pickupField.getText().trim());
        summaryStatus.setText("Pending");
        summaryTotal.setText(OrderManager.pesos(total));
        summaryTendered.setText(OrderManager.pesos(tendered));
        summaryChange.setText(changeValue.getText());
        String special = instructionsArea.getText().trim();
        summaryInstructions.setText(special.isEmpty() ? "None" : special);
    }

    private boolean isDirty() {
        return selectedCustomer != null || !weightField.getText().trim().isEmpty()
                || !paymentField.getText().trim().isEmpty()
                || !pickupField.getText().trim().equals(LocalDate.now().toString())
                || serviceBox.getSelectedIndex() != 0
                || !instructionsArea.getText().trim().isEmpty();
    }

    private void clearValues() {
        selectedCustomer = null;
        if (customerNameValue == null) return;
        customerNameValue.setText("No customer selected");
        customerContactValue.setText("-");
        customerIdValue.setText("-");
        serviceBox.setSelectedIndex(0);
        weightField.setText("");
        instructionsArea.setText("");
        paymentField.setText("");
        pickupField.setText(LocalDate.now().toString());
        refreshCalculations();
    }

    private void confirmOrder() {
        try {
            if (selectedCustomer == null) throw new IllegalArgumentException("Select or register a customer.");
            double weight = parsePositive(weightField.getText(), "Weight");
            if (weight > 100) throw new IllegalArgumentException("Maximum weight is 100 kg.");
            LocalDate date = parsePickupDate();
            if (date.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Pickup date cannot be earlier than today.");
            }
            String instructions = instructionsArea.getText().trim();
            if (instructions.length() > 250) {
                throw new IllegalArgumentException("Special Instructions cannot exceed 250 characters.");
            }
            double amount = parsePositive(paymentField.getText(), "Amount tendered");
            String service = (String) serviceBox.getSelectedItem();
            double total = Math.round(weight * OrderManager.serviceRate(service) * 100.0) / 100.0;
            if (amount < total) throw new IllegalArgumentException(
                    "Full payment is required. Total: " + OrderManager.pesos(total));
            JPanel confirmation = DialogUI.column();
            JPanel prompt = DialogUI.notice("Please confirm this laundry transaction.", false);
            prompt.setAlignmentX(Component.LEFT_ALIGNMENT);
            confirmation.add(prompt);
            confirmation.add(Box.createVerticalStrut(12));

            JPanel details = DialogUI.section("Customer & Laundry Details", new String[][] {
                {"Customer", selectedCustomer.getName()},
                {"Contact", selectedCustomer.getContactNumber()},
                {"Service", service},
                {"Weight", weight + " kg"},
                {"Rate", OrderManager.pesos(OrderManager.serviceRate(service)) + " / kg"},
                {"Pickup Date", date.toString()},
                {"Initial Status", "Pending"},
                {"Instructions", instructions.isEmpty() ? "None" : instructions}
            });
            details.setAlignmentX(Component.LEFT_ALIGNMENT);
            confirmation.add(details);
            confirmation.add(Box.createVerticalStrut(12));

            JPanel payment = DialogUI.section("Payment Summary", new String[][] {
                {"Total", OrderManager.pesos(total)},
                {"Tendered", OrderManager.pesos(amount)},
                {"Change", OrderManager.pesos(amount - total)}
            });
            payment.setAlignmentX(Component.LEFT_ALIGNMENT);
            confirmation.add(payment);

            int choice = DialogUI.confirm(parent, "Confirm New Order", confirmation);
            if (choice != JOptionPane.YES_OPTION) return;

            Order created = orderManager.createOrder(selectedCustomer, service,
                    weight, date, amount, signedIn.getId(), instructions);
            clearValues();
            int next = JOptionPane.showConfirmDialog(parent,
                    "Order " + created.getId() + " saved successfully!\n"
                    + "Total: " + OrderManager.pesos(created.getTotal()) + "\n"
                    + "Change: " + OrderManager.pesos(created.getChange()) + "\n\n"
                    + "Go to Order Management now?",
                    "Order Created", JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
            if (next == JOptionPane.YES_OPTION) goToOrders.run();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(),
                    "Check New Order Details", JOptionPane.WARNING_MESSAGE);
        }
    }
}
