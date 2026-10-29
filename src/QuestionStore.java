import java.util.ArrayList;
import java.util.List;

public class QuestionStore {
    private List<Question> questions;
    private int currentIndex;

    public QuestionStore() {
        questions = new ArrayList<>();
        currentIndex = 0;
    }

    public void addQuestion(Question q) {
        questions.add(q);
    }

    public void addAll(List<Question> list) {
        questions.addAll(list);
    }

    public List<Question> getAll() {
        return questions;
    }

    public Question getCurrentQuestion() {
        if (questions.isEmpty()) return null;
        return questions.get(currentIndex);
    }

    public Question getQuestion(int index) {
        if (index < 0 || index >= questions.size()) return null;
        return questions.get(index);
    }

    public Question nextQuestion() {
        if (currentIndex < questions.size() - 1) {
            currentIndex++;
            return getCurrentQuestion();
        }
        return null;
    }

    public Question previousQuestion() {
        if (currentIndex > 0) {
            currentIndex--;
            return getCurrentQuestion();
        }
        return null;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public int getTotalQuestions() {
        return questions.size();
    }
        
    public int size() {
        return questions.size();
    }
}
