package com.shirbi.downfall;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RotatableImage extends ImageView {
    public RotatableImage(Context context) {
        super(context);
    }

    public RotatableImage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void Rotate(double angle) {
        Matrix matrix = new Matrix();
        setScaleType(ImageView.ScaleType.MATRIX);   //required

        int pivotX = getWidth() / 2;
        int pivotY = getHeight() / 2;

        float scaleFactor = getWidth()/(float)getDrawable().getIntrinsicWidth();
        matrix.setScale(scaleFactor, scaleFactor, 0, 0);
        matrix.postRotate((float) angle, pivotX, pivotY);
        setImageMatrix(matrix);
    }
}
