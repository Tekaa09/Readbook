import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class RoundedButton extends JButton {
    private Color backgroundColor;
    private Color hoverColor;
    private Color pressedColor;
    private Color textColor;

    private boolean hovered = false;
    private boolean pressed = false;

    public RoundedButton(String text, Color backgroundColor, Color textColor) {
        super(text);
        this.backgroundColor = backgroundColor;
        this.hoverColor = backgroundColor.darker(); 
        this.pressedColor = backgroundColor.darker(); 
        this.textColor = textColor;
        SetUpBaseStyle();
        SetUpTextPosition();
        SetUpMouseEffect();
    }

    public RoundedButton(String text, Color backgroundColor, Color textColor,ImageIcon icon) {
        super(text,icon);
        this.backgroundColor = backgroundColor;
        this.hoverColor = backgroundColor.darker(); 
        this.pressedColor = backgroundColor.darker(); 
        this.textColor = textColor;
        SetUpBaseStyle();
        SetUpTextPosition();
        SetUpMouseEffect();
    }

    private void SetUpTextPosition() {
    if (getIcon() != null) {
     setHorizontalTextPosition(SwingConstants.CENTER);
     setVerticalTextPosition(SwingConstants.BOTTOM);
    }
    else {
     setHorizontalAlignment(SwingConstants.CENTER);
     setVerticalAlignment(SwingConstants.CENTER);
    }
    } 

    private void SetUpBaseStyle() {
        setFocusPainted(false);
        setForeground(textColor);
        setFont(new Font("Segoe UI", Font.BOLD, 16));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(140, 50));
    }

    private void SetUpMouseEffect() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                pressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressed = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int arc = 43; 
        int shadowOffset = 20; 
        int shadowAlpha = 20; 

        Color shadowColor = new Color(0, 0, 0, shadowAlpha);
        g2.setColor(shadowColor);
        g2.fillRoundRect(shadowOffset, shadowOffset, getWidth() - shadowOffset, getHeight() - shadowOffset, arc, arc);
    
        Color fillColor;
        if (pressed) fillColor = pressedColor;
        else if (hovered) fillColor = hoverColor;
        else fillColor = backgroundColor;

        g2.setColor(fillColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 42, 42);

        //g2.setColor(textColor);
        //FontMetrics fm = g2.getFontMetrics();
        //int x = (getWidth() - fm.stringWidth(getText())) / 2;
        //int y = (getHeight() + fm.getAscent()) / 2 - 3;
        //g2.drawString(getText(), x, y);

        g2.dispose();

        super.paintComponent(g);


    }

    @Override
    public void paintBorder(Graphics g) {
    }
}
