import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Xuất đề, câu hỏi và từ vựng từ MySQL của admin ra file redbook-data.dat để đóng gói vào bản người dùng (offline).
 * build.bat chạy: java DatasetExporter <file ra>
 */
public class DatasetExporter {
    public static void main(String[] args) throws Exception {
        List<Module> modules = new ArrayList<>();
        List<Question> questions = new ArrayList<>();
        List<Vocab> vocab;

        try (Connection conn = connect()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM verbalmodules ORDER BY module_id");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modules.add(new Module(rs.getString("module_id"), rs.getString("title"), rs.getString("done")));
                }
            }
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM questions ORDER BY module_id, question_id");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    questions.add(new Question(rs.getInt("question_id"), rs.getString("module_id"),
                        rs.getString("text"), rs.getString("question"), rs.getString("A"), rs.getString("B"),
                        rs.getString("C"), rs.getString("D"), rs.getString("answer")));
                }
            }
            vocab = VocabService.read(conn);
        }

        try (OutputStream out = new FileOutputStream(args[0])) {
            OfflineData.write(out, modules, questions, vocab);
        }
        System.out.println("Exported " + modules.size() + " modules, " + questions.size() + " questions, "
            + vocab.size() + " vocabulary words.");
    }

    private static Connection connect() {
        try {
            return DatabaseConnection.getConnection();
        } catch (java.sql.SQLException e) {
            System.out.println(DatabaseConnection.describe(e));
            System.exit(1);
            return null;
        }
    }
}
