import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Cài đặt của app: máy chủ database, tài khoản kết nối, email đã nhớ.
 * Giá trị mặc định nằm trong file jar (redbook-default.properties),
 * file redbook.properties (nếu có) sẽ ghi đè lên: cạnh app ở bản admin,
 * trong %APPDATA%\Redbook ở bản người dùng (offline) để app đặt ở thư mục nào cũng lưu được.
 */
public class AppConfig {
    private static final File FILE = OfflineData.isActive()
        ? new File(userDir(), "redbook.properties")
        : new File("redbook.properties");
    private static final Properties defaults = new Properties();
    private static final Properties saved = new Properties();

    static {
        try (InputStream in = AppConfig.class.getResourceAsStream("/redbook-default.properties")) {
            if (in != null) defaults.load(in);
        } catch (IOException ignored) {
        }
        if (FILE.isFile()) {
            try (InputStream in = new FileInputStream(FILE)) {
                saved.load(in);
            } catch (IOException ignored) {
            }
        }
    }

    public static String get(String key, String fallback) {
        String value = saved.getProperty(key, defaults.getProperty(key));
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    public static void set(String key, String value) {
        if (value == null) saved.remove(key);
        else saved.setProperty(key, value);
    }

    public static boolean save() {
        try (OutputStream out = new FileOutputStream(FILE)) {
            saved.store(out, "Redbook settings");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static String host() { return get("db.host", "localhost"); }
    public static String database() { return get("db.name", "redbook"); }
    public static String user() { return get("db.user", "root"); }
    public static String password() { return get("db.password", ""); }

    public static int port() {
        try {
            return Integer.parseInt(get("db.port", "3306"));
        } catch (NumberFormatException e) {
            return 3306;
        }
    }

    /** Thư mục riêng của người dùng Windows này: %APPDATA%\Redbook (tài khoản, email đã nhớ). */
    public static File userDir() {
        String base = System.getenv("APPDATA");
        File dir = new File(base != null ? base : System.getProperty("user.home"), "Redbook");
        dir.mkdirs();
        return dir;
    }

    /** Bản RedbookUser.jar được đóng gói không có AdminTools (không sửa được câu hỏi, đề). */
    public static boolean hasAdminTools() {
        try {
            Class.forName("AdminTools");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
