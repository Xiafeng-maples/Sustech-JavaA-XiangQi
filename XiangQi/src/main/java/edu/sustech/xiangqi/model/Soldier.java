package edu.sustech.xiangqi.model;

// 兵 / 卒：未过河只能向前一格；过河后还可左右一格；永不后退
public class Soldier extends Piece {
    public Soldier(Side side) {
        super(side);
    }

    @Override
    public String getName() {
        return side == Side.RED ? "兵" : "卒";
    }

    @Override
    public String getTypeCode() {
        return "s";
    }

    @Override
    public boolean canMove(Board board, int fromRow, int fromCol, int toRow, int toCol) {
        int dr = toRow - fromRow;
        int dc = toCol - fromCol;
        // 前进一步（红方向上、黑方向下），不能走两格
        int forward = side == Side.RED ? -1 : 1;
        if (dc == 0 && dr == forward) {
            return true;
        }
        // 过河之后才能左右平移一格，且不能后退
        boolean crossedRiver = side == Side.RED ? fromRow <= 4 : fromRow >= 5;
        return crossedRiver && dr == 0 && Math.abs(dc) == 1;
    }
}
