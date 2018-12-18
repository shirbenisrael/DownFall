package com.shirbi.downfall;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

public class Wheel extends ConnectableImage {
    private double m_previous_angle, m_start_touch_angle, m_current_angle;
    private long m_start_time_milliseconds;
    private Timer m_timer;
    private int m_auto_rotate_angle;
    private Context m_context;

    private void Init() {

    }

    public Wheel(Context context) {
        super(context);
        m_context = context;
        Init();
    }

    public Wheel(Context context, AttributeSet attrs) {
        super(context, attrs);
        m_context = context;
        Init();
    }

    public void ConnectToInputQueue(InputTokenQueue input_queue, double bottom_angle)  {
        Connection connection = new Connection(input_queue, this, bottom_angle);
        m_connections.add(connection);
        input_queue.m_connections.add(connection);
    }



    public void Rotate(double angle) {
        super.Rotate(angle);

        for (Hole hole : m_holes) {
            hole.SetAngle(m_current_angle);
            hole.CheckConnection(m_connections);
        }
    }

    public void AddRotation(int angle) {
        m_auto_rotate_angle = angle;
        m_timer = new Timer();
        m_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }

        }, 0, 20);
    }

    private void TimerMethod() {
        ((Activity)m_context).runOnUiThread(m_timer_tick);
    }

    private Runnable m_timer_tick = new Runnable() {
        public void run() {
            if (m_auto_rotate_angle  ==0) {
                m_timer.cancel();
                m_previous_angle = m_current_angle;
            }

            if (m_auto_rotate_angle > 0) {
                m_current_angle++;
                m_auto_rotate_angle--;
            }

            if (m_auto_rotate_angle < 0) {
                m_current_angle--;
                m_auto_rotate_angle++;
            }

            Rotate(m_current_angle);
        }
    };

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
}
