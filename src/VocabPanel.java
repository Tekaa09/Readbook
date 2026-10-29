import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** Trang Vocab (mục trên thanh menu): thẻ ghi nhớ (Space lật thẻ, 1 = chưa biết, 2 = đã biết) và danh sách từ. */
public class VocabPanel extends JPanel {
    private static final Color RED = new Color(231, 64, 50);
    private static final Color GREEN = new Color(46, 160, 67);
    private static final Color ORANGE = new Color(220, 130, 20);
    private static final String[] TYPES = {"n", "v", "adj", "adv", "phrase", "other"};

    private final VocabBook book;
    private final List<Module> modules = ModuleService.getAllModules();
    private final JTabbedPane tabs = new JTabbedPane();

    // thẻ ghi nhớ
    private List<Vocab> queue = new ArrayList<>();
    private int index;
    private boolean revealed;
    private final JLabel statsLabel = new JLabel();
    private final JLabel progressLabel = new JLabel();
    private final JLabel wordLabel = new JLabel("", JLabel.CENTER);
    private final JLabel typeLabel = new JLabel("", JLabel.CENTER);
    private final JLabel meaningLabel = new JLabel("", JLabel.CENTER);
    private final RoundedButton showBtn = new RoundedButton("SHOW MEANING  (Space)", new Color(66, 133, 244), Color.WHITE);
    private final RoundedButton againBtn = new RoundedButton("NOT YET  (1)", RED, Color.WHITE);
    private final RoundedButton knowBtn = new RoundedButton("I KNOW IT  (2)", GREEN, Color.WHITE);
    private final RoundedButton practiceAllBtn = new RoundedButton("PRACTICE ALL WORDS", new Color(120, 120, 120), Color.WHITE);

    // danh sách từ
    private final JTextField searchField = new JTextField(22);
    private final DefaultTableModel listModel = new DefaultTableModel(
        new String[]{"Word", "Type", "Meaning (Vietnamese)", "Meaning (English)", "Status", "Source"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable listTable = new JTable(listModel);
    private final JLabel detailLabel = new JLabel();
    private final RoundedButton deleteBtn = new RoundedButton("DELETE", new Color(150, 150, 150), Color.WHITE);
    private List<Vocab> listed = new ArrayList<>();

    public VocabPanel(Users user) {
        book = VocabBook.of(user);
        setLayout(new BorderLayout());
        add(header(), BorderLayout.NORTH);

        tabs.setFont(new Font("Arial", Font.BOLD, 14));
        tabs.addTab("Flashcards", flashcardPanel());
        tabs.addTab("Word list", listPanel());
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 1) refreshList();
        });
        add(tabs, BorderLayout.CENTER);

        bindKey(KeyEvent.VK_SPACE, this::reveal);
        bindKey(KeyEvent.VK_1, () -> answer(false));
        bindKey(KeyEvent.VK_NUMPAD1, () -> answer(false));
        bindKey(KeyEvent.VK_2, () -> answer(true));
        bindKey(KeyEvent.VK_NUMPAD2, () -> answer(true));

        startSession(false);
    }

    /** Mở lại trang này: cập nhật số liệu, hết thẻ thì lấy các từ mới đến hạn. */
    public void reload() {
        if (index >= queue.size()) startSession(false);
        else updateStats();
        if (tabs.getSelectedIndex() == 1) refreshList();
    }

    private JPanel header() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(225, 225, 225)),
            BorderFactory.createEmptyBorder(14, 30, 14, 30)));
        JLabel title = new JLabel("Vocabulary");
        title.setForeground(new Color(40, 40, 40));
        title.setFont(new Font("Arial", Font.BOLD, 26));
        statsLabel.setForeground(Color.GRAY);
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        bar.add(title, BorderLayout.WEST);
        bar.add(statsLabel, BorderLayout.EAST);
        return bar;
    }

    // ================================================================ thẻ ghi nhớ

    private JPanel flashcardPanel() {
        wordLabel.setFont(new Font("Georgia", Font.BOLD, 46));
        typeLabel.setFont(new Font("Georgia", Font.ITALIC, 20));
        typeLabel.setForeground(Color.GRAY);
        meaningLabel.setFont(new Font("Segoe UI", Font.PLAIN, 19));
        meaningLabel.setVerticalAlignment(JLabel.TOP);
        progressLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        progressLabel.setForeground(Color.GRAY);
        progressLabel.setHorizontalAlignment(JLabel.CENTER);

        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
                g2.setColor(new Color(215, 215, 215));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 30, 30);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(40, 40, 30, 40));
        card.setPreferredSize(new Dimension(760, 400));
        JPanel top = new JPanel(new GridLayout(2, 1));
        top.setOpaque(false);
        top.add(wordLabel);
        top.add(typeLabel);
        card.add(top, BorderLayout.NORTH);
        card.add(meaningLabel, BorderLayout.CENTER);

        for (RoundedButton b : new RoundedButton[]{showBtn, againBtn, knowBtn, practiceAllBtn}) {
            b.setFont(new Font("Arial", Font.BOLD, 15));
            b.setPreferredSize(new Dimension(b == showBtn ? 260 : 200, 48));
            b.setFocusable(false);   // để phím Space lật thẻ, không bấm nhầm nút đang được chọn
        }
        showBtn.addActionListener(e -> reveal());
        againBtn.addActionListener(e -> answer(false));
        knowBtn.addActionListener(e -> answer(true));
        practiceAllBtn.addActionListener(e -> startSession(true));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        buttons.setOpaque(false);
        buttons.add(showBtn);
        buttons.add(againBtn);
        buttons.add(knowBtn);
        buttons.add(practiceAllBtn);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(new Color(245, 245, 245));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        center.add(progressLabel, c);
        c.gridy = 1;
        c.insets = new Insets(12, 0, 24, 0);
        center.add(card, c);
        c.gridy = 2;
        c.insets = new Insets(0, 0, 0, 0);
        center.add(buttons, c);
        return center;
    }

    /** Bắt đầu lượt học: các từ đến hạn hôm nay, hoặc tất cả các từ (xáo trộn). */
    private void startSession(boolean all) {
        queue = all ? new ArrayList<>(book.words()) : book.dueToday();
        if (all) Collections.shuffle(queue);
        index = 0;
        showCard();
    }

    private void showCard() {
        updateStats();
        revealed = false;
        meaningLabel.setText("");

        if (index >= queue.size()) {
            LocalDate next = book.nextDue();
            wordLabel.setText(book.words().isEmpty() ? "No words yet" : "All done for today!");
            typeLabel.setText(book.words().isEmpty()
                ? "Add words in the Word list tab."
                : next == null ? "" : "Next review: " + next);
            progressLabel.setText(queue.isEmpty() ? " " : "You went through " + queue.size() + " cards.");
            setButtons(false, false, !book.words().isEmpty());
            return;
        }

        Vocab v = queue.get(index);
        wordLabel.setText(v.getWord());
        typeLabel.setText(v.getPartOfSpeech().isEmpty() ? " " : "(" + v.getPartOfSpeech() + ")");
        progressLabel.setText("Card " + (index + 1) + " of " + queue.size() + "   ·   " + book.status(v)
            + (book.box(v) > 0 ? " (box " + book.box(v) + " of " + VocabBook.MAX_BOX + ")" : ""));
        setButtons(true, false, false);
    }

    private void reveal() {
        if (!isShowing() || tabs.getSelectedIndex() != 0 || index >= queue.size() || revealed) return;
        revealed = true;
        Vocab v = queue.get(index);
        meaningLabel.setText(Html.block(describe(v, true), 640, "text-align:center"));
        setButtons(false, true, false);
    }

    private void answer(boolean known) {
        if (!isShowing() || tabs.getSelectedIndex() != 0 || index >= queue.size() || !revealed) return;
        Vocab v = queue.get(index);
        book.answer(v, known);
        if (!known) queue.add(v);   // chưa nhớ: gặp lại cuối lượt này
        index++;
        showCard();
    }

    private void setButtons(boolean show, boolean answer, boolean practiceAll) {
        showBtn.setVisible(show);
        againBtn.setVisible(answer);
        knowBtn.setVisible(answer);
        practiceAllBtn.setVisible(practiceAll);
    }

    private void updateStats() {
        statsLabel.setText(book.words().size() + " words  ·  " + book.dueToday().size() + " due today  ·  "
            + book.count(VocabBook.LEARNING) + " learning  ·  " + book.count(VocabBook.MASTERED) + " mastered");
    }

    // ================================================================ danh sách từ

    private JPanel listPanel() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refreshList(); }
            public void removeUpdate(DocumentEvent e) { refreshList(); }
            public void changedUpdate(DocumentEvent e) { refreshList(); }
        });

        RoundedButton addBtn = new RoundedButton("+ ADD WORD", GREEN, Color.WHITE);
        addBtn.setFont(new Font("Arial", Font.BOLD, 13));
        addBtn.setPreferredSize(new Dimension(130, 36));
        addBtn.addActionListener(e -> addWord());
        deleteBtn.setFont(new Font("Arial", Font.BOLD, 13));
        deleteBtn.setPreferredSize(new Dimension(110, 36));
        deleteBtn.addActionListener(e -> deleteWord());

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        search.setOpaque(false);
        search.add(new JLabel("Search:"));
        search.add(searchField);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(addBtn);
        actions.add(deleteBtn);
        top.add(search, BorderLayout.WEST);
        top.add(actions, BorderLayout.EAST);

        listTable.setRowHeight(28);
        listTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        listTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        listTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] widths = {150, 60, 260, 380, 90, 70};
        for (int i = 0; i < widths.length; i++) listTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        listTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                                                           boolean focus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, selected, focus, row, column);
                String s = String.valueOf(value);
                c.setForeground(column != 4 ? Color.DARK_GRAY
                    : VocabBook.MASTERED.equals(s) ? GREEN : VocabBook.LEARNING.equals(s) ? ORANGE : Color.GRAY);
                return c;
            }
        });
        listTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showDetail();
        });

        detailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        detailLabel.setVerticalAlignment(JLabel.TOP);
        detailLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        detailLabel.setPreferredSize(new Dimension(0, 150));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(listTable), BorderLayout.CENTER);
        panel.add(detailLabel, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshList() {
        String query = searchField.getText().trim().toLowerCase();
        listed = new ArrayList<>();
        listModel.setRowCount(0);
        for (Vocab v : book.words()) {
            String all = (v.getWord() + " " + v.getMeaningVi() + " " + v.getMeaningEn()).toLowerCase();
            if (!query.isEmpty() && !all.contains(query)) continue;
            listed.add(v);
            listModel.addRow(new Object[]{v.getWord(), v.getPartOfSpeech(), v.getMeaningVi(), v.getMeaningEn(),
                book.status(v), v.isOwn() ? "Mine" : "Test"});
        }
        if (!listed.isEmpty()) listTable.setRowSelectionInterval(0, 0);
        showDetail();
        updateStats();
    }

    private void showDetail() {
        int row = listTable.getSelectedRow();
        Vocab v = row >= 0 && row < listed.size() ? listed.get(row) : null;
        deleteBtn.setVisible(v != null && v.isOwn());   // chỉ xoá được từ tự thêm
        detailLabel.setText(v == null ? "" : "<html><b style='font-size:15px'>" + Html.escape(v.getWord()) + "</b>"
            + (v.getPartOfSpeech().isEmpty() ? "" : " <i>(" + Html.escape(v.getPartOfSpeech()) + ")</i>")
            + "<br>" + describe(v, false) + "</html>");
    }

    private void addWord() {
        JTextField word = new JTextField(24);
        JComboBox<String> type = new JComboBox<>(TYPES);
        JTextField vi = new JTextField(24);
        JTextField en = new JTextField(24);
        JTextField example = new JTextField(24);
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 6));
        form.add(new JLabel("Word:"));
        form.add(word);
        form.add(new JLabel("Type:"));
        form.add(type);
        form.add(new JLabel("Meaning (Vietnamese):"));
        form.add(vi);
        form.add(new JLabel("Meaning (English):"));
        form.add(en);
        form.add(new JLabel("Example (optional):"));
        form.add(example);

        while (true) {
            int answer = JOptionPane.showConfirmDialog(this, form, "Add a word", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) return;
            if (vi.getText().trim().isEmpty() && en.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a meaning (Vietnamese or English).");
                continue;
            }
            try {
                book.addOwn(word.getText(), String.valueOf(type.getSelectedItem()), en.getText(), vi.getText(),
                    example.getText());
                break;
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Cannot save the word: " + ex.getMessage());
                return;
            }
        }
        searchField.setText("");
        refreshList();
        selectWord(word.getText().trim());
    }

    private void deleteWord() {
        int row = listTable.getSelectedRow();
        if (row < 0 || row >= listed.size() || !listed.get(row).isOwn()) return;
        Vocab v = listed.get(row);
        int answer = JOptionPane.showConfirmDialog(this, "Delete \"" + v.getWord() + "\" from your list?",
            "Delete word", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) return;
        try {
            book.removeOwn(v);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Cannot delete the word: " + ex.getMessage());
        }
        refreshList();
    }

    private void selectWord(String word) {
        for (int i = 0; i < listed.size(); i++) {
            if (listed.get(i).getWord().equalsIgnoreCase(word)) {
                listTable.setRowSelectionInterval(i, i);
                listTable.scrollRectToVisible(listTable.getCellRect(i, 0, true));
                return;
            }
        }
    }

    // ================================================================ chung

    /** Nghĩa tiếng Việt, tiếng Anh, ví dụ và câu hỏi có từ này. */
    private String describe(Vocab v, boolean large) {
        StringBuilder s = new StringBuilder();
        if (!v.getMeaningVi().isEmpty()) {
            s.append("<b>").append(Html.escape(v.getMeaningVi())).append("</b><br>");
        }
        if (!v.getMeaningEn().isEmpty()) s.append(Html.escape(v.getMeaningEn())).append("<br>");
        if (!v.getExample().isEmpty()) {
            s.append(large ? "<br>" : "").append("<i>“").append(Html.escape(v.getExample())).append("”</i><br>");
        }
        String source = source(v);
        if (!source.isEmpty()) {
            s.append("<br><font color='#888888'>").append(Html.escape(source)).append("</font>");
        }
        return s.toString();
    }

    /** "Seen in: March 2023 Form A Module 1, question 4" (question_id = số câu + mã module). */
    private String source(Vocab v) {
        if (v.getQuestionId() <= 0) return "";
        String id = String.valueOf(v.getQuestionId());
        for (Module m : modules) {
            String moduleId = m.getModuleId();
            if (id.length() > moduleId.length() && id.endsWith(moduleId)) {
                return "Seen in: " + m.getModuleName().trim() + ", question " + id.substring(0, id.length() - moduleId.length());
            }
        }
        return "";
    }

    private void bindKey(int key, Runnable action) {
        String name = "vocab-key-" + key;
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key, 0), name);
        getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }
}
