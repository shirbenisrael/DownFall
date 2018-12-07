package com.shirbi.downfall;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

public class Wheel extends ImageButton {
    private double m_previous_angle, m_start_touch_angle, m_current_angle;
    private long m_start_time_milliseconds;

    public Wheel(Context context) {
        super(context);
    }

    public Wheel(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void rotate() {
        Matrix matrix = new Matrix();
        setScaleType(ImageView.ScaleType.MATRIX);   //required

        int pivotX = getDrawable().getBounds().width()/2;
        int pivotY = getDrawable().getBounds().height()/2;

        matrix.postRotate((float) m_current_angle, pivotX, pivotY);
        setImageMatrix(matrix);
    }

    public boolean onTouch(View v, MotionEvent event) {
        final float xc = getWidth() / 2;
        final float yc = getHeight() / 2;

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

                    rotate();
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
