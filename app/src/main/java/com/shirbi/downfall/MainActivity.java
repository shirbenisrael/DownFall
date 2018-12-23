package com.shirbi.downfall;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends Activity implements View.OnTouchListener {

    private Point m_size;
    private int m_wheel_ids[] = {R.id.wheel1, R.id.wheel2, R.id.wheel3, R.id.wheel4, R.id.wheel5};
    SimpleStupidAI m_simple_stupid_ai;
    Wheel m_wheels[];
    int m_last_wheel_rotated;

    private Point GetWindowSize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    private void AddHole(int wheel_id, int angle, Boolean opposite) {
        Hole hole = new Hole(this);
        hole.SetDiameter(m_size.x / 18);
        ((ConnectableImage)findViewById(wheel_id)).AddHole(hole, angle);

        if (opposite) {
            hole.SetOppositeSide();
        }
    }

    private void AddHoles(int wheel_id, int first_angle, int num_holes) {
        int angle = first_angle;
        for (int i = 0; i < num_holes; i++) {
            AddHole(wheel_id, angle, false);
            angle += 360 / num_holes;
        }

        angle = first_angle + (180 / num_holes);
        for (int i = 0; i < num_holes; i++) {
            AddHole(wheel_id, angle, true);
            angle += 360 / num_holes;
        }
    }

    private void ConnectWheels(int bottom_id, int top_id, int bottom_angle) {
        ((ConnectableImage)findViewById(bottom_id)).ConnectAsBottom(((Wheel)findViewById(top_id)), bottom_angle);
    }

    private void SetConnectableImageDiameter(int wheel_id, double diameter) {
        ((ConnectableImage)findViewById(wheel_id)).UpdateDisplay((int)diameter);
    }

    private void SetWheelLocation(int wheel_id, double left, double top) {
        ((ConnectableImage)findViewById(wheel_id)).SetLocation((int)left, (int)top);
    }

    private void SetTokenQueueLocation(int queue_id, double left, double top) {
        ((ConnectableImage)findViewById(queue_id)).SetLocation((int)left, (int)top);
    }

    private void AddTokensToInputQueue(int queue_id, Token.COLOR color) {
        for (int number = 1; number <= InputTokenQueue.MAX_TOKENS; number++) {
            InputTokenQueue inputQueue = ((InputTokenQueue) findViewById(queue_id));

            Token token = new Token(this);
            token.SetType(color, number);
            inputQueue.AddToken(token);

            token = new Token(this);
            token.SetType(color, number);
            token.SetOppositeSide();
            inputQueue.AddToken(token);
        }
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

        m_wheels = new Wheel[5];

        m_wheels[0] = ((Wheel)findViewById(R.id.wheel1));
        m_wheels[1] = ((Wheel)findViewById(R.id.wheel2));
        m_wheels[2] = ((Wheel)findViewById(R.id.wheel3));
        m_wheels[3] = ((Wheel)findViewById(R.id.wheel4));
        m_wheels[4] = ((Wheel)findViewById(R.id.wheel5));

        for (int i = 0; i < m_wheels.length; i++) {
            m_wheels[i].setOnTouchListener(this);
            m_wheels[i].SetWheelNum(i);
        }

        m_simple_stupid_ai = new SimpleStupidAI(m_wheels);

        double base_diameter = m_size.x / 3;

        SetConnectableImageDiameter(R.id.input_token_queue_left, base_diameter * 1);
        SetConnectableImageDiameter(R.id.input_token_queue_right, base_diameter * 1);

        SetConnectableImageDiameter(R.id.wheel1, base_diameter * 1);
        SetConnectableImageDiameter(R.id.wheel2, base_diameter * 11 / 10);
        SetConnectableImageDiameter(R.id.wheel3, base_diameter * 1);
        SetConnectableImageDiameter(R.id.wheel4, base_diameter * 4 / 3);
        SetConnectableImageDiameter(R.id.wheel5, base_diameter * 3 / 2);

        SetConnectableImageDiameter(R.id.output, base_diameter * 1);

        SetTokenQueueLocation(R.id.input_token_queue_left, 0, 0);
        SetTokenQueueLocation(R.id.input_token_queue_right, base_diameter * 2, 0);

        ConnectWheelToInputQueue(R.id.wheel1, R.id.input_token_queue_left, 270);
        ConnectWheelToInputQueue(R.id.wheel1, R.id.input_token_queue_right, 90);

        SetWheelLocation(R.id.wheel1, base_diameter,30 );
        ConnectWheels(R.id.wheel2, R.id.wheel1, 20);
        ConnectWheels(R.id.wheel3, R.id.wheel2, 315);
        ConnectWheels(R.id.wheel4, R.id.wheel2, 15);
        ConnectWheels(R.id.wheel4, R.id.wheel3, 70);
        ConnectWheels(R.id.wheel5, R.id.wheel4, 307);
        ConnectWheels(R.id.wheel5, R.id.wheel3, 358);

        ConnectWheels(R.id.output, R.id.wheel5, 90);

        AddHole(R.id.input_token_queue_left, 90, false);
        AddHole(R.id.input_token_queue_left, 90, true);
        AddHole(R.id.input_token_queue_right, 270, false);
        AddHole(R.id.input_token_queue_right, 270, true);

        AddHoles(R.id.wheel1, 30, 1);
        AddHoles(R.id.wheel2, 90, 3);
        AddHoles(R.id.wheel3, 90, 2);
        AddHoles(R.id.wheel4, 45, 4);
        AddHoles(R.id.wheel5, 90, 5);

        AddHole(R.id.output, 90, false);
        AddHole(R.id.output, 90, true);

        findViewById(R.id.wheels_layout).requestLayout();

        onNewGameButtonClick(null);
    }

    public boolean onTouch(View v, MotionEvent event) {
        Wheel wheel = (Wheel)v;

        if (!wheel.GetAllowRotation()) {
            return true;
        }

        int selected_wheel_num = wheel.GetWheelNum();

        // New wheel touch - block all other wheels.
        if (m_last_wheel_rotated == m_wheels.length) {
            m_last_wheel_rotated = selected_wheel_num;
            for (int i = 0 ; i < m_wheels.length; i++) {
                m_wheels[i].SetAllowRotation(i == m_last_wheel_rotated);
            }
        }

        wheel.onTouch(v, event);
        return true;
    }

    public void onFinishTurnButtonClick(View view) {
        EnableButtons(false);

        if (m_last_wheel_rotated != m_wheels.length) {
            // If player touch a wheel,
            // allow AI to use all wheels expect the one which rotated by the human player.
            for (int i = 0; i < m_wheels.length; i++) {
                m_wheels[i].SetAllowRotation(i != m_last_wheel_rotated);
            }
            // Else - player skip its turn. The AI is allowed to touch all wheels except the one he touched.
        }

        m_last_wheel_rotated = m_wheels.length;

        int rotated_wheel = m_simple_stupid_ai.Run();

        // Allow the player use all wheels except the one used by the AI.
        if (rotated_wheel < m_wheels.length) {
            for (int i = 0; i < m_wheels.length; i++) {
                m_wheels[i].SetAllowRotation(i != rotated_wheel);
            }
        } else {
            EnableButtons(true);
        }
    }

    public void onNewGameButtonClick(View view) {
        for (int i = 0; i < m_wheels.length; i++) {
            m_wheels[i].Reset();
        }

        ((InputTokenQueue) findViewById(R.id.input_token_queue_left)).Reset();
        ((InputTokenQueue) findViewById(R.id.input_token_queue_right)).Reset();

        AddTokensToInputQueue(R.id.input_token_queue_left, Token.COLOR.COLOR_1);
        AddTokensToInputQueue(R.id.input_token_queue_right, Token.COLOR.COLOR_2);

        m_last_wheel_rotated = m_wheels.length;
    }

    public void EnableButtons(Boolean enable) {
        findViewById(R.id.finish_turn_button).setEnabled(enable);
        findViewById(R.id.new_game_button).setEnabled(enable);
    }
}
