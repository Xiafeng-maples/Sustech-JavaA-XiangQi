package edu.sustech.xiangqi.model;

// 士 / 仕：九宫内斜走一步
public class Advisor extends Piece {
    public Advisor(Side side) {
        super(side);
    }

    @Override
    public String getName() {
        return side == Side.RED ? "仕" : "士";
    }

    @Override
    public String getTypeCode() {
        return "a";
    }

    @Override
    public boolean canMove(Board board, int fromRow, int fromCol, int toRow, int toCol) {
        if (!Board.inPalace(side, toRow, toCol)) {
            return false;
        }
        return Math.abs(toRow - fromRow) == 1 && Math.abs(toCol - fromCol) == 1;
    }
}
