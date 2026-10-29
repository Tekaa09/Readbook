import javax.swing.JLabel;

/** Chữ HTML cho JLabel: thoát ký tự đặc biệt và khối xuống dòng có độ rộng thật đúng như mong muốn. */
public class Html {
    private static double cssScale = -1;

    public static String escape(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
    }

    /** Khối rộng đúng {@code pixels} điểm ảnh, chữ dài tự xuống dòng. */
    public static String block(String innerHtml, int pixels, String style) {
        return "<html><div style='width:" + css(pixels) + "px" + (style.isEmpty() ? "" : "; " + style) + "'>"
            + innerHtml + "</div></html>";
    }

    // Swing phóng to đơn vị px của CSS (khoảng 1.33 lần), nên đo một lần rồi quy đổi
    private static int css(int pixels) {
        if (cssScale < 0) {
            double measured = new JLabel("<html><div style='width:300px'>x</div></html>").getPreferredSize().width / 300.0;
            cssScale = measured > 0.1 ? measured : 1;
        }
        return Math.max(50, (int) Math.round(pixels / cssScale));
    }
}
