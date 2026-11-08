package LaundryTransactionSystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CustomerRegistrationDialog extends JDialog {
    private final CustomerManager customers;
    private final JTextField nameField = new JTextField();
    private final JTextField contactField = new JTextField();
    private final JLabel errorLabel = new JLabel(" ");
    private Customer savedCustomer;

    private CustomerRegistrationDialog(JFrame parent, CustomerManager customers) {
        super(parent, "Add Customer", Dialog.ModalityType.APPLICATION_MODAL);
        this.customers = customers;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(530, 445);
        setMinimumSize(new Dimension(470, 420));
        setLocationRelativeTo(parent);
        buildScreen();
    }
    public static Customer showDialog(JFrame parent, CustomerManager customers) {
        CustomerRegistrationDialog dialog = new CustomerRegistrationDialog(parent, customers);
        dialog.setVisible(true);
        return dialog.savedCustomer;
    }

    private void buildScreen() {
        Color darkBlue = new Color(11, 75, 163);
        Color darkText = new Color(23, 43, 77);
        Color yellow = new Color(226, 233, 85);
        Color white = Color.WHITE;

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(white);
        setContentPane(outer);

        JPanel header = new JPanel();
        header.setBackground(darkBlue);
        header.setBorder(new EmptyBorder(23, 18, 21, 18));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel brand = new JLabel("JOCASHA'S LAUNDRY SHOP", SwingConstants.CENTER);
        brand.setForeground(white);
        brand.setFont(new Font("Sylfaen", Font.BOLD, 20));
        brand.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel title = new JLabel("Add New Customer", SwingConstants.CENTER);
        title.setForeground(white);
        title.setFont(new Font("Arial", Font.BOLD, 27));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(brand);
        header.add(Box.createVerticalStrut(12));
        header.add(title);
        outer.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 13));
        content.setBackground(white);
        content.setBorder(new EmptyBorder(23, 38, 27, 38));
        outer.add(content, BorderLayout.CENTER);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        styleField(nameField);
        styleField(contactField);
        addLabeledField(fields, 0, "Customer Name", nameField);
        addLabeledField(fields, 1, "Contact Number", contactField);
        content.add(fields, BorderLayout.NORTH);

        JPanel footer = new JPanel(new BorderLayout(0, 11));
        footer.setOpaque(false);
        errorLabel.setForeground(new Color(178, 45, 45));
        errorLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        errorLabel.setVerticalAlignment(SwingConstants.TOP);
        footer.add(errorLabel, BorderLayout.NORTH);

        JPanel actions = new JPanel(new GridLayout(1, 2, 12, 0));
        actions.setOpaque(false);
        JButton cancel = actionButton("Cancel", new Color(234, 241, 251), darkBlue);
        JButton save = actionButton("Save Customer", yellow, darkText);
        actions.add(cancel);
        actions.add(save);
        footer.add(actions, BorderLayout.SOUTH);
        content.add(footer, BorderLayout.SOUTH);

        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> saveCustomer());
        getRootPane().setDefaultButton(save);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow());
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("Arial", Font.PLAIN, 15));
        field.setForeground(new Color(23, 43, 77));
        field.setBackground(new Color(248, 250, 252));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                new EmptyBorder(0, 12, 0, 12)));
        field.setPreferredSize(new Dimension(390, 43));
    }

    private void addLabeledField(JPanel panel, int index, String labelText, JComponent field) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = index * 2;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 6, 0);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(23, 43, 77));
        panel.add(label, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = index * 2 + 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 18, 0);
        panel.add(field, c);
    }

    private JButton actionButton(String label, Color background, Color foreground) {
        RoundedButton button = new RoundedButton(label, 12);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFont(new Font("Arial", Font.BOLD, 15));
        button.setPreferredSize(new Dimension(190, 47));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void saveCustomer() {
        try {
            if (customers.hasAnotherActiveCustomerNamed(nameField.getText(), null)) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "A customer with this name already exists.\n"
                        + "Are you sure this is a different customer?",
                        "Possible Duplicate", JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (confirm != JOptionPane.YES_OPTION) return;
            }
            savedCustomer = customers.registerCustomer(nameField.getText(), contactField.getText());
            dispose();
        } catch (IllegalArgumentException ex) {
            errorLabel.setText(ex.getMessage());
        }
    }
}
