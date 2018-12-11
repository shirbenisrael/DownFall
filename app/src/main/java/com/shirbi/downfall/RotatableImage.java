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

    protected int m_diameter;

    public void SetDiameter(int diameter) {
        m_diameter = diameter;
    }

    public void Rotate(double angle) {
        Matrix matrix = new Matrix();
        setScaleType(ImageView.ScaleType.MATRIX);   //required

        int pivotXY = m_diameter /2;

        float scaleFactor = m_diameter/(float)getDrawable().getIntrinsicWidth();
        matrix.setScale(scaleFactor, scaleFactor, 0, 0);
        matrix.postRotate((float) angle, pivotXY, pivotXY);
        setImageMatrix(matrix);
    }
}
