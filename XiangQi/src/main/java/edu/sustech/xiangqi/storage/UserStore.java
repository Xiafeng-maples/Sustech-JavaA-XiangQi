package edu.sustech.xiangqi.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// 用户账号：以文本文件持久化，程序重启后仍可登录。
// 文件位置：<用户主目录>/.xiangqi/users.txt，每行 "用户名<TAB>密码"。
public final class UserStore {
    private static final Path DIR = Path.of(System.getProperty("user.home"), ".xiangqi");
    private static final Path USERS = DIR.resolve("users.txt");

    private UserStore() {
    }

    private static Map<String, String> load() {
        Map<String, String> map = new LinkedHashMap<>();
        try {
            if (!Files.exists(USERS)) {
                return map;
            }
            List<String> lines = Files.readAllLines(USERS, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line.isBlank()) {
                    continue;
                }
                int tab = line.indexOf('\t');
                if (tab > 0) {
                    map.put(line.substring(0, tab), line.substring(tab + 1));
                }
            }
        } catch (IOException e) {
            // 读取失败时按空用户表处理
        }
        return map;
    }

    private static void save(Map<String, String> map) throws IOException {
        Files.createDirectories(DIR);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : map.entrySet()) {
            sb.append(e.getKey()).append('\t').append(e.getValue()).append('\n');
        }
        Files.writeString(USERS, sb.toString(), StandardCharsets.UTF_8);
    }

    // 用户名是否可用（非空、不含制表符/换行）
    public static boolean isValidUsername(String username) {
        return username != null && !username.isBlank()
                && username.indexOf('\t') < 0 && username.indexOf('\n') < 0
                && username.indexOf('\r') < 0;
    }

    // 注册：用户名已存在则返回 false
    public static boolean register(String username, String password) {
        if (!isValidUsername(username) || password == null || password.isEmpty()) {
            return false;
        }
        Map<String, String> map = load();
        if (map.containsKey(username)) {
            return false;
        }
        map.put(username, password);
        try {
            save(map);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // 校验用户名和密码
    public static boolean login(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        return password.equals(load().get(username));
    }
}
