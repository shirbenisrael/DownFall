package com.shirbi.downfall;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnTouchListener {

    private Point m_size;
    private int m_wheel_ids[] = {R.id.wheel1, R.id.wheel2, R.id.wheel3, R.id.wheel4, R.id.wheel5};
    SimpleStupidAI m_simple_stupid_ai;
    SmartAI m_smart_ai;
    Wheel m_wheels[];
    ConnectableImage m_connectable_images[];
    int m_last_wheel_rotated;
    TextView m_player_text_view_token_counter_left[] = new TextView[PlayerType.NUM_PLAYERS];
    Boolean m_game_starting_now;
    int m_wheel_finished_rotate_counter;

    private Point GetWindowSize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    private void StoreState() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.enable_sound), IsSoundEnable());
        editor.putBoolean(getString(R.string.smart_ai), IsSmartAI());

        for (int i = 0; i < m_connectable_images.length; i++) {
            m_connectable_images[i].StoreState(editor);
        }

        editor.commit();
    }

    private void RestoreState() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SetSoundEnable(sharedPref.getBoolean(getString(R.string.enable_sound), true));
        SetSmartAI(sharedPref.getBoolean(getString(R.string.smart_ai), false));

        for (int i = 0; i < m_connectable_images.length; i++) {
            m_connectable_images[i].RestoreState(sharedPref);
        }
    }

    private void RestoreStatePart2() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

        for (int i = 0; i < m_connectable_images.length; i++) {
            m_connectable_images[i].RestoreStatePart2(sharedPref);
        }
    }

    @Override
    protected void onDestroy() {
        StoreState();
        super.onDestroy();
    }

    private void AddHole(int wheel_id, int angle, PlayerType player_type) {
        Hole hole = new Hole(this);
        hole.SetDiameter(m_size.x / 18);
        ((ConnectableImage)findViewById(wheel_id)).AddHole(hole, angle);
        hole.SetPlayerType(player_type);
    }

    private void AddHoles(int wheel_id, int first_angle, int num_holes) {
        int angle = first_angle;
        for (int i = 0; i < num_holes; i++) {
            AddHole(wheel_id, angle, PlayerType.HUMAN_PLAYER);
            angle += 360 / num_holes;
        }

        angle = first_angle + (180 / num_holes);
        for (int i = 0; i < num_holes; i++) {
            AddHole(wheel_id, angle, PlayerType.AI_PLAYER);
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
        m_connectable_images = new ConnectableImage[8];

        m_wheels[0] = ((Wheel)findViewById(R.id.wheel1));
        m_wheels[1] = ((Wheel)findViewById(R.id.wheel2));
        m_wheels[2] = ((Wheel)findViewById(R.id.wheel3));
        m_wheels[3] = ((Wheel)findViewById(R.id.wheel4));
        m_wheels[4] = ((Wheel)findViewById(R.id.wheel5));

        for (int i = 0; i < m_wheels.length; i++) {
            m_wheels[i].setOnTouchListener(this);
            m_wheels[i].SetWheelNum(i);
            m_connectable_images[i] = m_wheels[i];
        }

        m_connectable_images[5] = ((ConnectableImage)findViewById(R.id.input_token_queue_left));
        m_connectable_images[6] = ((ConnectableImage)findViewById(R.id.input_token_queue_right));
        m_connectable_images[7] = ((ConnectableImage)findViewById(R.id.output));

        m_player_text_view_token_counter_left[PlayerType.HUMAN_PLAYER.getInt()] =
                (TextView)findViewById(R.id.player_token_counter);
        m_player_text_view_token_counter_left[PlayerType.AI_PLAYER.getInt()]
                = (TextView)findViewById(R.id.opposite_token_counter);

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

        AddHole(R.id.input_token_queue_left, 90, PlayerType.HUMAN_PLAYER);
        AddHole(R.id.input_token_queue_left, 90, PlayerType.AI_PLAYER);
        AddHole(R.id.input_token_queue_right, 270, PlayerType.HUMAN_PLAYER);
        AddHole(R.id.input_token_queue_right, 270, PlayerType.AI_PLAYER);

        AddHole(R.id.wheel1, 30, PlayerType.HUMAN_PLAYER);
        AddHole(R.id.wheel1, 330, PlayerType.AI_PLAYER);
        AddHoles(R.id.wheel2, 90, 3);
        AddHoles(R.id.wheel3, 90, 2);
        AddHoles(R.id.wheel4, 45, 4);
        AddHoles(R.id.wheel5, 90, 5);

        AddHole(R.id.output, 90, PlayerType.HUMAN_PLAYER);
        AddHole(R.id.output, 90, PlayerType.AI_PLAYER);

        m_simple_stupid_ai = new SimpleStupidAI(m_wheels);
        m_smart_ai = new SmartAI(m_wheels);

        findViewById(R.id.wheels_layout).requestLayout();

        //StartNewGame();

        m_game_starting_now = true;
        m_wheel_finished_rotate_counter = m_wheels.length;

        RestoreState();
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

    private Boolean IsSmartAI() {
        CheckBox smart_ai_checkbox = (CheckBox)findViewById(R.id.smart_ai_checkbox);
        return smart_ai_checkbox.isChecked();
    }

    private void SetSmartAI(Boolean is_smart_ai) {
        CheckBox smart_ai_checkbox = (CheckBox)findViewById(R.id.smart_ai_checkbox);
        smart_ai_checkbox.setChecked(is_smart_ai);
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

        OppositePlayer ai_player = IsSmartAI() ? m_smart_ai : m_simple_stupid_ai;
        int rotated_wheel = ai_player.Run();

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
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Start new game?");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                StartNewGame();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // do nothing
            }
        });
        //builder.setIcon(R.drawable.new_game_icon); // TODO: Add this
        builder.show();
    }

    public void StartNewGame() {
        for (int i = 0; i < m_connectable_images.length; i++) {
            m_connectable_images[i].Reset();
        }

        m_last_wheel_rotated = m_wheels.length;
    }

    public void WheelFinishedRotating() {
        if (m_game_starting_now) {
            m_wheel_finished_rotate_counter--;
            if (m_wheel_finished_rotate_counter == 0) {
                m_game_starting_now = false; // shir
                RestoreStatePart2();
            } else {
                return;
            }
        }
        EnableButtons(true);
    }

    private void EnableButtons(Boolean enable) {
        findViewById(R.id.finish_turn_button).setEnabled(enable);
        findViewById(R.id.new_game_button).setEnabled(enable);
        findViewById(R.id.setting_button).setEnabled(enable);
    }

    public void ShowNumTokenLeft(PlayerType player_type, int num_token_left) {
        m_player_text_view_token_counter_left[player_type.getInt()].setText(String.valueOf(num_token_left));
    }

    public void onSettingClick(View view) {
        findViewById(R.id.main_game_layout).setVisibility(View.INVISIBLE);
        findViewById(R.id.setting_layout).setVisibility(View.VISIBLE);
    }

    public void onBackFromSettingClick(View view) {
        findViewById(R.id.main_game_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.setting_layout).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (findViewById(R.id.setting_layout).getVisibility() == View.VISIBLE) {
            onBackFromSettingClick(null);
            return;
        }

        super.onBackPressed();
    }

    private void SetSoundEnable(Boolean is_enable) {
        CheckBox enable_sound_check_box = (CheckBox)findViewById(R.id.enable_sound_checkbox);
        enable_sound_check_box.setChecked(is_enable);
    }

    private Boolean IsSoundEnable() {
        CheckBox enable_sound_check_box = (CheckBox)findViewById(R.id.enable_sound_checkbox);
        return enable_sound_check_box.isChecked();
    }

    public void PlaySound(int sound_id) {
        if (!IsSoundEnable()) {
            return;
        }

        MediaPlayer media_player;
        media_player = MediaPlayer.create(this, sound_id);
        media_player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
        media_player.start();
    }
}
