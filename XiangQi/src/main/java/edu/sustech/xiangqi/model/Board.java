package edu.sustech.xiangqi.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// 保存棋盘数据，并统一检查、执行移动、判定状态、提供存盘编码
public class Board {
    public static final int ROWS = 10;
    public static final int COLS = 9;

    private final Piece[][] pieces = new Piece[ROWS][COLS];
    private Side currentTurn = Side.RED;
    private final List<Move> history = new ArrayList<>();

    private boolean gameOver = false;
    private Side winner = null;          // 胜负时的赢家；和棋时为 null
    private String resultMessage = "";   // 结束后展示的文字

    // 上一步（用于高亮）
    private int lastFromRow = -1;
    private int lastFromCol = -1;
    private int lastToRow = -1;
    private int lastToCol = -1;

    public Board() {
        reset();
    }

    // ---------- 初始化 ----------

    public void reset() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                pieces[r][c] = null;
            }
        }
        backRank(0, Side.BLACK);
        backRank(9, Side.RED);
        pieces[2][1] = new Cannon(Side.BLACK);
        pieces[2][7] = new Cannon(Side.BLACK);
        pieces[7][1] = new Cannon(Side.RED);
        pieces[7][7] = new Cannon(Side.RED);
        for (int c = 0; c < COLS; c += 2) {
            pieces[3][c] = new Soldier(Side.BLACK);
            pieces[6][c] = new Soldier(Side.RED);
        }
        currentTurn = Side.RED;
        history.clear();
        gameOver = false;
        winner = null;
        resultMessage = "";
        clearLastMove();
    }

    private void backRank(int row, Side side) {
        pieces[row][0] = new Rook(side);
        pieces[row][1] = new Horse(side);
        pieces[row][2] = new Elephant(side);
        pieces[row][3] = new Advisor(side);
        pieces[row][4] = new General(side);
        pieces[row][5] = new Advisor(side);
        pieces[row][6] = new Elephant(side);
        pieces[row][7] = new Horse(side);
        pieces[row][8] = new Rook(side);
    }

    private void clearLastMove() {
        lastFromRow = lastFromCol = lastToRow = lastToCol = -1;
    }

    // ---------- 基本访问 ----------

    public static boolean inPalace(Side side, int row, int col) {
        if (col < 3 || col > 5) {
            return false;
        }
        return side == Side.RED ? (row >= 7 && row <= 9) : (row >= 0 && row <= 2);
    }

    public boolean isInside(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }

    public Piece getPieceAt(int row, int col) {
        if (!isInside(row, col)) {
            return null;
        }
        return pieces[row][col];
    }

    // 统计两点连线之间（不含端点）的棋子数；不在同一行列返回 -1
    public int countBetween(int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow != toRow && fromCol != toCol) {
            return -1;
        }
        int dr = Integer.compare(toRow, fromRow);
        int dc = Integer.compare(toCol, fromCol);
        int count = 0;
        int r = fromRow + dr;
        int c = fromCol + dc;
        while (r != toRow || c != toCol) {
            if (pieces[r][c] != null) {
                count++;
            }
            r += dr;
            c += dc;
        }
        return count;
    }

    public Side getCurrentTurn() {
        return currentTurn;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public Side getWinner() {
        return winner;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public List<Move> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public int getLastFromRow() {
        return lastFromRow;
    }

    public int getLastFromCol() {
        return lastFromCol;
    }

    public int getLastToRow() {
        return lastToRow;
    }

    public int getLastToCol() {
        return lastToCol;
    }

    public static String sideName(Side side) {
        return side == Side.RED ? "红方" : "黑方";
    }

    // ---------- 规则判定 ----------

    public boolean isInCheck(Side side) {
        int[] general = findGeneral(side);
        if (general == null) {
            return true; // 将帅已不存在，视为被将军
        }
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Piece p = pieces[r][c];
                if (p != null && p.getSide() != side && p.canMove(this, r, c, general[0], general[1])) {
                    return true;
                }
            }
        }
        return generalsFacing();
    }

    // 双方将帅是否在同一列且中间无子
    public boolean generalsFacing() {
        int[] red = findGeneral(Side.RED);
        int[] black = findGeneral(Side.BLACK);
        if (red == null || black == null) {
            return false;
        }
        if (red[1] != black[1]) {
            return false;
        }
        int col = red[1];
        int r1 = Math.min(red[0], black[0]);
        int r2 = Math.max(red[0], black[0]);
        for (int r = r1 + 1; r < r2; r++) {
            if (pieces[r][col] != null) {
                return false;
            }
        }
        return true;
    }

    private int[] findGeneral(Side side) {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Piece p = pieces[r][c];
                if (p != null && p.getSide() == side && "g".equals(p.getTypeCode())) {
                    return new int[]{r, c};
                }
            }
        }
        return null;
    }

    // 该方是否还有任何合法走法
    public boolean hasAnyLegalMove(Side side) {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Piece p = pieces[r][c];
                if (p == null || p.getSide() != side) {
                    continue;
                }
                for (int tr = 0; tr < ROWS; tr++) {
                    for (int tc = 0; tc < COLS; tc++) {
                        if (isLegalFor(side, r, c, tr, tc)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    // 判断走法是否合法（含：己方棋子、目标非己方、走法、走完后己方将帅安全）
    public boolean isLegalFor(Side side, int fromRow, int fromCol, int toRow, int toCol) {
        if (!isInside(fromRow, fromCol) || !isInside(toRow, toCol)) {
            return false;
        }
        if (fromRow == toRow && fromCol == toCol) {
            return false;
        }
        Piece piece = pieces[fromRow][fromCol];
        if (piece == null || piece.getSide() != side) {
            return false;
        }
        Piece target = pieces[toRow][toCol];
        if (target != null && target.getSide() == side) {
            return false;
        }
        if (!piece.canMove(this, fromRow, fromCol, toRow, toCol)) {
            return false;
        }
        // 试走一步，检查己方将帅是否安全
        pieces[toRow][toCol] = piece;
        pieces[fromRow][fromCol] = null;
        boolean safe = !isInCheck(side);
        pieces[fromRow][fromCol] = piece;
        pieces[toRow][toCol] = target;
        return safe;
    }

    // ---------- 执行移动 ----------

    public boolean tryMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (gameOver) {
            return false;
        }
        if (!isLegalFor(currentTurn, fromRow, fromCol, toRow, toCol)) {
            return false;
        }
        Piece piece = pieces[fromRow][fromCol];
        Piece captured = pieces[toRow][toCol];

        pieces[toRow][toCol] = piece;
        pieces[fromRow][fromCol] = null;
        lastFromRow = fromRow;
        lastFromCol = fromCol;
        lastToRow = toRow;
        lastToCol = toCol;
        history.add(new Move(fromRow, fromCol, toRow, toCol, piece.getName(),
                captured == null ? null : captured.getName()));

        currentTurn = currentTurn.opposite();
        evaluateStatus();
        return true;
    }

    private void evaluateStatus() {
        Side side = currentTurn;
        if (!hasAnyLegalMove(side)) {
            gameOver = true;
            winner = side.opposite();
            resultMessage = (isInCheck(side) ? "将死" : "困毙") + "！" + sideName(winner) + "获胜";
        } else {
            gameOver = false;
            winner = null;
            resultMessage = "";
        }
    }

    public void resign() {
        if (gameOver) {
            return;
        }
        winner = currentTurn.opposite();
        gameOver = true;
        resultMessage = sideName(currentTurn) + "认输，" + sideName(winner) + "获胜";
    }

    public void drawByAgreement() {
        if (gameOver) {
            return;
        }
        gameOver = true;
        winner = null;
        resultMessage = "双方同意和棋";
    }

    // ---------- 存档编码 / 解码 ----------

    public String encode() {
        StringBuilder sb = new StringBuilder();
        sb.append("XQSAVE1\n");
        sb.append("turn ").append(currentTurn).append('\n');
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (c > 0) {
                    sb.append(' ');
                }
                sb.append(tokenOf(pieces[r][c]));
            }
            sb.append('\n');
        }
        sb.append("moves ").append(history.size()).append('\n');
        for (Move m : history) {
            sb.append(m.fromRow).append(' ').append(m.fromCol).append(' ')
                    .append(m.toRow).append(' ').append(m.toCol).append('\n');
        }
        return sb.toString();
    }

    private static String tokenOf(Piece p) {
        if (p == null) {
            return "--";
        }
        return (p.getSide() == Side.RED ? "r" : "b") + p.getTypeCode();
    }

    // 解码；格式或数据不合法时返回 null（用于存档损坏检测）
    public static Board decode(String text) {
        try {
            if (text == null) {
                return null;
            }
            String[] lines = text.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
            int idx = 0;
            if (idx >= lines.length || !"XQSAVE1".equals(lines[idx].trim())) {
                return null;
            }
            idx++;
            if (idx >= lines.length || !lines[idx].trim().startsWith("turn ")) {
                return null;
            }
            String turnStr = lines[idx].trim().substring(5).trim();
            Side turn;
            if ("RED".equals(turnStr)) {
                turn = Side.RED;
            } else if ("BLACK".equals(turnStr)) {
                turn = Side.BLACK;
            } else {
                return null;
            }
            idx++;

            Board board = new Board();
            for (int r = 0; r < ROWS; r++) {
                if (idx >= lines.length) {
                    return null;
                }
                String[] parts = lines[idx].trim().split("\\s+");
                if (parts.length != COLS) {
                    return null;
                }
                for (int c = 0; c < COLS; c++) {
                    Piece p = createPiece(parts[c]);
                    if ("--".equals(parts[c])) {
                        board.pieces[r][c] = null;
                    } else if (p == null) {
                        return null;
                    } else {
                        board.pieces[r][c] = p;
                    }
                }
                idx++;
            }

            // 双方将帅必须都在
            if (board.findGeneral(Side.RED) == null || board.findGeneral(Side.BLACK) == null) {
                return null;
            }

            board.currentTurn = turn;

            if (idx >= lines.length || !lines[idx].trim().startsWith("moves ")) {
                return null;
            }
            int moveCount = Integer.parseInt(lines[idx].trim().substring(6).trim());
            if (moveCount < 0) {
                return null;
            }
            idx++;
            board.history.clear();
            for (int i = 0; i < moveCount; i++) {
                if (idx >= lines.length) {
                    return null;
                }
                String[] m = lines[idx].trim().split("\\s+");
                if (m.length != 4) {
                    return null;
                }
                int fr = Integer.parseInt(m[0]);
                int fc = Integer.parseInt(m[1]);
                int tr = Integer.parseInt(m[2]);
                int tc = Integer.parseInt(m[3]);
                if (!board.isInside(fr, fc) || !board.isInside(tr, tc)) {
                    return null;
                }
                board.history.add(new Move(fr, fc, tr, tc, "", null));
                idx++;
            }

            if (!board.history.isEmpty()) {
                Move last = board.history.get(board.history.size() - 1);
                board.lastFromRow = last.fromRow;
                board.lastFromCol = last.fromCol;
                board.lastToRow = last.toRow;
                board.lastToCol = last.toCol;
            }
            board.evaluateStatus();
            return board;
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private static Piece createPiece(String token) {
        if (token == null || token.length() != 2 || "--".equals(token)) {
            return null;
        }
        char sideChar = token.charAt(0);
        char type = token.charAt(1);
        Side side;
        if (sideChar == 'r') {
            side = Side.RED;
        } else if (sideChar == 'b') {
            side = Side.BLACK;
        } else {
            return null;
        }
        switch (type) {
            case 'g':
                return new General(side);
            case 'a':
                return new Advisor(side);
            case 'e':
                return new Elephant(side);
            case 'h':
                return new Horse(side);
            case 'r':
                return new Rook(side);
            case 'c':
                return new Cannon(side);
            case 's':
                return new Soldier(side);
            default:
                return null;
        }
    }

    // 用另一局的数据覆盖当前局面（用于读档）
    public void assign(Board other) {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                pieces[r][c] = other.pieces[r][c];
            }
        }
        currentTurn = other.currentTurn;
        history.clear();
        history.addAll(other.history);
        lastFromRow = other.lastFromRow;
        lastFromCol = other.lastFromCol;
        lastToRow = other.lastToRow;
        lastToCol = other.lastToCol;
        gameOver = other.gameOver;
        winner = other.winner;
        resultMessage = other.resultMessage;
    }
}
