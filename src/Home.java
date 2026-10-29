import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.Border;

public class Home extends JFrame {
    // các mục trên thanh menu
    public static final String HOME = "home";
    public static final String ERROR_LOG = "errors";
    public static final String VOCAB = "vocab";

    private JPanel panel;
    private Users currentUser;
    private final CardLayout pages = new CardLayout();
    private final JPanel pageArea = new JPanel(pages);
    private final java.util.Map<String, NavItem> navItems = new java.util.LinkedHashMap<>();
    private ErrorLogPanel errorLogPage;
    private VocabPanel vocabPage;

    public Home(Users user) {
        this(user, HOME);
    }

    /** Mở trang chủ ở một mục trên thanh menu (vd. ERROR_LOG sau khi nộp bài). */
    public Home(Users user, String page) {
        this.currentUser = user; 
        ImageIcon icon = new ImageIcon("Images/icon.png");
        ImageIcon logo = new ImageIcon("Images/LOGO.png");
        setIconImage(icon.getImage());

        JLabel textLabel = new JLabel(" Redbook",logo, JLabel.LEFT);
        textLabel.setForeground(Color.WHITE);
        textLabel.setFont(new Font("Arial", Font.BOLD, 32)); // Arial Bold
        textLabel.setBounds(120, 10, 200, 70);

        Border underline = BorderFactory.createMatteBorder(0, 0, 2, 0, Color.LIGHT_GRAY);
        //Border module_underline = BorderFactory.createMatteBorder(1, 1, 2, 1, Color.LIGHT_GRAY);

        panel = new JPanel();   
        panel.setLayout(null);     
        panel.setBorder(underline);
        panel.setBackground(new Color(231, 64, 50));
        panel.setBounds(0, 0, 1700, 90);      
        panel.add(textLabel);

        JPanel MainPanel = new JPanel();       
        MainPanel.setLayout(new BorderLayout());
        MainPanel.setBackground(Color.WHITE);
        MainPanel.setPreferredSize(new Dimension(1600, 900));
        MainPanel.setBounds(0, 90, 1700, 880);

        int cardWidth = 250;
        int cardHeight = 300;
        int horizontalGap = 30;
        int verticalGap = 40;
        int cardsPerRow = 4;

        int totalRows = (int) Math.ceil((double) 4 / cardsPerRow);
        int width = cardsPerRow * (cardWidth + horizontalGap);
        int height = totalRows * (cardHeight + verticalGap);
        if (currentUser.canEditQuestions() && AppConfig.hasAdminTools()) {
            AdminTools.addButton(this, panel);
        }

        // thanh menu: HOME (danh sách đề), ERROR LOG (sổ lỗi sai), VOCAB (học từ vựng)
        addNavItem(HOME, "HOME", 370, 110);
        addNavItem(ERROR_LOG, "ERROR LOG", 492, 196);
        addNavItem(VOCAB, "VOCAB", 700, 116);
        updateErrorCount();

        JLabel userLabel = new JLabel(currentUser.getUsername() + "  (" + currentUser.getRoleName() + ")", JLabel.RIGHT);
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        userLabel.setBounds(890, 30, 410, 30);
        panel.add(userLabel);

        RoundedButton logoutBtn = new RoundedButton("LOG OUT", Color.WHITE, new Color(231,64,50));
        logoutBtn.setBounds(1540, 20, 130, 50);
        logoutBtn.setFont(new Font("Arial", Font.BOLD, 17));
        logoutBtn.addActionListener(e -> {
            this.dispose();
            new Login();
        });
        panel.add(logoutBtn);
      
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 30, 40));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setPreferredSize(new Dimension(width, height));

        List<Module> modules = ModuleService.getAllModules();
        ModuleStore.setModules(modules);

        for (Module m : modules) {
            JPanel card = createModuleCard(m);
            contentPanel.add(card);
        }

        String loadError = OfflineData.isActive() ? OfflineData.error() : DatabaseConnection.lastError();
        if (modules.isEmpty() && loadError != null) {
            JLabel error = new JLabel("<html>Cannot load the modules.<br>" + loadError + "</html>");
            error.setFont(new Font("Arial", Font.PLAIN, 18));
            error.setForeground(new Color(231, 64, 50));
            error.setPreferredSize(new Dimension(700, 60));

            RoundedButton retryBtn = new RoundedButton("TRY AGAIN", new Color(231, 64, 50), Color.WHITE);
            retryBtn.setPreferredSize(new Dimension(160, 45));
            retryBtn.addActionListener(e -> {
                this.dispose();
                new Home(currentUser);
            });

            contentPanel.add(error);
            contentPanel.add(retryBtn);
        }

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        MainPanel.add(scrollPane, BorderLayout.EAST);

        pageArea.setBounds(0, 90, 1700, 880);
        pageArea.add(MainPanel, HOME);
        this.add(pageArea);
        // vùng nội dung lấp đầy phần còn lại của cửa sổ (không bị cắt mất hàng nút dưới cùng)
        getContentPane().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                pageArea.setBounds(0, 90, getContentPane().getWidth(), getContentPane().getHeight() - 90);
                pageArea.revalidate();
            }
        });
        this.setLayout(null);
        this.add(panel);
        //this.add(welcome);
        this.getContentPane().setBackground(Color.WHITE);
        this.setTitle("Redbook");
        this.setSize(1700, 1000);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        showPage(page);
        this.setVisible(true);
        setVisible(true);
        }

    private void addNavItem(String page, String text, int x, int width) {
        NavItem item = new NavItem(text);
        item.setBounds(x, 25, width, 40);
        item.addActionListener(e -> showPage(page));
        navItems.put(page, item);
        panel.add(item);
    }

    /** Đổi nội dung bên dưới thanh menu; trang Error Log / Vocab chỉ tạo khi mở lần đầu. */
    private void showPage(String page) {
        if (ERROR_LOG.equals(page)) {
            if (errorLogPage == null) {
                errorLogPage = new ErrorLogPanel(currentUser, this::updateErrorCount);
                pageArea.add(errorLogPage, ERROR_LOG);
            } else {
                errorLogPage.reload();
            }
        } else if (VOCAB.equals(page)) {
            if (vocabPage == null) {
                vocabPage = new VocabPanel(currentUser);
                pageArea.add(vocabPage, VOCAB);
            } else {
                vocabPage.reload();
            }
        } else {
            page = HOME;
        }
        pages.show(pageArea, page);
        for (java.util.Map.Entry<String, NavItem> item : navItems.entrySet()) {
            item.getValue().setCurrent(item.getKey().equals(page));
        }
        updateErrorCount();
    }

    /** Số câu chưa xem lại trong sổ lỗi sai, hiện cạnh chữ ERROR LOG. */
    private void updateErrorCount() {
        NavItem item = navItems.get(ERROR_LOG);
        if (item == null) return;
        int toReview = ErrorLog.of(currentUser).countToReview();
        item.setText(toReview > 0 ? "ERROR LOG (" + toReview + ")" : "ERROR LOG");
    }


    
    private JPanel createModuleCard(Module m) {
        CardPanel card = new CardPanel();
        card.setPreferredSize(new Dimension(320, 350));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);

        JTextArea title = new JTextArea(m.getModuleName());
        title.setLineWrap(true);
        title.setWrapStyleWord(true);
        title.setEditable(false);
        title.setOpaque(false);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setMaximumSize(new Dimension(220, 50));

        //STATUS
        //JLabel status = new JLabel(m.getStatus());
        //status.setFont(new Font("Arial", Font.PLAIN, 14));
        //status.setForeground(Color.WHITE);
        //status.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel infoRow = new JPanel();
        infoRow.setLayout(new BoxLayout(infoRow, BoxLayout.X_AXIS));
        infoRow.setOpaque(false);

        ImageIcon clockIcon = new ImageIcon("Images/clock.png");
        JLabel info = new JLabel(" 32 minutes  •", clockIcon, JLabel.LEFT);
        info.setForeground(Color.WHITE);
        info.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        ImageIcon bookIcon = new ImageIcon("Images/book.png");
        JLabel info2 = new JLabel(" 27 questions", bookIcon, JLabel.LEFT);
        info2.setForeground(Color.WHITE);
        info2.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        infoRow.setMaximumSize(new Dimension(270, 20));
        infoRow.add(info);
        infoRow.add(Box.createHorizontalStrut(15));
        infoRow.add(info2);

        JButton startButton = new RoundedButton("START PRACTICE", new Color(231, 64, 50), Color.WHITE);
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.setMaximumSize(new Dimension(200, 40));

        startButton.addActionListener(e -> {
            //System.out.println("Starting module: " + store.getModuleById("152024"));
            this.dispose();
            new TestSession(m.getModuleId(), m.getModuleName(), currentUser); 
        });

        card.add(Box.createVerticalStrut(25));
        card.add(title);
        card.add(Box.createVerticalStrut(7));
        card.add(infoRow);
        //card.add(status);
        card.add(Box.createVerticalStrut(35));
        card.add(startButton);

        return card;        
        
    }
}
