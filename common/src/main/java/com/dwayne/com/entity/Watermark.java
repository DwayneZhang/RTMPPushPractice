package com.dwayne.com.entity;

import android.graphics.Bitmap;

public class Watermark {
    public Bitmap markImg;
    public int width;
    public int height;
    public int orientation;
    public int vMargin;
    public int hMargin;

    public Watermark(Bitmap img, int width, int height, int orientation, int vmargin, int hmargin) {
        markImg = img;
        this.width = width;
        this.height = height;
        this.orientation = orientation;
        vMargin = vmargin;
        hMargin = hmargin;
    }
}
