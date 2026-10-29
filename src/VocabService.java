import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Từ vựng có sẵn trong đề: bản người dùng đọc từ file đóng gói, bản admin đọc bảng vocab trong MySQL. */
public class VocabService {
    private static final int NO_SUCH_TABLE = 1146;

    public static List<Vocab> getAll() {
        if (OfflineData.isActive()) return OfflineData.vocab();

        try (Connection conn = DatabaseConnection.getConnection()) {
            return read(conn);
        } catch (SQLException e) {
            if (e.getErrorCode() != NO_SUCH_TABLE) e.printStackTrace();
            return new ArrayList<>();   // chưa có bảng vocab: sổ từ chỉ có từ tự thêm
        }
    }

    /** Đọc bảng vocab (DatasetExporter dùng lại khi đóng gói). Chưa có bảng thì trả về danh sách rỗng. */
    public static List<Vocab> read(Connection conn) throws SQLException {
        List<Vocab> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM vocab ORDER BY word");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Vocab(rs.getString("word"), rs.getString("part_of_speech"),
                    rs.getString("meaning_en"), rs.getString("meaning_vi"), rs.getString("example"),
                    rs.getInt("question_id"), false));
            }
        } catch (SQLException e) {
            if (e.getErrorCode() != NO_SUCH_TABLE) throw e;
        }
        return list;
    }
}
