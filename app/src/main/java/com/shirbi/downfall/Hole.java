package com.shirbi.downfall;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.util.Set;

public class Hole extends RotatableImage {
    private int m_baseAngle;
    private double m_current_angle;
    private ConnectableImage m_owner_wheel;
    private Token m_resident;

    public void Init() {
        setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        setImageResource(R.drawable.hole);
    }

    public Hole(Context context) {
        super(context);
        Init();
    }

    public Hole(Context context, AttributeSet attrs) {
        super(context, attrs);
        Init();
    }

    public void SetBaseAngle(ConnectableImage owner_wheel, int angle) {
        m_owner_wheel = owner_wheel;
        m_baseAngle = angle;
    }

    public void SetAngle(double wheelAngle) {
        RelativeLayout relativeLayout = (RelativeLayout) this.getParent();

        RelativeLayout.LayoutParams params =
           new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                   ViewGroup.LayoutParams.WRAP_CONTENT);
        double centerXY = relativeLayout.getLayoutParams().width/2;
        double radius = centerXY * 0.8;
        double angle = wheelAngle + m_baseAngle;
        double angleRadians = Math.toRadians(angle);
        double leftFromCenter = radius * Math.sin(angleRadians);
        double topFromCenter = -radius * Math.cos(angleRadians);

        params.leftMargin = (int)(centerXY + leftFromCenter) - (m_diameter / 2);
        params.topMargin  = (int)(centerXY + topFromCenter) - (m_diameter / 2);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

        relativeLayout.removeView(this);
        relativeLayout.addView(this, params);

        super.Rotate(angle);

        if (m_resident != null) {
            params.leftMargin = (int)(centerXY + leftFromCenter) - (m_resident.getWidth() / 2);
            params.topMargin  = (int)(centerXY + topFromCenter) - (m_resident.getHeight() / 2);

            m_resident.SetParentView(relativeLayout, params);
        }

        m_current_angle = angle;
    }

    public void SetResident(Token resident) {
        m_resident = resident;
    }

    public void CheckConnection(Set<Connection> connections) {
        boolean connected = false;
        for (Connection connection : connections) {
            if (connection.CompareHoleAngle(m_owner_wheel, this, m_current_angle)) {
                setImageResource(R.drawable.hole_connected);
                connected = true;
                break;
            }
        }

        if (!connected) {
            setImageResource(R.drawable.hole);
        }
    }

    public void FallDownToken(Hole bottom_hole) {
        if ((m_resident == null) || (bottom_hole == null)) {
            return;
        }
        if (bottom_hole.m_resident != null) {
            return;
        }

        bottom_hole.m_resident = m_resident;
        m_resident = null;

        bottom_hole.SetAngle(bottom_hole.m_current_angle - bottom_hole.m_baseAngle);
    }
}
