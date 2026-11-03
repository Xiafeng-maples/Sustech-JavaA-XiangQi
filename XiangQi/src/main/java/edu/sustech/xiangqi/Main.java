package edu.sustech.xiangqi;

import edu.sustech.xiangqi.model.Board;
import edu.sustech.xiangqi.storage.UserSession;
import edu.sustech.xiangqi.view.ChessFrame;
import edu.sustech.xiangqi.view.LoginDialog;

import javax.swing.SwingUtilities;

// 程序入口：先登录（或游客进入），再打开对局窗口。
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::startLogin);
    }

    // 显示登录框；关闭则退出程序
    public static void startLogin() {
        UserSession session = LoginDialog.prompt(null);
        if (session == null) {
            System.exit(0);
            return;
        }
        Board board = new Board();
        ChessFrame frame = new ChessFrame(board, session);
        frame.setVisible(true);
    }
}
