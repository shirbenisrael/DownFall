package com.shirbi.downfall;

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
    protected MainActivity m_activity;
    private Timer m_timer;
    private int m_count_down;
    private Token m_this_token;
    private Token.HORIZONTAL_ALIGNMENT m_move_animation_direction;
    private SlideToken m_slider;
    RotatableImage m_connected_image;
    RotatableImage m_number_image;
    private float m_alpha;
    private Timer m_fade_out_timer;
    private ConnectableImage m_owner_wheel;
    private Boolean m_is_inflate;
    private int m_inflate_center_x;
    private int m_inflate_center_y;

    static Token m_token_list[][][] = new Token[PlayerType.NUM_PLAYERS][Token.COLOR.NUM_COLORS][5];

    enum COLOR {
        COLOR_1(0), COLOR_2(1);

        static public final int NUM_COLORS = 2;

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
        m_is_inflate = false;
        m_owner_wheel = null;
        m_player_type = PlayerType.PLAYER_0;
        m_this_token = this;
        m_color = COLOR.COLOR_1;
        m_number = 1;
        m_number_image = new RotatableImage(m_activity);
        m_connected_image = new RotatableImage(m_activity);

        m_connected_image.setImageResource(R.drawable.token_connected);
        SetDiameter(m_activity.GetTokenDiameter());
    }

    public Token(Context context) {
        super(context);
        m_activity = (MainActivity)context;
        Init();
    }

    public Token(Context context, AttributeSet attrs) {
        super(context, attrs);
        m_activity = (MainActivity)context;
        Init();
    }

    public void SetPlayerType(PlayerType playerType) {
        m_player_type = playerType;
        UpdateImage();
        RulesChanged();
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
        if (m_player_type == m_activity.GetPlayerType().GetOpposite()) {
            m_activity.GetObjectVisibility().SetOnView(m_connected_image);
        } else {
            m_connected_image.setVisibility(VISIBLE);
        }
    }


    private void UpdateLocationWhenInflate(ViewGroup new_parent) {
        RelativeLayout.LayoutParams token_params =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        token_params.leftMargin = m_inflate_center_x - m_diameter / 2;
        token_params.topMargin = m_inflate_center_y - m_diameter / 2;

        SetParentView(new_parent, token_params);

        /* Actually this is resize */
        Rotate(0);
    }

    public void Inflate(ViewGroup new_parent, int token_center_x, int token_center_y) {
        m_inflate_center_x = token_center_x;
        m_inflate_center_y = token_center_y;
        m_is_inflate = true;

        UpdateLocationWhenInflate(new_parent);

        FadeOut();
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

        /* Make sure than tokens of player hide opponent's tokens on input queue. */
        PlayerType player_type = m_activity.GetPlayerType();
        this.setZ(m_player_type == player_type ? 4 : 1);
        m_connected_image.setZ(m_player_type == player_type ? 5 : 2);
        m_number_image.setZ(m_player_type == player_type ? 6 : 3);
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

    public void ShowOnExit() {
        setVisibility(VISIBLE);
        m_number_image.setVisibility(VISIBLE);
    }

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
        m_activity.runOnUiThread(m_timer_tick);
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

    public void RulesChanged() {
        ObjectVisibility visibility = (m_player_type == m_activity.GetPlayerType()) ?
                ObjectVisibility.ALWAYS_VISIBLE : m_activity.GetObjectVisibility();

        visibility.SetOnView(this);
        visibility.SetOnView(m_connected_image);
        visibility.SetOnView(m_number_image);
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

    public void FadeOut() {
        setVisibility(VISIBLE);
        m_connected_image.setVisibility(VISIBLE);
        m_number_image.setVisibility(VISIBLE);

        m_alpha = (float)1.0;
        m_fade_out_timer = new Timer();
        m_fade_out_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                FadeOutTimerMethod();
            }

        }, 0, 30);
    }

    private void FadeOutTimerMethod() {
        if (m_alpha < 0.1) {
            m_fade_out_timer.cancel();
            return;
        }

        m_activity.runOnUiThread(m_fade_out_timer_tick);
    }

    private void SetAlphaOnImages() {
        setAlpha(m_alpha);
        m_connected_image.setAlpha(m_alpha);
        m_number_image.setAlpha(m_alpha);
    }

    private Runnable m_fade_out_timer_tick = new Runnable() {
        public void run() {
            m_alpha -= 0.01;
            SetAlphaOnImages();

            if (m_is_inflate) {
                m_diameter += GameConstants.TOKEN_INFLATE_SPEED;
                m_connected_image.m_diameter += GameConstants.TOKEN_INFLATE_SPEED;;
                m_number_image.m_diameter += GameConstants.TOKEN_INFLATE_SPEED;;
                UpdateLocationWhenInflate((ViewGroup)getParent());
            }

            if (m_alpha < 0.1) {
                m_alpha = (float)1.0;
                m_fade_out_timer.cancel();
                setVisibility(INVISIBLE);
                m_connected_image.setVisibility(INVISIBLE);
                m_number_image.setVisibility(INVISIBLE);
                SetAlphaOnImages();

                if (m_is_inflate) {
                    RemoveFromParentView();
                }
            }
        }
    };

    public void Register() {
        m_token_list[GetPlayerType().getInt()][GetColor().getInt()][GetNumber() - 1] = this;
    }

    public void Unregister() {
        m_token_list[GetPlayerType().getInt()][GetColor().getInt()][GetNumber() - 1] = null;
    }

    public Token GetPreviousToken() {
        if (GetNumber() == 1) {
            return null;
        }
        return m_token_list[GetPlayerType().getInt()][GetColor().getInt()][GetNumber() - 2];
    }

    public void SetOwnerWheel(ConnectableImage owner) {
        m_owner_wheel = owner;
    }

    public ConnectableImage GetOwnerWheel() {
        return m_owner_wheel;
    }
}
