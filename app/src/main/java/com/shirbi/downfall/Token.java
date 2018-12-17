package com.shirbi.downfall;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;

public class Token extends RotatableImage {

    static final int color_1_images[] = {R.drawable.token1_1, R.drawable.token1_2,
            R.drawable.token1_3, R.drawable.token1_4, R.drawable.token1_5};
    static final int color_2_images[] = {R.drawable.token2_1, R.drawable.token2_2,
            R.drawable.token2_3, R.drawable.token2_4, R.drawable.token2_5};

    private int m_number;
    private COLOR m_color;

    enum COLOR {
        COLOR_1,
        COLOR_2
    }

    public Token(Context context) {
        super(context);
    }

    public Token(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void SetType(COLOR color, int number) {
        m_number = number;
        m_color = color;
        switch (color) {
            case COLOR_1:
                setImageResource(color_1_images[number-1]);
                break;

            case COLOR_2:
                setImageResource(color_2_images[number-1]);
                break;
        }
    }

    public void SetParentView(ViewGroup newParent, RelativeLayout.LayoutParams params) {
        ViewParent oldParent = getParent();

        if (oldParent != null) {
            ((ViewGroup)oldParent).removeView(this);
        }

        newParent.addView(this, params);
    }

    public enum HORIZONTAL_ALIGNMENT {
        LEFT_EDJE,
        RIGHT_EDJE,
    };

    public enum VERTICAL_ALIGNMENT {
        TOP,
        BOTTOM,
    }

    public void SetLocationNearOtherToken( Token other_token, HORIZONTAL_ALIGNMENT hor, VERTICAL_ALIGNMENT ver) {
        RelativeLayout relativeLayout = (RelativeLayout) other_token.getParent();

        RelativeLayout.LayoutParams other_token_params = (RelativeLayout.LayoutParams)other_token.getLayoutParams();
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(other_token_params);

        if (hor == HORIZONTAL_ALIGNMENT.LEFT_EDJE) {
            params.leftMargin =  other_token_params.leftMargin - m_diameter;
        } else {
            params.leftMargin =  other_token_params.leftMargin + m_diameter;
        }

        if (ver == VERTICAL_ALIGNMENT.TOP) {
            params.topMargin = other_token_params.topMargin - (m_diameter / 2);
        } else {
            params.topMargin = other_token_params.topMargin + (m_diameter / 2);
        }

        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

        SetParentView(relativeLayout, params);
    }
}
