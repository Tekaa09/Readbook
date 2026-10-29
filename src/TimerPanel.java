import java.awt.*;
import javax.swing.*;

public class TimerPanel extends JPanel {
    private JLabel timerLabel,label;
    private int timeLeft;

    public TimerPanel(int seconds) {
        this.timeLeft = seconds;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel timePanel = new JPanel();
        timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.Y_AXIS));
        timePanel.setBackground(Color.WHITE);

        label = new JLabel("Count Down");
        label.setFont(new Font("Segoe UI", Font.BOLD,23));
        label.setForeground(Color.lightGray);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        timerLabel = new JLabel(formatTime(timeLeft));
        timerLabel.setFont(new Font("Arial", Font.BOLD, 23));
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        timePanel.add(Box.createVerticalStrut(12));
        timePanel.add(label);
        timePanel.add(Box.createVerticalStrut(10));
        timePanel.add(timerLabel);

        add(timePanel, BorderLayout.CENTER);
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void startTimer() {
        new javax.swing.Timer(1000, e -> {
            timerLabel.setText(formatTime(timeLeft));
            timeLeft--;
            if (timeLeft < 0) {
                ((javax.swing.Timer) e.getSource()).stop();
                System.exit(0);
            }
        }).start();
    }
}
