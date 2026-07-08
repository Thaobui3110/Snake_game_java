import javax.swing.*;
import java.awt.*;

/**
 * GameFrame.java — Cửa sổ chính (cập nhật cho Phase 2A).
 *
 * Thay đổi so với trước:
 *  - Dùng CardLayout để switch giữa StartPanel và GamePanel
 *  - Implement GameStartListener để nhận mode từ StartPanel
 *  - showStart() / showGame() để điều hướng giữa 2 màn hình
 */
public class GameFrame extends JFrame implements StartPanel.GameStartListener {

    private static final String CARD_START = "start";
    private static final String CARD_GAME  = "game";

    private final CardLayout   cardLayout;
    private final JPanel       mainContainer;

    private final StartPanel   startPanel;
    private GamePanel          gamePanel; // tạo mới mỗi lần chơi

    public GameFrame() {
        this.setTitle("Snake Game");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);

        // Container dùng CardLayout — chứa cả 2 màn hình
        cardLayout     = new CardLayout();
        mainContainer  = new JPanel(cardLayout);

        // Tạo StartPanel, truyền 'this' làm listener
        startPanel = new StartPanel(this);
        mainContainer.add(startPanel, CARD_START);

        // GamePanel chưa tạo — sẽ tạo khi người chơi nhấn Play
        // (cần biết mode trước)

        this.add(mainContainer);
        this.pack();
        this.setLocationRelativeTo(null);

        // Hiện màn hình Start trước
        showStart();
    }

    // ─── Điều hướng màn hình ─────────────────────────────────────────────────

    /** Quay lại màn hình Start (gọi khi từ GamePanel muốn về menu). */
    public void showStart() {
        cardLayout.show(mainContainer, CARD_START);
        startPanel.requestFocusInWindow();
    }

    /** Chuyển sang GamePanel với mode đã chọn. */
    private void showGame(GameMode mode) {
        // Nếu đã có gamePanel cũ thì xóa đi trước
        if (gamePanel != null) {
            mainContainer.remove(gamePanel);
        }

        // Tạo GamePanel mới với mode vừa chọn, truyền 'this' để có thể quay lại
        gamePanel = new GamePanel(mode, this);
        mainContainer.add(gamePanel, CARD_GAME);
        mainContainer.revalidate();

        cardLayout.show(mainContainer, CARD_GAME);
        gamePanel.requestFocusInWindow();
    }

    // ─── GameStartListener callback ──────────────────────────────────────────

    /**
     * onGameStart() — StartPanel gọi hàm này khi người chơi nhấn Play.
     * Nhận mode đã chọn và chuyển sang GamePanel.
     */
    @Override
    public void onGameStart(GameMode mode) {
        showGame(mode);
    }
}
