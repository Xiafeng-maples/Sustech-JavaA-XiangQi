package edu.sustech.xiangqi.model;

// 象 / 相：田字，不过河，塞象眼不可走
public class Elephant extends Piece {
    public Elephant(Side side) {
        super(side);
    }

    @Override
    public String getName() {
        return side == Side.RED ? "相" : "象";
    }

    @Override
    public String getTypeCode() {
        return "e";
    }

    @Override
    public boolean canMove(Board board, int fromRow, int fromCol, int toRow, int toCol) {
        if (Math.abs(toRow - fromRow) != 2 || Math.abs(toCol - fromCol) != 2) {
            return false;
        }
        // 不能过河：红方只在第 5~9 行，黑方只在第 0~4 行
        if (side == Side.RED && toRow < 5) {
            return false;
        }
        if (side == Side.BLACK && toRow > 4) {
            return false;
        }
        // 象眼（田字中心）必须为空
        int eyeRow = (fromRow + toRow) / 2;
        int eyeCol = (fromCol + toCol) / 2;
        return board.getPieceAt(eyeRow, eyeCol) == null;
    }
}
