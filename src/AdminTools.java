import java.awt.*;
import java.util.List;
import javax.swing.*;

/**
 * Nút ADMIN PANEL trên trang Home: tạo đề mới, sửa đề và câu hỏi.
 * Bản cho người dùng (RedbookUser.jar) được đóng gói không có class này.
 */
public class AdminTools {

    public static void addButton(JFrame owner, JPanel header) {
        RoundedButton adminBtn = new RoundedButton("ADMIN PANEL", Color.WHITE, new Color(231,64,50));
        adminBtn.setBounds(1320, 20, 200, 50);
        adminBtn.setFont(new Font("Arial", Font.BOLD, 19));

        adminBtn.addActionListener(e -> {
            String[] options = {"Create New Module", "Modify Module"};

            int choice = JOptionPane.showOptionDialog(
                owner,
                "What would you like to do?",
                "Admin Panel",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
            );

            if (choice == 0) {
                String tempModuleId = "temp_" + System.currentTimeMillis();
                new AdminUI(tempModuleId);
            } else if (choice == 1) {
                // Modify Module - Show module selection
                List<Module> modules = ModuleService.getAllModules();
                if (modules.isEmpty()) {
                    JOptionPane.showMessageDialog(
                        owner,
                        "No modules available. Please create a module first.",
                        "No Modules",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    return;
                }

                String[] moduleNames = new String[modules.size()];
                for (int i = 0; i < modules.size(); i++) {
                    moduleNames[i] = modules.get(i).getModuleName();
                }

                String selected = (String) JOptionPane.showInputDialog(
                    owner,
                    "Select a module to modify:",
                    "Modify Module",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    moduleNames,
                    moduleNames[0]
                );
                if (selected != null) {
                    String selectedModuleId = null;
                    for (Module m : modules) {
                        if (m.getModuleName().equals(selected)) {
                            selectedModuleId = m.getModuleId();
                            break;
                        }
                    }

                    if (selectedModuleId != null) {
                        new AdminUI(selectedModuleId);
                    }
                }
            }
        });

        header.add(adminBtn);
    }
}
