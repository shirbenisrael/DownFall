package com.shirbi.downfall;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import java.util.ArrayList;

public class InputTokenQueue extends ConnectableImage {

    private static final int MAX_TOKENS = 5;

    private ArrayList<Token> m_tokens;

    private Hole m_hole_out;

    private void Init(Context context) {
        m_tokens = new ArrayList<Token>();
        m_hole_out = new Hole(context);
    }

    public InputTokenQueue(Context context) {
        super(context);
        Init(context);
    }

    public InputTokenQueue(Context context, AttributeSet attrs) {
        super(context, attrs);
        Init(context);
    }

    public void AddToken(Token token) {
        if (m_hole_out.HasResident()) {
            m_tokens.add(token);
        } else {
            m_hole_out.SetResident(token);
            m_hole_out.SetAngle(0); // This will make the token shown */
        }
    }

    public void TokenUsed() {
        if (m_tokens.isEmpty()) {
            return;
        }

        Token token = m_tokens.get(0);
        m_hole_out.SetResident(token);
        m_hole_out.SetAngle(0); // This will make the token shown */
        m_tokens.remove(0);
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

    public void AddHole(Hole hole, int base_angle) {
        m_hole_out = hole;
        hole.SetBaseAngle(this, base_angle);

        RelativeLayout relativeLayout = (RelativeLayout) this.getParent();
        relativeLayout.addView(hole);
        hole.SetAngle(0);
        hole.CheckConnection(m_connections);
    }


}
