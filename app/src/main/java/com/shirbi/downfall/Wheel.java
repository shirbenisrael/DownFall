package com.shirbi.downfall;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Timer;
import java.util.TimerTask;

public class Wheel extends ConnectableImage {
    private int m_start_touch_angle; //angle of finger touch with respect to center.
    private int m_previous_angle, m_current_angle; //angles of wheel with respect to its base rotation.
    private int m_turn_rotation, m_max_turn_rotation; // number of degrees in this turn
    private long m_start_time_milliseconds;
    private Timer m_timer;
    private TimerTask m_timer_task;
    private int m_auto_rotate_angle;
    private boolean m_currently_auto_rotating;
    private Boolean m_allow_rotation;
    private int m_wheel_num;
    private ImageView m_touch_view;

    static private final int WHEEL_ROTATION_RATE = 15; // Larger = slower.

    enum ROTATION_DIRECTION {
        POSITIVE(0), NEGATIVE(1), UNDEFINED(2);

        static final int LOCK_DEGREES = 10;

        private final int m_direction;

        ROTATION_DIRECTION(int direction) {
            this.m_direction = direction;
        }

        public int getInt() {
            return this.m_direction;
        }
    }

    ROTATION_DIRECTION m_rotation_direction;

    private void Init() {
        m_touch_view = new ImageView(m_activity);
        m_touch_view.setImageResource(R.drawable.forbid_rotate);
        m_touch_view.setImageAlpha(50);
        SetAllowRotation(true);
    }

    public Wheel(Context context) {
        super(context);
        Init();
    }

    public Wheel(Context context, AttributeSet attrs) {
        super(context, attrs);
        Init();
    }

    public int GetCurrentAngle() { return m_current_angle; }

    public void ConnectToInputQueue(InputTokenQueue input_queue, int bottom_angle)  {
        Connection connection = new Connection(input_queue, this, bottom_angle);
        m_connections.add(connection);
        input_queue.m_connections.add(connection);
    }


    public void SetAngleByOtherPlayer(int angle) {
        m_current_angle = angle;
        Rotate(angle);
    }

    public void Rotate(int angle) {
        super.Rotate(angle);

        for (Hole hole : m_holes) {
            hole.SetAngle(m_current_angle);
            hole.CheckConnection(m_connections);
        }
    }

    public void AddRotation(int angle) {
        m_auto_rotate_angle = angle;
        m_timer = new Timer();
        m_currently_auto_rotating = true;
        m_timer_task = (new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }
        });

        m_timer.schedule(m_timer_task,0, WHEEL_ROTATION_RATE);
    }

    private void TimerMethod() {
        if (m_auto_rotate_angle  ==0) {
            m_timer.cancel();
            m_timer_task.cancel();
            m_timer = null;
            m_timer_task = null;
        }

        m_activity.runOnUiThread(m_timer_tick);
    }

    private Runnable m_timer_tick = new Runnable() {
        public void run() {
            if (m_auto_rotate_angle == 0) {
                m_previous_angle = m_current_angle;

                // There is a chance that this task was already queued couple of times to ui thread.
                // make sure it doesn't trigger the m_activity twice.
                if (m_currently_auto_rotating) {
                    m_activity.WheelFinishedRotating();
                }
                m_currently_auto_rotating = false;
            }

            if (m_auto_rotate_angle > 0) {
                m_current_angle++;
                m_auto_rotate_angle--;
            }

            if (m_auto_rotate_angle < 0) {
                m_current_angle--;
                m_auto_rotate_angle++;
            }

            m_current_angle = m_current_angle % 360;
            if (m_current_angle < 0) {
                m_current_angle += 360;
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
                m_start_touch_angle = (int)Math.toDegrees(Math.atan2(x - xc, yc - y));
                m_start_time_milliseconds = System.currentTimeMillis();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int new_angle = (int)Math.toDegrees(Math.atan2(x - xc, yc - y));

                long current_time_milliseconds = System.currentTimeMillis();
                long delta_time_milliseconds = current_time_milliseconds - m_start_time_milliseconds;
                int rotation_angle = new_angle - m_start_touch_angle;
                while (rotation_angle < -180) {
                    rotation_angle += 360;
                }
                while(rotation_angle > 180) {
                    rotation_angle -= 360;
                }

                if (rotation_angle == 0) {
                    break;
                }

                int abs_rotation = (int)Math.abs(m_turn_rotation + rotation_angle);
                if (abs_rotation > m_max_turn_rotation) {
                    m_max_turn_rotation = abs_rotation;
                }

                ROTATION_DIRECTION direction = (rotation_angle > 0) ?
                        ROTATION_DIRECTION.POSITIVE : ROTATION_DIRECTION.NEGATIVE;

                if (m_rotation_direction == ROTATION_DIRECTION.UNDEFINED) {
                    if (ROTATION_DIRECTION.LOCK_DEGREES < abs_rotation) {
                        m_rotation_direction = direction;
                    }
                } else {
                    if (m_rotation_direction != direction) {
                        if (ROTATION_DIRECTION.LOCK_DEGREES < m_max_turn_rotation - abs_rotation) {
                            m_start_touch_angle = new_angle;
                            break;
                        }
                    }
                }

                m_turn_rotation += rotation_angle;

                m_start_time_milliseconds = current_time_milliseconds;
                if (Math.abs(rotation_angle / (delta_time_milliseconds)) < 0.5) {
                    m_current_angle = (m_previous_angle + rotation_angle);
                    int round_down = (((int)m_current_angle) / 360) * 360;
                    m_current_angle -= round_down;

                    m_previous_angle = m_current_angle;
                    m_start_touch_angle = new_angle;

                    Rotate(m_current_angle);

                    m_activity.SendWheelMoveMessage(m_wheel_num, (int)rotation_angle);
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

    public void SetAllowRotation(Boolean is_allow) {
        m_allow_rotation = is_allow;
        m_touch_view.setVisibility(m_allow_rotation ? INVISIBLE : VISIBLE);
        if (is_allow) {
            m_rotation_direction = ROTATION_DIRECTION.UNDEFINED;
            m_turn_rotation = 0;
            m_max_turn_rotation = 0;
        }
    }

    public Boolean GetAllowRotation() {
        return m_allow_rotation;
    }

    public void SetWheelNum(int wheel_num) {
        m_wheel_num = wheel_num;
    }

    public int GetWheelNum() {
        return m_wheel_num;
    }

    public void Reset() {
        super.Reset();
        m_current_angle = 0;
        m_previous_angle = 0;
        SetAllowRotation(true);
        Rotate(m_current_angle);
    }

    public void StoreState(SharedPreferences.Editor editor) {
        String str = (m_activity.getString(R.string.wheel_angle)) + String.valueOf(m_wheel_num);
        editor.putInt(str, (int)m_current_angle);

        str = (m_activity.getString(R.string.wheel_allow_rotation)) + String.valueOf(m_wheel_num);
        editor.putBoolean(str, m_allow_rotation);

        str = (m_activity.getString(R.string.wheel_rotation_direction)) + String.valueOf(m_wheel_num);
        editor.putInt(str, m_rotation_direction.getInt());

        String hole_prefix = (m_activity.getString(R.string.hole_prefix)) + String.valueOf(m_wheel_num);
        for ( Hole hole : m_holes) {
            hole.StoreState(hole_prefix, editor);
        }
    }

    public void RestoreState(SharedPreferences sharedPref) {
        String str = (m_activity.getString(R.string.wheel_angle)) + String.valueOf(m_wheel_num);
        AddRotation(sharedPref.getInt(str, 0));

        str = (m_activity.getString(R.string.wheel_allow_rotation)) + String.valueOf(m_wheel_num);
        SetAllowRotation(sharedPref.getBoolean(str, true));
        if (GetAllowRotation()) {
            str = (m_activity.getString(R.string.wheel_rotation_direction)) + String.valueOf(m_wheel_num);
            int direction = sharedPref.getInt(str, 2);
            m_rotation_direction = ROTATION_DIRECTION.values()[direction];
        }
    }

    public void RestoreStatePart2(SharedPreferences sharedPref) {
        String hole_prefix = (m_activity.getString(R.string.hole_prefix)) + String.valueOf(m_wheel_num);
        for ( Hole hole : m_holes) {
            hole.RestoreState(hole_prefix, sharedPref);
        }
    }

    public void SetLocation(int left, int top) {
        super.SetLocation(left, top);

        RelativeLayout relativeLayout = (RelativeLayout) this.getParent();

        ViewParent oldParent = m_touch_view.getParent();
        if (oldParent == null) {
            relativeLayout.addView(m_touch_view, getLayoutParams());
        }
    }
}

