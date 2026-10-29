import java.awt.*;
import javax.swing.*;

class CardPanel extends JPanel {
    public CardPanel() {
        super();
        setLayout(null);
        setOpaque(false); 
        setPreferredSize(new Dimension(300, 200));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int arc = 30;
        int width = getWidth();
        int height = getHeight();

        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, width, height, arc, arc);


        g2.setColor(new Color(231, 64, 50)); 
        g2.fillRoundRect(0, 0, width, height/4, arc, arc);
        g2.fillRect(0, 50, width,65);

        g2.setColor(Color.LIGHT_GRAY);
        g2.setStroke(new BasicStroke(2)); 
        g2.drawRoundRect(0, 0, width , height - 1, arc, arc);
        g2.dispose();
    }
}
