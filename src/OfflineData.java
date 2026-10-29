import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Đề, câu hỏi và từ vựng đóng gói sẵn trong bản người dùng (file redbook-data.dat trong jar),
 * nên app chạy không cần mạng hay MySQL. File này do DatasetExporter tạo từ database của admin.
 */
public class OfflineData {
    public static final String RESOURCE = "/redbook-data.dat";
    private static final String MAGIC = "REDBOOK-DATA";
    private static final int VERSION = 2;   // 2: thêm từ vựng

    private static List<Module> modules;
    private static Map<String, List<Question>> questions;
    private static List<Vocab> vocab;
    private static String error;

    /** Bản người dùng có file dữ liệu đi kèm; bản admin thì không và đọc thẳng từ MySQL. */
    public static boolean isActive() {
        return OfflineData.class.getResource(RESOURCE) != null;
    }

    public static synchronized List<Module> modules() {
        load();
        return modules;
    }

    public static synchronized List<Question> questions(String moduleId) {
        load();
        List<Question> list = questions.get(moduleId);
        return list == null ? new ArrayList<>() : new ArrayList<>(list);
    }

    public static synchronized List<Vocab> vocab() {
        load();
        return new ArrayList<>(vocab);
    }

    public static synchronized Module module(String moduleId) {
        for (Module m : modules()) {
            if (m.getModuleId().equals(moduleId)) return m;
        }
        return null;
    }

    /** Lỗi khi đọc file dữ liệu (null nếu đọc được). */
    public static synchronized String error() {
        load();
        return error;
    }

    private static void load() {
        if (modules != null) return;
        modules = new ArrayList<>();
        questions = new LinkedHashMap<>();
        vocab = new ArrayList<>();

        try (InputStream raw = OfflineData.class.getResourceAsStream(RESOURCE)) {
            if (raw == null) throw new IOException("the question file is missing from the app");
            DataInputStream in = new DataInputStream(new GZIPInputStream(raw));
            if (!MAGIC.equals(in.readUTF()) || in.readInt() != VERSION) {
                throw new IOException("the question file is damaged or from another version");
            }
            int moduleCount = in.readInt();
            for (int i = 0; i < moduleCount; i++) {
                modules.add(new Module(readText(in), readText(in), readText(in)));
            }
            int questionCount = in.readInt();
            for (int i = 0; i < questionCount; i++) {
                Question q = new Question(in.readInt(), readText(in), readText(in), readText(in),
                    readText(in), readText(in), readText(in), readText(in), readText(in));
                questions.computeIfAbsent(q.getModuleId(), k -> new ArrayList<>()).add(q);
            }
            int vocabCount = in.readInt();
            for (int i = 0; i < vocabCount; i++) {
                vocab.add(new Vocab(readText(in), readText(in), readText(in), readText(in), readText(in), in.readInt(), false));
            }
        } catch (IOException e) {
            modules = Collections.emptyList();
            questions.clear();
            vocab.clear();
            error = "Cannot read the questions: " + e.getMessage() + ". Please download the app again.";
        }
    }

    /** Ghi file dữ liệu (dùng khi đóng gói bản người dùng). */
    public static void write(OutputStream target, List<Module> modules, List<Question> questions,
                             List<Vocab> vocab) throws IOException {
        GZIPOutputStream zip = new GZIPOutputStream(target);
        DataOutputStream out = new DataOutputStream(zip);
        out.writeUTF(MAGIC);
        out.writeInt(VERSION);
        out.writeInt(modules.size());
        for (Module m : modules) {
            writeText(out, m.getModuleId());
            writeText(out, m.getModuleName());
            writeText(out, m.getStatus());
        }
        out.writeInt(questions.size());
        for (Question q : questions) {
            out.writeInt(q.getQuestionId());
            writeText(out, q.getModuleId());
            writeText(out, q.getText());
            writeText(out, q.getQuestion());
            writeText(out, q.getChoiceA());
            writeText(out, q.getChoiceB());
            writeText(out, q.getChoiceC());
            writeText(out, q.getChoiceD());
            writeText(out, q.getCorrectChoice());
        }
        out.writeInt(vocab.size());
        for (Vocab v : vocab) {
            writeText(out, v.getWord());
            writeText(out, v.getPartOfSpeech());
            writeText(out, v.getMeaningEn());
            writeText(out, v.getMeaningVi());
            writeText(out, v.getExample());
            out.writeInt(v.getQuestionId());
        }
        out.flush();
        zip.finish();
    }

    // độ dài + UTF-8 (writeUTF giới hạn 64KB, đoạn văn dài có thể vượt)
    private static void writeText(DataOutputStream out, String text) throws IOException {
        if (text == null) {
            out.writeInt(-1);
            return;
        }
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    private static String readText(DataInputStream in) throws IOException {
        int length = in.readInt();
        if (length < 0) return null;
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
