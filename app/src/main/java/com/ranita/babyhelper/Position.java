package com.ranita.babyhelper;

public class Position {
    private int left = -1;
    private int top = -1;
    private int right = -1;
    private int bottom = -1;

    public Position(int _left, int _top, int _right, int _bottom) {
        left = _left;
        top = _top;
        right = _right;
        bottom = _bottom;
    }

    public int left() {
        return left;
    }

    public int top() {
        return top;
    }

    public int right() {
        return right;
    }

    public int bottom() {
        return bottom;
    }
}
