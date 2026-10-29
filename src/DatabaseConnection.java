import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // quá thời gian này mà máy chủ không trả lời thì báo lỗi, không để app bị treo
    private static final String OPTIONS =
        "?connectTimeout=5000&socketTimeout=30000&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";

    private static volatile String lastError;

    public static Connection getConnection() throws SQLException {
        return connect(AppConfig.host(), AppConfig.port());
    }

    public static Connection connect(String host, int port) throws SQLException {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + AppConfig.database() + OPTIONS;
        try {
            Connection conn = DriverManager.getConnection(url, AppConfig.user(), AppConfig.password());
            lastError = null;
            return conn;
        } catch (SQLException e) {
            lastError = describe(e, host, port);
            throw e;
        }
    }

    /** Lỗi kết nối gần nhất (null nếu lần kết nối cuối thành công). */
    public static String lastError() {
        return lastError;
    }

    public static String describe(SQLException e) {
        return describe(e, AppConfig.host(), AppConfig.port());
    }

    public static String describe(SQLException e, String host, int port) {
        String state = e.getSQLState() == null ? "" : e.getSQLState();
        if (state.equals("45000")) {
            return e.getMessage();   // thông báo do thủ tục trong database trả về
        }
        if (state.startsWith("08")) {
            return "Cannot reach MySQL at " + host + ":" + port + ". Make sure MySQL is running.";
        }
        switch (e.getErrorCode()) {
            case 1045: return "The server rejected the database account in redbook.properties.";
            case 1049: return "Database '" + AppConfig.database() + "' does not exist on " + host + ".";
            case 1305: return "The server is not set up for this yet (run database_setup.sql).";
            case 1044:
            case 1142:
            case 1370: return "This account is not allowed to do that on the server.";
            default:   return "Database error: " + e.getMessage();
        }
    }
}
