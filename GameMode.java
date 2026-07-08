/**
 * GameMode.java — Enum định nghĩa các chế độ chơi.
 *
 * Dùng enum thay vì int/String để:
 * - Compiler báo lỗi nếu bỏ sót case trong switch
 * - Code tự documenting, dễ đọc hơn so với magic number
 */
public enum GameMode {
    CLASSIC,  // có border, chạm tường = chết
    ENDLESS,  // không border, xuyên tường ra phía đối diện
    WALL      // không border, có tường random, chạm tường = chết
}
