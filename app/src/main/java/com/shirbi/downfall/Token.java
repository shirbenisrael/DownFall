package com.shirbi.downfall;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class Token extends ImageView {

    public Token(Context context) {
        super(context);
    }

    public Token(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void SetParentView(ViewGroup newParent, RelativeLayout.LayoutParams params) {
        ViewParent oldParent = getParent();

        if (oldParent != null) {
            ((ViewGroup)oldParent).removeView(this);
        }
        newParent.addView(this, params);
    }
}
