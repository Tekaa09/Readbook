import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

public class ResultFrame extends JFrame {
    private Users currentUser;
    
    public ResultFrame(List<QuestionResult> results, String moduleName, String testName, String timeSpent, Users user) {
        this.currentUser = user;
        setTitle("Score Details");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1950, 1150);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 245, 245));
        ImageIcon icon = new ImageIcon("Images/icon.png");
        setIconImage(icon.getImage());
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(new Color(245, 245, 245));
        mainContainer.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 245, 245));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        
        JLabel titleLabel = new JLabel("Score Details");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(new Color(40, 40, 40));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        mainContainer.add(headerPanel, BorderLayout.NORTH);

        int correct = (int) results.stream()
                .filter(r -> r.getUserAnswer() != null 
                    && !r.getUserAnswer().equals("Omitted")
                    && r.getUserAnswer().equals(r.getCorrectAnswer()))
                .count();
        int omitted = (int) results.stream()
                .filter(r -> r.getUserAnswer() != null && r.getUserAnswer().equals("Omitted"))
                .count();
        int incorrect = results.size() - correct - omitted;
        int total = results.size();

        JPanel scorePanel = new JPanel(new GridLayout(1, 4, 20, 0));
        scorePanel.setBackground(new Color(245, 245, 245));
        scorePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        scorePanel.add(createScoreCard("Total", String.valueOf(total), new Color(100, 149, 237)));
        scorePanel.add(createScoreCard("Correct", String.valueOf(correct), new Color(76, 175, 80)));
        scorePanel.add(createScoreCard("Incorrect", String.valueOf(incorrect), new Color(244, 67, 54)));
        if (omitted > 0) {
            scorePanel.add(createScoreCard("Omitted", String.valueOf(omitted), new Color(158, 158, 158)));
        } else {
            scorePanel.add(createScoreCard("Omitted", "0", new Color(158, 158, 158)));
        }
        mainContainer.add(scorePanel, BorderLayout.CENTER);
        String[] columns = {"No.", "Questions", "Correct Answer", "Your Answer"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        int i = 1;
        for (QuestionResult r : results) {
            String userAnswer = r.getUserAnswer();
            if (userAnswer == null) {
                userAnswer = "Omitted";
            }
            model.addRow(new Object[]{
                i++, 
                r.getQuestion(), 
                r.getCorrectAnswer(), 
                userAnswer
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(60);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        table.getTableHeader().setBackground(new Color(60, 60, 60));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));
        table.setGridColor(new Color(230, 230, 230));
        table.setShowGrid(true);
        
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(800);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(200);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (column == 3) { // Your Answer column
                    String userAnswer = value != null ? value.toString() : "";
                    String correctAnswer = table.getModel().getValueAt(row, 2).toString();
                    boolean isCorrect = !userAnswer.equals("Omitted") && userAnswer.equals(correctAnswer);
                    
                    if (isCorrect) {
                        c.setForeground(new Color(76, 175, 80)); 
                        c.setFont(new Font("Segoe UI", Font.BOLD, 15));
                    } else if (userAnswer.equals("Omitted")) {
                        c.setForeground(new Color(158, 158, 158)); 
                    } else {
                        c.setForeground(new Color(244, 67, 54)); 
                    }
                } else {
                    c.setForeground(new Color(60, 60, 60));
                }
                
                if (isSelected) {
                    c.setBackground(new Color(240, 248, 255));
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 250, 250));
                }
                
                return c;
            }
        });
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        scrollPane.setBackground(Color.WHITE);
        
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(new Color(245, 245, 245));
        tablePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        
        ImageIcon eyeIcon = new ImageIcon("Images/eye.png");
        RoundedButton backBtn = new RoundedButton("BACK TO HOME", new Color(66, 133, 244), Color.WHITE);
        if (eyeIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            backBtn = new RoundedButton("BACK TO HOME", new Color(66, 133, 244), Color.WHITE, eyeIcon);
        }
        backBtn.setPreferredSize(new Dimension(200, 50));
        backBtn.setFont(new Font("Arial", Font.BOLD, 16));
        backBtn.addActionListener(e -> {
            dispose();
            if (currentUser != null) {
                new Home(currentUser);
            }
        });

        buttonPanel.add(backBtn);

        // câu sai / bỏ trống đã được ghi vào sổ lỗi sai lúc nộp bài
        if (currentUser != null && incorrect + omitted > 0) {
            RoundedButton errorLogBtn = new RoundedButton("OPEN ERROR LOG (" + (incorrect + omitted) + ")",
                new Color(231, 64, 50), Color.WHITE);
            errorLogBtn.setPreferredSize(new Dimension(260, 50));
            errorLogBtn.setFont(new Font("Arial", Font.BOLD, 16));
            errorLogBtn.addActionListener(e -> {
                dispose();
                new Home(currentUser, Home.ERROR_LOG);
            });
            buttonPanel.add(Box.createHorizontalStrut(20));
            buttonPanel.add(errorLogBtn);
        }
        tablePanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainContainer.add(tablePanel, BorderLayout.SOUTH);

        add(mainContainer, BorderLayout.CENTER);
        setVisible(true);
    }
    
    private JPanel createScoreCard(String label, String value, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            BorderFactory.createEmptyBorder(30, 20, 30, 20)
        ));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        labelComponent.setForeground(new Color(120, 120, 120));
        labelComponent.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(labelComponent);
        
        return card;
    }
}
