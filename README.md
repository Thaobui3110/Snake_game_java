# Snake Game — Java Swing

Game rắn săn mồi cổ điển, viết bằng Java thuần (Swing), không phụ thuộc thư viện ngoài. Đồ án minh họa các khái niệm OOP cơ bản.

3 chế độ chơi: **Classic** (có tường biên) · **Endless** (xuyên tường) · **Wall** (tường ngẫu nhiên).

## Yêu cầu

- **JDK 11 trở lên** (khuyến nghị JDK 17+). Kiểm tra bằng lệnh:
  ```
  java -version
  ```
  Nếu chưa có, tải tại: https://adoptium.net/

Không cần cài thêm thư viện nào khác (chỉ dùng Java Standard Library).

## Cách chạy game

### 1. Tải code về

```bash
git clone https://github.com/Thaobui3110/Snake_game_java.git
cd Snake_game_java
```

Hoặc bấm nút **Code → Download ZIP** trên GitHub rồi giải nén.

### 2. Biên dịch

**Windows (cmd / PowerShell):**
```bash
javac Main.java GameFrame.java GameMode.java StartPanel.java ScoreManager.java GamePanel.java
```

**macOS / Linux (terminal):**
```bash
javac *.java
```

### 3. Chạy

```bash
java Main
```

Cửa sổ game sẽ hiện ra ở màn hình Start — chọn chế độ chơi rồi bấm **PLAY**.

> Nếu dùng IDE (IntelliJ, VS Code + Java Extension Pack, Eclipse): mở thư mục project, chạy trực tiếp file `Main.java`.

## Điều khiển

| Phím | Chức năng |
|---|---|
| ↑ ↓ ← → hoặc `W A S D` | Di chuyển |
| `P` hoặc `ESC` | Tạm dừng / Tiếp tục |
| `R` | Chơi lại (khi Game Over) |
| `M` | Về màn hình chính |

## Chế độ chơi

| Mode | Mô tả |
|---|---|
| **CLASSIC** | Có tường biên, chạm tường = thua |
| **ENDLESS** | Không tường biên, đi xuyên qua sẽ hiện ở phía đối diện |
| **WALL** | Xuyên biên như Endless, nhưng có 8 đoạn tường ngẫu nhiên trên bảng — chạm vào là thua |

Điểm cao nhất của mỗi mode được lưu vào file `highscore.txt` (tự tạo khi chạy lần đầu, nằm cùng thư mục với game).

## Cấu trúc project

```
Main.java          Điểm khởi chạy chương trình
GameFrame.java      Cửa sổ chính, điều hướng giữa các màn hình
GameMode.java       Enum 3 chế độ chơi
StartPanel.java     Màn hình chọn chế độ chơi
GamePanel.java      Toàn bộ logic + hiển thị game
ScoreManager.java   Đọc/ghi điểm cao ra file
```

## Xử lý sự cố thường gặp

- **`'javac' is not recognized...`** → chưa cài JDK hoặc chưa thêm vào biến môi trường `PATH`.
- **Lỗi font/tiếng Việt hiển thị lạ** → không ảnh hưởng gameplay, game không dùng tiếng Việt trong giao diện.
- **Cửa sổ không mở khi chạy `java Main`** → kiểm tra đã `javac` đủ cả 6 file `.java` chưa, và đang đứng đúng trong thư mục chứa các file `.class`.

---

*Đồ án môn học — minh họa OOP, Java Swing, Event Handling, File I/O.*
