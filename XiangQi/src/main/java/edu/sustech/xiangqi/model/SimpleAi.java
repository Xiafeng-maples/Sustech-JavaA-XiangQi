package edu.sustech.xiangqi.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// 极简 AI：在“自己走一步 + 对方最佳应手”的两层范围内搜索，按子力差评估。
// 目的只是能陪人下棋，不追求强度；同分走法随机挑选，避免每局一模一样。
public final class SimpleAi {
    private static final int WIN = 1_000_000;
    private static final Random RANDOM = new Random();

    private SimpleAi() {
    }

    // 为 side 选一步棋，返回 {fromRow, fromCol, toRow, toCol}；无棋可走返回 null
    public static int[] chooseMove(Board board, Side side) {
        List<int[]> moves = board.legalMoves(side);
        if (moves.isEmpty()) {
            return null;
        }
        int best = Integer.MIN_VALUE;
        List<int[]> bestMoves = new ArrayList<>();
        for (int[] move : moves) {
            Piece captured = board.applyMove(move[0], move[1], move[2], move[3]);
            int score = -search(board, side.opposite(), 1);
            board.revertMove(move[0], move[1], move[2], move[3], captured);
            if (score > best) {
                best = score;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (score == best) {
                bestMoves.add(move);
            }
        }
        return bestMoves.get(RANDOM.nextInt(bestMoves.size()));
    }

    // 返回 side 视角的分数（正数对 side 有利）
    private static int search(Board board, Side side, int depth) {
        List<int[]> moves = board.legalMoves(side);
        if (moves.isEmpty()) {
            // 该方没有合法走法：被将军是将死，否则是困毙，都算输
            return board.isInCheck(side) ? -WIN : -WIN / 2;
        }
        if (depth == 0) {
            return board.materialScore(side);
        }
        int best = Integer.MIN_VALUE;
        for (int[] move : moves) {
            Piece captured = board.applyMove(move[0], move[1], move[2], move[3]);
            int score = -search(board, side.opposite(), depth - 1);
            board.revertMove(move[0], move[1], move[2], move[3], captured);
            if (score > best) {
                best = score;
            }
        }
        return best;
    }
}
