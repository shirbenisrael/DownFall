package com.shirbi.downfall;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends Activity implements View.OnTouchListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((ImageView)findViewById(R.id.wheel1)).setOnTouchListener(this);
        ((ImageView)findViewById(R.id.wheel2)).setOnTouchListener(this);
    }

    public boolean onTouch(View v, MotionEvent event) {
        Wheel wheel = (Wheel)v;
        wheel.onTouch(v, event);
        return true;
    }
}
