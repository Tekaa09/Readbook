
public class Users {
    // khớp với bảng roles trong database
    public static final String STUDENT = "0";
    public static final String TEACHER = "1";
    public static final String ADMIN = "2";

    private int user_id;
    private String username;
    private String role;

    public Users(int user_id, String username, String role) {
        this.user_id = user_id;
        this.username = username;
        this.role = role;
    }

    public Users(String username,String role) {
        this(0, username, role);
    }

    public int getUserId() { return user_id; }
    public String getUsername() { return username; }
    public String getRole() { return role; }

    public String getRoleName() {
        if (ADMIN.equals(role)) return "Admin";
        if (TEACHER.equals(role)) return "Teacher";
        return "Student";
    }

    /** Chỉ admin mới được tạo/sửa đề và câu hỏi. */
    public boolean canEditQuestions() {
        return ADMIN.equals(role);
    }
}
