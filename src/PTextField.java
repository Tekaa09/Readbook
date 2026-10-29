import java.awt.*;
import javax.swing.*;

public class PTextField extends JTextField {
    private String placeholder;

    public PTextField(String placeholder) {
        this.placeholder = placeholder;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (getText().isEmpty()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(Color.GRAY);   
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.drawString(placeholder, 4, getHeight() / 2 + getFont().getSize() / 2 );
            g2.dispose();
        }
    }
}
