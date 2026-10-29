/** Một từ vựng: từ trong đề (admin soạn trong bảng vocab) hoặc từ người học tự thêm. */
public class Vocab {
    private final String word;
    private final String partOfSpeech;
    private final String meaningEn;
    private final String meaningVi;
    private final String example;
    private final int questionId;   // câu hỏi có từ này, 0 nếu không có
    private final boolean own;      // người học tự thêm

    public Vocab(String word, String partOfSpeech, String meaningEn, String meaningVi,
                 String example, int questionId, boolean own) {
        this.word = word;
        this.partOfSpeech = blank(partOfSpeech);
        this.meaningEn = blank(meaningEn);
        this.meaningVi = blank(meaningVi);
        this.example = blank(example);
        this.questionId = questionId;
        this.own = own;
    }

    private static String blank(String s) {
        return s == null ? "" : s.trim();
    }

    public String getWord() { return word; }
    public String getPartOfSpeech() { return partOfSpeech; }
    public String getMeaningEn() { return meaningEn; }
    public String getMeaningVi() { return meaningVi; }
    public String getExample() { return example; }
    public int getQuestionId() { return questionId; }
    public boolean isOwn() { return own; }

    /** Khoá so sánh: không phân biệt hoa/thường. */
    public String key() {
        return word.trim().toLowerCase();
    }
}
