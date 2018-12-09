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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_size = GetWindowSize();

        ((ImageView)findViewById(R.id.wheel1)).setOnTouchListener(this);
        ((ImageView)findViewById(R.id.wheel2)).setOnTouchListener(this);

        Hole hole = (Hole)findViewById(R.id.hole1_1);
        ((Wheel)findViewById(R.id.wheel1)).AddHole(hole, 30);

        hole = (Hole)findViewById(R.id.hole2_1);
        ((Wheel)findViewById(R.id.wheel2)).AddHole(hole, 90);

        hole = (Hole)findViewById(R.id.hole2_2);
        ((Wheel)findViewById(R.id.wheel2)).AddHole(hole, 270);

        ((Wheel)findViewById(R.id.wheel1)).UpdateDisplay(m_size.x / 2);
        ((Wheel)findViewById(R.id.wheel2)).UpdateDisplay(m_size.x / 3);

        ((TextView)findViewById(R.id.text_view)).setText("Width: " + m_size.x + ", Height: " + m_size.y);

        //((Hole)findViewById(R.id.hole1_1)).SetResident((Token)findViewById(R.id.token1_1));
        ((Hole)findViewById(R.id.hole2_1)).SetResident((Token)findViewById(R.id.token1_2));

        ((Wheel)findViewById(R.id.wheel2)).ConnectAsBottom(((Wheel)findViewById(R.id.wheel1)), 0);
    }

    public boolean onTouch(View v, MotionEvent event) {
        Wheel wheel = (Wheel)v;
        wheel.onTouch(v, event);
        return true;
    }
}
