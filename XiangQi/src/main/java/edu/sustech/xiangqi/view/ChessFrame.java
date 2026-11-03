package edu.sustech.xiangqi.view;

import edu.sustech.xiangqi.controller.ChessController;
import edu.sustech.xiangqi.model.Board;
import edu.sustech.xiangqi.model.Move;
import edu.sustech.xiangqi.model.Side;
import edu.sustech.xiangqi.model.SimpleAi;
import edu.sustech.xiangqi.storage.SaveStore;
import edu.sustech.xiangqi.storage.UserSession;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.util.Arrays;
import java.util.List;

// 主窗口：棋盘 + 走棋历史侧栏 + 状态栏 + 菜单
// 菜单支持：新游戏（人人/人机）、悔棋、多存档位存读档、认输、和棋、退出、切换用户
public final class ChessFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final Board board;
    private final UserSession session;
    private final BoardPanel boardPanel;
    private final JLabel statusLabel;
    private final ChessController controller;
    private final DefaultListModel<String> historyModel = new DefaultListModel<>();
    private final JList<String> historyList = new JList<>(historyModel);

    // 人机对战：humanSide 为玩家一方，aiSide 为电脑一方；都为 null 表示人人对战
    private Side humanSide;
    private Side aiSide;
    private boolean aiThinking;

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

        // 棋盘居中放置，避免被拉伸
        JPanel boardHolder = new JPanel(new GridBagLayout());
        boardHolder.setBackground(new Color(239, 218, 179));
        boardHolder.add(boardPanel);

        historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyList.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        JScrollPane historyScroll = new JScrollPane(historyList);
        historyScroll.setPreferredSize(new Dimension(200, 400));
        historyScroll.setBorder(BorderFactory.createTitledBorder("走棋历史"));

        setLayout(new BorderLayout());
        add(boardHolder, BorderLayout.CENTER);
        add(historyScroll, BorderLayout.EAST);
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
        game.add(menuItem("新游戏（人人对战）", () -> startGame(null)));
        game.add(menuItem("新游戏（人机 · 我执红）", () -> startGame(Side.RED)));
        game.add(menuItem("新游戏（人机 · 我执黑）", () -> startGame(Side.BLACK)));
        game.addSeparator();
        game.add(menuItem("悔棋", this::undo));
        game.addSeparator();
        game.add(menuItem("保存游戏…", this::saveGame));
        game.add(menuItem("读取存档…", this::loadGame));
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

    // human == null 表示人人对战
    private void startGame(Side human) {
        humanSide = human;
        aiSide = human == null ? null : human.opposite();
        aiThinking = false;
        board.reset();
        controller.setControllableSide(human);
        refresh();
    }

    private void undo() {
        if (!board.canUndo()) {
            info("没有可以悔的棋了。");
            return;
        }
        board.undo();
        // 人机对战：撤到轮到自己走为止
        if (aiSide != null && !board.isGameOver()
                && board.getCurrentTurn() != humanSide && board.canUndo()) {
            board.undo();
        }
        controller.resetSelection();
        refresh();
    }

    private void saveGame() {
        if (session.isGuest()) {
            info("游客不能保存进度，请登录后使用存档功能。");
            return;
        }
        Integer slot = chooseSlot("保存到哪个存档位？");
        if (slot == null) {
            return;
        }
        if (SaveStore.save(session.username, slot, board)) {
            info("已保存到存档位 " + slot + "。");
        } else {
            info("保存失败，请检查磁盘权限。");
        }
    }

    private void loadGame() {
        if (session.isGuest()) {
            info("游客不能读取存档，请登录后使用存档功能。");
            return;
        }
        Integer slot = chooseSlot("从哪个存档位读取？");
        if (slot == null) {
            return;
        }
        Board loaded = SaveStore.load(session.username, slot);
        if (loaded == null) {
            info("该存档位没有存档，或存档已损坏。");
            return;
        }
        board.assign(loaded);
        controller.resetSelection();
        refresh();
        info("读取成功。");
    }

    private Integer chooseSlot(String title) {
        String[] options = new String[SaveStore.SLOTS];
        for (int i = 0; i < SaveStore.SLOTS; i++) {
            int slot = i + 1;
            options[i] = "存档位 " + slot
                    + (SaveStore.hasSave(session.username, slot) ? "（已有存档）" : "（空）");
        }
        Object choice = JOptionPane.showInputDialog(this, title, "选择存档位",
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (choice == null) {
            return null;
        }
        return Arrays.asList(options).indexOf(choice) + 1;
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
        updateStatus();
        updateHistory();
        boardPanel.repaint();
        maybeAiMove();
    }

    private void updateStatus() {
        String text;
        if (board.isGameOver()) {
            text = "对局结束：" + board.getResultMessage();
        } else {
            Side turn = board.getCurrentTurn();
            text = Board.sideName(turn) + " 行棋";
            if (aiSide != null) {
                text += (turn == aiSide) ? "（电脑）" : "（你）";
            }
            if (board.isInCheck(turn)) {
                text += " —— 将军！";
            }
        }
        statusLabel.setText(text);
    }

    private void updateHistory() {
        historyModel.clear();
        List<Move> moves = board.getHistory();
        int index = 1;
        for (Move move : moves) {
            String name = (move.pieceName == null || move.pieceName.isEmpty()) ? "?" : move.pieceName;
            StringBuilder sb = new StringBuilder();
            sb.append(index++).append(". ").append(name)
                    .append(" (").append(move.fromRow).append(',').append(move.fromCol).append(')')
                    .append("→(").append(move.toRow).append(',').append(move.toCol).append(')');
            if (move.capturedName != null) {
                sb.append(" 吃").append(move.capturedName);
            }
            historyModel.addElement(sb.toString());
        }
        if (!historyModel.isEmpty()) {
            historyList.ensureIndexIsVisible(historyModel.size() - 1);
        }
    }

    // 轮到电脑时，稍作延迟后自动走一步（避免阻塞界面刷新）
    private void maybeAiMove() {
        if (aiSide == null || aiThinking) {
            return;
        }
        if (board.isGameOver() || board.getCurrentTurn() != aiSide) {
            return;
        }
        aiThinking = true;
        statusLabel.setText("电脑思考中…");
        Timer timer = new Timer(30, e -> {
            int[] move = SimpleAi.chooseMove(board, aiSide);
            if (move != null) {
                board.tryMove(move[0], move[1], move[2], move[3]);
            }
            aiThinking = false;
            refresh();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void info(String message) {
        JOptionPane.showMessageDialog(this, message, "提示", JOptionPane.INFORMATION_MESSAGE);
    }
}
