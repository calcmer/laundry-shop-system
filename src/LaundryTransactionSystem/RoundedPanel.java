package LaundryTransactionSystem;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class RoundedPanel extends JPanel {
    private final int radius;
    private Color outlineColor; 

    public RoundedPanel(int radius) {
        this.radius = radius;
        setOpaque(false);
    }

    public void setOutlineColor(Color color) {
        outlineColor = color;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        if (outlineColor != null) {
            g2.setColor(outlineColor);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, Math.max(0, getWidth() - 3),
                    Math.max(0, getHeight() - 3), radius, radius);
        }
        g2.dispose();
    }
}
