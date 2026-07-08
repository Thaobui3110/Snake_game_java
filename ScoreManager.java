import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * ScoreManager.java — Quản lý high score theo từng GameMode.
 *
 * Chức năng:
 *  - load()           → đọc highscore.txt khi khởi động
 *  - saveIfBetter()   → ghi file nếu score mới cao hơn kỷ lục
 *  - getHighScore()   → trả về kỷ lục của một mode
 *
 * Format file highscore.txt:
 *   CLASSIC=120
 *   ENDLESS=200
 *   WALL=80
 *
 * File tự tạo lần đầu nếu chưa có.
 * Dòng sai format sẽ bị bỏ qua, không crash chương trình.
 */
public class ScoreManager {

    private static final String FILE_PATH = "highscore.txt";

    // Map lưu kỷ lục: GameMode → điểm cao nhất
    private final Map<GameMode, Integer> highScores = new HashMap<>();

    // ─── Constructor ─────────────────────────────────────────────────────────

    public ScoreManager() {
        // Khởi tạo tất cả mode về 0 trước
        for (GameMode mode : GameMode.values()) {
            highScores.put(mode, 0);
        }
        load(); // rồi đọc từ file đè lên
    }

    // ─── Đọc file ─────────────────────────────────────────────────────────────

    /**
     * load() — đọc highscore.txt và nạp vào map.
     *
     * Nếu file không tồn tại → giữ nguyên giá trị 0 (không lỗi).
     * Nếu một dòng sai format → bỏ qua dòng đó, tiếp tục đọc.
     */
    private void load() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return; // lần đầu chạy, chưa có file → bình thường

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue; // bỏ qua dòng trống

                // Tách "CLASSIC=120" thành ["CLASSIC", "120"]
                String[] parts = line.split("=");
                if (parts.length != 2) continue; // sai format → bỏ qua

                try {
                    GameMode mode  = GameMode.valueOf(parts[0].trim()); // "CLASSIC" → enum
                    int      score = Integer.parseInt(parts[1].trim()); // "120" → int
                    if (score >= 0) { // không chấp nhận điểm âm
                        highScores.put(mode, score);
                    }
                } catch (IllegalArgumentException e) {
                    // GameMode.valueOf() hoặc parseInt() thất bại → bỏ qua dòng này
                }
            }
        } catch (IOException e) {
            // Không đọc được file → giữ nguyên giá trị 0, game vẫn chạy bình thường
            System.err.println("ScoreManager: Cannot read " + FILE_PATH);
        }
    }

    // ─── Ghi file ─────────────────────────────────────────────────────────────

    /**
     * saveIfBetter() — so sánh score mới với kỷ lục, ghi file nếu cao hơn.
     *
     * @return true nếu phá kỷ lục (để GamePanel hiển thị "NEW RECORD!")
     */
    public boolean saveIfBetter(GameMode mode, int score) {
        if (score > highScores.get(mode)) {
            highScores.put(mode, score);
            saveToFile();
            return true; // có kỷ lục mới
        }
        return false;
    }

    /**
     * saveToFile() — ghi toàn bộ map ra file.
     * Ghi đè hoàn toàn, không append — đảm bảo file luôn sạch.
     */
    private void saveToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (GameMode mode : GameMode.values()) {
                writer.write(mode.name() + "=" + highScores.get(mode));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("ScoreManager: Cannot write " + FILE_PATH);
        }
    }

    // ─── Getter ───────────────────────────────────────────────────────────────

    /** Trả về kỷ lục của một mode cụ thể. */
    public int getHighScore(GameMode mode) {
        return highScores.get(mode);
    }
}
