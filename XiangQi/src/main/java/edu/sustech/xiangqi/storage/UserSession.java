package edu.sustech.xiangqi.storage;

// 登录会话：username 为 null 表示游客
public class UserSession {
    public final String username;

    public UserSession(String username) {
        this.username = username;
    }

    public boolean isGuest() {
        return username == null;
    }

    public String displayName() {
        return username == null ? "游客" : username;
    }
}
