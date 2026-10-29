import java.awt.*;
import javax.swing.*;

public class CustomRadioButton extends JRadioButton {

    private String labelLetter; // A, B, C, D
    private JTextArea textArea; 

    public CustomRadioButton(String labelLetter, String text) {
        super(text);
        this.labelLetter = labelLetter;
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setBorderPainted(false);
        setFont(new Font("Times New Roman", Font.PLAIN, 20));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        if (isSelected()) {
            g2.setColor(new Color(66, 133, 244)); 
            g2.setStroke(new BasicStroke(2));
        } else {
            g2.setColor(Color.black);
            g2.setStroke(new BasicStroke(1));
        }

        g2.drawRoundRect(5, 5, width-10 , height - 7, 24, 24);

        int circleX = 20;
        int circleY = height / 2 - 15;
        int circleSize = 30;

        g2.setColor(isSelected() ? new Color(66, 133, 244) : Color.WHITE);
        g2.fillOval(circleX, circleY, circleSize, circleSize);

        g2.setColor(isSelected() ?  Color.black : Color.LIGHT_GRAY);
        g2.drawOval(circleX, circleY, circleSize, circleSize);

        g2.setFont(new Font("Arial", Font.BOLD, 19));
        FontMetrics fm = g2.getFontMetrics();
        int letterWidth = fm.stringWidth(labelLetter);
        int letterHeight = fm.getAscent();
        g2.setColor(isSelected() ? Color.WHITE : Color.BLACK);
        g2.drawString(labelLetter, circleX + (circleSize - letterWidth) / 2, circleY + (circleSize + letterHeight) / 2 - 3);


        int textX = 70;
        int textWidth = width - textX -10;
        int lineHeight = 26;
  
        int lineCount = countWrappedLines(g2, getText(), textWidth);        
        int topPadding = height/3+5-lineCount*59/10;
        int totalTextHeight = lineCount * lineHeight-10;
        int startY = topPadding + fm.getAscent() ;

        g2.setFont(new Font("Times New Roman", Font.PLAIN, 21));
        g2.setColor(Color.BLACK);
        drawMultilineText(g2, getText(), textX, startY, textWidth, lineHeight+5);

        g2.dispose();
    }

    private void drawMultilineText(Graphics2D g2, String text, int x, int y, int maxWidth, int lineHeight) {
        FontMetrics fm = g2.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int currentY = y;

        for (String word : words) {
            String testLine = line + word + " ";
            if (fm.stringWidth(testLine) > maxWidth) {
                g2.drawString(line.toString(), x, currentY);
                line = new StringBuilder(word + " ");
                currentY += lineHeight;
            } else {
                line = new StringBuilder(testLine);
            }
        }

        g2.drawString(line.toString(), x, currentY);
    }

    @Override
    public Dimension getPreferredSize() {
        int textLength = getText().length();
        int height = 45 + (textLength / 50) * 30;
        return new Dimension(1000, height);
    }

    private int countWrappedLines(Graphics2D g2, String text, int maxWidth) {
        FontMetrics fm = g2.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int lines = 1;

        for (String word : words) {
            String testLine = line + word + " ";
            if (fm.stringWidth(testLine) > maxWidth) {
                lines++;
                line = new StringBuilder(word + " ");
            } else {
                line = new StringBuilder(testLine);
            }
        }
        return lines;
    }
}
