package LaundryTransactionSystem;

import javax.swing.JTextField;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class HintTextField extends JTextField {
    private final String hint;

    public HintTextField(String hint) {
        this.hint = hint;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getText().isEmpty()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(135, 150, 169));
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(hint, 2, y);
            g2.dispose();
        }
    }
}
