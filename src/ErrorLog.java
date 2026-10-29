import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/**
 * Sổ lỗi sai của một người học: nộp bài xong, câu sai / bỏ trống được ghi lại để xem lại sau.
 * Lưu trong %APPDATA%\Redbook\errorlog_<email>.properties.
 */
public class ErrorLog {
    public static final String[] REASONS = {
        "Not set", "Vocabulary", "Grammar / punctuation", "Misread the question",
        "Logic / inference", "Data / graph", "Ran out of time", "Careless mistake", "Other"
    };
    public static final String TO_REVIEW = "To review";
    public static final String REVIEWED = "Reviewed";
    public static final String FIXED = "Fixed";      // sau đó đã làm đúng câu này

    /** Một câu đã từng làm sai. */
    public static class Entry {
        public int questionId;
        public String moduleId = "";
        public String moduleName = "";
        public int number;
        public String yourAnswer = "";
        public String correctAnswer = "";
        public int timesWrong;
        public String date = "";
        public String reason = REASONS[0];
        public String note = "";
        public String status = TO_REVIEW;
        public String fixedDate = "";
    }

    private final File file;
    private final Properties data;
    private long loadedAt;

    private ErrorLog(Users user) {
        file = UserData.file(user, "errorlog");
        data = UserData.load(file);
        loadedAt = file.lastModified();
    }

    /** File đã bị ghi từ chỗ khác (vd. vừa nộp bài) sau khi đọc. */
    public boolean isStale() {
        return file.lastModified() != loadedAt;
    }

    public static ErrorLog of(Users user) {
        return new ErrorLog(user);
    }

    /** Mới nhất trước. */
    public List<Entry> entries() {
        Set<Integer> ids = new TreeSet<>();
        for (String key : data.stringPropertyNames()) {
            int dot = key.indexOf('.');
            if (dot > 0) {
                try {
                    ids.add(Integer.parseInt(key.substring(0, dot)));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        List<Entry> list = new ArrayList<>();
        for (int id : ids) list.add(read(id));
        list.sort((a, b) -> {
            int byDate = b.date.compareTo(a.date);
            if (byDate != 0) return byDate;
            int byModule = a.moduleName.compareTo(b.moduleName);
            return byModule != 0 ? byModule : Integer.compare(a.number, b.number);
        });
        return list;
    }

    public int countToReview() {
        int n = 0;
        for (Entry e : entries()) {
            if (TO_REVIEW.equals(e.status)) n++;
        }
        return n;
    }

    /**
     * Ghi kết quả một bài làm: câu sai hoặc bỏ trống được thêm vào sổ (làm sai lại thì tăng số lần sai),
     * câu đã có trong sổ mà lần này làm đúng thì chuyển sang Fixed. Trả về số câu sai/bỏ trống của bài.
     */
    public int record(String moduleName, List<Question> questions) {
        String today = LocalDate.now().toString();
        int wrong = 0;

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            String correct = q.getCorrectChoice();
            if (correct == null || correct.trim().isEmpty()) continue;   // câu chưa có đáp án

            String answer = q.getSelectedAnswer();
            boolean omitted = answer == null || answer.trim().isEmpty();
            boolean inLog = data.containsKey(q.getQuestionId() + ".module");

            if (!omitted && answer.trim().equalsIgnoreCase(correct.trim())) {
                if (inLog && !FIXED.equals(data.getProperty(q.getQuestionId() + ".status"))) {
                    Entry e = read(q.getQuestionId());
                    e.status = FIXED;
                    e.fixedDate = today;
                    write(e);
                }
                continue;
            }

            wrong++;
            Entry e = inLog ? read(q.getQuestionId()) : new Entry();
            e.questionId = q.getQuestionId();
            e.moduleId = q.getModuleId() == null ? "" : q.getModuleId();
            e.moduleName = moduleName == null ? "" : moduleName.trim();
            e.number = i + 1;
            e.yourAnswer = omitted ? "Omitted" : answer.trim();
            e.correctAnswer = correct.trim();
            e.timesWrong++;
            e.date = today;
            e.status = TO_REVIEW;
            e.fixedDate = "";
            write(e);
        }
        save();
        return wrong;
    }

    /** Lưu lý do, ghi chú, trạng thái người học vừa sửa. */
    public void update(Entry e) {
        write(e);
        save();
    }

    public void remove(int questionId) {
        String prefix = questionId + ".";
        for (String key : data.stringPropertyNames()) {
            if (key.startsWith(prefix)) data.remove(key);
        }
        save();
    }

    private Entry read(int id) {
        Entry e = new Entry();
        e.questionId = id;
        e.moduleId = get(id, "module");
        e.moduleName = get(id, "moduleName");
        e.number = number(get(id, "number"));
        e.yourAnswer = get(id, "answer");
        e.correctAnswer = get(id, "correct");
        e.timesWrong = number(get(id, "wrong"));
        e.date = get(id, "date");
        e.reason = get(id, "reason").isEmpty() ? REASONS[0] : get(id, "reason");
        e.note = get(id, "note");
        e.status = get(id, "status").isEmpty() ? TO_REVIEW : get(id, "status");
        e.fixedDate = get(id, "fixed");
        return e;
    }

    private void write(Entry e) {
        int id = e.questionId;
        data.setProperty(id + ".module", e.moduleId);
        data.setProperty(id + ".moduleName", e.moduleName);
        data.setProperty(id + ".number", String.valueOf(e.number));
        data.setProperty(id + ".answer", e.yourAnswer);
        data.setProperty(id + ".correct", e.correctAnswer);
        data.setProperty(id + ".wrong", String.valueOf(e.timesWrong));
        data.setProperty(id + ".date", e.date);
        data.setProperty(id + ".reason", e.reason);
        data.setProperty(id + ".note", e.note);
        data.setProperty(id + ".status", e.status);
        data.setProperty(id + ".fixed", e.fixedDate);
    }

    private String get(int id, String field) {
        return data.getProperty(id + "." + field, "");
    }

    private static int number(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void save() {
        try {
            UserData.save(file, data, "Redbook error log");
            loadedAt = file.lastModified();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
