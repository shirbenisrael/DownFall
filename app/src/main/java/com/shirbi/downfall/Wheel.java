package com.shirbi.downfall;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class Wheel extends ImageButton {
    double m_angle = 0;

    public Wheel(Context context) {
        super(context);
    }

    public Wheel(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
