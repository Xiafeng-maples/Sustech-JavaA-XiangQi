package edu.sustech.xiangqi.model;

// 棋子的抽象父类。棋子只保存阵营，所在位置由 Board 的数组下标表示。
public abstract class Piece {
    protected final Side side;

    protected Piece(Side side) {
        this.side = side;
    }

    public Side getSide() {
        return side;
    }

    public boolean isRed() {
        return side == Side.RED;
    }

    // 显示用名称（红黑用字不同）
    public abstract String getName();

    // 存盘用的类型编码：g/a/e/h/r/c/s
    public abstract String getTypeCode();

    /**
     * 判断本棋子的“走法”是否合法（只考虑走法本身，不考虑走完后己方将帅是否安全）。
     */
    public abstract boolean canMove(Board board, int fromRow, int fromCol, int toRow, int toCol);
}
