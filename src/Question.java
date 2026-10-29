public class Question {
    private int id;
    private String module_id;
    private String text , question;
    private String a, b, c, d, correct;
    private String selectedAnswer;  

    public Question(int id, String module_id,String text,String question, String a, String b, String c, String d, String correct) {
        this.id = id;
        this.module_id = module_id;
        this.text = text;
        this.question = question;
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.correct = correct;
    }

    public int getQuestionId() { return id; }
    public String getModuleId() { return module_id; }
    public String getText() { return text; }
    public String getQuestion() { return question; }
    public String getChoiceA() { return a; }
    public String getChoiceB() { return b; }
    public String getChoiceC() { return c; }
    public String getChoiceD() { return d; }
    public String getCorrectChoice() { return correct; }

    public void setSelectedAnswer(String selectedAnswer) {
     this.selectedAnswer = selectedAnswer;
    }

    public String getSelectedAnswer() {
     return selectedAnswer;
    }
}