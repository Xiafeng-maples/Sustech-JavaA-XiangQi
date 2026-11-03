package edu.sustech.xiangqi.model;

// 一步走棋记录：起点、终点、走的棋子名、被吃的棋子名（可能为空）
public class Move {
    public final int fromRow;
    public final int fromCol;
    public final int toRow;
    public final int toCol;
    public final String pieceName;
    public final String capturedName;

    public Move(int fromRow, int fromCol, int toRow, int toCol, String pieceName, String capturedName) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.pieceName = pieceName;
        this.capturedName = capturedName;
    }
}
