package edu.sustech.xiangqi.model;

// 阵营：红方 / 黑方
public enum Side {
    RED, BLACK;

    public Side opposite() {
        return this == RED ? BLACK : RED;
    }
}
