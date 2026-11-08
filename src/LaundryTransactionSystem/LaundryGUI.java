package LaundryTransactionSystem;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.BorderFactory;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import java.awt.Font;
import java.awt.Color;

public class LaundryGUI {
    public static void main(String[] args) {
        styleMessageDialogs();
        SwingUtilities.invokeLater(() -> {
            try {
                AccountManager.getInstance();
                CustomerManager.getInstance();
                OrderManager.getInstance();
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(null,
                        "The system cannot start because a saved data file is damaged or incomplete.\n\n"
                        + ex.getMessage()
                        + "\n\nThe file was not changed. Restore it from a backup, then open the program again.",
                        "Data File Problem", JOptionPane.ERROR_MESSAGE);
                return;
            }
            new LaundryLogIn().setVisible(true);
        });
    }

    private static void styleMessageDialogs() {
        FontUIResource messageFont = new FontUIResource("Segoe UI", Font.PLAIN, 14);
        FontUIResource buttonFont = new FontUIResource("Segoe UI", Font.BOLD, 14);
        ColorUIResource background = new ColorUIResource(Theme.PEACH);
        ColorUIResource text = new ColorUIResource(Theme.NAVY);

        UIManager.put("OptionPane.background", background);
        UIManager.put("OptionPane.foreground", text);
        UIManager.put("OptionPane.messageForeground", text);
        UIManager.put("OptionPane.font", messageFont);
        UIManager.put("OptionPane.messageFont", messageFont);
        UIManager.put("OptionPane.buttonFont", buttonFont);
        UIManager.put("OptionPane.border", BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(181, 204, 232)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        UIManager.put("OptionPane.messageAreaBorder", BorderFactory.createEmptyBorder(8, 10, 8, 10));
        UIManager.put("OptionPane.buttonAreaBorder", BorderFactory.createEmptyBorder(9, 10, 7, 10));
    }

}
