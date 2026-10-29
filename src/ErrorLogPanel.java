import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** Trang Error Log (mục trên thanh menu): câu đã làm sai, xem lại câu hỏi kèm đáp án, ghi lý do sai và ghi chú. */
public class ErrorLogPanel extends JPanel {
    private static final Color RED = new Color(231, 64, 50);
    private static final Color GREEN = new Color(46, 160, 67);
    private static final Color ORANGE = new Color(220, 130, 20);
    private static final String ALL = "All";

    private final Users user;
    private final Runnable onChange;   // báo trang chủ cập nhật số câu cần xem lại
    private ErrorLog log;
    private JScrollPane detailScroll;
    private final Map<String, List<Question>> questionCache = new HashMap<>();
    private final Map<String, String> moduleNames = new HashMap<>();   // mã module -> tên hiện tại
    private List<ErrorLog.Entry> shown = new ArrayList<>();
    private ErrorLog.Entry current;

    private final DefaultTableModel model = new DefaultTableModel(
        new String[]{"Date", "Module", "Q", "Yours", "Correct", "Wrong", "Reason", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JComboBox<String> moduleFilter = new JComboBox<>();
    private final JComboBox<String> reasonFilter = new JComboBox<>();
    private final JComboBox<String> statusFilter = new JComboBox<>(
        new String[]{ALL, ErrorLog.TO_REVIEW, ErrorLog.REVIEWED, ErrorLog.FIXED});
    private final JLabel summary = new JLabel();

    private final JLabel infoLabel = new JLabel();
    private final JTextPane passage = new JTextPane();
    private final JLabel questionLabel = new JLabel();
    private final JLabel[] choiceLabels = new JLabel[4];
    private final JComboBox<String> reasonBox = new JComboBox<>(ErrorLog.REASONS);
    private final JTextArea noteArea = new JTextArea(3, 40);
    private final JPanel detail = new JPanel(new BorderLayout());
    private final JLabel emptyLabel = new JLabel();

    public ErrorLogPanel(Users user, Runnable onChange) {
        this.user = user;
        this.onChange = onChange;
        log = ErrorLog.of(user);
        for (Module m : ModuleService.getAllModules()) moduleNames.put(m.getModuleId(), m.getModuleName().trim());
        setLayout(new BorderLayout());

        add(header(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel(), detailPanel());
        split.setDividerLocation(800);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);

        reasonFilter.addItem(ALL);
        for (String r : ErrorLog.REASONS) reasonFilter.addItem(r);
        moduleFilter.addActionListener(e -> refresh());
        reasonFilter.addActionListener(e -> refresh());
        statusFilter.addActionListener(e -> refresh());

        refresh();
    }

    /** Mở lại trang này: nếu vừa nộp bài thì đọc lại sổ. */
    public void reload() {
        if (log.isStale()) refresh();
    }

    private JPanel header() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(225, 225, 225)),
            BorderFactory.createEmptyBorder(14, 30, 14, 30)));
        JLabel title = new JLabel("Error Log");
        title.setForeground(new Color(40, 40, 40));
        title.setFont(new Font("Arial", Font.BOLD, 26));
        summary.setForeground(Color.GRAY);
        summary.setFont(new Font("Arial", Font.PLAIN, 16));
        bar.add(title, BorderLayout.WEST);
        bar.add(summary, BorderLayout.EAST);
        return bar;
    }

    private JPanel listPanel() {
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        filters.setBackground(Color.WHITE);
        filters.add(new JLabel("Module:"));
        filters.add(moduleFilter);
        filters.add(new JLabel("Reason:"));
        filters.add(reasonFilter);
        filters.add(new JLabel("Status:"));
        filters.add(statusFilter);

        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] widths = {95, 190, 35, 65, 70, 55, 140, 90};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                                                           boolean focus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, selected, focus, row, column);
                c.setForeground(column == 7 ? statusColor(String.valueOf(value))
                              : column == 3 ? RED
                              : column == 4 ? GREEN : Color.DARK_GRAY);
                return c;
            }
        });
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = table.getSelectedRow();
            show(row >= 0 && row < shown.size() ? shown.get(row) : null);
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.add(filters, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JComponent detailPanel() {
        infoLabel.setFont(new Font("Arial", Font.BOLD, 15));
        infoLabel.setBorder(BorderFactory.createEmptyBorder(14, 20, 6, 20));

        passage.setEditable(false);
        passage.setFont(new Font("Georgia", Font.PLAIN, 18));
        passage.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        passage.setAlignmentX(Component.LEFT_ALIGNMENT);
        questionLabel.setFont(new Font("Georgia", Font.BOLD, 17));
        questionLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 8, 20));
        questionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel body = new WidthTrackingPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.add(passage);
        body.add(questionLabel);
        for (int i = 0; i < 4; i++) {
            choiceLabels[i] = new JLabel();
            choiceLabels[i].setFont(new Font("Georgia", Font.PLAIN, 16));
            choiceLabels[i].setOpaque(true);
            choiceLabels[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            JPanel wrap = new JPanel(new BorderLayout());
            wrap.setBackground(Color.WHITE);
            wrap.setBorder(BorderFactory.createEmptyBorder(4, 20, 4, 20));
            wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
            wrap.add(choiceLabels[i], BorderLayout.CENTER);
            body.add(wrap);
        }
        JScrollPane scroll = new JScrollPane(body);
        detailScroll = scroll;
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);
        // đổi cỡ cửa sổ thì dàn lại chữ của câu đang xem
        scroll.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                if (current != null) layoutText(find(current), current);
            }
        });
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        RoundedButton saveBtn = new RoundedButton("SAVE NOTE", new Color(66, 133, 244), Color.WHITE);
        RoundedButton reviewedBtn = new RoundedButton("I UNDERSTAND IT", GREEN, Color.WHITE);
        RoundedButton removeBtn = new RoundedButton("REMOVE", new Color(150, 150, 150), Color.WHITE);
        for (RoundedButton b : new RoundedButton[]{saveBtn, reviewedBtn, removeBtn}) {
            b.setFont(new Font("Arial", Font.BOLD, 13));
            b.setPreferredSize(new Dimension(b == reviewedBtn ? 170 : 120, 38));
        }
        saveBtn.addActionListener(e -> save(null));
        reviewedBtn.addActionListener(e -> save(ErrorLog.REVIEWED));
        removeBtn.addActionListener(e -> remove());

        JPanel noteRow = new JPanel(new BorderLayout(10, 6));
        noteRow.setBackground(new Color(248, 248, 248));
        noteRow.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(10, 20, 12, 20)));
        JPanel reasonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        reasonRow.setOpaque(false);
        reasonRow.add(new JLabel("Why did you miss it?"));
        reasonRow.add(reasonBox);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(saveBtn);
        buttons.add(reviewedBtn);
        buttons.add(removeBtn);
        noteRow.add(reasonRow, BorderLayout.NORTH);
        noteRow.add(new JScrollPane(noteArea), BorderLayout.CENTER);
        noteRow.add(buttons, BorderLayout.SOUTH);

        detail.setBackground(Color.WHITE);
        detail.add(infoLabel, BorderLayout.NORTH);
        detail.add(scroll, BorderLayout.CENTER);
        detail.add(noteRow, BorderLayout.SOUTH);

        emptyLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        emptyLabel.setForeground(Color.GRAY);
        emptyLabel.setHorizontalAlignment(JLabel.CENTER);

        JPanel holder = new JPanel(new CardLayout());
        holder.add(detail, "detail");
        holder.add(emptyLabel, "empty");
        return holder;
    }

    /** Đọc lại sổ, áp bộ lọc, giữ câu đang chọn. */
    private void refresh() {
        int keep = current == null ? -1 : current.questionId;
        if (log.isStale()) log = ErrorLog.of(user);
        List<ErrorLog.Entry> all = log.entries();

        Set<String> modules = new LinkedHashSet<>();
        for (ErrorLog.Entry e : all) modules.add(moduleName(e));
        Object selectedModule = moduleFilter.getSelectedItem();
        if (moduleFilter.getItemCount() != modules.size() + 1) {
            ActionListenerGuard.run(moduleFilter, () -> {
                moduleFilter.removeAllItems();
                moduleFilter.addItem(ALL);
                for (String m : modules) moduleFilter.addItem(m);
                moduleFilter.setSelectedItem(selectedModule == null ? ALL : selectedModule);
            });
        }

        String module = String.valueOf(moduleFilter.getSelectedItem());
        String reason = String.valueOf(reasonFilter.getSelectedItem());
        String status = String.valueOf(statusFilter.getSelectedItem());
        shown = new ArrayList<>();
        int toReview = 0, reviewed = 0, fixed = 0;
        for (ErrorLog.Entry e : all) {
            if (ErrorLog.TO_REVIEW.equals(e.status)) toReview++;
            else if (ErrorLog.REVIEWED.equals(e.status)) reviewed++;
            else if (ErrorLog.FIXED.equals(e.status)) fixed++;
            if (!ALL.equals(module) && !module.equals(moduleName(e))) continue;
            if (!ALL.equals(reason) && !reason.equals(e.reason)) continue;
            if (!ALL.equals(status) && !status.equals(e.status)) continue;
            shown.add(e);
        }
        summary.setText(all.size() + " questions  ·  " + toReview + " to review  ·  "
            + reviewed + " reviewed  ·  " + fixed + " fixed");

        model.setRowCount(0);
        int selectRow = shown.isEmpty() ? -1 : 0;
        for (int i = 0; i < shown.size(); i++) {
            ErrorLog.Entry e = shown.get(i);
            model.addRow(new Object[]{e.date, moduleName(e), e.number, e.yourAnswer, e.correctAnswer,
                e.timesWrong, e.reason, e.status});
            if (e.questionId == keep) selectRow = i;
        }

        CardLayout cards = (CardLayout) detail.getParent().getLayout();
        if (shown.isEmpty()) {
            current = null;
            emptyLabel.setText(all.isEmpty()
                ? "<html><center>No mistakes yet.<br>Finish a practice test and the questions you miss will appear here.</center></html>"
                : "No questions match these filters.");
            cards.show(detail.getParent(), "empty");
        } else {
            cards.show(detail.getParent(), "detail");
            table.setRowSelectionInterval(selectRow, selectRow);
            table.scrollRectToVisible(table.getCellRect(selectRow, 0, true));
            show(shown.get(selectRow));
        }
    }

    private void show(ErrorLog.Entry e) {
        current = e;
        if (e == null) return;

        infoLabel.setText(Html.escape(moduleName(e)) + "  ·  Question " + e.number + "  ·  wrong "
            + e.timesWrong + (e.timesWrong == 1 ? " time" : " times") + "  ·  last " + e.date
            + (ErrorLog.FIXED.equals(e.status) ? "  ·  answered correctly on " + e.fixedDate : ""));
        infoLabel.setForeground(statusColor(e.status));

        Question q = find(e);
        if (q == null) {
            PassageFormatter.show(passage, "(This question is no longer in the app.)");
            questionLabel.setText("");
            for (JLabel l : choiceLabels) l.setVisible(false);
        } else {
            PassageFormatter.show(passage, q.getText() == null ? "" : q.getText());
            layoutText(q, e);
            for (int i = 0; i < 4; i++) {
                String letter = String.valueOf((char) ('A' + i));
                boolean correct = letter.equalsIgnoreCase(e.correctAnswer);
                boolean yours = letter.equalsIgnoreCase(e.yourAnswer);
                JLabel l = choiceLabels[i];
                l.setBackground(correct ? new Color(230, 244, 234) : yours ? new Color(253, 236, 234) : Color.WHITE);
                l.setForeground(correct ? new Color(20, 110, 40) : yours ? new Color(180, 40, 30) : Color.DARK_GRAY);
                l.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(correct ? GREEN : yours ? RED : new Color(210, 210, 210), correct || yours ? 2 : 1, true),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));
                l.setVisible(true);
            }
        }
        reasonBox.setSelectedItem(e.reason);
        noteArea.setText(e.note);
        passage.setCaretPosition(0);
    }

    /** Câu hỏi và 4 đáp án xuống dòng vừa khung bên phải. */
    private void layoutText(Question q, ErrorLog.Entry e) {
        if (q == null) return;
        int width = detailScroll.getViewport().getWidth();
        if (width < 200) width = 700;
        questionLabel.setText(Html.block(Html.escape(q.getQuestion()), width - 50, ""));
        String[] texts = {q.getChoiceA(), q.getChoiceB(), q.getChoiceC(), q.getChoiceD()};
        for (int i = 0; i < 4; i++) {
            String letter = String.valueOf((char) ('A' + i));
            String tag = letter.equalsIgnoreCase(e.correctAnswer) ? "  — correct answer"
                       : letter.equalsIgnoreCase(e.yourAnswer) ? "  — your answer" : "";
            choiceLabels[i].setText(Html.block("<b>" + letter + "</b>&nbsp;&nbsp;" + Html.escape(texts[i])
                + "<b>" + tag + "</b>", width - 80, ""));
        }
    }

    /** Tên module hiện tại (đề có thể đã được đổi tên sau khi ghi vào sổ). */
    private String moduleName(ErrorLog.Entry e) {
        String name = moduleNames.get(e.moduleId);
        return name != null ? name : e.moduleName;
    }

    private Question find(ErrorLog.Entry e) {
        List<Question> list = questionCache.computeIfAbsent(e.moduleId, QuestionService::getQuestionsByModule);
        for (Question q : list) {
            if (q.getQuestionId() == e.questionId) return q;
        }
        return null;
    }

    private void save(String newStatus) {
        if (current == null) return;
        current.reason = String.valueOf(reasonBox.getSelectedItem());
        current.note = noteArea.getText().trim();
        if (newStatus != null) current.status = newStatus;
        log.update(current);
        refresh();
        onChange.run();
    }

    private void remove() {
        if (current == null) return;
        int answer = JOptionPane.showConfirmDialog(this, "Remove this question from your error log?",
            "Remove", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) return;
        log.remove(current.questionId);
        current = null;
        refresh();
        onChange.run();
    }

    private static Color statusColor(String status) {
        if (ErrorLog.FIXED.equals(status)) return GREEN;
        if (ErrorLog.REVIEWED.equals(status)) return ORANGE;
        return RED;
    }

    /** Nội dung co theo chiều ngang khung cuộn (đoạn văn tự xuống dòng), chiều dọc giữ đúng chiều cao cần. */
    private static class WidthTrackingPanel extends JPanel implements Scrollable {
        public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) { return 16; }
        public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) { return r.height - 32; }
        public boolean getScrollableTracksViewportWidth() { return true; }
        public boolean getScrollableTracksViewportHeight() { return false; }
    }

    /** Đổi danh sách trong combo mà không kích hoạt bộ lọc nhiều lần. */
    private static class ActionListenerGuard {
        static void run(JComboBox<String> box, Runnable change) {
            java.awt.event.ActionListener[] listeners = box.getActionListeners();
            for (java.awt.event.ActionListener l : listeners) box.removeActionListener(l);
            try {
                change.run();
            } finally {
                for (java.awt.event.ActionListener l : listeners) box.addActionListener(l);
            }
        }
    }
}
