package LaundryTransactionSystem;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class OrderDialogs {
    private final JFrame parent;
    private final OrderManager manager;
    private final Runnable afterChange;
    private final AccountManager accountManager;
    private final Account signedIn;

    public OrderDialogs(JFrame parent, OrderManager manager,
                        AccountManager accounts, Account session, Runnable afterChange) {
        this.parent = parent;
        this.manager = manager;
        this.accountManager = accounts;
        this.signedIn = session;
        this.afterChange = afterChange;
    }

    public void showDetails(Order order) {
        JPanel sections = DialogUI.column();
        sections.add(DialogUI.section("Customer Information", new String[][] {
                {"Order ID", order.getId()},
                {"Customer", order.getCustomer()},
                {"Contact", order.getContactNumber().isEmpty() ? "Not recorded" : order.getContactNumber()},
                {"Expected pickup", order.getExpectedPickupDate().isEmpty() ? "Not recorded" : order.getExpectedPickupDate()}
        }));
        sections.add(Box.createVerticalStrut(10));
        sections.add(DialogUI.section("Laundry Details", new String[][] {
                {"Service", order.getService()},
                {"Weight", order.getWeight() + " kg"},
                {"Price per kg", OrderManager.pesos(order.getPricePerKg())},
                {"Instructions", order.getSpecialInstructions().isEmpty() ? "None" : order.getSpecialInstructions()}
        }));
        sections.add(Box.createVerticalStrut(10));
        sections.add(DialogUI.section("Payment Summary", new String[][] {
                {"Total", OrderManager.pesos(order.getTotal())},
                {"Payment", OrderManager.pesos(order.getPayment())},
                {"Change", OrderManager.pesos(order.getChange())}
        }));
        sections.add(Box.createVerticalStrut(10));
        sections.add(DialogUI.section("Order Status & History", new String[][] {
                {"Status", order.getStatus()},
                {"Date", order.getDate()},
                {"Created by", order.getCreatedBy()},
                {"Created at", order.getCreatedAt()}
        }));
        if (order.getRefundAmount() > 0) {
            sections.add(Box.createVerticalStrut(10));
            sections.add(DialogUI.section("Refund", new String[][] {
                    {"Refund amount", OrderManager.pesos(order.getRefundAmount())},
                    {"Refund date", order.getRefundDate()},
                    {"Recorded by", order.getRefundedBy()}
            }));
        }
        if (order.getStatus().equals("Canceled")) {
            sections.add(Box.createVerticalStrut(10));
            sections.add(DialogUI.section("Cancellation", new String[][] {
                    {"Canceled by", order.getCanceledBy()},
                    {"Canceled at", order.getCanceledAt()},
                    {"Reason", order.getCancelReason()}
            }));
        }
        JScrollPane scroller = new JScrollPane(sections);
        scroller.setBorder(null);
        scroller.getVerticalScrollBar().setUnitIncrement(16);
        scroller.setPreferredSize(new Dimension(550, 465));
        DialogUI.showInfo(parent, "Order Details", scroller);
    }

    public void editOrder(Order order) {
        boolean canChangeCharge = order.getStatus().equals("Pending")
                && order.getPayment() == 0;

        JTextField customerField = new JTextField(order.getCustomer(), 20);
        customerField.setEditable(false);
        customerField.setToolTipText("Edit the registered customer's name in Customers instead.");
        JComboBox<String> serviceField = new JComboBox<>(new String[]{
            "Wash, Dry & Fold", "Dry Cleaning", "Express Laundry"
        });
        serviceField.setSelectedItem(order.getService());
        JTextField weightField = new JTextField(String.valueOf(order.getWeight()), 12);
        JTextField rateField = new JTextField(String.valueOf(order.getPricePerKg()), 12);
        JTextArea instructionsField = new JTextArea(order.getSpecialInstructions(), 2, 20);
        instructionsField.setLineWrap(true);
        instructionsField.setWrapStyleWord(true);
        DialogUI.field(customerField);
        DialogUI.field(weightField);
        DialogUI.field(rateField);
        DialogUI.area(instructionsField);
        JScrollPane instructionsScroll = DialogUI.scroll(instructionsField);
        instructionsScroll.setPreferredSize(new Dimension(310, 72));
        instructionsScroll.setMinimumSize(new Dimension(310, 72));
        serviceField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        serviceField.setBackground(Color.WHITE);
        serviceField.setForeground(Theme.NAVY);
        serviceField.setPreferredSize(new Dimension(310, 39));

        serviceField.setEnabled(canChangeCharge);
        weightField.setEditable(canChangeCharge);
        rateField.setEditable(canChangeCharge);
        if (!canChangeCharge) {
            weightField.setBackground(new Color(238, 244, 251));
            rateField.setBackground(new Color(238, 244, 251));
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        String[] names = {"Customer", "Service", "Weight (kg)", "Price per kg", "Special Instructions (optional)"};
        JComponent[] fields = {customerField, serviceField, weightField, rateField, instructionsScroll};
        for (int i = 0; i < names.length; i++) {
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0; c.gridy = i; c.weightx = 0;
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets(4, 0, 6, 18);
            form.add(DialogUI.label(names[i]), c);
            c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(4, 0, 6, 0);
            form.add(fields[i], c);
        }
        String note = canChangeCharge
                ? "Sample rates are editable. The total will be recalculated."
                : "Weight, service and price are locked once an order is paid. To correct them, cancel the order and create a new one.";
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.add(DialogUI.notice(note, !canChangeCharge), BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);

        while (true) {
            int answer = DialogUI.ask(parent, "Edit " + order.getId(), panel, "Save Changes", false);
            if (answer != JOptionPane.OK_OPTION) return;

            String newName = customerField.getText().trim();
            if (newName.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "Customer name is required.");
                continue;
            }

            double weight = order.getWeight();
            double rate = order.getPricePerKg();
            if (canChangeCharge) {
                try {
                    weight = Double.parseDouble(weightField.getText().trim());
                    rate = Double.parseDouble(rateField.getText().trim());
                    if (!Double.isFinite(weight) || !Double.isFinite(rate)
                            || weight <= 0 || rate <= 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(parent,
                        "Enter valid positive numbers for weight and price per kg.",
                        "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    continue;
                }
            }
            String confirmation = "Save changes to " + order.getId() + "?";
            if (canChangeCharge) {
                double newTotal = Math.round(weight * rate * 100.0) / 100.0;
                confirmation += "\nUpdated total: " + OrderManager.pesos(newTotal);
            }
            int save = JOptionPane.showConfirmDialog(parent, confirmation,
                    "Confirm Changes", JOptionPane.YES_NO_OPTION);
            if (save != JOptionPane.YES_OPTION) return;

            try {
                manager.editOrder(order, newName,
                        serviceField.getSelectedItem().toString(), weight, rate,
                        instructionsField.getText());
                afterChange.run();
                return;
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(parent, ex.getMessage(),
                        "Cannot Edit", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    public void advanceOrder(Order order, String nextStatus) {
        double payment = order.getPayment();
        if (nextStatus.equals("Completed") && order.getPayment() >= order.getTotal()) {
            int prepaidConfirm = JOptionPane.showConfirmDialog(parent,
                    "This order has already been paid.\nMark " + order.getId()
                        + " as Completed? (No additional payment required)",
                    "Confirm Completion", JOptionPane.YES_NO_OPTION);
            if (prepaidConfirm != JOptionPane.YES_OPTION) return;
        } else if (nextStatus.equals("Completed")) {
            JTextField paymentField = new JTextField(String.valueOf(order.getTotal()));
            JPanel form = new JPanel(new GridLayout(2, 1, 6, 6));
            form.add(new JLabel("Payment received (total "
                    + OrderManager.pesos(order.getTotal()) + "):"));
            form.add(paymentField);
            int answer = JOptionPane.showConfirmDialog(parent, form,
                    "Complete " + order.getId(), JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) return;
            try {
                payment = Double.parseDouble(paymentField.getText().trim());
                if (!Double.isFinite(payment) || payment < order.getTotal()) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(parent,
                    "Payment must be a valid amount covering "
                        + OrderManager.pesos(order.getTotal()) + ".",
                    "Invalid Payment", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int answer2 = JOptionPane.showConfirmDialog(parent,
                    "Mark " + order.getId() + " Completed?\nChange: "
                        + OrderManager.pesos(payment - order.getTotal()),
                    "Confirm Completion", JOptionPane.YES_NO_OPTION);
            if (answer2 != JOptionPane.YES_OPTION) return;
        } else {
            int confirm = JOptionPane.showConfirmDialog(parent,
                    "Change " + order.getId() + " from " + order.getStatus()
                        + " to " + nextStatus + "?",
                    "Update Order Status", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
        }
        try {
            manager.advanceStatus(order, nextStatus, payment);
            afterChange.run();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(),
                    "Cannot Update", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void cancelOrder(Order order) {
        if (!accountManager.isAdmin(signedIn)) {
            JOptionPane.showMessageDialog(parent,
                    "Only an administrator may cancel a laundry order.",
                    "Access Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JTextArea reasonArea = new JTextArea(4, 25);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        JPasswordField password = new JPasswordField(22);
        JCheckBox refundConfirmed = new JCheckBox(
                "I confirm the laundry charge was already refunded in cash.");
        DialogUI.area(reasonArea);
        DialogUI.field(password);
        refundConfirmed.setOpaque(false);
        refundConfirmed.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        refundConfirmed.setForeground(Theme.NAVY);
        JPanel form = DialogUI.column();
        JPanel approvalNotice = DialogUI.notice(
                "This action requires administrator approval and will remain in order history.", true);
        approvalNotice.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(approvalNotice);
        form.add(Box.createVerticalStrut(13));
        JScrollPane reasonScroll = DialogUI.scroll(reasonArea);
        reasonScroll.setPreferredSize(new Dimension(500, 100));
        reasonScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        DialogUI.addField(form, "Cancellation reason for " + order.getId(), reasonScroll);
        if (order.getPayment() > 0) {
            JPanel refundNote = DialogUI.notice("Refund due: " + OrderManager.pesos(order.getTotal())
                    + " (the original change was already given).", true);
            refundNote.setAlignmentX(Component.LEFT_ALIGNMENT);
            form.add(refundNote);
            form.add(Box.createVerticalStrut(9));
            refundConfirmed.setAlignmentX(Component.LEFT_ALIGNMENT);
            form.add(refundConfirmed);
            form.add(Box.createVerticalStrut(15));
        }
        DialogUI.addField(form, "Administrator password", password);
        try {
            int response = DialogUI.ask(parent, "Cancel Order - Admin Approval", form, "Continue", true);
            if (response != JOptionPane.OK_OPTION) return;
            char[] entered = password.getPassword();
            try {
                int confirm = JOptionPane.showConfirmDialog(parent,
                        "Cancel " + order.getId() + "? The order will remain in history."
                        + (order.getPayment() > 0 ? "\nConfirm cash refund: "
                        + OrderManager.pesos(order.getTotal()) : ""),
                        "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) return;
                manager.cancelOrder(order, reasonArea.getText(), accountManager,
                        signedIn, entered, order.getPayment() == 0 || refundConfirmed.isSelected());
                afterChange.run();
                JOptionPane.showMessageDialog(parent, "Order canceled and saved."
                        + (order.getRefundAmount() > 0
                        ? "\nRefund recorded: " + OrderManager.pesos(order.getRefundAmount()) : ""));
            } finally {
                Arrays.fill(entered, '\0');
            }
        } catch (IllegalArgumentException | SecurityException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(),
                    "Cannot Cancel", JOptionPane.WARNING_MESSAGE);
        } finally {
            password.setText("");
        }
    }
}
