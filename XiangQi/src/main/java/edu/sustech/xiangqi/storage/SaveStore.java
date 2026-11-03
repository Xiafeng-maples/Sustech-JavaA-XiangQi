package edu.sustech.xiangqi.storage;

import edu.sustech.xiangqi.model.Board;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

// 游戏存读档：每个用户有多个存档位（默认 3 个）。
// 文件位置：<用户主目录>/.xiangqi/saves/<用户>/slot<N>.txt。
// 读档时若格式或数据损坏，Board.decode 返回 null，调用方提示而不崩溃。
public final class SaveStore {
    public static final int SLOTS = 3;

    private static final Path DIR = Path.of(System.getProperty("user.home"), ".xiangqi", "saves");

    private SaveStore() {
    }

    private static Path slotFile(String username, int slot) {
        return DIR.resolve(safeName(username)).resolve("slot" + slot + ".txt");
    }

    // 简单的文件名净化，避免路径穿越
    private static String safeName(String username) {
        return username.replaceAll("[^a-zA-Z0-9_\\-\\u4e00-\\u9fa5]", "_");
    }

    public static boolean hasSave(String username, int slot) {
        return username != null && Files.exists(slotFile(username, slot));
    }

    public static boolean save(String username, int slot, Board board) {
        if (username == null) {
            return false;
        }
        try {
            Path file = slotFile(username, slot);
            Files.createDirectories(file.getParent());
            Files.writeString(file, board.encode(), StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // 读档；没有存档或存档损坏时返回 null
    public static Board load(String username, int slot) {
        if (username == null) {
            return null;
        }
        Path file = slotFile(username, slot);
        if (!Files.exists(file)) {
            return null;
        }
        try {
            String text = Files.readString(file, StandardCharsets.UTF_8);
            return Board.decode(text);
        } catch (IOException e) {
            return null;
        }
    }
}
