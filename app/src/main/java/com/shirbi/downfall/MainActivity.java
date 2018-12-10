package com.shirbi.downfall;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnTouchListener {

    private Point m_size;

    private Point GetWindowSize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    private void SetHoleBaseAngle(int wheel_id, int hole_id, int angle) {
        Hole hole = (Hole)findViewById(hole_id);
        ((Wheel)findViewById(wheel_id)).AddHole(hole, angle);
    }

    private void ConnectWheels(int bottom_id, int top_id, int bottom_angle) {
        ((Wheel)findViewById(bottom_id)).ConnectAsBottom(((Wheel)findViewById(top_id)), bottom_angle);
    }

    private void SetWheelDiameter(int wheel_id, double diameter) {
        ((Wheel)findViewById(wheel_id)).UpdateDisplay((int)diameter);
    }

    private void SetWheelLocation(int wheel_id, double left, double top) {
        ((Wheel)findViewById(wheel_id)).SetLocation((int)left, (int)top);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_size = GetWindowSize();

        ((ImageView)findViewById(R.id.wheel1)).setOnTouchListener(this);
        ((ImageView)findViewById(R.id.wheel2)).setOnTouchListener(this);
        ((ImageView)findViewById(R.id.wheel3)).setOnTouchListener(this);
        ((ImageView)findViewById(R.id.wheel4)).setOnTouchListener(this);
        ((ImageView)findViewById(R.id.wheel5)).setOnTouchListener(this);

        SetHoleBaseAngle(R.id.wheel1, R.id.hole1_1, 30);
        SetHoleBaseAngle(R.id.wheel2, R.id.hole2_1, 90);
        SetHoleBaseAngle(R.id.wheel2, R.id.hole2_2, 270);
        SetHoleBaseAngle(R.id.wheel3, R.id.hole3_1, 90);
        SetHoleBaseAngle(R.id.wheel3, R.id.hole3_2, 270);
        SetHoleBaseAngle(R.id.wheel4, R.id.hole4_1, 90);
        SetHoleBaseAngle(R.id.wheel4, R.id.hole4_2, 270);
        SetHoleBaseAngle(R.id.wheel5, R.id.hole5_1, 90);
        SetHoleBaseAngle(R.id.wheel5, R.id.hole5_2, 270);

        double base_diameter = m_size.x;

        SetWheelDiameter(R.id.wheel1, base_diameter / 3);
        SetWheelDiameter(R.id.wheel2, base_diameter / 3);
        SetWheelDiameter(R.id.wheel3, base_diameter/ 3);
        SetWheelDiameter(R.id.wheel4, base_diameter/ 3);
        SetWheelDiameter(R.id.wheel5, base_diameter / 3);

        SetWheelLocation(R.id.wheel1, base_diameter / 3,0 );

        ((TextView)findViewById(R.id.text_view)).setText("Width: " + m_size.x + ", Height: " + m_size.y);

        ((Hole)findViewById(R.id.hole1_1)).SetResident((Token)findViewById(R.id.token1_1));
        ((Hole)findViewById(R.id.hole2_1)).SetResident((Token)findViewById(R.id.token1_2));

        ConnectWheels(R.id.wheel2, R.id.wheel1, 30);
        ConnectWheels(R.id.wheel3, R.id.wheel2, 0);
        ConnectWheels(R.id.wheel4, R.id.wheel3, 0);
        ConnectWheels(R.id.wheel5, R.id.wheel4, 0);
    }

    public boolean onTouch(View v, MotionEvent event) {
        Wheel wheel = (Wheel)v;
        wheel.onTouch(v, event);
        return true;
    }
}
