import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginService {
    private static final int NO_SUCH_PROCEDURE = 1305;

    /**
     * Trả về user nếu đúng email + mật khẩu, null nếu sai.
     * Bản người dùng (offline) kiểm tra tài khoản lưu trên máy; bản admin hỏi MySQL,
     * không kết nối được thì ném SQLException để màn hình đăng nhập báo đúng lỗi.
     */
    public static Users checkLogin(String email, String password) throws SQLException, LocalAccounts.AccountException {
        if (OfflineData.isActive()) {
            return LocalAccounts.login(email, password);
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // login_user kiểm tra mật khẩu đã băm ngay trên máy chủ (xem database_setup.sql)
            try (PreparedStatement ps = conn.prepareStatement("CALL login_user(?, ?)")) {
                return readUser(ps, email, password);
            } catch (SQLException e) {
                if (e.getErrorCode() != NO_SUCH_PROCEDURE) throw e;
            }

            // database cũ chưa có login_user: so mật khẩu trực tiếp (phân biệt hoa/thường)
            String query = "SELECT user_id, email, role FROM users "
                         + "WHERE email = ? AND CAST(password AS BINARY) = CAST(? AS BINARY)";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                return readUser(ps, email, password);
            }
        }
    }

    /** Tạo tài khoản Student mới trên máy này (chỉ có ở bản người dùng). */
    public static Users register(String email, String password) throws LocalAccounts.AccountException {
        return LocalAccounts.register(email, password);
    }

    private static Users readUser(PreparedStatement ps, String email, String password) throws SQLException {
        ps.setString(1, email.trim());
        ps.setString(2, password);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new Users(rs.getInt("user_id"), rs.getString("email"), rs.getString("role"));
            }
        }
        return null;
    }
}
