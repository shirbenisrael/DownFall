package com.shirbi.downfall;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import java.util.HashSet;
import java.util.Set;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

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

    public void UpdateDisplay(int diameter) {
        SetDiameter(diameter);

        RelativeLayout relativeLayout = (RelativeLayout) this.getParent();

        relativeLayout.getLayoutParams().width = diameter;
        relativeLayout.getLayoutParams().height = diameter;

        getLayoutParams().width = diameter;
        getLayoutParams().height = diameter;

        requestLayout();
        relativeLayout.requestLayout();
    }

    public void ConnectAsBottom(Wheel top_wheel, double bottom_angle) {
        Connection connection = new Connection(top_wheel, this, bottom_angle);
        m_connections.add(connection);
        top_wheel.m_connections.add(connection);

        RelativeLayout topWheelRelativeLayout = (RelativeLayout) top_wheel.getParent();
        double top = ((RelativeLayout.LayoutParams)topWheelRelativeLayout.getLayoutParams()).topMargin;
        double left = ((RelativeLayout.LayoutParams)topWheelRelativeLayout.getLayoutParams()).leftMargin;

        top += top_wheel.m_diameter / 2;
        left += top_wheel.m_diameter / 2;

        double hypotenuse = (m_diameter + top_wheel.m_diameter)/2;

        double angleRadians = Math.toRadians(bottom_angle);
        top += cos(angleRadians) * hypotenuse;
        left -= sin(angleRadians) * hypotenuse;

        top -= m_diameter / 2;
        left -= m_diameter / 2;

        SetLocation((int)left,(int)top);
    }

    public abstract void AddHole(Hole hole, int base_angle);

    public void TokenUsed() {}

    public void TokenEntered() {}
}
