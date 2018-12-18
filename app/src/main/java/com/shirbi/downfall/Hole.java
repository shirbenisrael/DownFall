package com.shirbi.downfall;

import android.content.Context;
import android.media.MediaPlayer;
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
    private MediaPlayer m_media_player;
    private Context m_context;

    public void Init() {
        setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        setImageResource(R.drawable.hole);
    }

    public Hole(Context context) {
        super(context);
        Init();
        m_context = context;
    }

    public Hole(Context context, AttributeSet attrs) {
        super(context, attrs);
        Init();
        m_context = context;
    }

    public void SetBaseAngle(ConnectableImage owner_wheel, int angle) {
        m_owner_wheel = owner_wheel;
        m_baseAngle = angle;
    }

    public int GetBaseAngle() {
        return m_baseAngle;
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
            // Use same parameters for hole and resident.
            // Remember that if we want to change this, we need to use copy constructor to
            // create new params for the resident.
            m_resident.SetParentView(relativeLayout, params);
        }

        m_current_angle = angle;
    }

    public void SetResident(Token resident) {
        m_resident = resident;
    }

    public Boolean HasResident() {
        return (m_resident != null);
    }

    public Token GetResident() {
        return m_resident;
    }

    public void CheckConnection(Set<Connection> connections) {
        boolean connected = false;
        boolean connected_top = false;

        for (Connection connection : connections) {
            if (connection.CompareHoleAngle(m_owner_wheel, this, m_current_angle)) {
                connected_top = connection.IsTopWheel(m_owner_wheel);
                setImageResource(R.drawable.hole_connected);
                connected = true;
                break;
            }
        }

        if (!connected) {
            setImageResource(R.drawable.hole);
        }

        if (m_resident != null) {
            if (connected_top) {
                m_resident.SetImageConnected();
            } else {
                // When token already fallen, no reason to color it.
                m_resident.SetImageDisconnected();
            }
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

        m_owner_wheel.TokenUsed();
        bottom_hole.m_owner_wheel.TokenEntered();

        m_media_player = MediaPlayer.create(m_context, R.raw.token_fall);
        m_media_player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
        m_media_player.start();
    }
}
