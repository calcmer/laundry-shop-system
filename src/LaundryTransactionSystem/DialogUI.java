package LaundryTransactionSystem;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

final class DialogUI {
    private static final Color INK = Theme.NAVY;
    private static final Color BORDER = new Color(113, 151, 195);
    private static final Color FIELD_BG = new Color(250, 252, 255);
    private static final Color RED = new Color(168, 44, 44);
    private static final Font BODY = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font BOLD = new Font("Segoe UI", Font.BOLD, 14);

    private DialogUI() { }

    static JLabel label(String caption) {
        JLabel label = new JLabel(caption);
        label.setForeground(INK);
        label.setFont(BOLD);
        return label;
    }

    static JLabel muted(String caption) {
        JLabel label = new JLabel(caption);
        label.setForeground(Theme.MUTED_BLUE);
        label.setFont(BODY);
        return label;
    }

    static void field(JTextField input) {
        input.setFont(BODY);
        input.setForeground(INK);
        input.setBackground(input.isEditable() ? FIELD_BG : new Color(238, 244, 251));
        input.setCaretColor(Theme.BLUE);
        input.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(8, 10, 8, 10)));
        input.setPreferredSize(new Dimension(310, 39));
    }

    static void combo(JComboBox<?> combo) {
        combo.setFont(BODY);
        combo.setForeground(INK);
        combo.setBackground(FIELD_BG);
        combo.setPreferredSize(new Dimension(310, 39));
        combo.setBorder(new LineBorder(BORDER, 1, true));
    }

    static void area(JTextArea input) {
        input.setFont(BODY);
        input.setForeground(INK);
        input.setBackground(FIELD_BG);
        input.setCaretColor(Theme.BLUE);
        input.setLineWrap(true);
        input.setWrapStyleWord(true);
        input.setBorder(new EmptyBorder(8, 10, 8, 10));
    }

    static JScrollPane scroll(JComponent content) {
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(new LineBorder(BORDER, 1, true));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    static JPanel column() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    static void addField(JPanel column, String caption, JComponent field) {
        JLabel label = label(caption);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        column.add(label);
        column.add(Box.createVerticalStrut(6));
        column.add(field);
        column.add(Box.createVerticalStrut(15));
    }

    static JPanel notice(String text, boolean warning) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(warning ? new Color(255, 244, 238) : new Color(237, 245, 255));
        panel.setBorder(new CompoundBorder(
                new LineBorder(warning ? new Color(223, 145, 125) : BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        JTextArea message = new JTextArea(text);
        message.setFont(BOLD);
        message.setForeground(warning ? new Color(139, 46, 36) : INK);
        message.setEditable(false);
        message.setOpaque(false);
        message.setWrapStyleWord(true);
        message.setLineWrap(true);
        message.setFocusable(false);
        message.setColumns(35);
        panel.add(message, BorderLayout.CENTER);
        return panel;
    }

    static JButton button(String title, boolean primary, boolean danger) {
        JButton button = new JButton(title);
        button.setFont(BOLD);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBackground(primary ? (danger ? RED : Theme.BLUE) : new Color(233, 242, 253));
        button.setForeground(primary ? Color.WHITE : Theme.BLUE);
        button.setBorder(new CompoundBorder(new LineBorder(primary ?
                (danger ? RED : Theme.BLUE) : BORDER, 1, true), new EmptyBorder(10, 18, 10, 18)));
        button.setPreferredSize(new Dimension(Math.max(122, title.length() * 9 + 38), 39));
        return button;
    }

    private static JDialog frame(Component owner, String title, JComponent body, int width) {
        Window window = owner instanceof Window ? (Window) owner : SwingUtilities.getWindowAncestor(owner);
        JDialog dialog = new JDialog(window, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        dialog.setContentPane(root);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.PEACH);
        header.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 2, 0, new Color(194, 213, 235)),
                new EmptyBorder(15, 20, 15, 20)));
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 19));
        heading.setForeground(INK);
        header.add(heading, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(18, 21, 15, 21));
        content.add(body, BorderLayout.CENTER);
        root.add(content, BorderLayout.CENTER);
        dialog.setMinimumSize(new Dimension(width, 190));
        return dialog;
    }

    private static void finish(JDialog dialog, JPanel footer, int width) {
        footer.setBackground(new Color(245, 249, 255));
        footer.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, new Color(210, 225, 241)),
                new EmptyBorder(11, 18, 11, 18)));
        dialog.getContentPane().add(footer, BorderLayout.SOUTH);
        dialog.pack();
        if (dialog.getWidth() < width) dialog.setSize(width, dialog.getHeight());
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        if (dialog.getHeight() > screen.height - 95) dialog.setSize(dialog.getWidth(), screen.height - 95);
        dialog.setLocationRelativeTo(dialog.getOwner());
    }

    static int ask(Component owner, String title, JComponent body, String primary, boolean danger) {
        JDialog dialog = frame(owner, title, body, 460);
        final int[] choice = { JOptionPane.CANCEL_OPTION };
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton cancel = button("Cancel", false, false);
        JButton save = button(primary, true, danger);
        cancel.addActionListener(e -> dialog.dispose());
        save.addActionListener(e -> { choice[0] = JOptionPane.OK_OPTION; dialog.dispose(); });
        footer.add(cancel);
        footer.add(save);
        dialog.getRootPane().setDefaultButton(save);
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        finish(dialog, footer, 460);
        dialog.setVisible(true);
        return choice[0];
    }

    static int confirm(Component owner, String title, JComponent body) {
        JDialog dialog = frame(owner, title, body, 580);
        final int[] choice = { JOptionPane.NO_OPTION };
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton no = button("No", false, false);
        JButton yes = button("Yes", true, false);
        no.addActionListener(e -> dialog.dispose());
        yes.addActionListener(e -> { choice[0] = JOptionPane.YES_OPTION; dialog.dispose(); });
        footer.add(no);
        footer.add(yes);
        dialog.getRootPane().setDefaultButton(yes);
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        finish(dialog, footer, 580);
        dialog.setVisible(true);
        return choice[0];
    }

    static void showInfo(Component owner, String title, JComponent body) {
        JDialog dialog = frame(owner, title, body, 560);
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton close = button("Close", true, false);
        close.addActionListener(e -> dialog.dispose());
        footer.add(close);
        dialog.getRootPane().setDefaultButton(close);
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        finish(dialog, footer, 560);
        if (body instanceof JScrollPane) {
            JScrollPane scroller = (JScrollPane) body;
            SwingUtilities.invokeLater(() -> scroller.getViewport().setViewPosition(new Point(0, 0)));
        }
        dialog.setVisible(true);
    }

    static JPanel section(String title, String[][] entries) {
        JPanel section = new JPanel(new GridBagLayout());
        section.setBackground(Color.WHITE);
        section.setBorder(new CompoundBorder(new LineBorder(new Color(191, 213, 238), 1, true),
                new EmptyBorder(11, 13, 11, 13)));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 10, 0);
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 15));
        heading.setForeground(Theme.BLUE);
        section.add(heading, c);
        c.gridwidth = 1;
        for (String[] pair : entries) {
            c.gridy++;
            c.gridx = 0;
            c.weightx = 0;
            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.insets = new Insets(3, 0, 5, 16);
            JLabel label = muted(pair[0]);
            label.setPreferredSize(new Dimension(125, 18));
            section.add(label, c);
            c.gridx = 1;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(3, 0, 5, 0);
            JTextArea value = new JTextArea(pair[1]);
            value.setFont(BOLD);
            value.setForeground(INK);
            value.setEditable(false);
            value.setOpaque(false);
            value.setWrapStyleWord(true);
            value.setLineWrap(true);
            value.setFocusable(false);
            section.add(value, c);
        }
        return section;
    }
}
