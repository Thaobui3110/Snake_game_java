/**
 * Main.java — Entry point (không đổi về logic, chỉ ghi chú thêm).
 */
public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            GameFrame frame = new GameFrame();
            frame.setVisible(true);
        });
    }
}
