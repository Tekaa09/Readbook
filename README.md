# Redbook

App luyện đề SAT phần Reading & Writing, giao diện giống Bluebook: có đồng hồ, đánh dấu câu, tô sáng đoạn văn và xem kết quả sau khi nộp bài.
App chạy **offline**: không cần Internet, không gửi dữ liệu đi đâu.

*Offline SAT Reading & Writing practice app with a Bluebook-style interface.*

## Tải về

Vào mục **Releases** (bên phải trang này) rồi tải một trong hai file:

| File | Dùng cho | Cần cài gì |
|---|---|---|
| `Redbook-windows.zip` | Windows | Không cần gì. Giải nén rồi mở `Redbook.exe`. |
| `Redbook-jar.zip` | Windows, macOS, Linux | Java 8 trở lên. Mở `Redbook.bat` (Windows) hoặc chạy `java -jar RedbookUser.jar`. |

## Sử dụng

1. Lần đầu mở app, bấm **No account? Sign up** để tạo tài khoản (mật khẩu ít nhất 8 ký tự).
2. Đăng nhập, chọn một module rồi bấm **START PRACTICE**.
3. Làm xong thì nộp bài để xem điểm và đáp án.
4. **ERROR LOG** (sổ lỗi sai): nộp bài xong, các câu sai hoặc bỏ trống được tự ghi lại.
   Mở lại để xem câu hỏi kèm đáp án đúng và đáp án bạn chọn, ghi lý do sai (từ vựng, ngữ pháp, đọc nhầm...) và ghi chú.
   Lần sau làm đúng câu đó, câu sẽ tự chuyển sang **Fixed**.
5. **VOCAB** (từ vựng): học bằng thẻ, gồm các từ trong đề (có nghĩa tiếng Việt, tiếng Anh và ví dụ) và từ bạn tự thêm.
   Phím Space lật thẻ, phím 1 = chưa nhớ, phím 2 = đã nhớ. Từ đã nhớ sẽ cách lâu hơn mới phải ôn lại
   (1, 3, 7, rồi 21 ngày), từ chưa nhớ sẽ gặp lại ngay.

Tài khoản, sổ lỗi sai và tiến độ học từ chỉ lưu trên máy bạn (`%APPDATA%\Redbook`). Mật khẩu được băm bằng PBKDF2, không lưu ở dạng đọc được.
Muốn có đề mới thì tải bản release mới.

## Dữ liệu đề thi

Đề và câu hỏi do tác giả quản lý trong database riêng, **không** nằm trong repo này.
Mỗi bản release được đóng gói sẵn bộ đề mới nhất. App người dùng không sửa được đề hay câu hỏi.

## Dành cho người phát triển

Cần JDK 17 (build ra file chạy được trên Java 8) và MySQL có database `redbook` (bảng `users`, `roles`, `verbalmodules`, `questions`...).

```
src/                 mã nguồn Java (Swing; MySQL Connector/J trong lib/ chỉ dùng cho bản admin)
config/              cấu hình (file *.example là mẫu), icon, hướng dẫn kèm bản release
database_setup.sql   thủ tục đăng nhập có băm mật khẩu cho bản admin
build.bat            đóng gói
```

1. Chạy `database_setup.sql` bằng root. Script này cũng tạo bảng `vocab`
   (từ, loại từ, nghĩa tiếng Anh, nghĩa tiếng Việt, ví dụ, câu hỏi có từ đó) để admin soạn từ vựng.
2. Copy `config/admin.properties.example` → `config/admin.properties` rồi điền mật khẩu MySQL.
   File này chứa mật khẩu nên đã được loại khỏi git.
3. Bật MySQL rồi chạy `build.bat`:
   - `app.jar`: bản admin, đăng nhập bằng tài khoản trong MySQL. Tài khoản role 2 có **ADMIN PANEL** để tạo và sửa đề, câu hỏi.
   - `dist/RedbookUser/RedbookUser.jar`: bản người dùng offline. Lúc build, đề và câu hỏi được xuất từ MySQL
     (`DatasetExporter`) rồi đóng gói vào bên trong. Bản này không có chức năng sửa và không kết nối database.

   Chạy `build.bat release` để tạo thêm `release/Redbook-windows.zip` (kèm Java) và `release/Redbook-jar.zip`.
