import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

/** Mục trên thanh menu đỏ của trang chủ (HOME, ERROR LOG, VOCAB): mục đang mở nền trắng chữ đỏ. */
public class NavItem extends JButton {
    private static final Color RED = new Color(231, 64, 50);
    private boolean current;
    private boolean hovered;

    public NavItem(String text) {
        super(text);
        setFont(new Font("Arial", Font.BOLD, 17));
        setForeground(Color.WHITE);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
            @Override
            public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
        });
    }

    public void setCurrent(boolean current) {
        this.current = current;
        setForeground(current ? RED : Color.WHITE);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (current || hovered) {
            g2.setColor(current ? Color.WHITE : new Color(255, 255, 255, 50));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
