package edu.sustech.xiangqi.controller;

import edu.sustech.xiangqi.model.Board;
import edu.sustech.xiangqi.model.Piece;
import edu.sustech.xiangqi.view.BoardPanel;

import javax.swing.SwingUtilities;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// 处理鼠标点击：选子、切换选择、落子。落子成功后通过 onUpdate 通知界面刷新。
public class ChessController extends MouseAdapter {
    private final Board board;
    private final BoardPanel boardPanel;
    private final Runnable onUpdate;

    private int selectedRow = -1;
    private int selectedCol = -1;

    public ChessController(Board board, BoardPanel boardPanel, Runnable onUpdate) {
        this.board = board;
        this.boardPanel = boardPanel;
        this.onUpdate = onUpdate;
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        if (!SwingUtilities.isLeftMouseButton(event) || board.isGameOver()) {
            return;
        }
        Point position = boardPanel.getIntersectionAt(event.getX(), event.getY());
        if (position != null) {
            handleClick(position.y, position.x);
        }
    }

    private void handleClick(int row, int col) {
        if (row == selectedRow && col == selectedCol) {
            clearSelection();
            return;
        }

        Piece clickedPiece = board.getPieceAt(row, col);
        if (selectedRow == -1) {
            if (clickedPiece != null) {
                select(row, col);
            }
            return;
        }

        Piece selectedPiece = board.getPieceAt(selectedRow, selectedCol);
        // 点击己方另一枚棋子：切换选择
        if (clickedPiece != null && selectedPiece != null
                && clickedPiece.getSide() == selectedPiece.getSide()) {
            select(row, col);
            return;
        }

        // 尝试移动；成功则清除选择
        if (board.tryMove(selectedRow, selectedCol, row, col)) {
            clearSelection();
        }
    }

    private void select(int row, int col) {
        selectedRow = row;
        selectedCol = col;
        boardPanel.showSelection(row, col);
    }

    private void clearSelection() {
        selectedRow = -1;
        selectedCol = -1;
        boardPanel.showSelection(-1, -1);
        if (onUpdate != null) {
            onUpdate.run();
        }
    }

    // 供新游戏 / 读档后清除选择
    public void resetSelection() {
        clearSelection();
    }
}
