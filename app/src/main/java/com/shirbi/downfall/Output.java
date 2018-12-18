package com.shirbi.downfall;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import java.util.Timer;
import java.util.TimerTask;

public class Output extends ConnectableImage {
    private Timer m_timer;
    private int m_count_down;
    private Hole m_hole_with_animation;
    Context m_context;

    public Output(Context context) {
        super(context);
        m_context = context;
    }

    public Output(Context context, AttributeSet attrs) {
        super(context, attrs);
        m_context = context;
    }

    @Override
    public void AddHole(Hole hole, int base_angle) {
        super.AddHole(hole, base_angle);

        hole.CheckConnection(m_connections);
    }

    public void TokenEntered(Hole hole) {
        m_count_down = 5;
        m_timer = new Timer();
        m_hole_with_animation = hole;

        m_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }

        }, 0, 100);
    }

    private void TimerMethod() {
        ((Activity)m_context).runOnUiThread(m_timer_tick);
    }

    private Runnable m_timer_tick = new Runnable() {
        public void run() {
            Token token = m_hole_with_animation.GetResident();
            m_count_down--;

            if (m_count_down == 0) {
                m_timer.cancel();
                ((ViewGroup) (token.getParent())).removeView(token);
                m_hole_with_animation.SetResident(null);
            } else {
                int angle = m_hole_with_animation.GetBaseAngle();
                token.SetLocationNearOtherToken(token,
                        Token.HORIZONTAL_ALIGNMENT.LEFT_EDJE,
                        Token.VERTICAL_ALIGNMENT.BOTTOM);
            }
        }
    };
}
