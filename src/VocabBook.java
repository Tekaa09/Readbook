import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Sổ từ vựng của một người học: từ trong đề + từ tự thêm, và tiến độ học theo hộp Leitner
 * (biết từ thì lên hộp sau, lâu hơn mới phải ôn lại; quên thì về hộp 1).
 * Lưu trong %APPDATA%\Redbook\vocab_<email>.properties.
 */
public class VocabBook {
    // số ngày tới lần ôn tiếp theo, theo hộp 1..5
    private static final int[] INTERVAL_DAYS = {0, 0, 1, 3, 7, 21};
    public static final int MAX_BOX = 5;
    public static final String NEW = "New";
    public static final String LEARNING = "Learning";
    public static final String MASTERED = "Mastered";

    private final File file;
    private final Properties data;
    private List<Vocab> words;

    private VocabBook(Users user) {
        file = UserData.file(user, "vocab");
        data = UserData.load(file);
    }

    public static VocabBook of(Users user) {
        return new VocabBook(user);
    }

    /** Từ trong đề + từ tự thêm, theo thứ tự ABC. */
    public List<Vocab> words() {
        if (words != null) return words;
        Map<String, Vocab> all = new TreeMap<>();
        for (Vocab v : VocabService.getAll()) all.put(v.key(), v);
        for (String key : data.stringPropertyNames()) {
            if (!key.startsWith("own.") || !key.endsWith(".word")) continue;
            String k = key.substring(4, key.length() - 5);
            if (all.containsKey(k)) continue;
            all.put(k, new Vocab(data.getProperty(key), own(k, "pos"), own(k, "en"), own(k, "vi"), own(k, "ex"), 0, true));
        }
        words = new ArrayList<>(all.values());
        return words;
    }

    public Vocab find(String word) {
        String key = word.trim().toLowerCase();
        for (Vocab v : words()) {
            if (v.key().equals(key)) return v;
        }
        return null;
    }

    /** Thêm từ của người học; từ đã có thì báo lỗi. */
    public Vocab addOwn(String word, String pos, String meaningEn, String meaningVi, String example) throws IOException {
        word = word.trim();
        if (word.isEmpty()) throw new IllegalArgumentException("Please enter a word.");
        if (find(word) != null) throw new IllegalArgumentException("\"" + word + "\" is already in your vocabulary list.");

        String k = word.toLowerCase();
        data.setProperty("own." + k + ".word", word);
        data.setProperty("own." + k + ".pos", pos.trim());
        data.setProperty("own." + k + ".en", meaningEn.trim());
        data.setProperty("own." + k + ".vi", meaningVi.trim());
        data.setProperty("own." + k + ".ex", example.trim());
        save();
        words = null;
        return find(word);
    }

    /** Chỉ xoá được từ tự thêm (từ trong đề do admin quản lý). */
    public void removeOwn(Vocab v) throws IOException {
        if (!v.isOwn()) return;
        for (String field : new String[]{"word", "pos", "en", "vi", "ex"}) data.remove("own." + v.key() + "." + field);
        data.remove("box." + v.key());
        data.remove("due." + v.key());
        save();
        words = null;
    }

    public int box(Vocab v) {
        try {
            return Integer.parseInt(data.getProperty("box." + v.key(), "0"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** Ngày phải ôn lại (null nếu chưa học lần nào). */
    public LocalDate due(Vocab v) {
        String day = data.getProperty("due." + v.key());
        try {
            return day == null ? null : LocalDate.ofEpochDay(Long.parseLong(day));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String status(Vocab v) {
        int box = box(v);
        if (box == 0) return NEW;
        return box >= 4 ? MASTERED : LEARNING;
    }

    public boolean isDue(Vocab v) {
        LocalDate due = due(v);
        return due == null || !due.isAfter(LocalDate.now());
    }

    /** Thẻ cần ôn hôm nay: từ đang học trước (hộp thấp trước), rồi tới từ mới. */
    public List<Vocab> dueToday() {
        List<Vocab> learning = new ArrayList<>();
        List<Vocab> fresh = new ArrayList<>();
        for (Vocab v : words()) {
            if (!isDue(v)) continue;
            if (box(v) == 0) fresh.add(v);
            else learning.add(v);
        }
        learning.sort((a, b) -> Integer.compare(box(a), box(b)));
        learning.addAll(fresh);
        return learning;
    }

    /** Lần ôn gần nhất sắp tới (null nếu không có từ nào đang chờ). */
    public LocalDate nextDue() {
        LocalDate next = null;
        for (Vocab v : words()) {
            LocalDate due = due(v);
            if (due != null && due.isAfter(LocalDate.now()) && (next == null || due.isBefore(next))) next = due;
        }
        return next;
    }

    public int count(String status) {
        int n = 0;
        for (Vocab v : words()) {
            if (status(v).equals(status)) n++;
        }
        return n;
    }

    /** Người học trả lời thẻ: biết thì lên hộp sau, không biết thì về hộp 1 (ôn lại ngay hôm nay). */
    public void answer(Vocab v, boolean known) {
        int box = known ? Math.min(Math.max(box(v), 1) + 1, MAX_BOX) : 1;
        data.setProperty("box." + v.key(), String.valueOf(box));
        data.setProperty("due." + v.key(), String.valueOf(LocalDate.now().plusDays(INTERVAL_DAYS[box]).toEpochDay()));
        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String own(String key, String field) {
        return data.getProperty("own." + key + "." + field, "");
    }

    private void save() throws IOException {
        UserData.save(file, data, "Redbook vocabulary");
    }
}
