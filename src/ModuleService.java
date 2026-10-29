import java.sql.*;
import java.util.*;

public class ModuleService {

    public static List<Module> getAllModules() {
        if (OfflineData.isActive()) {
            List<Module> modules = new ArrayList<>(OfflineData.modules());
            modules.sort(Module.BY_NAME);
            return modules;
        }

        List<Module> modules = new ArrayList<>();
        String sql = "SELECT * FROM verbalmodules";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             
             ResultSet rs = stmt.executeQuery()) {

             while (rs.next()) {
                Module m = new Module(
                    rs.getString("module_id"),
                    rs.getString("title"),
                    rs.getString("done")
                );
                modules.add(m);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        modules.sort(Module.BY_NAME);   // Practice 1, Practice 2 - Module 1, Practice 2 - Module 2...
        return modules;
    }
    
    public static Module getModuleById(String moduleId) {
        if (OfflineData.isActive()) return OfflineData.module(moduleId);

        String sql = "SELECT * FROM verbalmodules WHERE module_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, moduleId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Module(
                    rs.getString("module_id"),
                    rs.getString("title"),
                    rs.getString("done")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static boolean addModule(Module m) {
        String sql = "INSERT INTO verbalmodules (module_id, title, done) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, m.getModuleId());
            stmt.setString(2, m.getModuleName());
            stmt.setString(3, m.getStatus());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updateModule(Module m) {
        String sql = "UPDATE verbalmodules SET title = ?, done = ? WHERE module_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, m.getModuleName());
            stmt.setString(2, m.getStatus());
            stmt.setString(3, m.getModuleId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean deleteModule(String moduleId) {
        String sql = "DELETE FROM verbalmodules WHERE module_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, moduleId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
