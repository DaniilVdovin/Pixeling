package com.daniilvdovin.pixelit.colorize;

import android.graphics.Color;

import androidx.gridlayout.widget.GridLayout;

public class PixelData {
    public int x;
    public int y;
    public int color;
    public int number;
    public String descript;

    public PixelData(int x, int y, int color, int number,String descript) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.number = number;
        this.descript = descript;
    }

}
