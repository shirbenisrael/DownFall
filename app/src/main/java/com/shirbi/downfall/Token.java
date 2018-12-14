package com.shirbi.downfall;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;

public class Token extends RotatableImage {

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

    public void SetLocationNearOtherToken( Token other_token, int angle) {
        RelativeLayout relativeLayout = (RelativeLayout) other_token.getParent();

        RelativeLayout.LayoutParams other_token_params = (RelativeLayout.LayoutParams)other_token.getLayoutParams();
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(other_token_params);

        if (angle < 180) {
            params.leftMargin =  other_token_params.leftMargin - m_diameter;
        } else {
            params.leftMargin =  other_token_params.leftMargin + m_diameter;

        }

        params.topMargin  = other_token_params.topMargin - (m_diameter / 2);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

        SetParentView(relativeLayout, params);
    }
}
