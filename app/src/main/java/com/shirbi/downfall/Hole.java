package com.shirbi.downfall;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.View;
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
    private PlayerType m_player_type;

    private static final float ALPHA_FOR_OPPOSITE = (float) 0.2;

    public void Init() {
        setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        setImageResource(R.drawable.hole);

        m_player_type = PlayerType.PLAYER_0;
    }

    public Hole(Context context) {
        super(context);
        Init();
    }

    public Hole(Context context, AttributeSet attrs) {
        super(context, attrs);
        Init();
    }

    public void RulesChanged() {
        if (m_player_type == m_activity.GetPlayerType()) {
            ((View) this).setAlpha((float)1.0);
            ObjectVisibility.ALWAYS_VISIBLE.SetOnView(this);
        } else {
            ((View) this).setAlpha(ALPHA_FOR_OPPOSITE);
            m_activity.GetObjectVisibility().SetOnView(this);
        }

        if (m_resident != null) {
            m_resident.RulesChanged();
        }
    }

    public void SetPlayerType(PlayerType player_type) {
        m_player_type = player_type;
        RulesChanged();
    }

    public PlayerType GetPlayerType() {
        return m_player_type;
    }

    public void SetBaseAngle(ConnectableImage owner_wheel, int angle) {
        m_owner_wheel = owner_wheel;
        m_baseAngle = angle;
    }

    public int GetBaseAngle() {
        return m_baseAngle;
    }

    public double GetCurrentAngle() {
        return m_current_angle;
    }

    public void SetAngle(double wheelAngle) {
        RelativeLayout relativeLayout = (RelativeLayout) this.getParent();

        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        double centerXY = relativeLayout.getLayoutParams().width / 2;
        double radius = (centerXY - (m_diameter / 2)) *0.95;
        double angle = wheelAngle + m_baseAngle;
        double angleRadians = Math.toRadians(angle);
        double leftFromCenter = radius * Math.sin(angleRadians);
        double topFromCenter = -radius * Math.cos(angleRadians);

        params.leftMargin = (int) (centerXY + leftFromCenter) - (m_diameter / 2);
        params.topMargin = (int) (centerXY + topFromCenter) - (m_diameter / 2);
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
        if (m_resident != null) {
            resident.SetOwnerWheel(m_owner_wheel);
        }
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
        if (m_player_type != bottom_hole.m_player_type) {
            return;
        }

        bottom_hole.SetResident(m_resident);
        m_resident = null;

        bottom_hole.SetAngle(bottom_hole.m_current_angle - bottom_hole.m_baseAngle);

        m_owner_wheel.TokenUsed(this);

        if (m_activity.GetObjectVisibility() == ObjectVisibility.VISIBLE_ON_CONNECT) {
            if (m_player_type != m_activity.GetPlayerType())
            bottom_hole.m_resident.FadeOut();
        }

        bottom_hole.m_owner_wheel.TokenEntered(bottom_hole);

        m_activity.PlaySound(R.raw.token_fall);
    }

    public void StoreState(String prefix, SharedPreferences.Editor editor) {
        Token token = GetResident();
        int data = -1;
        if (token != null) {
            data = token.GetNumber() * 10 + token.GetColor().getInt();
        }

        String key = prefix + "_" + m_baseAngle;
        editor.putInt(key, (int) data);
    }

    public void RestoreState(String prefix, SharedPreferences sharedPref) {
        String key = prefix + "_" + m_baseAngle;
        int data = sharedPref.getInt(key, -1);

        if (data != -1) {
            Token token = new Token(m_activity);
            token.SetPlayerType(m_player_type);
            Token.COLOR color = Token.COLOR.values()[data % 10];
            int number = data / 10;

            token.SetType(color, number);
            token.Rotate(0); /* This will scale the token image to the correct size */
            token.Register();

            SetResident(token);
            SetAngle(m_current_angle - m_baseAngle); /* Will put the token on the hole */
        }
    }
}
