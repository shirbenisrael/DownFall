package com.shirbi.downfall;

import android.app.Activity;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnTouchListener {

    private double m_previous_angle, m_start_touch_angle, m_current_angle;
    long m_start_time_milliseconds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView view = (ImageView) findViewById(R.id.rotate_button);
        view.setOnTouchListener(this);

        m_previous_angle = 0;
        m_current_angle = 0;
    }

    public void onRotateButtonClick(View view) {

        Matrix matrix = new Matrix();
        ImageView imageView = (ImageView )view;
        imageView.setScaleType(ImageView.ScaleType.MATRIX);   //required

        int pivotX = imageView.getDrawable().getBounds().width()/2;
        int pivotY = imageView.getDrawable().getBounds().height()/2;

        matrix.postRotate((float) m_current_angle, pivotX, pivotY);
        imageView.setImageMatrix(matrix);

        TextView textView = (TextView)findViewById(R.id.text_view);
        textView.setText(Double.toString(m_current_angle));
    }

    public boolean onTouch(View v, MotionEvent event) {

        ImageView imageView = (ImageView) findViewById(R.id.rotate_button);

        final float xc = imageView.getWidth() / 2;
        final float yc = imageView.getHeight() / 2;

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

                    onRotateButtonClick(imageView);
                } else {
                    TextView textView = (TextView)findViewById(R.id.text_view);
                    textView.setText("rotation_angle:" + Double.toString(rotation_angle) +
                    " delta time: " +   Long.toString(delta_time_milliseconds));
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
