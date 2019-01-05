package com.shirbi.downfall;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;

import java.util.Timer;
import java.util.TimerTask;

public class Token extends RotatableImage {

    private static final int m_numbers_images[] = {R.drawable.token_1, R.drawable.token_2, R.drawable.token_3, R.drawable.token_4, R.drawable.token_5};
    private static final int m_color_images[] = {R.drawable.token_red, R.drawable.token_yellow, R.drawable.token_blue, R.drawable.token_green};

    private int m_number;
    private COLOR m_color;
    public PlayerType m_player_type;
    private Context m_context;
    private Timer m_timer;
    private int m_count_down;
    private Token m_this_token;
    private Token.HORIZONTAL_ALIGNMENT m_move_animation_direction;
    private SlideToken m_slider;
    private ObjectVisibility m_visibility;
    RotatableImage m_connected_image;
    RotatableImage m_number_image;

    enum COLOR {
        COLOR_1(0), COLOR_2(1);

        private final int m_color;

        COLOR(int color) {
            this.m_color = color;
        }

        public int getInt() {
            return this.m_color;
        }
    }

    // TODO: We have the same in Hole. Move it to different file.
    private static final float ALPHA_FOR_OPPOSITE = (float)0.2;

    private void Init() {
        m_player_type = PlayerType.HUMAN_PLAYER;
        m_this_token = this;
        m_color = COLOR.COLOR_1;
        m_number = 1;
        m_visibility = ObjectVisibility.ALWAYS_VISIBLE;
        m_number_image = new RotatableImage(m_activity);
        m_connected_image = new RotatableImage(m_activity);

        m_connected_image.setImageResource(R.drawable.token_connected);
    }

    public Token(Context context) {
        super(context);
        m_context = context;
        Init();
    }

    public Token(Context context, AttributeSet attrs) {
        super(context, attrs);
        m_context = context;
        Init();
    }

    public void SetPlayerType(PlayerType playerType) {
        m_player_type = playerType;
        UpdateImage();
    }

    public PlayerType GetPlayerType() {
        return m_player_type;
    }

    private void UpdateImage() {
        setImageResource(m_color_images[m_color.getInt() + m_player_type.getInt() * 2]);
    }

    public void SetType(COLOR color, int number) {
        m_number = number;
        m_color = color;

        m_number_image.setImageResource(m_numbers_images[m_number-1]);
        UpdateImage();

        SetImageDisconnected();
    }

    public Token.COLOR GetColor() { return m_color; }

    public void SetImageDisconnected() {
        m_connected_image.setVisibility(INVISIBLE);
    }

    public void SetImageConnected() {
        if ((m_player_type == PlayerType.AI_PLAYER) &&
                (m_visibility == ObjectVisibility.INVISIBLE)) {
            m_connected_image.setVisibility(INVISIBLE);
        } else {
            m_connected_image.setVisibility(VISIBLE);
        }
    }

    public void SetParentView(ViewGroup newParent, RelativeLayout.LayoutParams params) {
        ViewParent oldParent = getParent();
        if (oldParent != null) {
            ((ViewGroup)oldParent).removeView(this);
        }

        oldParent = m_connected_image.getParent();
        if (oldParent != null) {
            ((ViewGroup)oldParent).removeView(m_connected_image);
        }

        oldParent = m_number_image.getParent();
        if (oldParent != null) {
            ((ViewGroup)oldParent).removeView(m_number_image);
        }

        newParent.addView(this, params);
        newParent.addView(m_connected_image, params);
        newParent.addView(m_number_image, params);

        this.setZ(m_player_type == PlayerType.HUMAN_PLAYER ? 4 : 1);
        m_connected_image.setZ(m_player_type == PlayerType.HUMAN_PLAYER ? 5 : 2);
        m_number_image.setZ(m_player_type == PlayerType.HUMAN_PLAYER ? 6 : 3);
    }

    public enum HORIZONTAL_ALIGNMENT {
        LEFT_EDJE,
        RIGHT_EDJE,
    };

    public enum VERTICAL_ALIGNMENT {
        TOP,
        BOTTOM,
    }

    public void SetLocationNearOtherToken( Token other_token, HORIZONTAL_ALIGNMENT hor, VERTICAL_ALIGNMENT ver) {
        RelativeLayout relativeLayout = (RelativeLayout) other_token.getParent();

        RelativeLayout.LayoutParams other_token_params = (RelativeLayout.LayoutParams)other_token.getLayoutParams();
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(other_token_params);

        if (hor == HORIZONTAL_ALIGNMENT.LEFT_EDJE) {
            params.leftMargin =  other_token_params.leftMargin - m_diameter;
        } else {
            params.leftMargin =  other_token_params.leftMargin + m_diameter;
        }

        if (ver == VERTICAL_ALIGNMENT.TOP) {
            params.topMargin = other_token_params.topMargin - (m_diameter / 2);
        } else {
            params.topMargin = other_token_params.topMargin + (m_diameter / 2);
        }

        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

        SetParentView(relativeLayout, params);
    }

    public void SetStartingLocationSlidingToHole(Hole hole, Token.HORIZONTAL_ALIGNMENT hor) {
        RelativeLayout relativeLayout = (RelativeLayout) hole.getParent();
        RelativeLayout.LayoutParams hole_params = (RelativeLayout.LayoutParams)hole.getLayoutParams();
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(hole_params);

        if (hor == HORIZONTAL_ALIGNMENT.LEFT_EDJE) {
            params.leftMargin =  hole_params.leftMargin - m_diameter * 4;
        } else {
            params.leftMargin =  hole_params.leftMargin + m_diameter * 4;
        }

        params.topMargin = hole_params.topMargin - ((m_diameter / 2) * 4);

        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

        SetParentView(relativeLayout, params);
    }

    public int GetNumber() { return m_number; }

    public void QueueAnimation(int num_moves, Token.HORIZONTAL_ALIGNMENT direction, SlideToken slider) {
        m_count_down = num_moves;
        m_move_animation_direction = direction;
        m_slider = slider;
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
            m_count_down--;

            if (m_count_down == 0) {
                m_timer.cancel();
                m_slider.TokenStoppedMoving(m_this_token);
            } else {
                SetLocationNearOtherToken(m_this_token,
                        m_move_animation_direction,
                        Token.VERTICAL_ALIGNMENT.BOTTOM);
            }
        }
    };

    public void SetOppositePlayerObjectsVisibility(ObjectVisibility visibility) {
        if (m_player_type == PlayerType.HUMAN_PLAYER) {
            return;
        }

        m_visibility = visibility;
        m_visibility.SetOnView(this);
        m_visibility.SetOnView(m_connected_image);
        m_visibility.SetOnView(m_number_image);
    }

    public void Rotate(double angle) {
        super.Rotate(angle);
        m_connected_image.Rotate(angle);
        m_number_image.Rotate(angle);
    }

    public void SetDiameter(int diameter) {
        super.SetDiameter(diameter);
        m_connected_image.SetDiameter(diameter);
        m_number_image.SetDiameter(diameter);
    }

    public void RemoveFromParentView() {
        ((ViewGroup)(this.getParent())).removeView(this);
        ((ViewGroup)(m_connected_image.getParent())).removeView(m_connected_image);
        ((ViewGroup)(m_number_image.getParent())).removeView(m_number_image);
    }

}
