import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.*;

public class StartPanel extends JPanel {

    private static final int WIDTH  = 600;
    private static final int HEIGHT = 600;

    private static final Color BG          = new Color(15, 15, 15);
    private static final Color GREEN       = new Color(0, 220, 80);
    private static final Color GREEN_DARK  = new Color(0, 60, 25);
    private static final Color GREEN_HOVER = new Color(0, 255, 100);
    private static final Color BTN_BG      = new Color(35, 35, 35);
    private static final Color BORDER_OFF  = new Color(60, 60, 60);
    private static final Color TEXT_DIM    = new Color(140, 140, 140);
    private static final Color TEXT_HINT   = new Color(80, 80, 80);

    // Kích thước nút cố định — không đổi dù border thay đổi
    private static final Dimension BTN_SIZE = new Dimension(440, 54);

    private GameMode selectedMode = GameMode.CLASSIC;
    private final GameStartListener listener;

    private JButton btnClassic, btnEndless, btnWall;
    private JLabel  lblDescription;

    public StartPanel(GameStartListener listener) {
        this.listener = listener;
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(BG);
        this.setLayout(new GridBagLayout());
        this.setFocusable(true);
        buildUI();
    }

    private void buildUI() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx  = 0;
        gbc.fill   = GridBagConstraints.NONE; // NONE thay vì HORIZONTAL — không kéo dãn
        gbc.anchor = GridBagConstraints.CENTER;

        // ── Tiêu đề ──
        JLabel title = new JLabel("SNAKE", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 78));
        title.setForeground(GREEN);
        title.setPreferredSize(new Dimension(440, 90));
        gbc.gridy  = 0;
        gbc.insets = new Insets(0, 0, 6, 0);
        this.add(title, gbc);

        // ── Subtitle ──
        JLabel subtitle = new JLabel("Choose your mode", SwingConstants.CENTER);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 17));
        subtitle.setForeground(TEXT_DIM);
        subtitle.setPreferredSize(new Dimension(440, 30));
        gbc.gridy  = 1;
        gbc.insets = new Insets(0, 0, 28, 0);
        this.add(subtitle, gbc);

        // ── 3 nút mode ──
        btnClassic = makeModeBtn("CLASSIC");
        btnEndless = makeModeBtn("ENDLESS");
        btnWall    = makeModeBtn("WALL");

        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.gridy  = 2; this.add(btnClassic, gbc);
        gbc.gridy  = 3; this.add(btnEndless, gbc);
        gbc.gridy  = 4; this.add(btnWall,    gbc);

        // ── Mô tả mode — chiều cao cố định để không đẩy layout ──
        lblDescription = new JLabel(descriptionOf(selectedMode), SwingConstants.CENTER);
        lblDescription.setFont(new Font("Arial", Font.ITALIC, 14));
        lblDescription.setForeground(TEXT_DIM);
        // Chiều cao cố định 24px — dù text dài hay ngắn cũng không ảnh hưởng layout
        lblDescription.setPreferredSize(new Dimension(440, 24));
        gbc.gridy  = 5;
        gbc.insets = new Insets(12, 0, 18, 0);
        this.add(lblDescription, gbc);

        // ── Nút PLAY ──
        JButton btnPlay = new JButton("PLAY");
        btnPlay.setFont(new Font("Arial", Font.BOLD, 24));
        btnPlay.setForeground(Color.BLACK);
        btnPlay.setBackground(GREEN);
        btnPlay.setFocusPainted(false);
        btnPlay.setBorderPainted(false);
        btnPlay.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPlay.setPreferredSize(BTN_SIZE);
        btnPlay.setMinimumSize(BTN_SIZE);
        btnPlay.setMaximumSize(BTN_SIZE);

        btnPlay.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnPlay.setBackground(GREEN_HOVER); }
            @Override public void mouseExited (MouseEvent e) { btnPlay.setBackground(GREEN); }
        });
        btnPlay.addActionListener(e -> listener.onGameStart(selectedMode));

        gbc.gridy  = 6;
        gbc.insets = new Insets(0, 0, 0, 0);
        this.add(btnPlay, gbc);

        // ── Hint ──
        JLabel hint = new JLabel("Arrow Keys / WASD to move  \u00B7  P to Pause",
                SwingConstants.CENTER);
        hint.setFont(new Font("Arial", Font.PLAIN, 13));
        hint.setForeground(TEXT_HINT);
        hint.setPreferredSize(new Dimension(440, 20));
        gbc.gridy  = 7;
        gbc.insets = new Insets(16, 0, 0, 0);
        this.add(hint, gbc);

        refreshHighlight();
    }

    /**
     * makeModeBtn() — khóa cứng kích thước bằng cả 3 phương thức:
     * setPreferredSize, setMinimumSize, setMaximumSize.
     * Đây là cách duy nhất ngăn Swing resize nút khi border bị thay đổi.
     */
    private JButton makeModeBtn(String label) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setForeground(new Color(160, 160, 160));
        btn.setBackground(BTN_BG);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.CENTER);

        // Khóa kích thước — bộ 3 này là bắt buộc
        btn.setPreferredSize(BTN_SIZE);
        btn.setMinimumSize(BTN_SIZE);
        btn.setMaximumSize(BTN_SIZE);

        // Set border sau setPreferredSize để border không ảnh hưởng preferred size
        btn.setBorder(new RoundedBorder(8, BORDER_OFF, 2));

        btn.addActionListener(e -> {
            selectedMode = GameMode.valueOf(label);
            lblDescription.setText(descriptionOf(selectedMode));
            refreshHighlight();
        });

        return btn;
    }

    private void refreshHighlight() {
        JButton[] all = { btnClassic, btnEndless, btnWall };

        for (JButton b : all) {
            b.setForeground(new Color(160, 160, 160));
            b.setBackground(BTN_BG);
            // Phải set lại đủ 3 size sau khi đổi border để kích thước không drift
            b.setBorder(new RoundedBorder(8, BORDER_OFF, 2));
            b.setPreferredSize(BTN_SIZE);
            b.setMinimumSize(BTN_SIZE);
            b.setMaximumSize(BTN_SIZE);
        }

        JButton active = switch (selectedMode) {
            case CLASSIC -> btnClassic;
            case ENDLESS -> btnEndless;
            case WALL    -> btnWall;
        };
        active.setForeground(Color.WHITE);
        active.setBackground(GREEN_DARK);
        active.setBorder(new RoundedBorder(8, GREEN, 2));
        active.setPreferredSize(BTN_SIZE);
        active.setMinimumSize(BTN_SIZE);
        active.setMaximumSize(BTN_SIZE);
    }

    private String descriptionOf(GameMode mode) {
        return switch (mode) {
            case CLASSIC -> "Classic Snake — stay inside the border!";
            case ENDLESS -> "No walls, no limits — wrap around and keep going.";
            case WALL    -> "Random obstacles on the board. No border, but danger lurks!";
        };
    }

    // ─── Custom border bo tròn ───────────────────────────────────────────────

    static class RoundedBorder extends AbstractBorder {
        private final int   radius;
        private final Color color;
        private final int   thickness;

        RoundedBorder(int radius, Color color, int thickness) {
            this.radius    = radius;
            this.color     = color;
            this.thickness = thickness;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x + 1, y + 1, w - 2, h - 2, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            // Trả về insets cố định — không phụ thuộc vào thickness
            // để tránh làm thay đổi preferred size của nút
            return new Insets(6, 12, 6, 12);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(6, 12, 6, 12);
            return insets;
        }
    }

    // ─── Interface ───────────────────────────────────────────────────────────

    public interface GameStartListener {
        void onGameStart(GameMode mode);
    }
}
