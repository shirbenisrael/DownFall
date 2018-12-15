package com.shirbi.downfall;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.HashSet;
import java.util.Set;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Wheel extends ConnectableImage {
    private double m_previous_angle, m_start_touch_angle, m_current_angle;
    private long m_start_time_milliseconds;

    private Set<Hole> m_holes;

    private void Init() {
        m_holes = new HashSet<Hole>();
    }

    public Wheel(Context context) {
        super(context);
        Init();
    }

    public Wheel(Context context, AttributeSet attrs) {
        super(context, attrs);
        Init();
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
        m_holes.add(hole);
        hole.SetBaseAngle(this, base_angle);

        RelativeLayout relativeLayout = (RelativeLayout) this.getParent();
        relativeLayout.addView(hole);
        hole.SetAngle(0);
    }

    public void ConnectToInputQueue(InputTokenQueue input_queue, double bottom_angle)  {
        Connection connection = new Connection(input_queue, this, bottom_angle);
        m_connections.add(connection);
        input_queue.m_connections.add(connection);
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

    public void Rotate(double angle) {
        super.Rotate(angle);

        for (Hole hole : m_holes) {
            hole.SetAngle(m_current_angle);
            hole.CheckConnection(m_connections);
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        final float xc = m_diameter / 2;
        final float yc = m_diameter / 2;

        final float x = event.getX();
        final float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                m_start_touch_angle = Math.toDegrees(Math.atan2(x - xc, yc - y));
                m_start_time_milliseconds = System.currentTimeMillis();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                double new_angle = Math.toDegrees(Math.atan2(x - xc, yc - y));

                long current_time_milliseconds = System.currentTimeMillis();
                long delta_time_milliseconds = current_time_milliseconds - m_start_time_milliseconds;
                double rotation_angle = new_angle - m_start_touch_angle;
                while (rotation_angle < -180) {
                    rotation_angle += 360;
                }
                while(rotation_angle > 180) {
                    rotation_angle -= 360;
                }

                m_start_time_milliseconds = current_time_milliseconds;
                if (Math.abs(rotation_angle / (delta_time_milliseconds)) < 0.5) {
                    m_current_angle = (m_previous_angle + rotation_angle);
                    int round_down = (((int)m_current_angle) / 360) * 360;
                    m_current_angle -= round_down;

                    m_previous_angle = m_current_angle;
                    m_start_touch_angle = new_angle;

                    Rotate(m_current_angle);
                }

                break;
            }
            case MotionEvent.ACTION_UP: {
                m_previous_angle = m_current_angle;
                break;
            }
        }

        return true;
    }

    //temp
    public void SetToken(Token token) {
        m_holes.iterator().next().SetResident(token);
    }
}
