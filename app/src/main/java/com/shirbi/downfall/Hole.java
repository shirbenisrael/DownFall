package com.shirbi.downfall;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
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
        RelativeLayout relativeLayout = (RelativeLayout)this.getParent();
        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        double centerXY = relativeLayout.getWidth()/2;
        double radius = centerXY * 0.8;
        double angle = wheelAngle + m_baseAngle;
        double angleRadians = Math.toRadians(angle);
        double leftFromCenter = radius * Math.sin(angleRadians);
        double topFromCenter = -radius * Math.cos(angleRadians);

        params.leftMargin = (int)(centerXY + leftFromCenter);
        params.topMargin  = (int)(centerXY + topFromCenter);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

        relativeLayout.removeView(this);
        relativeLayout.addView(this, params);

        super.Rotate(angle);
    }

}
