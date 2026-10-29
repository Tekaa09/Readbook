import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuestionService {
    public static List<Question> getQuestionsByModule(String moduleId) {
        if (OfflineData.isActive()) return OfflineData.questions(moduleId);

        List<Question> questions = new ArrayList<>();
        String query = "SELECT * FROM questions WHERE module_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, moduleId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Question q = new Question(
                    rs.getInt("question_id"),
                    rs.getString("module_id"),
                    rs.getString("text"),
                    rs.getString("question"),
                    rs.getString("A"),
                    rs.getString("B"),
                    rs.getString("C"),
                    rs.getString("D"),
                    rs.getString("answer")
                );
                questions.add(q);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questions;
    }
    public static boolean addQuestion(Question q) {
        // question_id = question_number + module_id (e.g. question 7 of module 132023 -> 7132023)
        // module_number = first digit of module_id
        String nextNumberSql = "SELECT COALESCE(MAX(CAST(question_number AS UNSIGNED)), 0) + 1 " +
                    "FROM questions WHERE module_id = ?";
        String sql = "INSERT INTO questions (question_id, module_id, module_number, question_number, " +
                    "text, question, answer, explanation, A, B, C, D) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement next = conn.prepareStatement(nextNumberSql);
            PreparedStatement ps = conn.prepareStatement(sql)) {

            String moduleId = q.getModuleId();
            next.setString(1, moduleId);
            ResultSet rs = next.executeQuery();
            rs.next();
            int questionNumber = rs.getInt(1);

            ps.setInt(1, Integer.parseInt(questionNumber + moduleId));
            ps.setString(2, moduleId);
            ps.setString(3, moduleId.substring(0, 1));
            ps.setString(4, String.valueOf(questionNumber));
            ps.setString(5, q.getText());
            ps.setString(6, q.getQuestion());
            ps.setString(7, q.getCorrectChoice());
            ps.setString(8, "null");
            ps.setString(9, q.getChoiceA());
            ps.setString(10, q.getChoiceB());
            ps.setString(11, q.getChoiceC());
            ps.setString(12, q.getChoiceD());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean updateQuestion(Question q) {

        String sql = " UPDATE questions "+
            "SET module_id = ?, text = ?, question = ?,A = ?, B = ?, C = ?, D = ?, answer = ? " +
            "WHERE question_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, q.getModuleId());
            ps.setString(2, q.getText());
            ps.setString(3, q.getQuestion());
            ps.setString(4, q.getChoiceA());
            ps.setString(5, q.getChoiceB());
            ps.setString(6, q.getChoiceC());
            ps.setString(7, q.getChoiceD());
            ps.setString(8, q.getCorrectChoice());
            ps.setInt(9, q.getQuestionId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public static boolean deleteQuestion(int questionId) {
        String sql = "DELETE FROM questions WHERE question_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, questionId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}