package com.shirbi.downfall;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.Timer;
import java.util.TimerTask;

public class Output extends ConnectableImage {
    private Hole m_hole_in;
    private Timer m_timer;
    private int m_count_down;
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
        m_hole_in = hole;
        hole.SetBaseAngle(this, base_angle);

        RelativeLayout relativeLayout = (RelativeLayout) this.getParent();
        relativeLayout.addView(hole);
        hole.SetAngle(0);
        hole.CheckConnection(m_connections);
    }

    public void TokenEntered() {
        m_count_down = 5;
        m_timer = new Timer();

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
            Token token = m_hole_in.GetResident();
            m_count_down--;

            if (m_count_down == 0) {
                m_timer.cancel();
                ((ViewGroup) (token.getParent())).removeView(token);
                m_hole_in.SetResident(null);
            } else {
                int angle = m_hole_in.GetBaseAngle();
                token.SetLocationNearOtherToken(token,
                        Token.HORIZONTAL_ALIGNMENT.LEFT_EDJE,
                        Token.VERTICAL_ALIGNMENT.BOTTOM);
            }
        }
    };
}
