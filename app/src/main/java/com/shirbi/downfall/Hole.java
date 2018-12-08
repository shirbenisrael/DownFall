package com.shirbi.downfall;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class Hole extends RotatableImage {
    private int m_baseAngle;

    public Hole(Context context) {
        super(context);
    }

    public Hole(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void SetBaseAngle(int angle) {
        m_baseAngle = angle;
    }

    public void SetAngle(double wheelAngle) {
        RelativeLayout relativeLayout = (RelativeLayout) this.getParent();
        double angle = wheelAngle + m_baseAngle;

        getLayoutParams().width = relativeLayout.getLayoutParams().width;
        getLayoutParams().height = relativeLayout.getLayoutParams().height;

        super.Rotate(angle);
    }

}
