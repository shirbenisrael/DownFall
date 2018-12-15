package com.shirbi.downfall;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import java.util.HashSet;
import java.util.Set;

public abstract class ConnectableImage extends RotatableImage {
    protected Set<Connection> m_connections;

    private void Init() {
        m_connections = new HashSet<Connection>();
    }

    public ConnectableImage(Context context) {
        super(context);
        Init();
    }

    public ConnectableImage(Context context, AttributeSet attrs) {
        super(context, attrs);
        Init();
    }

    public void SetLocation(int left, int top) {
        RelativeLayout relativeLayout = (RelativeLayout) this.getParent();
        RelativeLayout boardLayout = (RelativeLayout) relativeLayout.getParent();

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(m_diameter, m_diameter);

        params.leftMargin = left;
        params.topMargin = top;
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

        boardLayout.removeView(relativeLayout);
        boardLayout.addView(relativeLayout, params);
    }

    public abstract void AddHole(Hole hole, int base_angle);

    public void TokenUsed() {}
}
