package LaundryTransactionSystem;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class LaundryLogIn extends JFrame {

    private final AccountManager accountManager = AccountManager.getInstance();

    private final Color darkBlue = new Color(11, 75, 163);
    private final Color white = Color.WHITE;
    private final Color darkText = new Color(23, 43, 77);
    private final Color grayText = new Color(100, 116, 139);
    private final Color fieldColor = new Color(248, 250, 252);
    private final Color borderColor = new Color(203, 213, 225);
    private final Color yellow = new Color(226, 233, 85);

    public LaundryLogIn() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(null);
        getContentPane().setBackground(darkBlue);

        if (accountManager.needsFirstAdmin()) {
            showFirstAdminRegistration();
        } else {
            showLoginScreen();
        }
    }
    
    private void prepareScreen(String title, int height) {
        getContentPane().removeAll();
        setTitle("Jocasha's Laundry Shop - " + title);
        setSize(850, height);
        setLocationRelativeTo(null);
        getContentPane().setBackground(darkBlue);

        JLabel shopTitle = new JLabel("JOCASHA'S LAUNDRY SHOP", SwingConstants.CENTER);
        shopTitle.setFont(new Font("Sylfaen", Font.BOLD, 27));
        shopTitle.setForeground(white);
        shopTitle.setBounds(130, 25, 580, 50);
        add(shopTitle);
    }

    private JPanel whitePanel(int height) {
        JPanel panel = new JPanel(null);
        panel.setBackground(white);
        panel.setBounds(175, 95, 500, height);
        add(panel);
        return panel;
    }

    private void addHeading(JPanel panel, String heading, String subtitleText) {
        JLabel headingLabel = new JLabel(heading, SwingConstants.CENTER);
        headingLabel.setFont(new Font("MS Reference Sans Serif", Font.BOLD, 30));
        headingLabel.setForeground(darkText);
        headingLabel.setBounds(35, 26, 430, 48);
        panel.add(headingLabel);

        JLabel subtitle = new JLabel(subtitleText, SwingConstants.CENTER);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(grayText);
        subtitle.setBounds(35, 76, 430, 25);
        panel.add(subtitle);
    }

    private void addFieldLabel(JPanel panel, String label, int y) {
        JLabel text = new JLabel(label);
        text.setFont(new Font("Arial", Font.BOLD, 15));
        text.setForeground(darkText);
        text.setBounds(55, y, 390, 24);
        panel.add(text);
    }

    private void styleField(JTextField field, int y, int height) {
        field.setFont(new Font("Arial", Font.PLAIN, 16));
        field.setBackground(fieldColor);
        field.setForeground(darkText);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                BorderFactory.createEmptyBorder(0, 15, 0, 15)));
        field.setBounds(55, y, 390, height);
    }

    private JCheckBox showPasswordCheckbox(JPanel panel, int y,
                                          JPasswordField first, JPasswordField second) {
        JCheckBox show = new JCheckBox("Show password");
        show.setFont(new Font("Arial", Font.BOLD, 15));
        show.setForeground(darkText);
        show.setBackground(white);
        show.setFocusPainted(false);
        show.setBounds(55, y, 200, 30);
        panel.add(show);
        final char firstMask = first.getEchoChar();
        final char secondMask = second == null ? 0 : second.getEchoChar();
        show.addActionListener(e -> {
            first.setEchoChar(show.isSelected() ? (char) 0 : firstMask);
            if (second != null) {
                second.setEchoChar(show.isSelected() ? (char) 0 : secondMask);
            }
        });
        return show;
    }

    private JButton actionButton(JPanel panel, String title, int y) {
        JButton button = new JButton(title);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setBackground(yellow);
        button.setForeground(darkText);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBounds(55, y, 390, 50);
        panel.add(button);
        getRootPane().setDefaultButton(button);
        return button;
    }

    private void showLoginScreen() {
        prepareScreen("Login", 590);
        JPanel panel = whitePanel(435);
        addHeading(panel, "Welcome back!", "Sign in to manage laundry transactions");

        addFieldLabel(panel, "Employee ID / Username", 125);
        JTextField employeeField = new JTextField();
        styleField(employeeField, 155, 48);
        panel.add(employeeField);

        addFieldLabel(panel, "Password", 220);
        JPasswordField passwordField = new JPasswordField();
        styleField(passwordField, 250, 48);
        panel.add(passwordField);

        showPasswordCheckbox(panel, 310, passwordField, null);
        JButton loginButton = actionButton(panel, "Log In  \u2192", 365);

        loginButton.addActionListener(e -> {
            String employeeID = employeeField.getText().trim();
            char[] password = passwordField.getPassword();
            try {
                if (employeeID.isEmpty() || password.length == 0) {
                    JOptionPane.showMessageDialog(this,
                            "Please enter your Employee ID and Password.",
                            "Missing Information", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                AccountManager.LoginResult result = accountManager.authenticate(employeeID, password);
                if (result.succeeded()) {
                    new MainFrame(accountManager, result.account);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, result.message,
                            "Login Failed", JOptionPane.WARNING_MESSAGE);
                    passwordField.setText("");
                    passwordField.requestFocusInWindow();
                    if (result.newlyBlocked) {
                        dispose();
                        System.exit(0);
                    }
                }
            } finally {
                Arrays.fill(password, '\0');
            }
        });

        revalidate();
        repaint();
    }

    private void showFirstAdminRegistration() {
        if (!accountManager.needsFirstAdmin()) {
            showLoginScreen();
            return;
        }

        prepareScreen("First Administrator Setup", 690);
        JPanel panel = whitePanel(530);
        addHeading(panel, "Create an account", "Register the first administrator to get started");

        addFieldLabel(panel, "Administrator Full Name", 113);
        JTextField fullName = new JTextField();
        styleField(fullName, 140, 41);
        panel.add(fullName);

        addFieldLabel(panel, "Username", 190);
        JTextField username = new JTextField();
        styleField(username, 217, 41);
        panel.add(username);

        addFieldLabel(panel, "Password", 267);
        JPasswordField password = new JPasswordField();
        styleField(password, 294, 41);
        panel.add(password);

        addFieldLabel(panel, "Confirm Password", 344);
        JPasswordField confirmation = new JPasswordField();
        styleField(confirmation, 371, 41);
        panel.add(confirmation);

        showPasswordCheckbox(panel, 421, password, confirmation);
        JButton createButton = actionButton(panel, "Create Administrator  \u2192", 464);

        createButton.addActionListener(e -> {
            char[] chosen = password.getPassword();
            char[] repeated = confirmation.getPassword();
            try {
                Account account = accountManager.createFirstAdmin(
                        fullName.getText(), username.getText(), chosen, repeated);
                JOptionPane.showMessageDialog(this,
                        "Administrator account created!\nYour User ID is " + account.getId()
                        + ".\nPlease sign in with your User ID or username.",
                        "Account Ready", JOptionPane.INFORMATION_MESSAGE);
                showLoginScreen();
            } catch (IllegalArgumentException | SecurityException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Check Account Details", JOptionPane.WARNING_MESSAGE);
                password.setText("");
                confirmation.setText("");
                password.requestFocusInWindow();
            } finally {
                Arrays.fill(chosen, '\0');
                Arrays.fill(repeated, '\0');
            }
        });

        revalidate();
        repaint();
    }
}
