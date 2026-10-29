import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.text.*;

public class TestSession extends JFrame {

    private JTextArea moduleTitle;
    private JTextPane questionLabel;
    private TimerPanel timerLabel;
    private JLabel questionTextLabel; 
    private JRadioButton[] options; 
    private QuestionStore store;
    private ButtonGroup group;
    private int currentIndex=0;
    private JButton nextButton, backButton,HighlightButton;
    private boolean highlightMode = false;  
    private Highlighter highlighter;
    private Highlighter.HighlightPainter painter;
    private Users currentUser;
    private JPanel progressBarPanel;
    private JComboBox<String> questionDropdown;
    private JLabel questionNavLabel;

    public TestSession (String module_id, String moduleName, Users user) {
     this.currentUser = user;
     this.setTitle("Redbook");
     this.setSize(1950, 1150);
     this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     ImageIcon icon = new ImageIcon("Images/icon.png");
     setIconImage(icon.getImage());
     setLayout(new BorderLayout(10, 10));
     List<Question> list = QuestionService.getQuestionsByModule(module_id);
     if (list == null || list.isEmpty()) {
         JOptionPane.showMessageDialog(this, "Cannot find in database");
         if (currentUser != null) {
             new Home(currentUser);
         } else {
             dispose();
         }
         return;
        }
     store = new QuestionStore();
     store.addAll(list);
     Question q = store.getQuestion(currentIndex);

     Border text = BorderFactory.createMatteBorder(0, 0, 0, 5, Color.LIGHT_GRAY);
     Border time = BorderFactory.createMatteBorder(2, 2, 2, 2, Color.LIGHT_GRAY);
    
     JPanel UpPanel = new JPanel();
     UpPanel.setLayout(new BorderLayout());
     UpPanel.setOpaque(true);
     UpPanel.setBackground(Color.WHITE);
     UpPanel.setBorder(time);
     UpPanel.setPreferredSize(new Dimension(100, 95));
     add(UpPanel, BorderLayout.NORTH);

     JPanel topContentPanel = new JPanel(new BorderLayout());
     topContentPanel.setBackground(Color.WHITE);
     topContentPanel.setOpaque(true);

     moduleTitle = new JTextArea(moduleName);
     moduleTitle.setLineWrap(true);
     moduleTitle.setWrapStyleWord(true);
     moduleTitle.setEditable(false);
     moduleTitle.setPreferredSize(new Dimension(370, 90));
     moduleTitle.setOpaque(true);  
     moduleTitle.setFont(new Font("Arial", Font.BOLD, 26));
     moduleTitle.setMargin(new Insets(10, 17, 0, 0));
     moduleTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
     topContentPanel.add(moduleTitle, BorderLayout.WEST);

     timerLabel = new TimerPanel(32*60);
     topContentPanel.add(timerLabel, BorderLayout.CENTER);
     timerLabel.setPreferredSize(new Dimension(120, 90));
     timerLabel.setBackground(Color.WHITE);
     timerLabel.startTimer();
     
     UpPanel.add(topContentPanel, BorderLayout.CENTER);

     progressBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 0));
     progressBarPanel.setBackground(Color.WHITE);
     progressBarPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
     progressBarPanel.setPreferredSize(new Dimension(0, 3));
     updateProgressBar();
     UpPanel.add(progressBarPanel, BorderLayout.SOUTH);

     questionLabel = new JTextPane();
     PassageFormatter.show(questionLabel, q.getText());
     questionLabel.setEditable(false);
     questionLabel.setOpaque(true);  
     questionLabel.setFont(new Font("Georgia", Font.PLAIN, 23));
     questionLabel.setMargin(new Insets(60, 80, 70, 100));
     questionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
     JScrollPane scrollPassage = new JScrollPane(questionLabel);
     scrollPassage.setPreferredSize(new Dimension(1050, 700));
     scrollPassage.setBackground(Color.WHITE);
     scrollPassage.setBorder(text);
     add(scrollPassage, BorderLayout.WEST);

     JPanel rightPanel = new JPanel();
     rightPanel.setOpaque(true);
     rightPanel.setBackground(Color.WHITE);
     rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
     rightPanel.add(Box.createVerticalStrut(60));

     questionTextLabel = new JLabel(q.getQuestion());
     questionTextLabel.setFont(new Font("Georgia", Font.PLAIN, 22));
     rightPanel.add(questionTextLabel);

     group = new ButtonGroup();
     options = new JRadioButton[4];

     options[0] = new CustomRadioButton("A",q.getChoiceA());
     options[1] = new CustomRadioButton("B",q.getChoiceB());
     options[2] = new CustomRadioButton("C",q.getChoiceC());
     options[3] = new CustomRadioButton("D",q.getChoiceD());

     rightPanel.add(Box.createVerticalStrut(20));
     for (int i = 0; i < 4; i++) {         
         rightPanel.add(Box.createVerticalStrut(22));
         options[i].setPreferredSize(new Dimension(700, 200));  
         options[i].setMaximumSize(new Dimension(700, 600)); 
         options[i].setFont(new Font("Georgia", Font.PLAIN, 16));
         group.add(options[i]);
         rightPanel.add(options[i]);
        }
     add(rightPanel, BorderLayout.CENTER);
     rightPanel.add(Box.createHorizontalStrut(100));

     JPanel bottomPanel = new JPanel(new BorderLayout());
     bottomPanel.setBackground(Color.WHITE);
     bottomPanel.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createMatteBorder(2, 0, 0, 0, Color.BLACK),
         BorderFactory.createEmptyBorder(8, 0, 8, 0)
     ));

     JPanel blackPanel = new JPanel(new BorderLayout(8, 0));
     blackPanel.setOpaque(false);
     blackPanel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 8));
     
     questionNavLabel = new JLabel("Question " + (currentIndex + 1) + " of " + store.size());
     questionNavLabel.setFont(new Font("Arial", Font.BOLD, 14));
     questionNavLabel.setForeground(Color.WHITE);
     blackPanel.add(questionNavLabel, BorderLayout.CENTER);
     
     JLabel arrowLabel = new JLabel("^");
     arrowLabel.setFont(new Font("Arial", Font.BOLD, 12));
     arrowLabel.setForeground(Color.WHITE);
     arrowLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
     arrowLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
     
     questionDropdown = new JComboBox<>();
     for (int i = 1; i <= store.size(); i++) {
         questionDropdown.addItem("Question " + i);
     }
     questionDropdown.setSelectedIndex(currentIndex);
     questionDropdown.setFont(new Font("Arial", Font.BOLD, 12));
     questionDropdown.setBackground(Color.BLACK);
     questionDropdown.setForeground(Color.WHITE);
     questionDropdown.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
     questionDropdown.setPreferredSize(new Dimension(1, 1));
     questionDropdown.setMaximumSize(new Dimension(1, 1));
     questionDropdown.setFocusable(true);
     questionDropdown.setEnabled(true);
     questionDropdown.addActionListener(e -> {
         int selected = questionDropdown.getSelectedIndex();
         if (selected >= 0 && selected < store.size() && selected != currentIndex) {
             saveCurrentAnswer();
             currentIndex = selected;
             displayQuestion(currentIndex);
             updateQuestionNavigation();
             updateProgressBar();
         }
     });
     
     questionDropdown.setRenderer(new DefaultListCellRenderer() {
         @Override
         public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                 boolean isSelected, boolean cellHasFocus) {
             Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
             c.setBackground(isSelected ? new Color(66, 133, 244) : Color.BLACK);
             c.setForeground(Color.WHITE);
             return c;
         }
     });
     
     JPanel arrowPanel = new JPanel(new BorderLayout());
     arrowPanel.setOpaque(false);
     arrowPanel.add(arrowLabel, BorderLayout.CENTER);
     arrowPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
     
     arrowPanel.addMouseListener(new java.awt.event.MouseAdapter() {
         @Override
         public void mouseClicked(java.awt.event.MouseEvent e) {
             questionDropdown.requestFocusInWindow();
             SwingUtilities.invokeLater(() -> {
                 questionDropdown.showPopup();
             });
         }
     });
     
     blackPanel.add(arrowPanel, BorderLayout.EAST);
     
     JPanel roundedBlackPanel = new JPanel(new BorderLayout()) {
         @Override
         protected void paintComponent(Graphics g) {
             super.paintComponent(g);
             Graphics2D g2 = (Graphics2D) g.create();
             g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
             g2.setColor(Color.BLACK);
             g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
             g2.dispose();
         }
     };
     roundedBlackPanel.setOpaque(false);
     roundedBlackPanel.add(blackPanel, BorderLayout.CENTER);
     roundedBlackPanel.setPreferredSize(new Dimension(220, 32));
     roundedBlackPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
     
     questionDropdown.setBounds(-1000, -1000, 1, 1);
     questionDropdown.setOpaque(false);
     bottomPanel.add(questionDropdown);
     
     MouseAdapter showDropdownListener = new java.awt.event.MouseAdapter() {
         @Override
         public void mouseClicked(java.awt.event.MouseEvent e) {
             // Request focus and show popup
             questionDropdown.requestFocusInWindow();
             SwingUtilities.invokeLater(() -> {
                 questionDropdown.showPopup();
             });
         }
     };
     
     roundedBlackPanel.addMouseListener(showDropdownListener);
     blackPanel.addMouseListener(showDropdownListener);
     questionNavLabel.addMouseListener(showDropdownListener);
     
     JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
     buttonPanel.setBackground(Color.WHITE);
     buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 20));

     backButton = new RoundedButton("← BACK", new Color(66, 133, 244), Color.WHITE);
     nextButton = new RoundedButton("Next", new Color(66, 133, 244), Color.WHITE);

     backButton.setFont(new Font("Arial", Font.BOLD, 14));
     nextButton.setFont(new Font("Arial", Font.BOLD, 14));
     backButton.setPreferredSize(new Dimension(100, 32));
     nextButton.setPreferredSize(new Dimension(100, 32));

     backButton.setFocusPainted(false);
     nextButton.setFocusPainted(false);
     backButton.setEnabled(false);

     buttonPanel.add(backButton);
     buttonPanel.add(nextButton);

     JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
     centerPanel.setBackground(Color.WHITE);
     centerPanel.setOpaque(false);
     centerPanel.add(roundedBlackPanel);
     
     bottomPanel.add(Box.createHorizontalGlue(), BorderLayout.WEST);
     bottomPanel.add(centerPanel, BorderLayout.CENTER);
     bottomPanel.add(buttonPanel, BorderLayout.EAST);

     add(bottomPanel, BorderLayout.SOUTH);

     backButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {  
             saveCurrentAnswer();       
             if (currentIndex > 0) {
                 currentIndex--;
                 displayQuestion(currentIndex);
             }
            }
        });

     nextButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {   
             saveCurrentAnswer();     
             if (currentIndex < store.size() - 1) {
                currentIndex++;
                displayQuestion(currentIndex);
             } 
             else {
                int choice = JOptionPane.showConfirmDialog(
                    TestSession.this,
                    "Bạn đã hoàn thành bài! Xem kết quả chứ?",
                    "Hoàn tất",
                    JOptionPane.YES_NO_OPTION
                );

                if (choice == JOptionPane.YES_OPTION) {
                    saveCurrentAnswer();
                    
                    List<QuestionResult> results = new ArrayList<>();
                    for (Question q : store.getAll()) {
                        String userAnswer = q.getSelectedAnswer();
                        // Handle omitted questions - convert null to "Omitted"
                        if (userAnswer == null || userAnswer.trim().isEmpty()) {
                            userAnswer = "Omitted";
                        }
                        results.add(new QuestionResult(
                            q.getQuestion(),
                            q.getCorrectChoice(),
                            userAnswer
                        ));
                    }

                    // câu sai / bỏ trống vào sổ lỗi sai; câu trong sổ mà lần này làm đúng thì đánh dấu Fixed
                    if (currentUser != null) ErrorLog.of(currentUser).record(moduleName, store.getAll());

                    String testName = "Reading & Writing";
                    String elapsedTime = "00:31:13";

                    new ResultFrame(results, moduleName, testName, elapsedTime, currentUser);

                    dispose();
                }
            }
        }
        });
        ImageIcon highlightIcon = new ImageIcon("Images/highlight.png");
        HighlightButton = new RoundedButton("Highlights", Color.white, Color.WHITE,highlightIcon);
        HighlightButton.setFont(new Font("Arial", Font.BOLD, 16));
        HighlightButton.setForeground(Color.black);
        HighlightButton.setPreferredSize(new Dimension(200, 95));
        UpPanel.add(HighlightButton,BorderLayout.EAST);
        HighlightButton.addActionListener(e -> {
            highlightMode = !highlightMode;
            if (highlightMode) {
               HighlightButton.setText("<html><u>Highlights</u></html>");
               HighlightButton.setBackground(new Color(255, 215, 0));
            } else {
               HighlightButton.setText("Highlights");
               HighlightButton.setBackground(new Color(200, 200, 200));
            }
        });
    
        highlighter = questionLabel.getHighlighter();
        painter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);

        questionLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (!highlightMode) return; 
                String selectedText = questionLabel.getSelectedText();
                if (selectedText != null && !selectedText.isEmpty()) {
                    try {
                        int start = questionLabel.getSelectionStart();
                        int end = questionLabel.getSelectionEnd();
                        highlighter.addHighlight(start, end, painter);
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

     this.getContentPane().setBackground(Color.WHITE);
     this.setVisible(true);

    }


    private void displayQuestion(int index) {
        Question q = store.getQuestion(index);
        PassageFormatter.show(questionLabel, q.getText());
        questionTextLabel.setText(q.getQuestion());
        options[0].setText(q.getChoiceA());
        options[1].setText(q.getChoiceB());
        options[2].setText(q.getChoiceC());
        options[3].setText(q.getChoiceD());
        group.clearSelection();

        if (q.getSelectedAnswer() != null) {
         switch (q.getSelectedAnswer()) {
             case "A": options[0].setSelected(true); break;
             case "B": options[1].setSelected(true); break;
             case "C": options[2].setSelected(true); break;
             case "D": options[3].setSelected(true); break;
       }}
       
       updateQuestionNavigation();
       updateProgressBar();
    }

    
    private void saveCurrentAnswer() {
        Question current = store.getQuestion(currentIndex);
        String selected = null;
        if (options[0].isSelected()) selected = "A";
        else if (options[1].isSelected()) selected = "B";
        else if (options[2].isSelected()) selected = "C";
        else if (options[3].isSelected()) selected = "D";
        current.setSelectedAnswer(selected);
        updateProgressBar();
    }
    
    private void updateProgressBar() {
        progressBarPanel.removeAll();
        int totalQuestions = store.size();
        int segmentWidth = Math.max(1, (1950 - (totalQuestions * 1)) / totalQuestions);
        
        Color lightBrown = new Color(210, 180, 140); 
        Color lightGreen = new Color(144, 238, 144); 
        Color darkBlue = new Color(0, 0, 139);        
        Color lightGray = new Color(211, 211, 211);   
        
        for (int i = 0; i < totalQuestions; i++) {
            Question q = store.getQuestion(i);
            Color segmentColor;
            
            if (i == currentIndex) {
                segmentColor = darkBlue;
            } else if (q.getSelectedAnswer() != null && !q.getSelectedAnswer().isEmpty()) {
                if (i % 2 == 0) {
                    segmentColor = lightBrown;
                } else {
                    segmentColor = lightGreen;
                }
            } else {
                segmentColor = lightGray;
            }
            
            JPanel segment = new JPanel();
            segment.setBackground(segmentColor);
            segment.setPreferredSize(new Dimension(segmentWidth, 3)); 
            progressBarPanel.add(segment);
        }
        progressBarPanel.revalidate();
        progressBarPanel.repaint();
    }
    
    private void updateQuestionNavigation() {
        if (questionDropdown != null) {
            questionDropdown.setSelectedIndex(currentIndex);
        }
        if (questionNavLabel != null) {
            questionNavLabel.setText("Question " + (currentIndex + 1) + " of " + store.size());
        }
        backButton.setEnabled(currentIndex > 0);
        if (currentIndex == store.size() - 1) {
            nextButton.setText("FINISH");
        } else {
            nextButton.setText("NEXT →");
        }
    }
}
