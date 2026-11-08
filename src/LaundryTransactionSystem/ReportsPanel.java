package LaundryTransactionSystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

public class ReportsPanel extends JPanel {
    private final OrderManager manager;
    private JComboBox<String> periodBox;
    private JPanel customDateRow;
    private JTextField fromField;
    private JTextField toField;
    private JLabel dateError;
    private JLabel periodLabel;
    private JLabel readyNotice;
    private final JLabel[] amounts = new JLabel[6];
    private final String[] titles = {
        "Total Orders", "Pending Orders", "In Progress",
        "Completed Orders", "Canceled Orders", "Total Collected"
    };
    private final Color[] valueColors = {
        Theme.BLUE, new Color(228, 140, 0), new Color(0, 90, 225),
        new Color(0, 145, 91), new Color(181, 43, 65), new Color(0, 145, 91)
    };

    public ReportsPanel(OrderManager manager) {
        this.manager = manager;
        setLayout(new BorderLayout(0, 22));
        setBackground(Theme.PAGE_BLUE);
        setBorder(new EmptyBorder(25, 30, 25, 30));
        createScreen();
        refreshReports();
    }

    private void createScreen() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        add(header, BorderLayout.NORTH);

        JLabel title = new JLabel("Reports Overview");
        title.setForeground(Theme.NAVY);
        title.setFont(new Font("Arial", Font.BOLD, 29));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(5));

        header.add(Box.createVerticalStrut(20));

        RoundedPanel filterCard = new RoundedPanel(20);
        filterCard.setBackground(Theme.WHITE);
        filterCard.setBorder(new EmptyBorder(18, 22, 14, 22));
        filterCard.setLayout(new BoxLayout(filterCard, BoxLayout.Y_AXIS));
        filterCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(filterCard);

        JLabel dateLabel = new JLabel("Select Date Range");
        dateLabel.setForeground(Theme.NAVY);
        dateLabel.setFont(new Font("Arial", Font.BOLD, 14));
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        filterCard.add(dateLabel);
        filterCard.add(Box.createVerticalStrut(8));

        periodBox = new JComboBox<>(new String[]{
            "All Time", "Today", "This Week", "This Month", "Custom Range"
        });
        periodBox.setFont(new Font("Arial", Font.PLAIN, 15));
        periodBox.setBackground(Theme.WHITE);
        periodBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        periodBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        periodBox.setToolTipText("Choose a date range for the report");
        filterCard.add(periodBox);

        customDateRow = new JPanel(new GridLayout(1, 2, 15, 0));
        customDateRow.setOpaque(false);
        customDateRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        fromField = new JTextField(LocalDate.now().withDayOfMonth(1).toString());
        toField = new JTextField(LocalDate.now().toString());
        customDateRow.add(dateInput("From (YYYY-MM-DD)", fromField));
        customDateRow.add(dateInput("To (YYYY-MM-DD)", toField));
        customDateRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        customDateRow.setVisible(false);
        filterCard.add(customDateRow);

        dateError = new JLabel(" ");
        dateError.setForeground(new Color(181, 43, 65));
        dateError.setFont(new Font("Arial", Font.PLAIN, 12));
        dateError.setAlignmentX(Component.LEFT_ALIGNMENT);
        filterCard.add(dateError);

        header.add(Box.createVerticalStrut(22));
        JPanel summaryHeading = new JPanel(new BorderLayout());
        summaryHeading.setOpaque(false);
        summaryHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel summaryTitle = new JLabel("Transaction Summary");
        summaryTitle.setFont(new Font("Arial", Font.BOLD, 22));
        summaryTitle.setForeground(Theme.NAVY);
        summaryHeading.add(summaryTitle, BorderLayout.WEST);
        periodLabel = new JLabel("All Time");
        periodLabel.setFont(new Font("Arial", Font.BOLD, 13));
        periodLabel.setForeground(Theme.BLUE);
        summaryHeading.add(periodLabel, BorderLayout.EAST);
        header.add(summaryHeading);

        JPanel cards = new JPanel(new GridLayout(2, 3, 17, 17));
        cards.setOpaque(false);
        for (int i = 0; i < titles.length; i++) {
            RoundedPanel card = new RoundedPanel(20);
            card.setBackground(Theme.WHITE);
            card.setLayout(new BorderLayout(0, 12));
            card.setBorder(new EmptyBorder(24, 25, 22, 18));
            JLabel caption = new JLabel(titles[i]);
            caption.setFont(new Font("Arial", Font.PLAIN, 16));
            caption.setForeground(Theme.NAVY);
            JLabel amount = new JLabel("0");
            amount.setFont(new Font("Arial", Font.BOLD, 34));
            amount.setForeground(valueColors[i]);
            card.add(caption, BorderLayout.NORTH);
            card.add(amount, BorderLayout.CENTER);
            amounts[i] = amount;
            cards.add(card);
        }
        JPanel reportBody = new JPanel(new BorderLayout(0, 10));
        reportBody.setOpaque(false);
        reportBody.add(cards, BorderLayout.CENTER);
        readyNotice = new JLabel("Ready for Pickup: 0 (included in Total Orders)");
        readyNotice.setForeground(Theme.MUTED_BLUE);
        readyNotice.setFont(new Font("Arial", Font.PLAIN, 13));
        reportBody.add(readyNotice, BorderLayout.SOUTH);
        add(reportBody, BorderLayout.CENTER);

        periodBox.addActionListener(e -> {
            customDateRow.setVisible(periodBox.getSelectedIndex() == 4);
            revalidate();
            repaint();
            refreshReports();
        });
        DocumentListener dateListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refreshReports(); }
            @Override public void removeUpdate(DocumentEvent e) { refreshReports(); }
            @Override public void changedUpdate(DocumentEvent e) { refreshReports(); }
        };
        fromField.getDocument().addDocumentListener(dateListener);
        toField.getDocument().addDocumentListener(dateListener);
    }

    private JPanel dateInput(String caption, JTextField field) {
        JPanel section = new JPanel(new BorderLayout(0, 4));
        section.setOpaque(false);
        JLabel label = new JLabel(caption);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(Theme.NAVY);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(201, 218, 238)),
                new EmptyBorder(6, 9, 6, 9)));
        section.add(label, BorderLayout.NORTH);
        section.add(field, BorderLayout.CENTER);
        return section;
    }

    public void refreshReports() {
        if (periodBox == null) return;
        String period = (String) periodBox.getSelectedItem();
        periodLabel.setText(period);
        LocalDate today = LocalDate.now();
        LocalDate from = null;
        LocalDate to = null;
        try {
            switch (period) {
                case "Today":
                    from = today; to = today; break;
                case "This Week":
                    from = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    to = today; break;
                case "This Month":
                    from = today.withDayOfMonth(1); to = today; break;
                case "Custom Range":
                    from = LocalDate.parse(fromField.getText().trim());
                    to = LocalDate.parse(toField.getText().trim());
                    if (from.isAfter(to)) throw new IllegalArgumentException("From date must not be after To date.");
                    break;
                default:
                    break;
            }
            dateError.setText(" ");
        } catch (DateTimeParseException ex) {
            dateError.setText("Enter valid dates in YYYY-MM-DD format.");
            clearValues();
            return;
        } catch (IllegalArgumentException ex) {
            dateError.setText(ex.getMessage());
            clearValues();
            return;
        }
        
        
        // array int - count the orders in reports panel
        int[] counts = new int[5];
        int ready = 0;
        double collected = 0;
        List<Order> orders = manager.findMatching("All Orders", "", false);
        for (Order order : orders) {
            if (inRange(order.getDate(), from, to)) {
                counts[0]++;
                switch (order.getStatus()) {
                    case "Pending": counts[1]++; break;
                    case "In Progress": counts[2]++; break;
                    case "Completed": counts[3]++; break;
                    case "Canceled": counts[4]++; break;
                    default: 
                        break; 
                }
            }

            if (order.getPayment() > 0 && inRange(order.getPaymentDate(), from, to)) {
                collected += Math.min(order.getPayment(), order.getTotal());
            }
            if (order.getRefundAmount() > 0 && inRange(order.getRefundDate(), from, to)) {
                collected -= order.getRefundAmount();
            }
        }
        // display array values in report
        for (int i = 0; i < counts.length; i++) amounts[i].setText(String.valueOf(counts[i]));
        amounts[5].setText(OrderManager.pesos(collected));
        readyNotice.setText("Ready for Pickup: " + ready + " (included in Total Orders)");
    }

    private boolean inRange(String dateText, LocalDate from, LocalDate to) {
        if (dateText == null || dateText.trim().isEmpty()) return false;
        try {
            LocalDate date = LocalDate.parse(dateText.length() >= 10
                    ? dateText.substring(0, 10) : dateText);
            return (from == null || !date.isBefore(from)) && (to == null || !date.isAfter(to));
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    private void clearValues() {
        for (JLabel amount : amounts) amount.setText("--");
        if (readyNotice != null) readyNotice.setText("Ready for Pickup: --");
    }
}
