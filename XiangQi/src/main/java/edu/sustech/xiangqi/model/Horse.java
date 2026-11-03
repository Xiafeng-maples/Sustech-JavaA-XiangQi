package edu.sustech.xiangqi.model;

// 马：走日字，蹩马腿不可走
public class Horse extends Piece {
    public Horse(Side side) {
        super(side);
    }

    @Override
    public String getName() {
        return "马";
    }

    @Override
    public String getTypeCode() {
        return "h";
    }

    @Override
    public boolean canMove(Board board, int fromRow, int fromCol, int toRow, int toCol) {
        int dr = toRow - fromRow;
        int dc = toCol - fromCol;
        int adr = Math.abs(dr);
        int adc = Math.abs(dc);
        if (!((adr == 2 && adc == 1) || (adr == 1 && adc == 2))) {
            return false;
        }
        // 马腿：沿“日”字长边方向相邻的那一格必须为空
        int legRow;
        int legCol;
        if (adr == 2) {
            legRow = fromRow + (dr > 0 ? 1 : -1);
            legCol = fromCol;
        } else {
            legRow = fromRow;
            legCol = fromCol + (dc > 0 ? 1 : -1);
        }
        return board.getPieceAt(legRow, legCol) == null;
    }
}
