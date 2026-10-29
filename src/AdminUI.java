import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class AdminUI extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private String moduleId;

    public AdminUI(String moduleId) {
        this.moduleId = moduleId;

        setTitle("Admin - Manage Questions");
        setSize(1200, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===== TOP TOOLBAR - CREATE MODULE BUTTON =====
        JPanel topToolbar = new JPanel(new BorderLayout());
        topToolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        topToolbar.setBackground(Color.WHITE);
        
        RoundedButton createModuleBtn = new RoundedButton("+ Create New Module", 
            new Color(231, 64, 50), Color.WHITE);
        createModuleBtn.setPreferredSize(new java.awt.Dimension(220, 45));
        createModuleBtn.addActionListener(e -> addNewModule());
        
        topToolbar.add(createModuleBtn, BorderLayout.WEST);
        add(topToolbar, BorderLayout.NORTH);

        // ===== TABLE =====
        model = new DefaultTableModel(
            new String[]{"ID", "Text", "Question", "A", "B", "C", "D", "Correct"}, 0
        );
        table = new JTable(model);
        table.setRowHeight(28);

        loadQuestions();

        JScrollPane scroll = new JScrollPane(table);
        add(scroll, BorderLayout.CENTER);

        // ===== BUTTON PANEL =====
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));
        buttonPanel.setBackground(new Color(250, 250, 250));

        RoundedButton addBtn = new RoundedButton("Add Question", new Color(76, 175, 80), Color.WHITE);
        RoundedButton editBtn = new RoundedButton("Edit Question", new Color(33, 150, 243), Color.WHITE);
        RoundedButton deleteBtn = new RoundedButton("Delete Question", new Color(244, 67, 54), Color.WHITE);
        
        addBtn.setPreferredSize(new java.awt.Dimension(150, 45));
        editBtn.setPreferredSize(new java.awt.Dimension(150, 45));
        deleteBtn.setPreferredSize(new java.awt.Dimension(150, 45));

        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        // ===== EVENTS =====
        addBtn.addActionListener(e -> openQuestionForm (null));
        editBtn.addActionListener(e -> editSelected());
        deleteBtn.addActionListener(e -> deleteSelected());

        setVisible(true);
    }

    // ===== LOAD QUESTIONS =====
    private void loadQuestions() {
        model.setRowCount(0);
        List<Question> list = QuestionService.getQuestionsByModule(moduleId);

        for (Question q : list) {
            model.addRow(new Object[]{
                q.getQuestionId(),
                q.getText(),
                q.getQuestion(),
                q.getChoiceA(),
                q.getChoiceB(),
                q.getChoiceC(),
                q.getChoiceD(),
                q.getCorrectChoice()
            });
        }
    }

    // ===== ADD / EDIT =====
    private void openQuestionForm (Question q) {
        new QuestionForm (this, q, moduleId, this::loadQuestions);
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a question first!");
            return;
        }

        Question q = new Question(
            (int) model.getValueAt(row, 0),
            moduleId,
            model.getValueAt(row, 1).toString(),
            model.getValueAt(row, 2).toString(),
            model.getValueAt(row, 3).toString(),
            model.getValueAt(row, 4).toString(),
            model.getValueAt(row, 5).toString(),
            model.getValueAt(row, 6).toString(),
            model.getValueAt(row, 7).toString()
        );

        openQuestionForm (q);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        int id = (int) model.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(
            this, "Delete this question?", "Confirm", JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            QuestionService.deleteQuestion(id);
            loadQuestions();
        }
    }

    // ===== ADD NEW MODULE =====
    private void addNewModule() {
        openModuleForm(null);
    }

    private void openModuleForm(Module m) {
        JDialog dialog = new JDialog(this, true);
        dialog.setTitle(m == null ? "Add New Module" : "Edit Module");
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(0, 2, 8, 8));

        // ===== FIELDS =====
        JTextField moduleIdField = new JTextField();
        JTextField moduleNameField = new JTextField();
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"0", "1"});

        // ===== LOAD DATA IF EDIT =====
        if (m != null) {
            moduleIdField.setText(m.getModuleId());
            moduleIdField.setEditable(false); // Don't allow editing ID
            moduleNameField.setText(m.getModuleName());
            statusBox.setSelectedItem(m.getStatus());
        }

        // ===== UI =====
        dialog.add(new JLabel("Module ID:"));
        dialog.add(moduleIdField);
        dialog.add(new JLabel("Module Name:"));
        dialog.add(moduleNameField);
        dialog.add(new JLabel("Status:"));
        dialog.add(statusBox);

        RoundedButton saveBtn = new RoundedButton("Save", new Color(66, 133, 244), Color.WHITE);
        saveBtn.setPreferredSize(new java.awt.Dimension(120, 40));
        dialog.add(new JLabel());
        dialog.add(saveBtn);

        // ===== SAVE ACTION =====
        saveBtn.addActionListener(e -> {
            // VALIDATE
            if (moduleIdField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Module ID cannot be empty!");
                return;
            }

            if (moduleNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Module Name cannot be empty!");
                return;
            }

            if (m == null) {
                // ===== ADD NEW MODULE =====
                Module newModule = new Module(
                    moduleIdField.getText().trim(),
                    moduleNameField.getText().trim(),
                    statusBox.getSelectedItem().toString()
                );

                if (ModuleService.addModule(newModule)) {
                    JOptionPane.showMessageDialog(dialog, "Module added successfully!");
                    ModuleStore.addModule(newModule);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to add module. Module ID may already exist.");
                }
            } else {
                // ===== UPDATE MODULE =====
                Module updatedModule = new Module(
                    m.getModuleId(),
                    moduleNameField.getText().trim(),
                    statusBox.getSelectedItem().toString()
                );

                if (ModuleService.updateModule(updatedModule)) {
                    JOptionPane.showMessageDialog(dialog, "Module updated successfully!");
                    ModuleStore.updateStatus(m.getModuleId(), statusBox.getSelectedItem().toString());
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update module.");
                }
            }
        });

        dialog.setVisible(true);
    }
}
