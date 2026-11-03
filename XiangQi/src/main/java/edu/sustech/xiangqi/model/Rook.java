package edu.sustech.xiangqi.model;

// 车：直线走任意格，路径无障碍
public class Rook extends Piece {
    public Rook(Side side) {
        super(side);
    }

    @Override
    public String getName() {
        return "车";
    }

    @Override
    public String getTypeCode() {
        return "r";
    }

    @Override
    public boolean canMove(Board board, int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow != toRow && fromCol != toCol) {
            return false;
        }
        if (fromRow == toRow && fromCol == toCol) {
            return false;
        }
        return board.countBetween(fromRow, fromCol, toRow, toCol) == 0;
    }
}
