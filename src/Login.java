
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;

public class Login extends JFrame {

    private static final Color RED = new Color(231, 64, 50);

    private PTextField usernameField;
    private PPasswordField passwordField;
    private JButton loginBtn;
    private JCheckBox showPassword;
    private JCheckBox rememberBox;
    private JLabel messageLabel;
    private JLabel serverLabel;
    private char echoChar;
    private Point dragStart;

    public Login () {
        boolean offline = OfflineData.isActive();
        ImageIcon icon = new ImageIcon("Images/B1.png");

        JLabel BG1 = new JLabel(icon);

        JLabel TitleLabel = new JLabel("Login");
        TitleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        usernameField = new PTextField("Email");
        passwordField = new PPasswordField("Password");
        echoChar = passwordField.getEchoChar();

        String remembered = AppConfig.get("login.email", "");
        usernameField.setText(remembered);

        showPassword = checkBox("Show password");
        showPassword.addActionListener(e ->
            passwordField.setEchoChar(showPassword.isSelected() ? (char) 0 : echoChar));

        rememberBox = checkBox("Remember email");
        rememberBox.setSelected(!remembered.isEmpty());

        loginBtn = new JButton("Login");
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setBackground(Color.BLACK);
        loginBtn.setOpaque(true);
        loginBtn.setBorderPainted(false);
        loginBtn.setFont(new Font("Arial", Font.BOLD, 16));
        loginBtn.addActionListener(e -> doLogin());

        messageLabel = new JLabel();
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        messageLabel.setVerticalAlignment(JLabel.TOP);

        serverLabel = new JLabel();
        serverLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        serverLabel.setForeground(Color.GRAY);

        JButton serverBtn = linkButton("Server", 12);
        serverBtn.setToolTipText("Choose which computer has the Redbook database");
        serverBtn.addActionListener(e -> {
            if (ServerDialog.open(this)) {
                showMessage("", Color.GRAY);
                checkServer();
            }
        });

        JButton signUpBtn = linkButton("No account? Sign up", 12);
        signUpBtn.setForeground(new Color(40, 90, 200));
        signUpBtn.addActionListener(e -> {
            Users u = SignUpDialog.open(this, usernameField.getText().trim());
            if (u != null) {
                rememberEmail(rememberBox.isSelected() ? u.getUsername() : null);
                dispose();
                new Home(u);
            }
        });

        JButton closeBtn = linkButton("×", 22);
        closeBtn.setToolTipText("Exit");
        closeBtn.addActionListener(e -> System.exit(0));

        TitleLabel.setBounds(320, 60, 200, 25);
        usernameField.setBounds(320, 100, 200, 30);
        passwordField.setBounds(320, 145, 200, 30);
        showPassword.setBounds(316, 180, 200, 22);
        rememberBox.setBounds(316, 202, 200, 22);
        loginBtn.setBounds(320, 235, 200, 32);
        signUpBtn.setBounds(320, 272, 200, 20);
        messageLabel.setBounds(312, 298, 225, 60);
        serverLabel.setBounds(312, 362, 170, 20);
        serverBtn.setBounds(482, 360, 60, 24);
        closeBtn.setBounds(512, 4, 34, 28);
        BG1.setBounds(0, 0, 300,400);

        this.add(TitleLabel);
        this.add(usernameField);
        this.add(passwordField);
        this.add(showPassword);
        this.add(rememberBox);
        this.add(loginBtn);
        this.add(messageLabel);
        this.add(serverLabel);
        this.add(closeBtn);
        // bản người dùng: không mạng, tự tạo tài khoản trên máy; bản admin: đăng nhập bằng tài khoản trong MySQL
        if (offline) {
            this.add(signUpBtn);
        } else {
            this.add(serverBtn);
        }
        this.add(BG1);

        // Enter để đăng nhập, Esc để thoát
        getRootPane().setDefaultButton(loginBtn);
        getRootPane().registerKeyboardAction(e -> System.exit(0),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // cửa sổ không có thanh tiêu đề nên cho kéo bằng chuột
        MouseAdapter drag = new MouseAdapter() {
            public void mousePressed(MouseEvent e) { dragStart = e.getPoint(); }
            public void mouseDragged(MouseEvent e) {
                Point p = getLocation();
                setLocation(p.x + e.getX() - dragStart.x, p.y + e.getY() - dragStart.y);
            }
        };
        getContentPane().addMouseListener(drag);
        getContentPane().addMouseMotionListener(drag);

        this.setTitle(AppConfig.hasAdminTools() ? "Redbook Login" : "Redbook Login (Student)");
        this.setIconImage(new ImageIcon("Images/icon.png").getImage());
        this.setSize(550, 400);
        this.setLayout(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setUndecorated(true);
        this.setVisible(true);
        this.getContentPane().setBackground(Color.WHITE);

        if (remembered.isEmpty()) usernameField.requestFocusInWindow();
        else passwordField.requestFocusInWindow();

        if (offline) {
            serverLabel.setBounds(312, 362, 225, 20);
            serverLabel.setText("Offline · no Internet needed");
            serverLabel.setToolTipText("Questions are included in the app. Accounts are saved on this computer.");
        } else {
            checkServer();
        }
    }

    private void doLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            showMessage("Please enter your email and password.", RED);
            return;
        }

        setBusy(true);
        showMessage("", Color.GRAY);

        new SwingWorker<Users, Void>() {
            protected Users doInBackground() throws Exception {
                return LoginService.checkLogin(user, pass);
            }

            protected void done() {
                setBusy(false);
                Users u;
                try {
                    u = get();
                } catch (InterruptedException | ExecutionException ex) {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    showMessage(cause instanceof SQLException
                        ? DatabaseConnection.describe((SQLException) cause)
                        : "Login failed: " + cause.getMessage(), RED);
                    if (!OfflineData.isActive()) checkServer();
                    return;
                }

                if (u == null) {
                    showMessage(OfflineData.isActive() && LocalAccounts.isEmpty()
                        ? "No account on this computer yet. Click Sign up to create one."
                        : "Invalid email or password.", RED);
                    passwordField.setText("");
                    passwordField.requestFocusInWindow();
                    return;
                }

                rememberEmail(rememberBox.isSelected() ? user : null);
                dispose();
                new Home(u);
            }
        }.execute();
    }

    /** Kiểm tra máy chủ database ở nền, hiện chấm xanh/đỏ ở góc dưới. */
    private void checkServer() {
        String where = AppConfig.host() + ":" + AppConfig.port();
        serverLabel.setText(status("#999999", where));
        serverLabel.setToolTipText("Connecting...");

        new SwingWorker<String, Void>() {
            protected String doInBackground() {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    return null;
                } catch (SQLException e) {
                    return DatabaseConnection.describe(e);
                }
            }

            protected void done() {
                String error;
                try {
                    error = get();
                } catch (InterruptedException | ExecutionException e) {
                    error = e.getMessage();
                }
                serverLabel.setText(status(error == null ? "#2ea043" : "#e74032", where));
                serverLabel.setToolTipText(error == null ? "Connected to the database" : error);
                if (error != null && messageLabel.getText().isEmpty()) {
                    showMessage(error + " Click Server to change it.", RED);
                }
            }
        }.execute();
    }

    private void rememberEmail(String email) {
        String old = AppConfig.get("login.email", null);
        if (email == null ? old != null : !email.equals(old)) {
            AppConfig.set("login.email", email);
            AppConfig.save();
        }
    }

    private void setBusy(boolean busy) {
        loginBtn.setEnabled(!busy);
        loginBtn.setText(busy ? "Signing in…" : "Login");
        usernameField.setEnabled(!busy);
        passwordField.setEnabled(!busy);
        setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    private void showMessage(String text, Color color) {
        messageLabel.setForeground(color);
        messageLabel.setText(text.isEmpty() ? "" : "<html>" + escape(text) + "</html>");
    }

    private static String status(String color, String where) {
        return "<html><font color='" + color + "'>●</font> Server: " + escape(where) + "</html>";
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static JCheckBox checkBox(String text) {
        JCheckBox box = new JCheckBox(text);
        box.setFont(new Font("Arial", Font.PLAIN, 12));
        box.setOpaque(false);
        box.setFocusPainted(false);
        return box;
    }

    private static JButton linkButton(String text, int size) {
        JButton b = new JButton(text);
        b.setFont(new Font("Arial", Font.PLAIN, size));
        b.setForeground(Color.GRAY);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setMargin(new java.awt.Insets(0, 0, 0, 0));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
