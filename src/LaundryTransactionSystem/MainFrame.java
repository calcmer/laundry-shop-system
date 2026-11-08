package LaundryTransactionSystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import javax.imageio.ImageIO;

public class MainFrame extends JFrame {
    private JPanel sidebar;
    private JPanel pages;
    private CardLayout cardLayout;
    private JButton activeMenu;
    private OrdersPanel ordersPanel;
    private NewOrderPanel newOrderPanel;
    private CustomersPanel customersPanel;
    private ReportsPanel reportsPanel;
    private JButton ordersMenu;
    private AccountsPanel accountsPanel;
    private final AccountManager accountManager;
    private final Account signedIn;
    private final CustomerManager customerManager = CustomerManager.getInstance();
    private final OrderManager orderManager = OrderManager.getInstance();

    public MainFrame(AccountManager accounts, Account session) {
        accountManager = accounts;
        signedIn = session;
        if (!accountManager.isActiveAccount(signedIn)) {
            throw new SecurityException("Sign in through LaundryLogIn before opening the dashboard.");
        }
        customerManager.setOrderManager(orderManager);
        setTitle("Jocasha's Laundry Shop - Transaction Processing System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setLayout(new BorderLayout());
        createPages();
        createSidebar();
        createHeader();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    private void createPages() {
        cardLayout = new CardLayout();
        pages = new JPanel(cardLayout);
        ordersPanel = new OrdersPanel(this, orderManager, accountManager, signedIn);
        pages.add(ordersPanel, "Orders");
        newOrderPanel = new NewOrderPanel(this, orderManager, customerManager, signedIn, new Runnable() {
            @Override public void run() { showOrdersPage(); }
        });
        pages.add(newOrderPanel, "New Order");
        customersPanel = new CustomersPanel(this, customerManager, orderManager, accountManager, signedIn);
        pages.add(customersPanel, "Customers");
        reportsPanel = new ReportsPanel(orderManager);
        pages.add(reportsPanel, "Reports");
        if (accountManager.isAdmin(signedIn)) {
            accountsPanel = new AccountsPanel(this, accountManager, signedIn);
            pages.add(accountsPanel, "Accounts");   
        }
        add(pages, BorderLayout.CENTER);
        cardLayout.show(pages, "Orders");
    }

    private void createSidebar() {
        sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Theme.BLUE);
        sidebar.setPreferredSize(new Dimension(245, 0));
        add(sidebar, BorderLayout.WEST);

        JPanel top = new JPanel();
        top.setBackground(Theme.BLUE);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBorder(new EmptyBorder(25, 15, 35, 15));

        RoundedPanel logo = new RoundedPanel(20);
        logo.setBackground(Theme.WHITE);
        logo.setLayout(new BorderLayout());
        logo.setMaximumSize(new Dimension(72, 72));
        logo.setPreferredSize(new Dimension(72, 72));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel logoImage = new JLabel("JL", SwingConstants.CENTER);
        logoImage.setFont(new Font("Arial", Font.BOLD, 28));
        logoImage.setForeground(Theme.BLUE);
        ImageIcon laundryIcon = loadIcon("laundryicon.png", 58, 58);
        if (laundryIcon != null) {
            logoImage.setText("");
            logoImage.setIcon(laundryIcon);
        }
        logo.add(logoImage);

        JLabel brand = new JLabel("JOCASHA'S");
        brand.setFont(new Font("Arial", Font.BOLD, 22));
        brand.setForeground(Theme.WHITE);
        brand.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel brandSub = new JLabel("LAUNDRY SHOP");
        brandSub.setFont(new Font("Arial", Font.PLAIN, 13));
        brandSub.setForeground(new Color(213, 231, 253));
        brandSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        top.add(logo);
        top.add(Box.createVerticalStrut(15));
        top.add(brand);
        top.add(Box.createVerticalStrut(3));
        top.add(brandSub);
        sidebar.add(top, BorderLayout.NORTH);

        JPanel menu = new JPanel();
        menu.setBackground(Theme.BLUE);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(5, 14, 0, 14));
        sidebar.add(menu, BorderLayout.CENTER);

        JButton newOrder = menuButton("New Order");
        newOrder.setMnemonic(java.awt.event.KeyEvent.VK_N);
        JButton orderList = menuButton("Orders");
        orderList.setMnemonic(java.awt.event.KeyEvent.VK_O);
        ordersMenu = orderList;
        JButton customers = menuButton("Customers");
        customers.setMnemonic(java.awt.event.KeyEvent.VK_C);
        JButton reports = menuButton("Reports");
        reports.setMnemonic(java.awt.event.KeyEvent.VK_R);
        JButton accounts = menuButton("Accounts");
        accounts.setMnemonic(java.awt.event.KeyEvent.VK_A);
        addMenuItem(menu, newOrder);
        addMenuItem(menu, orderList);
        addMenuItem(menu, customers);
        addMenuItem(menu, reports);
        if (accountManager.isAdmin(signedIn)) addMenuItem(menu, accounts);
        setActiveMenu(orderList);

        connectMenu(newOrder, "New Order");
        connectMenu(orderList, "Orders");
        connectMenu(customers, "Customers");
        connectMenu(reports, "Reports");
        if (accountManager.isAdmin(signedIn)) connectMenu(accounts, "Accounts");

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(Theme.BLUE);
        bottom.setBorder(new EmptyBorder(12, 14, 25, 14));
        JButton logout = menuButton("Log Out");
        logout.setMnemonic(java.awt.event.KeyEvent.VK_L);
        bottom.add(logout, BorderLayout.CENTER);
        sidebar.add(bottom, BorderLayout.SOUTH);

        logout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int answer = JOptionPane.showConfirmDialog(MainFrame.this,
                        "Are you sure you want to log out?", "Log Out",
                        JOptionPane.YES_NO_OPTION);
                if (answer == JOptionPane.YES_OPTION) {
                    new LaundryLogIn().setVisible(true);
                    dispose();
                }
            }
        });
    }

    private void connectMenu(JButton button, String pageName) {
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pageName.equals("Accounts") && !accountManager.isAdmin(signedIn)) {
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Only administrators can open Accounts.", "Access Denied",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                setActiveMenu(button);
                if (pageName.equals("Accounts") && accountsPanel != null) accountsPanel.refreshAccounts();
                if (pageName.equals("Orders")) ordersPanel.refreshOrders();
                if (pageName.equals("Customers")) customersPanel.refreshCustomers();
                if (pageName.equals("Reports")) reportsPanel.refreshReports();
                cardLayout.show(pages, pageName);
                if (pageName.equals("New Order")) newOrderPanel.scrollToTop();
            }
        });
    }

    private void showOrdersPage() {
        ordersPanel.refreshOrders();
        cardLayout.show(pages, "Orders");
        if (ordersMenu != null) setActiveMenu(ordersMenu);
    }

    private void addMenuItem(JPanel menu, JButton button) {
        menu.add(button);
        menu.add(Box.createVerticalStrut(9));
    }

    private JButton menuButton(String text) {
        RoundedButton button = new RoundedButton(text, 18);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setForeground(Theme.WHITE);
        button.setBackground(Theme.BLUE);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(new EmptyBorder(0, 18, 0, 8));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        button.setPreferredSize(new Dimension(215, 52));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setToolTipText("Open " + text + " (Alt + " + text.charAt(0) + ")");
        button.setIconTextGap(13);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button != activeMenu) {
                    button.setBackground(new Color(36, 103, 189));
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (button != activeMenu) button.setBackground(Theme.BLUE);
            }
        });
        return button;
    }


    private ImageIcon loadIcon(String filename, int width, int height) {
        URL resource = MainFrame.class.getResource("/LaundryTransactionSystem/" + filename);
        if (resource == null) {
            System.err.println("Optional GUI icon not found: " + filename
                    + " (place it in src/LaundryTransactionSystem/)");
            return null;
        }
        try {
            BufferedImage source = ImageIO.read(resource);
            if (source == null) return null;

            BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = scaled.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(source, 0, 0, width, height, null);
            g.dispose();
            return new ImageIcon(scaled);
        } catch (IOException ex) {
            System.err.println("Cannot read optional GUI icon " + filename + ": " + ex.getMessage());
            return null;
        }
    }

    private void setActiveMenu(JButton button) {
        if (activeMenu != null) {
            activeMenu.setBackground(Theme.BLUE);
            activeMenu.setForeground(Theme.WHITE);
        }
        activeMenu = button;
        button.setBackground(new Color(235, 244, 255));
        button.setForeground(Theme.BLUE);
    }
    
            public static void changePassword(Component owner, AccountManager manager, Account signedIn) {
        JPasswordField current = new JPasswordField(22);
        JPasswordField updated = new JPasswordField(22);
        JPasswordField repeat = new JPasswordField(22);
        DialogUI.field(current);
        DialogUI.field(updated);
        DialogUI.field(repeat);
        JPanel form = DialogUI.column();
        DialogUI.addField(form, "Current Password", current);
        DialogUI.addField(form, "New Password (at least 6 characters)", updated);
        DialogUI.addField(form, "Confirm New Password", repeat);
        while (true) {
            int response = DialogUI.ask(owner, "Change Password", form, "Save Password", false);
            if (response != JOptionPane.OK_OPTION) return;
            char[] old = current.getPassword(), next = updated.getPassword(), again = repeat.getPassword();
            try {
                manager.changePassword(signedIn, old, next, again);
                JOptionPane.showMessageDialog(owner, "Password updated successfully.");
                return;
            } catch (IllegalArgumentException | SecurityException ex) {
                JOptionPane.showMessageDialog(owner, ex.getMessage(), "Cannot Change Password", JOptionPane.WARNING_MESSAGE);
            } finally {
                Arrays.fill(old, '\0'); Arrays.fill(next, '\0'); Arrays.fill(again, '\0');
                current.setText(""); updated.setText(""); repeat.setText("");
            }
        }
    }
    

    private void createHeader() {
        Container pane = getContentPane();
        pane.remove(pages);
        JPanel right = new JPanel(new BorderLayout());
        pane.add(right, BorderLayout.CENTER);

        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBackground(Theme.PEACH);
        header.setPreferredSize(new Dimension(0, 112));
        header.setBorder(new EmptyBorder(14, 32, 14, 30));

        JLabel heading = new JLabel("LAUNDRY TRANSACTION SYSTEM");
        heading.setFont(new Font("Arial", Font.BOLD, 25));
        heading.setForeground(Theme.NAVY);
        header.add(heading, BorderLayout.CENTER);

        JPanel userArea = new JPanel(new BorderLayout(12, 0));
        userArea.setOpaque(false);
        userArea.setBorder(new EmptyBorder(0, 0, 0, 0));

        JLabel profileIcon = new JLabel();
        ImageIcon accountIcon = loadIcon("accountupbar.png", 49, 49);
        if (accountIcon != null) {
            profileIcon.setIcon(accountIcon);
        } else {
            profileIcon.setText("◎");
            profileIcon.setFont(new Font("Arial", Font.BOLD, 33));
            profileIcon.setForeground(Theme.NAVY);
        }
        profileIcon.setPreferredSize(new Dimension(51, 56));
        profileIcon.setHorizontalAlignment(SwingConstants.CENTER);
        userArea.add(profileIcon, BorderLayout.WEST);

        JPanel profileText = new JPanel(new GridBagLayout());
        profileText.setOpaque(false);

        JLabel name = new JLabel("Welcome, " + signedIn.getFullName().split(" ")[0]);
        name.setFont(new Font("Arial", Font.BOLD, 16));
        name.setForeground(Theme.NAVY);

        JLabel role = new JLabel(signedIn.getRole().toUpperCase(java.util.Locale.ROOT));
        role.setFont(new Font("Arial", Font.PLAIN, 12));
        role.setForeground(Theme.MUTED_BLUE);

        JButton changePassword = new JButton("Change Password");
        changePassword.setForeground(Theme.BLUE);
        changePassword.setBackground(new Color(231, 241, 255));
        changePassword.setFont(new Font("Arial", Font.BOLD, 12));
        changePassword.setFocusPainted(false);
        changePassword.setOpaque(true);
        changePassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(168, 198, 236), 1),
                new EmptyBorder(5, 10, 5, 10)));
        changePassword.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        changePassword.setToolTipText("Change the password for your signed-in account");
        changePassword.addActionListener(e ->
                changePassword(MainFrame.this, accountManager, signedIn));

        GridBagConstraints r = new GridBagConstraints();
        r.gridx = 0;
        r.anchor = GridBagConstraints.WEST;
        r.fill = GridBagConstraints.NONE;
        r.gridy = 0;
        r.insets = new Insets(0, 0, 3, 0);
        profileText.add(name, r);
        r.gridy = 1;
        r.insets = new Insets(0, 0, 6, 0);
        profileText.add(role, r);
        r.gridy = 2;
        r.insets = new Insets(0, 0, 0, 0);
        profileText.add(changePassword, r);
        userArea.add(profileText, BorderLayout.CENTER);

        header.add(userArea, BorderLayout.EAST);
        right.add(header, BorderLayout.NORTH);
        right.add(pages, BorderLayout.CENTER);
    }
    

}
