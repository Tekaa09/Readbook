import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/** File riêng của từng người học trong %APPDATA%\Redbook (sổ lỗi sai, sổ từ vựng...). */
public class UserData {

    /** Ví dụ: errorlog_hs@test.com.properties */
    public static File file(Users user, String kind) {
        String name = user.getUsername().trim().toLowerCase().replaceAll("[^a-z0-9@._-]", "_");
        return new File(AppConfig.userDir(), kind + "_" + name + ".properties");
    }

    public static Properties load(File file) {
        Properties data = new Properties();
        if (file.isFile()) {
            try (InputStream in = new FileInputStream(file)) {
                data.load(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    // ghi ra file tạm rồi thay file cũ, để tắt app giữa chừng cũng không hỏng dữ liệu
    public static void save(File file, Properties data, String comment) throws IOException {
        File temp = new File(file.getPath() + ".tmp");
        try {
            try (OutputStream out = new FileOutputStream(temp)) {
                data.store(out, comment);
            }
            Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } finally {
            temp.delete();
        }
    }
}
