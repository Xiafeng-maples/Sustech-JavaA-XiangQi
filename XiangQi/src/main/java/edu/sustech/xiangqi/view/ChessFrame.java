package edu.sustech.xiangqi.view;

import edu.sustech.xiangqi.controller.ChessController;
import edu.sustech.xiangqi.model.Board;
import edu.sustech.xiangqi.storage.SaveStore;
import edu.sustech.xiangqi.storage.UserSession;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

// 主窗口：棋盘 + 状态栏 + 菜单（新游戏 / 存读档 / 认输 / 和棋 / 退出 / 切换用户）
public final class ChessFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final Board board;
    private final UserSession session;
    private final BoardPanel boardPanel;
    private final JLabel statusLabel;
    private final ChessController controller;

    public ChessFrame(Board board, UserSession session) {
        super("中国象棋 - " + session.displayName());
        this.board = board;
        this.session = session;

        boardPanel = new BoardPanel(board);
        controller = new ChessController(board, boardPanel, this::refresh);
        boardPanel.addMouseListener(controller);

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(245, 235, 210));

        setLayout(new BorderLayout());
        add(boardPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
        setJMenuBar(buildMenuBar());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        refresh();
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu game = new JMenu("对局");
        game.add(menuItem("新游戏", this::newGame));
        game.add(menuItem("保存游戏", this::saveGame));
        game.add(menuItem("读取存档", this::loadGame));
        game.addSeparator();
        game.add(menuItem("认输", this::resign));
        game.add(menuItem("和棋（双方同意）", this::agreeDraw));
        game.addSeparator();
        game.add(menuItem("退出", () -> System.exit(0)));

        JMenu account = new JMenu("账号");
        account.add(menuItem("切换用户 / 注销", this::logout));

        bar.add(game);
        bar.add(account);
        return bar;
    }

    private JMenuItem menuItem(String text, Runnable action) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(e -> action.run());
        return item;
    }

    // ---------- 菜单动作 ----------

    private void newGame() {
        board.reset();
        controller.resetSelection();
        refresh();
    }

    private void saveGame() {
        if (session.isGuest()) {
            info("游客不能保存进度，请登录后使用存档功能。");
            return;
        }
        if (SaveStore.save(session.username, board)) {
            info("存档已保存。");
        } else {
            info("保存失败，请检查磁盘权限。");
        }
    }

    private void loadGame() {
        if (session.isGuest()) {
            info("游客不能读取存档，请登录后使用存档功能。");
            return;
        }
        Board loaded = SaveStore.load(session.username);
        if (loaded == null) {
            info("没有可用的存档，或存档已损坏。");
            return;
        }
        board.assign(loaded);
        controller.resetSelection();
        refresh();
        info("读取成功。");
    }

    private void resign() {
        if (board.isGameOver()) {
            return;
        }
        board.resign();
        controller.resetSelection();
        refresh();
    }

    private void agreeDraw() {
        if (board.isGameOver()) {
            return;
        }
        board.drawByAgreement();
        controller.resetSelection();
        refresh();
    }

    private void logout() {
        dispose();
        edu.sustech.xiangqi.Main.startLogin();
    }

    // ---------- 状态刷新 ----------

    public void refresh() {
        String text;
        if (board.isGameOver()) {
            text = "对局结束：" + board.getResultMessage();
        } else {
            text = Board.sideName(board.getCurrentTurn()) + " 行棋";
            if (board.isInCheck(board.getCurrentTurn())) {
                text += " —— 将军！";
            }
        }
        statusLabel.setText(text);
        boardPanel.repaint();
    }

    private void info(String message) {
        JOptionPane.showMessageDialog(this, message, "提示", JOptionPane.INFORMATION_MESSAGE);
    }
}
