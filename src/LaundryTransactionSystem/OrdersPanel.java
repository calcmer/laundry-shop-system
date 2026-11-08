package LaundryTransactionSystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class OrdersPanel extends JPanel {
    private final OrderManager manager;
    private final AccountManager accountManager;
    private final Account signedIn;
    private final OrderDialogs dialogs;

    private JTextField searchField;
    private JComboBox<String> sortBox;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel resultLabel;
    private final ArrayList<JButton> filterButtons = new ArrayList<>();
    private final String[] statuses = {
        "All Orders", "Pending", "In Progress", "Ready for Pickup", "Completed", "Canceled"
    };
    private String selectedStatus = "All Orders";

    public OrdersPanel(JFrame parent, OrderManager manager,
                       AccountManager accounts, Account session) {
        this.manager = manager;
        this.accountManager = accounts;
        this.signedIn = session;
        dialogs = new OrderDialogs(parent, manager, accounts, session, new Runnable() {
            @Override
            public void run() { refreshOrders(); }
        });
        setLayout(new BorderLayout(0, 20));
        setBackground(Theme.PAGE_BLUE);
        setBorder(new EmptyBorder(25, 30, 25, 30));
        createScreen();
        refreshOrders();
    }

    private void createScreen() {
        JPanel upper = new JPanel();
        upper.setOpaque(false);
        upper.setLayout(new BoxLayout(upper, BoxLayout.Y_AXIS));
        add(upper, BorderLayout.NORTH);

        JLabel pageTitle = new JLabel("Order Management");
        pageTitle.setFont(new Font("Arial", Font.BOLD, 29));
        pageTitle.setForeground(Theme.NAVY);
        pageTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        upper.add(pageTitle);
        upper.add(Box.createVerticalStrut(22));

        JPanel filters = new JPanel(new GridLayout(1, statuses.length, 9, 0));
        filters.setOpaque(false);
        filters.setAlignmentX(Component.LEFT_ALIGNMENT);
        filters.setPreferredSize(new Dimension(0, 43));
        filters.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));

        for (String status : statuses) {
            RoundedButton button = new RoundedButton(status, 17);
            button.setFont(new Font("Arial", Font.BOLD, 12));
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.setToolTipText("Show " + status + " orders");
            filterButtons.add(button);
            filters.add(button);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectedStatus = status;
                    updateFilterButtons();
                    updateTable();
                }
            });
        }
        updateFilterButtons();
        upper.add(filters);
        upper.add(Box.createVerticalStrut(17));

        JPanel searchRow = new JPanel(new BorderLayout(16, 0));
        searchRow.setOpaque(false);
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchRow.setPreferredSize(new Dimension(0, 47));
        searchRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 47));

        RoundedPanel searchBox = new RoundedPanel(17);
        searchBox.setBackground(Theme.WHITE);
        searchBox.setOutlineColor(new Color(96, 135, 183));
        searchBox.setLayout(new BorderLayout());
        searchBox.setBorder(new EmptyBorder(0, 19, 0, 13));
        searchField = new HintTextField("Search Order ID or Customer...");
        searchField.setFont(new Font("Arial", Font.PLAIN, 15));
        searchField.setForeground(Theme.NAVY);
        searchField.setOpaque(false);
        searchField.setBorder(null);
        searchField.setToolTipText("Search Order ID or customer name");
        searchBox.add(searchField, BorderLayout.CENTER);
        searchRow.add(searchBox, BorderLayout.CENTER);

        JPanel sortArea = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 3));
        sortArea.setOpaque(false);
        JLabel sortLabel = new JLabel("Sort By:");
        sortLabel.setFont(new Font("Arial", Font.BOLD, 13));
        sortLabel.setForeground(Theme.NAVY);
        sortBox = new JComboBox<>(new String[]{"Newest First", "Oldest First"});
        sortBox.setFont(new Font("Arial", Font.PLAIN, 13));
        sortBox.setBackground(Theme.WHITE);
        sortBox.setPreferredSize(new Dimension(160, 40));
        sortArea.add(sortLabel);
        sortArea.add(sortBox);
        searchRow.add(sortArea, BorderLayout.EAST);
        upper.add(searchRow);

        createTable();
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateTable(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateTable(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateTable(); }
        });
        sortBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { updateTable(); }
        });
    }

    private void updateFilterButtons() {
        for (int i = 0; i < filterButtons.size(); i++) {
            JButton button = filterButtons.get(i);
            boolean chosen = statuses[i].equals(selectedStatus);
            button.setBackground(chosen ? Theme.BLUE : Theme.WHITE);
            button.setForeground(chosen ? Theme.WHITE : Theme.NAVY);
        }
    }

    private void createTable() {
        RoundedPanel tableCard = new RoundedPanel(18);
        tableCard.setBackground(Theme.WHITE);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(10, 10, 7, 10));
        add(tableCard, BorderLayout.CENTER);

        String[] headers = {"ORDER ID", "CUSTOMER", "DATE", "STATUS", "TOTAL", "ACTIONS"};
        tableModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(new Font("Arial", Font.BOLD, 14));
        table.setForeground(Theme.NAVY);
        table.setRowHeight(56);
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(151, 178, 210));
        table.setSelectionBackground(new Color(213, 232, 251));
        table.setSelectionForeground(Theme.NAVY);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setForeground(Theme.NAVY);
        table.getTableHeader().setBackground(new Color(232, 242, 253));
        table.getTableHeader().setPreferredSize(new Dimension(0, 47));
        table.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer centered = new DefaultTableCellRenderer();
        centered.setHorizontalAlignment(SwingConstants.CENTER);
        centered.setFont(new Font("Arial", Font.BOLD, 14));
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centered);
        }
        DefaultTableCellRenderer centeredHeader = new DefaultTableCellRenderer();
        centeredHeader.setHorizontalAlignment(SwingConstants.CENTER);
        centeredHeader.setBackground(new Color(232, 242, 253));
        centeredHeader.setForeground(Theme.NAVY);
        centeredHeader.setFont(new Font("Arial", Font.BOLD, 13));
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(centeredHeader);
        }

        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        t, value, isSelected, hasFocus, row, col);
                label.setFont(new Font("Arial", Font.BOLD, 14));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                if ("Completed".equals(value)) {
                    label.setForeground(new Color(0, 147, 91));
                } else if ("Pending".equals(value)) {
                    label.setForeground(new Color(228, 140, 0));
                } else if ("In Progress".equals(value)) {
                    label.setForeground(new Color(0, 87, 218));
                } else if ("Canceled".equals(value)) {
                    label.setForeground(new Color(181, 43, 65));
                } else {
                    label.setForeground(new Color(113, 55, 191));
                }
                return label;
            }
        });

        table.getColumnModel().getColumn(5).setCellRenderer(new TableCellRenderer() {
            private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 11));
            private final JButton button = new JButton();
            {
                button.setFont(new Font("Arial", Font.BOLD, 13));
                button.setPreferredSize(new Dimension(105, 34));
                button.setBackground(new Color(228, 240, 255));
                button.setForeground(Theme.BLUE);
                button.setBorder(BorderFactory.createLineBorder(new Color(153, 184, 224)));
                button.setFocusPainted(false);
                panel.add(button);
            }
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                button.setText(String.valueOf(value));
                panel.setBackground(isSelected ? t.getSelectionBackground() : Theme.WHITE);
                return panel;
            }
        });
        table.setToolTipText("Click the button in Actions to manage an order");

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 5) {
                    String id = table.getValueAt(row, 0).toString();
                    Order order = manager.findById(id);
                    if (order != null) showActionsMenu(order, row);
                }
            }
        });
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                table.setCursor(new Cursor(col == 5
                        ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(96, 135, 183), 2));
        scroll.getViewport().setBackground(Theme.WHITE);
        tableCard.add(scroll, BorderLayout.CENTER);

        resultLabel = new JLabel("Showing 0 orders");
        resultLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        resultLabel.setForeground(Theme.MUTED_BLUE);
        resultLabel.setBorder(new EmptyBorder(12, 15, 8, 0));
        tableCard.add(resultLabel, BorderLayout.SOUTH);
    }

    private void showActionsMenu(Order order, int row) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem view = new JMenuItem("View Details");
        view.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { dialogs.showDetails(order); }
        });
        menu.add(view);

        String status = order.getStatus();
        if (status.equals("Pending") || status.equals("In Progress")) {
            JMenuItem edit = new JMenuItem("Edit Order");
            edit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) { dialogs.editOrder(order); }
            });
            menu.add(edit);
        }
        String nextStatus = null;
        String actionName = null;
        if (status.equals("Pending")) {
            nextStatus = "In Progress";
            actionName = "Start Processing";
        } else if (status.equals("In Progress")) {
            nextStatus = "Ready for Pickup";
            actionName = "Mark Ready for Pickup";
        } else if (status.equals("Ready for Pickup")) {
            nextStatus = "Completed";
            actionName = "Mark as Completed";
        }
        if (nextStatus != null) {
            final String newStatus = nextStatus;
            JMenuItem advance = new JMenuItem(actionName);
            advance.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dialogs.advanceOrder(order, newStatus);
                }
            });
            menu.add(advance);
        }
        if (accountManager.isAdmin(signedIn) && (status.equals("Pending") || status.equals("In Progress")
                || status.equals("Ready for Pickup"))) {
            JMenuItem cancel = new JMenuItem("Cancel Order");
            cancel.setToolTipText("Administrator password required; paid orders require a refund");
            cancel.setForeground(new Color(181, 43, 65));
            cancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) { dialogs.cancelOrder(order); }
            });
            menu.addSeparator();
            menu.add(cancel);
        }
        Rectangle cell = table.getCellRect(row, 5, true);
        menu.show(table, cell.x + cell.width / 2 - 85, cell.y + cell.height - 4);
    }

    private String actionButtonText(String status) {
        if ("Pending".equals(status) || "In Progress".equals(status)) return "Edit";
        if ("Ready for Pickup".equals(status)) return "Update";
        return "View";
    }

    public void refreshOrders() {
        updateTable();
    }

    private void updateTable() {
        if (searchField == null || sortBox == null || tableModel == null) return;
        List<Order> matching = manager.findMatching(selectedStatus,
                searchField.getText(), sortBox.getSelectedIndex() == 0);
        tableModel.setRowCount(0);
        for (Order order : matching) {
            tableModel.addRow(new Object[]{
                order.getId(), order.getCustomer(), order.getDate(),
                order.getStatus(), OrderManager.pesos(order.getTotal()),
                actionButtonText(order.getStatus())
            });
        }
        resultLabel.setText("Showing " + matching.size() + " of "
                + manager.countAll() + " orders");
    }
}
