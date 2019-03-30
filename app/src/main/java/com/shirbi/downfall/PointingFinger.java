package com.shirbi.downfall;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Timer;
import java.util.TimerTask;

public class PointingFinger extends ImageView {
    protected MainActivity m_activity;
    private Timer m_fade_out_timer;
    private float m_alpha;
    private float m_alpha_change;
    private boolean m_should_be_visible;

    public PointingFinger(Context context) {
        super(context);
        m_activity = (MainActivity)context;

        setImageResource(R.drawable.pointing_finger);
        m_should_be_visible = false;
        m_fade_out_timer = new Timer();
        setVisibility(INVISIBLE);
    }

    public void PointToObject(View anchor, int size) {
        if (getParent() != null) {
            ((ViewGroup)getParent()).removeView(this);
        }

        int top_anchor = ((RelativeLayout.LayoutParams)anchor.getLayoutParams()).topMargin;
        int left_anchor = ((RelativeLayout.LayoutParams)anchor.getLayoutParams()).leftMargin;

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);

        params.leftMargin = left_anchor - size / 2;
        params.topMargin = top_anchor  - size / 3;

        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

        RelativeLayout boardLayout = (RelativeLayout) anchor.getParent();
        boardLayout.addView(this, params);

        this.setZ(10);
    }

    public void StartCountDown() {
        setVisibility(INVISIBLE);
        m_alpha = (float)1.0;
        m_alpha_change = (float)-0.01;
        m_should_be_visible = false;
        m_fade_out_timer.cancel();
        m_fade_out_timer = new Timer();

        m_fade_out_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                FadeOutTimerMethod();
            }

        }, 3000, 10);
    }

    private void FadeOutTimerMethod() {
        m_should_be_visible = true;
        m_activity.runOnUiThread(m_fade_out_timer_tick);
    }

    private Runnable m_fade_out_timer_tick = new Runnable() {
        public void run() {
            if (!m_should_be_visible) {
                return;
            }
            setVisibility(VISIBLE);
            m_alpha += m_alpha_change;
            setAlpha(m_alpha);
            if (m_alpha <= 0.1 || m_alpha >= 1) {
                m_alpha_change *= (float)-1;
            }
        }
    };

    public void Hide() {
        setVisibility(INVISIBLE);
        m_alpha = (float)1.0;
        m_alpha_change = (float)-0.01;
        m_should_be_visible = false;
        m_fade_out_timer.cancel();
        m_fade_out_timer = new Timer();
    }
}
