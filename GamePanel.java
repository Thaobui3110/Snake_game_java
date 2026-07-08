import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * GamePanel.java — Phase 2D: Lưu High Score ra file
 *
 * Thay đổi so với Phase 2C:
 *  - scoreManager      → instance của ScoreManager, khởi tạo 1 lần
 *  - isNewRecord       → cờ báo vừa phá kỷ lục, dùng để vẽ "NEW RECORD!"
 *  - triggerGameOver() → gọi scoreManager.saveIfBetter()
 *  - drawGameOver()    → hiển thị kỷ lục từ file + badge "NEW RECORD!"
 *  - bestScore         → không còn tự quản lý, lấy từ ScoreManager
 */
public class GamePanel extends JPanel implements ActionListener {

    // ─── Hằng số bảng game ───────────────────────────────────────────────────
    static final int TILE_SIZE    = 25;
    static final int GRID_WIDTH   = 24;
    static final int GRID_HEIGHT  = 24;
    static final int PANEL_WIDTH  = TILE_SIZE * GRID_WIDTH;
    static final int PANEL_HEIGHT = TILE_SIZE * GRID_HEIGHT;
    static final int MAX_TILES    = GRID_WIDTH * GRID_HEIGHT;

    // ─── Hằng số tốc độ ──────────────────────────────────────────────────────
    private static final int INITIAL_DELAY   = 150;
    private static final int MIN_DELAY       = 40;
    private static final int SPEED_STEP      = 10;
    private static final int SCORE_PER_LEVEL = 50;

    // ─── Hằng số Wall mode ───────────────────────────────────────────────────
    private static final int WALL_COUNT   = 8;
    private static final int WALL_MIN_LEN = 3;
    private static final int WALL_MAX_LEN = 5;
    private static final int SAFE_X1 = 8,  SAFE_Y1 = 8;
    private static final int SAFE_X2 = 16, SAFE_Y2 = 16;

    // ─── Mode & Frame ─────────────────────────────────────────────────────────
    private final GameMode     mode;
    private final GameFrame    gameFrame;
    private final ScoreManager scoreManager; // đọc/ghi high score

    // ─── Dữ liệu rắn ─────────────────────────────────────────────────────────
    private final int[] snakeX = new int[MAX_TILES];
    private final int[] snakeY = new int[MAX_TILES];
    private int  snakeLength;
    private char direction;

    // ─── Food ─────────────────────────────────────────────────────────────────
    private int foodX, foodY;
    private final Random random = new Random();

    // ─── Tường ───────────────────────────────────────────────────────────────
    private ArrayList<int[]> walls = new ArrayList<>();

    // ─── Điểm số ─────────────────────────────────────────────────────────────
    private int score;

    // ─── Trạng thái ──────────────────────────────────────────────────────────
    private boolean isRunning;
    private boolean isPaused;
    private boolean isNewRecord; // true nếu vừa phá kỷ lục ván này

    // ─── Game loop ────────────────────────────────────────────────────────────
    private Timer gameTimer;

    // ─── Constructor ─────────────────────────────────────────────────────────
    public GamePanel(GameMode mode, GameFrame gameFrame) {
        this.mode         = mode;
        this.gameFrame    = gameFrame;
        this.scoreManager = new ScoreManager(); // load highscore.txt ngay

        this.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e.getKeyCode());
            }
        });

        startGame();
    }

    // ─── Khởi động / Restart ─────────────────────────────────────────────────

    private void startGame() {
        initSnake();

        if (mode == GameMode.WALL) generateWalls();
        else walls.clear();

        spawnFood();
        score        = 0;
        isRunning    = true;
        isPaused     = false;
        isNewRecord  = false; // reset badge kỷ lục mỗi ván

        if (gameTimer == null) {
            gameTimer = new Timer(INITIAL_DELAY, this);
            gameTimer.start();
        } else {
            gameTimer.setDelay(INITIAL_DELAY);
            gameTimer.restart();
        }
    }

    private void initSnake() {
        snakeLength = 3;
        direction   = 'R';
        snakeX[0] = 12; snakeY[0] = 12;
        snakeX[1] = 11; snakeY[1] = 12;
        snakeX[2] = 10; snakeY[2] = 12;
    }

    // ─── Pause ───────────────────────────────────────────────────────────────

    private void togglePause() {
        isPaused = !isPaused;
        repaint();
    }

    // ─── Game loop ────────────────────────────────────────────────────────────

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isRunning && !isPaused) {
            move();
            repaint();
        }
    }

    // ─── Di chuyển ───────────────────────────────────────────────────────────

    private void move() {
        for (int i = snakeLength - 1; i > 0; i--) {
            snakeX[i] = snakeX[i - 1];
            snakeY[i] = snakeY[i - 1];
        }
        switch (direction) {
            case 'R': snakeX[0]++; break;
            case 'L': snakeX[0]--; break;
            case 'U': snakeY[0]--; break;
            case 'D': snakeY[0]++; break;
        }
        checkFood();
        checkCollision();
    }

    // ─── Food ─────────────────────────────────────────────────────────────────

    private void spawnFood() {
        boolean blocked;
        do {
            foodX   = random.nextInt(GRID_WIDTH);
            foodY   = random.nextInt(GRID_HEIGHT);
            blocked = false;
            for (int i = 0; i < snakeLength; i++) {
                if (snakeX[i] == foodX && snakeY[i] == foodY) {
                    blocked = true; break;
                }
            }
            if (!blocked) {
                for (int[] w : walls) {
                    if (w[0] == foodX && w[1] == foodY) {
                        blocked = true; break;
                    }
                }
            }
        } while (blocked);
    }

    private void checkFood() {
        if (snakeX[0] == foodX && snakeY[0] == foodY) {
            snakeLength++;
            score += 10;
            spawnFood();
            adjustSpeed();
        }
    }

    // ─── Tốc độ ──────────────────────────────────────────────────────────────

    private int computeDelay() {
        int level = score / SCORE_PER_LEVEL;
        return Math.max(INITIAL_DELAY - (level * SPEED_STEP), MIN_DELAY);
    }

    private void adjustSpeed() {
        int newDelay = computeDelay();
        if (newDelay != gameTimer.getDelay()) {
            gameTimer.setDelay(newDelay);
            gameTimer.restart();
        }
    }

    // ─── Collision ───────────────────────────────────────────────────────────

    private void checkCollision() {
        switch (mode) {
            case CLASSIC:
                if (hitBorder()) { triggerGameOver(); return; }
                break;
            case ENDLESS:
                wrapAround();
                break;
            case WALL:
                wrapAround();
                if (hitWall()) { triggerGameOver(); return; }
                break;
        }
        for (int i = 1; i < snakeLength; i++) {
            if (snakeX[0] == snakeX[i] && snakeY[0] == snakeY[i]) {
                triggerGameOver(); return;
            }
        }
    }

    private boolean hitBorder() {
        return snakeX[0] < 0 || snakeX[0] >= GRID_WIDTH
            || snakeY[0] < 0 || snakeY[0] >= GRID_HEIGHT;
    }

    private void wrapAround() {
        if (snakeX[0] < 0)            snakeX[0] = GRID_WIDTH  - 1;
        if (snakeX[0] >= GRID_WIDTH)  snakeX[0] = 0;
        if (snakeY[0] < 0)            snakeY[0] = GRID_HEIGHT - 1;
        if (snakeY[0] >= GRID_HEIGHT) snakeY[0] = 0;
    }

    private boolean hitWall() {
        for (int[] w : walls) {
            if (snakeX[0] == w[0] && snakeY[0] == w[1]) return true;
        }
        return false;
    }

    /**
     * triggerGameOver() — kết thúc game và lưu kỷ lục nếu cần.
     * saveIfBetter() trả về true nếu score này phá kỷ lục → bật badge.
     */
    private void triggerGameOver() {
        isRunning   = false;
        isPaused    = false;
        isNewRecord = scoreManager.saveIfBetter(mode, score);
        gameTimer.stop();
        repaint();
    }

    // ─── Tường random ────────────────────────────────────────────────────────

    private void generateWalls() {
        walls.clear();
        int attempts = 0, created = 0;
        while (created < WALL_COUNT && attempts < 200) {
            attempts++;
            int dir    = random.nextInt(2);
            int length = WALL_MIN_LEN + random.nextInt(WALL_MAX_LEN - WALL_MIN_LEN + 1);
            int ox, oy;
            if (dir == 0) { ox = random.nextInt(GRID_WIDTH - length); oy = random.nextInt(GRID_HEIGHT); }
            else          { ox = random.nextInt(GRID_WIDTH); oy = random.nextInt(GRID_HEIGHT - length); }

            boolean conflict = false;
            ArrayList<int[]> segment = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                int wx = (dir == 0) ? ox + i : ox;
                int wy = (dir == 1) ? oy + i : oy;
                if (wx >= SAFE_X1 && wx <= SAFE_X2 && wy >= SAFE_Y1 && wy <= SAFE_Y2) {
                    conflict = true; break;
                }
                for (int[] ex : walls) {
                    if (ex[0] == wx && ex[1] == wy) { conflict = true; break; }
                }
                if (conflict) break;
                segment.add(new int[]{wx, wy});
            }
            if (!conflict) { walls.addAll(segment); created++; }
        }
    }

    // ─── Bàn phím ────────────────────────────────────────────────────────────

    private void handleKeyPress(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_RIGHT: case KeyEvent.VK_D:
                if (isRunning && !isPaused && direction != 'L') direction = 'R'; break;
            case KeyEvent.VK_LEFT:  case KeyEvent.VK_A:
                if (isRunning && !isPaused && direction != 'R') direction = 'L'; break;
            case KeyEvent.VK_UP:    case KeyEvent.VK_W:
                if (isRunning && !isPaused && direction != 'D') direction = 'U'; break;
            case KeyEvent.VK_DOWN:  case KeyEvent.VK_S:
                if (isRunning && !isPaused && direction != 'U') direction = 'D'; break;
            case KeyEvent.VK_P: case KeyEvent.VK_ESCAPE:
                if (isRunning) togglePause(); break;
            case KeyEvent.VK_R:
                if (!isRunning) startGame(); break;
            case KeyEvent.VK_M:
                if (gameTimer != null) gameTimer.stop();
                gameFrame.showStart();
                break;
        }
    }

    // ─── Vẽ ──────────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGrid(g);
        if (mode == GameMode.WALL) drawWalls(g);
        drawFood(g);
        drawSnake(g);
        drawScore(g);
        if (!isRunning)      drawGameOver(g);
        else if (isPaused)   drawPause(g);
    }

    private void drawGrid(Graphics g) {
        g.setColor(new Color(30, 30, 30));
        for (int col = 0; col < GRID_WIDTH; col++)
            for (int row = 0; row < GRID_HEIGHT; row++)
                g.drawRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
    }

    private void drawWalls(Graphics g) {
        for (int[] w : walls) {
            int px = w[0] * TILE_SIZE, py = w[1] * TILE_SIZE;
            g.setColor(new Color(90, 90, 100));
            g.fillRect(px + 1, py + 1, TILE_SIZE - 2, TILE_SIZE - 2);
            g.setColor(new Color(130, 130, 145));
            g.drawRect(px + 1, py + 1, TILE_SIZE - 2, TILE_SIZE - 2);
            g.setColor(new Color(60, 60, 70));
            g.fillRect(px + TILE_SIZE / 2 - 2, py + TILE_SIZE / 2 - 2, 4, 4);
        }
    }

    private void drawFood(Graphics g) {
        g.setColor(new Color(220, 50, 50));
        g.fillOval(foodX * TILE_SIZE + 3, foodY * TILE_SIZE + 3, TILE_SIZE - 6, TILE_SIZE - 6);
        g.setColor(new Color(255, 120, 120));
        g.fillOval(foodX * TILE_SIZE + 6, foodY * TILE_SIZE + 5, 6, 6);
    }

    private void drawSnake(Graphics g) {
        for (int i = 0; i < snakeLength; i++) {
            int px = snakeX[i] * TILE_SIZE, py = snakeY[i] * TILE_SIZE;
            g.setColor(i == 0 ? new Color(0, 220, 80) : new Color(0, 160, 50));
            g.fillRoundRect(px + 1, py + 1, TILE_SIZE - 2, TILE_SIZE - 2, 8, 8);
        }
    }

    private void drawScore(Graphics g) {
        int highScore = scoreManager.getHighScore(mode);

        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, 10, 22);
        g.setColor(new Color(255, 215, 0));
        g.drawString("Best:  " + highScore, 10, 42);
        int level = score / SCORE_PER_LEVEL + 1;
        g.setColor(getLevelColor(level));
        g.drawString("Level: " + level, 10, 62);

        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(getModeColor());
        String modeLabel = mode.name();
        FontMetrics fm = g.getFontMetrics();
        g.drawString(modeLabel, PANEL_WIDTH - fm.stringWidth(modeLabel) - 10, 22);

        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.setColor(new Color(70, 70, 70));
        g.drawString(isPaused ? "P: Resume" : "P: Pause", PANEL_WIDTH - 70, 42);
        g.drawString("M: Menu", PANEL_WIDTH - 65, 58);
    }

    private void drawPause(Graphics g) {
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

        FontMetrics fm;
        String text;
        int x;

        g.setFont(new Font("Arial", Font.BOLD, 56));
        g.setColor(Color.WHITE);
        fm = g.getFontMetrics();
        text = "PAUSED";
        x = (PANEL_WIDTH - fm.stringWidth(text)) / 2;
        g.drawString(text, x, PANEL_HEIGHT / 2 - 20);

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.setColor(new Color(160, 160, 160));
        fm = g.getFontMetrics();
        text = "Press P or ESC to continue";
        x = (PANEL_WIDTH - fm.stringWidth(text)) / 2;
        g.drawString(text, x, PANEL_HEIGHT / 2 + 30);

        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.setColor(new Color(90, 90, 90));
        fm = g.getFontMetrics();
        text = "M: Main Menu";
        x = (PANEL_WIDTH - fm.stringWidth(text)) / 2;
        g.drawString(text, x, PANEL_HEIGHT / 2 + 65);
    }

    /**
     * drawGameOver() — hiển thị điểm + kỷ lục từ file + badge "NEW RECORD!" nếu có.
     */
    private void drawGameOver(Graphics g) {
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

        FontMetrics fm;
        String text;
        int x;

        // "GAME OVER"
        g.setFont(new Font("Arial", Font.BOLD, 52));
        g.setColor(Color.RED);
        fm = g.getFontMetrics();
        text = "GAME OVER";
        x = (PANEL_WIDTH - fm.stringWidth(text)) / 2;
        g.drawString(text, x, PANEL_HEIGHT / 2 - 90);

        // Score ván này
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.setColor(Color.WHITE);
        fm = g.getFontMetrics();
        text = "Score: " + score;
        x = (PANEL_WIDTH - fm.stringWidth(text)) / 2;
        g.drawString(text, x, PANEL_HEIGHT / 2 - 30);

        // High score từ file
        int highScore = scoreManager.getHighScore(mode);
        g.setFont(new Font("Arial", Font.PLAIN, 22));
        g.setColor(new Color(255, 215, 0));
        fm = g.getFontMetrics();
        text = "Best Score: " + highScore;
        x = (PANEL_WIDTH - fm.stringWidth(text)) / 2;
        g.drawString(text, x, PANEL_HEIGHT / 2 + 10);

        // Badge "NEW RECORD!" — chỉ hiện khi vừa phá kỷ lục
        if (isNewRecord) {
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.setColor(new Color(255, 215, 0));
            fm = g.getFontMetrics();
            text = "★  NEW RECORD!  ★";
            x = (PANEL_WIDTH - fm.stringWidth(text)) / 2;
            // Nền badge
            g.setColor(new Color(80, 60, 0));
            g.fillRoundRect(x - 10, PANEL_HEIGHT / 2 + 22,
                            fm.stringWidth(text) + 20, 28, 10, 10);
            // Chữ badge
            g.setColor(new Color(255, 215, 0));
            g.drawString(text, x, PANEL_HEIGHT / 2 + 41);
        }

        // Level đạt được
        int level = score / SCORE_PER_LEVEL + 1;
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.setColor(getLevelColor(level));
        fm = g.getFontMetrics();
        text = "Level reached: " + level;
        x = (PANEL_WIDTH - fm.stringWidth(text)) / 2;
        g.drawString(text, x, PANEL_HEIGHT / 2 + 70);

        // Hướng dẫn
        g.setFont(new Font("Arial", Font.PLAIN, 17));
        g.setColor(new Color(180, 180, 180));
        fm = g.getFontMetrics();
        text = "R: Restart      M: Main Menu";
        x = (PANEL_WIDTH - fm.stringWidth(text)) / 2;
        g.drawString(text, x, PANEL_HEIGHT / 2 + 108);
    }

    private Color getModeColor() {
        return switch (mode) {
            case CLASSIC -> new Color(100, 160, 255);
            case ENDLESS -> new Color(180, 100, 255);
            case WALL    -> new Color(255, 160, 50);
        };
    }

    private Color getLevelColor(int level) {
        if (level <= 3) return new Color(100, 220, 100);
        if (level <= 6) return new Color(255, 215, 0);
        if (level <= 9) return new Color(255, 140, 0);
        return new Color(255, 60, 60);
    }
}
