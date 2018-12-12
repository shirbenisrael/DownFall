package com.shirbi.downfall;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import java.util.ArrayList;

public class InputTokenQueue extends RotatableImage {

    private static final int MAX_TOKENS = 5;

    private ArrayList<Token> m_tokens;

    private Connection m_connection;

    private void Init() {
        m_tokens = new ArrayList<Token>();
    }

    public InputTokenQueue(Context context) {
        super(context);
        Init();
    }

    public InputTokenQueue(Context context, AttributeSet attrs) {
        super(context, attrs);
        Init();
    }

    public void AddToken(Token token) {
        m_tokens.add(token);
    }

    public void SetLocation(int left, int top) {
        RelativeLayout relativeLayout = (RelativeLayout) this.getParent();
        RelativeLayout boardLayout = (RelativeLayout) relativeLayout.getParent();

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(m_diameter, m_diameter);

        params.leftMargin = left;
        params.topMargin  = top;
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

        boardLayout.removeView(relativeLayout);
        boardLayout.addView(relativeLayout, params);
    }

    public void UpdateDisplay(int diameter) {
        SetDiameter(diameter);

        RelativeLayout relativeLayout = (RelativeLayout)this.getParent();

        relativeLayout.getLayoutParams().width = diameter;
        relativeLayout.getLayoutParams().height = diameter;

        getLayoutParams().width = diameter;
        getLayoutParams().height = diameter;

        requestLayout();
        relativeLayout.requestLayout();
    }


}
