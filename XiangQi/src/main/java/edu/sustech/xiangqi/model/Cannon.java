package edu.sustech.xiangqi.model;

// 炮：直线移动时路径无障碍；吃子时必须隔一个棋子（炮架）
public class Cannon extends Piece {
    public Cannon(Side side) {
        super(side);
    }

    @Override
    public String getName() {
        return "炮";
    }

    @Override
    public String getTypeCode() {
        return "c";
    }

    @Override
    public boolean canMove(Board board, int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow != toRow && fromCol != toCol) {
            return false;
        }
        if (fromRow == toRow && fromCol == toCol) {
            return false;
        }
        int between = board.countBetween(fromRow, fromCol, toRow, toCol);
        boolean targetOccupied = board.getPieceAt(toRow, toCol) != null;
        // 吃子需要正好一个炮架；不吃子需要中间无子
        return targetOccupied ? between == 1 : between == 0;
    }
}
