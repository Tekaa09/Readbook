import java.util.Comparator;

public class Module {
    /** Xếp theo tên, số theo thứ tự tự nhiên: Practice 2 đứng trước Practice 10. */
    public static final Comparator<Module> BY_NAME = (a, b) -> naturalCompare(a.getModuleName(), b.getModuleName());

    private String moduleId;
    private String moduleName;
    private String status;

    public Module(String moduleId, String moduleName, String status) {
        this.moduleId = moduleId;
        this.moduleName = moduleName;
        this.status = status;
    }

    public String getModuleId() { return moduleId; }
    public String getModuleName() { return moduleName; }
    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }
    public void setModuleId(String moduleId) { this.moduleId = moduleId; }

    private static int naturalCompare(String a, String b) {
        a = a == null ? "" : a.trim();
        b = b == null ? "" : b.trim();
        int i = 0, j = 0;
        while (i < a.length() && j < b.length()) {
            char ca = a.charAt(i), cb = b.charAt(j);
            if (Character.isDigit(ca) && Character.isDigit(cb)) {
                int si = i, sj = j;
                while (i < a.length() && Character.isDigit(a.charAt(i))) i++;
                while (j < b.length() && Character.isDigit(b.charAt(j))) j++;
                String na = a.substring(si, i).replaceFirst("^0+(?=.)", "");
                String nb = b.substring(sj, j).replaceFirst("^0+(?=.)", "");
                int byNumber = na.length() != nb.length() ? Integer.compare(na.length(), nb.length()) : na.compareTo(nb);
                if (byNumber != 0) return byNumber;
            } else {
                int byChar = Character.compare(Character.toLowerCase(ca), Character.toLowerCase(cb));
                if (byChar != 0) return byChar;
                i++;
                j++;
            }
        }
        return Integer.compare(a.length() - i, b.length() - j);
    }
}
