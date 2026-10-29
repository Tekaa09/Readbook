import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Properties;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Tài khoản của bản người dùng (offline), lưu trên chính máy này trong %APPDATA%\Redbook\accounts.properties.
 * Mật khẩu không bao giờ được lưu: chỉ lưu PBKDF2-SHA256 (120.000 vòng, salt ngẫu nhiên).
 */
public class LocalAccounts {
    private static final int ITERATIONS = 120000;
    private static final int MIN_PASSWORD = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    /** Lỗi hiện thẳng cho người dùng (email sai, mật khẩu ngắn, không ghi được file...). */
    public static class AccountException extends Exception {
        public AccountException(String message) { super(message); }
    }

    private static File file() {
        return new File(AppConfig.userDir(), "accounts.properties");
    }

    public static synchronized boolean isEmpty() {
        return load().isEmpty();
    }

    /** Trả về user nếu đúng email + mật khẩu, null nếu sai. */
    public static synchronized Users login(String email, String password) throws AccountException {
        String record = load().getProperty(key(email));
        if (record == null) {
            hash(password, new byte[16], ITERATIONS);   // tốn thời gian như khi có tài khoản
            return null;
        }
        String[] p = record.split("\\|", -1);           // id|role|vòng|salt|hash|email
        if (p.length != 6) return null;

        byte[] salt = Base64.getDecoder().decode(p[3]);
        byte[] expected = Base64.getDecoder().decode(p[4]);
        byte[] actual = hash(password, salt, Integer.parseInt(p[2]));
        if (!MessageDigest.isEqual(expected, actual)) return null;

        return new Users(Integer.parseInt(p[0]), p[5], p[1]);
    }

    /** Tạo tài khoản Student mới trên máy này. */
    public static synchronized Users register(String email, String password) throws AccountException {
        email = email.trim();
        if (!email.matches("[^@\\s|]+@[^@\\s|]+\\.[^@\\s|]+") || email.length() > 100) {
            throw new AccountException("Please enter a valid email address.");
        }
        if (password.length() < MIN_PASSWORD) {
            throw new AccountException("Password must have at least " + MIN_PASSWORD + " characters.");
        }

        Properties accounts = load();
        if (accounts.containsKey(key(email))) {
            throw new AccountException("This email already has an account on this computer.");
        }

        int id = accounts.size() + 1;
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        Base64.Encoder b64 = Base64.getEncoder();
        accounts.setProperty(key(email), id + "|" + Users.STUDENT + "|" + ITERATIONS + "|"
            + b64.encodeToString(salt) + "|" + b64.encodeToString(hash(password, salt, ITERATIONS)) + "|" + email);
        save(accounts);

        return new Users(id, email, Users.STUDENT);
    }

    private static String key(String email) {
        return email.trim().toLowerCase();
    }

    private static byte[] hash(String password, byte[] salt, int iterations) throws AccountException {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 256);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new AccountException("This computer cannot check passwords: " + e.getMessage());
        }
    }

    private static Properties load() {
        return UserData.load(file());
    }

    private static void save(Properties accounts) throws AccountException {
        try {
            UserData.save(file(), accounts, "Redbook accounts (passwords are not stored, only PBKDF2 hashes)");
        } catch (IOException e) {
            throw new AccountException("Cannot save the account: " + e.getMessage());
        }
    }
}
