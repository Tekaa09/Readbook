import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

/** Bản admin: chọn máy chủ MySQL có dữ liệu Redbook và thử kết nối. Bản người dùng chạy offline, không có hộp này. */
public class ServerDialog extends JDialog {
    private static final Color RED = new Color(231, 64, 50);
    private static final Color GREEN = new Color(46, 160, 67);

    private final JTextField hostField = new JTextField(AppConfig.host(), 16);
    private final JTextField portField = new JTextField(String.valueOf(AppConfig.port()), 6);
    private final JLabel resultLabel = new JLabel(" ");
    private final JButton testBtn = new JButton("Test connection");
    private boolean saved;

    /** Mở hộp thoại; trả về true nếu người dùng đã lưu địa chỉ mới. */
    public static boolean open(Frame owner) {
        ServerDialog dialog = new ServerDialog(owner);
        dialog.setVisible(true);
        return dialog.saved;
    }

    private ServerDialog(Frame owner) {
        super(owner, "Database server", true);

        JButton quickBtn = new JButton("This computer");
        quickBtn.addActionListener(e -> hostField.setText("localhost"));

        String hint = "Where the Redbook MySQL database runs. Use localhost (This computer) for the MySQL on this PC.";
        JLabel hintLabel = new JLabel("<html><div style='width:330px'>" + hint + "</div></html>");
        hintLabel.setForeground(Color.DARK_GRAY);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(16, 18, 6, 18));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0;
        form.add(new JLabel("Server address:"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        form.add(hostField, c);
        c.gridx = 2; c.fill = GridBagConstraints.NONE; c.weightx = 0;
        form.add(quickBtn, c);

        c.gridx = 0; c.gridy = 1;
        form.add(new JLabel("Port:"), c);
        c.gridx = 1;
        form.add(portField, c);

        c.gridx = 0; c.gridy = 2; c.gridwidth = 3;
        form.add(hintLabel, c);
        c.gridy = 3;
        form.add(resultLabel, c);

        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        testBtn.addActionListener(e -> test());
        saveBtn.addActionListener(e -> save());
        cancelBtn.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        buttons.add(testBtn);
        buttons.add(saveBtn);
        buttons.add(cancelBtn);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(saveBtn);
        getRootPane().registerKeyboardAction(e -> dispose(),
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void test() {
        String host = host();
        int port = port();
        if (host == null || port < 0) return;

        testBtn.setEnabled(false);
        show("Connecting to " + host + ":" + port + "…", Color.GRAY);

        new SwingWorker<String, Void>() {
            protected String doInBackground() {
                try (Connection conn = DatabaseConnection.connect(host, port)) {
                    return null;
                } catch (SQLException e) {
                    return DatabaseConnection.describe(e, host, port);
                }
            }

            protected void done() {
                testBtn.setEnabled(true);
                String error;
                try {
                    error = get();
                } catch (InterruptedException | ExecutionException e) {
                    error = e.getMessage();
                }
                if (error == null) show("Connected. You can save this server.", GREEN);
                else show(error, RED);
            }
        }.execute();
    }

    private void save() {
        String host = host();
        int port = port();
        if (host == null || port < 0) return;

        AppConfig.set("db.host", host);
        AppConfig.set("db.port", String.valueOf(port));
        if (!AppConfig.save()) {
            show("Cannot write redbook.properties in the app folder.", RED);
            return;
        }
        saved = true;
        dispose();
    }

    private String host() {
        String host = hostField.getText().trim();
        if (host.isEmpty() || host.contains(" ") || host.contains("/")) {
            show("Enter an IP address such as 192.168.1.11, or localhost.", RED);
            return null;
        }
        return host;
    }

    private int port() {
        try {
            int port = Integer.parseInt(portField.getText().trim());
            if (port > 0 && port < 65536) return port;
        } catch (NumberFormatException ignored) {
        }
        show("Port must be a number (MySQL uses 3306).", RED);
        return -1;
    }

    private void show(String text, Color color) {
        resultLabel.setForeground(color);
        resultLabel.setText("<html><div style='width:330px'>" + text + "</div></html>");
        pack();
    }
}
