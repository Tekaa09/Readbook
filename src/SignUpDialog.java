import java.awt.*;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

/** Tạo tài khoản mới trên máy này (luôn là Student). Trả về user đã tạo để đăng nhập luôn. */
public class SignUpDialog extends JDialog {
    private static final Color RED = new Color(231, 64, 50);

    private final JTextField emailField = new JTextField(22);
    private final JPasswordField passwordField = new JPasswordField(22);
    private final JPasswordField confirmField = new JPasswordField(22);
    private final JLabel messageLabel = new JLabel(" ");
    private final JButton createBtn = new JButton("Create account");
    private Users created;

    /** Mở hộp thoại; trả về user mới, hoặc null nếu người dùng đóng lại. */
    public static Users open(Frame owner, String email) {
        SignUpDialog dialog = new SignUpDialog(owner, email);
        dialog.setVisible(true);
        return dialog.created;
    }

    private SignUpDialog(Frame owner, String email) {
        super(owner, "Create a Redbook account", true);
        emailField.setText(email);

        JLabel title = new JLabel("Create account");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        JLabel note = new JLabel("Password: at least 8 characters. Saved only on this computer.");
        note.setForeground(Color.GRAY);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(18, 22, 8, 22));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 4, 5, 4);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        form.add(title, c);
        c.gridwidth = 1;
        addRow(form, c, 1, "Email:", emailField);
        addRow(form, c, 2, "Password:", passwordField);
        addRow(form, c, 3, "Confirm password:", confirmField);
        c.gridx = 1; c.gridy = 4;
        form.add(note, c);
        c.gridx = 0; c.gridy = 5; c.gridwidth = 2;
        form.add(messageLabel, c);

        createBtn.setForeground(Color.WHITE);
        createBtn.setBackground(Color.BLACK);
        createBtn.setOpaque(true);
        createBtn.setBorderPainted(false);
        createBtn.setFont(new Font("Arial", Font.BOLD, 14));
        createBtn.addActionListener(e -> create());
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        buttons.setBackground(Color.WHITE);
        buttons.add(createBtn);
        buttons.add(cancelBtn);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(createBtn);
        getRootPane().registerKeyboardAction(e -> dispose(),
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private static void addRow(JPanel form, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridx = 0; c.gridy = row;
        form.add(new JLabel(label), c);
        c.gridx = 1;
        form.add(field, c);
    }

    private void create() {
        String email = emailField.getText().trim();
        String pass = new String(passwordField.getPassword());

        if (!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            show("Please enter a valid email address.", RED);
            return;
        }
        if (pass.length() < 8) {
            show("Password must have at least 8 characters.", RED);
            return;
        }
        if (!pass.equals(new String(confirmField.getPassword()))) {
            show("The two passwords do not match.", RED);
            return;
        }

        createBtn.setEnabled(false);
        show("Creating account…", Color.GRAY);

        new SwingWorker<Users, Void>() {
            protected Users doInBackground() throws Exception {
                return LoginService.register(email, pass);
            }

            protected void done() {
                createBtn.setEnabled(true);
                try {
                    created = get();
                } catch (InterruptedException | ExecutionException ex) {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    show(cause instanceof LocalAccounts.AccountException
                        ? cause.getMessage()
                        : "Sign up failed: " + cause.getMessage(), RED);
                    return;
                }
                if (created == null) {
                    show("Sign up failed. Please try again.", RED);
                    return;
                }
                dispose();
            }
        }.execute();
    }

    private void show(String text, Color color) {
        messageLabel.setForeground(color);
        messageLabel.setText("<html><div style='width:300px'>" + text.replace("&", "&amp;").replace("<", "&lt;") + "</div></html>");
        pack();
    }
}
