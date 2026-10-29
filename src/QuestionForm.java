import java.awt.*;
import javax.swing.*;

public class QuestionForm extends JDialog {

    public QuestionForm(JFrame parent, Question q, String moduleId, Runnable reload) {
        super(parent, true);

        setTitle(q == null ? "Add Question" : "Edit Question");
        setSize(520, 520);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(0, 2, 8, 8));

        JTextField textField = new JTextField();
        JTextField questionField = new JTextField();
        JTextField aField = new JTextField();
        JTextField bField = new JTextField();
        JTextField cField = new JTextField();
        JTextField dField = new JTextField();
        JComboBox<String> correctBox =
                new JComboBox<>(new String[]{"A", "B", "C", "D"});

        if (q != null) {
            textField.setText(q.getText());
            questionField.setText(q.getQuestion());
            aField.setText(q.getChoiceA());
            bField.setText(q.getChoiceB());
            cField.setText(q.getChoiceC());
            dField.setText(q.getChoiceD());
            correctBox.setSelectedItem(q.getCorrectChoice());
        }

        add(new JLabel("Passage Text")); add(textField);
        add(new JLabel("Question")); add(questionField);
        add(new JLabel("Choice A")); add(aField);
        add(new JLabel("Choice B")); add(bField);
        add(new JLabel("Choice C")); add(cField);
        add(new JLabel("Choice D")); add(dField);
        add(new JLabel("Correct Answer")); add(correctBox);

        JButton saveBtn = new JButton("Save");
        add(new JLabel());
        add(saveBtn);

        saveBtn.addActionListener(e -> {

            // VALIDATE
            if (questionField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Question cannot be empty!");
                return;
            }

            if (q == null) {
                Question newQ = new Question(
                        0,                     
                        moduleId,
                        textField.getText(),
                        questionField.getText(),
                        aField.getText(),
                        bField.getText(),
                        cField.getText(),
                        dField.getText(),
                        correctBox.getSelectedItem().toString()
                );

                if (!QuestionService.addQuestion(newQ)) {
                    JOptionPane.showMessageDialog(this, "Failed to add question.");
                    return;
                }

            } else {
                Question updatedQ = new Question(
                        q.getQuestionId(),    
                        moduleId,
                        textField.getText(),
                        questionField.getText(),
                        aField.getText(),
                        bField.getText(),
                        cField.getText(),
                        dField.getText(),
                        correctBox.getSelectedItem().toString()
                );

                if (!QuestionService.updateQuestion(updatedQ)) {
                    JOptionPane.showMessageDialog(this, "Failed to update question.");
                    return;
                }
            }

            reload.run();
            dispose();
        });

        setVisible(true);
    }
}
