package com.shirbi.downfall;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class Output extends ConnectableImage {
    private Hole m_hole_in;

    public Output(Context context) {
        super(context);
    }

    public Output(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void AddHole(Hole hole, int base_angle) {
        m_hole_in = hole;
        hole.SetBaseAngle(this, base_angle);

        RelativeLayout relativeLayout = (RelativeLayout) this.getParent();
        relativeLayout.addView(hole);
        hole.SetAngle(0);
        hole.CheckConnection(m_connections);
    }

    public void TokenEntered() {
        Token token = m_hole_in.GetResident();
        ((ViewGroup)(token.getParent())).removeView(token);

        m_hole_in.SetResident(null);
    }
}
