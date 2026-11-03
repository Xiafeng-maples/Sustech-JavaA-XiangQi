package edu.sustech.xiangqi.model;

// 将 / 帅：九宫内直线一步
public class General extends Piece {
    public General(Side side) {
        super(side);
    }

    @Override
    public String getName() {
        return side == Side.RED ? "帅" : "将";
    }

    @Override
    public String getTypeCode() {
        return "g";
    }

    @Override
    public boolean canMove(Board board, int fromRow, int fromCol, int toRow, int toCol) {
        if (!Board.inPalace(side, toRow, toCol)) {
            return false;
        }
        return Math.abs(toRow - fromRow) + Math.abs(toCol - fromCol) == 1;
    }
}
