package com.shirbi.downfall;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends Activity implements View.OnTouchListener {

    private Point m_size;

    private Point GetWindowSize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    private void AddHole(int wheel_id, int angle) {
        Hole hole = new Hole(this);
        hole.SetDiameter(m_size.x / 18);
        ((ConnectableImage)findViewById(wheel_id)).AddHole(hole, angle);
    }

    private void AddHoles(int wheel_id, int first_angle, int num_holes) {
        int angle = first_angle;
        for (int i = 0; i < num_holes; i++) {
            AddHole(wheel_id, angle);
            angle += 360 / num_holes;
        }
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

    private void SetInputTokenQueueDiameter(int queue_id, double diameter) {
        ((InputTokenQueue)findViewById(queue_id)).UpdateDisplay((int)diameter);
    }

    private void SetInputTokenQueueLocation(int queue_id, double left, double top) {
        ((InputTokenQueue)findViewById(queue_id)).SetLocation((int)left, (int)top);
    }

    private void AddTokenToInputQueue(int queue_id, Token token) {
        ((InputTokenQueue)findViewById(queue_id)).AddToken(token);
    }

    private void ConnectWheelToInputQueue(int wheel_id, int queue_id, double bottom_angle) {
        Wheel wheel = ((Wheel)findViewById(wheel_id));
        wheel.ConnectToInputQueue(((InputTokenQueue)findViewById(queue_id)), bottom_angle);
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

        double base_diameter = m_size.x / 3;

        SetInputTokenQueueDiameter(R.id.input_token_queue_left, base_diameter * 1);
        SetInputTokenQueueDiameter(R.id.input_token_queue_right, base_diameter * 1);

        SetWheelDiameter(R.id.wheel1, base_diameter * 1);
        SetWheelDiameter(R.id.wheel2, base_diameter * 11 / 10);
        SetWheelDiameter(R.id.wheel3, base_diameter * 1);
        SetWheelDiameter(R.id.wheel4, base_diameter * 4 / 3);
        SetWheelDiameter(R.id.wheel5, base_diameter * 3 / 2);

        SetInputTokenQueueLocation(R.id.input_token_queue_left, 0, 0);
        SetInputTokenQueueLocation(R.id.input_token_queue_right, base_diameter * 2, 0);

        ConnectWheelToInputQueue(R.id.wheel1, R.id.input_token_queue_left, 270);
        ConnectWheelToInputQueue(R.id.wheel1, R.id.input_token_queue_right, 90);

        SetWheelLocation(R.id.wheel1, base_diameter,30 );
        ConnectWheels(R.id.wheel2, R.id.wheel1, 20);
        ConnectWheels(R.id.wheel3, R.id.wheel2, 315);
        ConnectWheels(R.id.wheel4, R.id.wheel2, 15);
        ConnectWheels(R.id.wheel4, R.id.wheel3, 70);
        ConnectWheels(R.id.wheel5, R.id.wheel4, 307);
        ConnectWheels(R.id.wheel5, R.id.wheel3, 358);

        AddHole(R.id.input_token_queue_left, 90);
        AddHole(R.id.input_token_queue_right, 270);

        AddHole(R.id.wheel1, 30);
        AddHoles(R.id.wheel2, 90, 3);
        AddHoles(R.id.wheel3, 90, 2);
        AddHoles(R.id.wheel4, 45, 4);
        AddHoles(R.id.wheel5, 90, 5);

        AddTokenToInputQueue(R.id.input_token_queue_left, (Token)findViewById(R.id.token1_1));
        AddTokenToInputQueue(R.id.input_token_queue_left, (Token)findViewById(R.id.token1_2));

        findViewById(R.id.wheels_layout).requestLayout();
    }

    public boolean onTouch(View v, MotionEvent event) {
        Wheel wheel = (Wheel)v;
        wheel.onTouch(v, event);
        return true;
    }
}
