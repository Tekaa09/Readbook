import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * Hiển thị đoạn văn trong khung bên trái của TestSession.
 * Đoạn văn không có bảng: gọi setText y như code gốc.
 * Đoạn văn có bảng (các dòng dạng "ô | ô | ô" do AutoMacro lưu): phần chữ vẫn chèn như chữ thường,
 * riêng bảng được chèn vào giữa dòng chữ dưới dạng một bảng có kẻ ô. Khung chữ không bị đổi kiểu,
 * nên font, lề và chức năng Highlights giữ nguyên như cũ.
 * Biểu đồ (dòng "[[image:<PNG base64>]]" do AutoMacro lưu) được chèn thành ảnh, thu nhỏ cho vừa khung.
 */
public class PassageFormatter {
    private static final String CURRENT_TEXT = "PassageFormatter.text";
    private static final Pattern IMAGE_LINE = Pattern.compile("^\\s*\\[\\[image:([A-Za-z0-9+/=]+)\\]\\]\\s*$");

    public static void show(JTextPane pane, String text) {
        if (pane.getClientProperty(CURRENT_TEXT) == null) {
            // TestSession đặt font và kích thước sau lần hiển thị đầu tiên → vẽ lại bảng cho khớp
            pane.addPropertyChangeListener("font", e -> redrawTable(pane));
            pane.addComponentListener(new ComponentAdapter() {
                private int lastWidth = -1;

                @Override
                public void componentResized(ComponentEvent e) {
                    if (pane.getWidth() != lastWidth) {
                        lastWidth = pane.getWidth();
                        redrawTable(pane);
                    }
                }
            });
        }
        pane.putClientProperty(CURRENT_TEXT, text == null ? "" : text);
        render(pane, text);
    }

    private static void redrawTable(JTextPane pane) {
        Object current = pane.getClientProperty(CURRENT_TEXT);
        if (current instanceof String && needsLayout((String) current)) {
            render(pane, (String) current);
        }
    }

    private static void render(JTextPane pane, String text) {
        if (text == null || !needsLayout(text)) {
            pane.setText(text); // y như code gốc
            return;
        }
        pane.setText("");
        StyledDocument doc = pane.getStyledDocument();
        String[] lines = text.split("\n", -1);
        StringBuilder paragraph = new StringBuilder();
        try {
            for (int i = 0; i < lines.length; i++) {
                Matcher image = IMAGE_LINE.matcher(lines[i]);
                if (image.matches()) {
                    doc.insertString(doc.getLength(), paragraph.toString(), null);
                    paragraph.setLength(0);
                    SimpleAttributeSet picture = new SimpleAttributeSet();
                    StyleConstants.setComponent(picture, createImage(image.group(1), pane));
                    doc.insertString(doc.getLength(), " ", picture);
                    doc.insertString(doc.getLength(), "\n", null);
                } else if (isTableRow(lines[i]) && i + 1 < lines.length && isTableRow(lines[i + 1])) {
                    List<String> rows = new ArrayList<>();
                    while (i < lines.length && isTableRow(lines[i])) {
                        rows.add(lines[i++]);
                    }
                    i--;
                    doc.insertString(doc.getLength(), paragraph.toString(), null);
                    paragraph.setLength(0);
                    SimpleAttributeSet table = new SimpleAttributeSet();
                    StyleConstants.setComponent(table, createTable(rows, pane));
                    doc.insertString(doc.getLength(), " ", table);
                    doc.insertString(doc.getLength(), "\n", null);
                } else {
                    paragraph.append(lines[i]);
                    if (i + 1 < lines.length) {
                        paragraph.append('\n');
                    }
                }
            }
            doc.insertString(doc.getLength(), paragraph.toString(), null);
        } catch (BadLocationException e) {
            pane.setText(text);
        }
        pane.setCaretPosition(0);
    }

    private static boolean isTableRow(String line) {
        return line.contains(" | ");
    }

    private static boolean needsLayout(String text) {
        return hasTable(text) || text.contains("[[image:");
    }

    private static boolean hasTable(String text) {
        String[] lines = text.split("\n");
        for (int i = 0; i + 1 < lines.length; i++) {
            if (isTableRow(lines[i]) && isTableRow(lines[i + 1])) {
                return true;
            }
        }
        return false;
    }

    /** Bảng có kẻ ô, vẽ bằng HTML trong một JLabel; dòng đầu là tiêu đề cột. */
    private static JLabel createTable(List<String> rows, JTextPane pane) {
        Font font = pane.getFont();
        Insets insets = pane.getInsets();
        int width = pane.getWidth() > 0 ? pane.getWidth() - insets.left - insets.right - 12 : 840;
        int size = Math.max(12, Math.round(font.getSize() * 0.75f));
        StringBuilder html = new StringBuilder("<html><head><style>")
            .append("td, th { font-family: '").append(font.getFamily()).append("'; font-size: ").append(size)
            .append("pt; font-weight: normal; }")
            .append("th { background-color: #f2f2f2; font-weight: bold; }")
            .append("</style></head><body><table border='1' cellspacing='0' cellpadding='6' width='")
            .append(Math.max(200, width)).append("'>");
        for (int r = 0; r < rows.size(); r++) {
            String tag = r == 0 ? "th" : "td";
            html.append("<tr>");
            for (String cell : rows.get(r).split(" \\| ", -1)) {
                html.append('<').append(tag).append('>').append(escape(cell.trim())).append("</").append(tag).append('>');
            }
            html.append("</tr>");
        }
        html.append("</table></body></html>");
        JLabel label = new JLabel(html.toString());
        label.setFont(new Font(font.getFamily(), Font.PLAIN, size)); // giao diện Metal mặc định làm nhãn in đậm
        label.setForeground(new Color(34, 34, 34));
        label.setAlignmentY(0.85f);
        return label;
    }

    /** Ảnh biểu đồ, thu nhỏ nếu rộng hơn khung chữ (không phóng to để khỏi mờ). */
    private static JLabel createImage(String base64, JTextPane pane) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(base64)));
            if (image == null) throw new IOException("not an image");
            Insets insets = pane.getInsets();
            int width = pane.getWidth() > 0 ? pane.getWidth() - insets.left - insets.right - 12 : 840;
            Image shown = image;
            if (image.getWidth() > width && width > 100) {
                int height = Math.round(image.getHeight() * (float) width / image.getWidth());
                shown = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            }
            JLabel label = new JLabel(new ImageIcon(shown));
            label.setAlignmentY(0.9f);
            return label;
        } catch (IllegalArgumentException | IOException e) {
            JLabel missing = new JLabel("[figure could not be shown]");
            missing.setForeground(Color.GRAY);
            return missing;
        }
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
