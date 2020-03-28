package com.potato.wuziqi;

import android.graphics.Point;

public interface GameCallBack {
    void gameOver(int winner);
    void userAtBell(Point p);
}
