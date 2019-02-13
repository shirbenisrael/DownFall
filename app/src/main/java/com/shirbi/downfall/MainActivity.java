package com.shirbi.downfall;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import static com.shirbi.downfall.BluetoothChatService.TOAST;

public class MainActivity extends Activity implements View.OnTouchListener {

    private Point m_size;
    private int m_wheel_ids[] = {R.id.wheel1, R.id.wheel2, R.id.wheel3, R.id.wheel4, R.id.wheel5};
    private RadioButton m_objects_visibility_radio_buttons[];
    SimpleStupidAI m_simple_stupid_ai;
    SmartAI m_smart_ai;
    Wheel m_wheels[];
    ConnectableImage m_connectable_images[];
    int m_last_wheel_rotated;
    int m_last_angle_rotated;
    int m_last_angle_rotated_min;
    int m_last_angle_rotated_max;
    int m_last_angle_rotation_array[];
    int m_last_angle_rotation_index;

    TextView m_player_text_view_token_counter_left[] = new TextView[PlayerType.NUM_PLAYERS];
    Boolean m_game_starting_now;
    Boolean m_two_players_game_runnig;
    Boolean m_allow_screen_touch = true;
    int m_wheel_finished_rotate_counter;
    ObjectVisibility m_objects_visibility;
    private PlayerType m_player_type;

    class BLUETOOTH_MESSAGES {
        static final int START_GAME = 0;
        static final int TURN_DONE = 2;
    }

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
        editor.putInt(getString(R.string.objects_visibility), m_objects_visibility.getInt());
        editor.putInt(getString(R.string.player_type), m_player_type.getInt());

        for (int i = 0; i < m_connectable_images.length; i++) {
            m_connectable_images[i].StoreState(editor);
        }

        editor.commit();
    }

    private void RestoreState() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SetSoundEnable(sharedPref.getBoolean(getString(R.string.enable_sound), true));
        SetSmartAI(sharedPref.getBoolean(getString(R.string.smart_ai), false));

        int visibility_int = sharedPref.getInt(getString(R.string.objects_visibility),
                ObjectVisibility.ALWAYS_VISIBLE.getInt());
        m_objects_visibility = ObjectVisibility.values()[visibility_int];

        int player_type = sharedPref.getInt(getString(R.string.player_type),
            PlayerType.PLAYER_0.getInt());
        m_player_type = PlayerType.values()[player_type];

        RefreshVisibilityRadioButtons();
        RulesChanged();

        // When wheel will finish restoring their position, button will be enabled again.
        EnableButtons(false);

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
        hole.SetDiameter(m_size.x / GameConstants.HOLE_SIZE_DIVISOR);
        hole.SetPlayerType(player_type);
        ((ConnectableImage)findViewById(wheel_id)).AddHole(hole, angle);
    }

    private void AddHoles(int wheel_id, int first_angle, int num_holes) {
        int angle = first_angle;
        for (int i = 0; i < num_holes; i++) {
            AddHole(wheel_id, angle, PlayerType.PLAYER_0);
            angle += 360 / num_holes;
        }

        angle = first_angle + (180 / num_holes);
        for (int i = 0; i < num_holes; i++) {
            AddHole(wheel_id, angle, PlayerType.PLAYER_1);
            angle += 360 / num_holes;
        }
    }

    private void ConnectWheels(int bottom_id, int top_id, int bottom_angle) {
        ((ConnectableImage)findViewById(bottom_id)).ConnectAsBottom(((Wheel)findViewById(top_id)), bottom_angle);
    }

    private void SetConnectableImageDiameter(int wheel_id, int diameter) {
        ((ConnectableImage)findViewById(wheel_id)).UpdateDisplay(diameter);
    }

    public void SetTurnDoneButtonLocation(int wheel_id_anchor, int left, int top) {
        RotatableImage anchor = (RotatableImage)findViewById(wheel_id_anchor);
        RelativeLayout anchorRelativeLayout = (RelativeLayout) anchor.getParent();

        int top_anchor = ((RelativeLayout.LayoutParams)anchorRelativeLayout.getLayoutParams()).topMargin;
        int left_anchor = ((RelativeLayout.LayoutParams)anchorRelativeLayout.getLayoutParams()).leftMargin;

        View turn_done_button = findViewById(R.id.finish_turn_button);
        RelativeLayout boardLayout = (RelativeLayout) turn_done_button.getParent();

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(anchor.m_diameter, anchor.m_diameter / 2);

        params.leftMargin = left + left_anchor + anchor.m_diameter;
        params.topMargin = top + top_anchor + anchor.m_diameter / 6;

        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

        boardLayout.removeView(turn_done_button);
        boardLayout.addView(turn_done_button, params);
    }

    private void SetWheelLocation(int wheel_id, int left, int top) {
        ((ConnectableImage)findViewById(wheel_id)).SetLocation(left, top);
    }

    private void SetTokenQueueLocation(int queue_id, int left, int top) {
        ((ConnectableImage)findViewById(queue_id)).SetLocation(left, top);
    }

    private void ConnectWheelToInputQueue(int wheel_id, int queue_id, int bottom_angle) {
        Wheel wheel = ((Wheel)findViewById(wheel_id));
        wheel.ConnectToInputQueue(((InputTokenQueue)findViewById(queue_id)), bottom_angle);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_two_players_game_runnig = false;
        m_objects_visibility = ObjectVisibility.ALWAYS_VISIBLE;
        m_player_type = PlayerType.PLAYER_0;

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

        m_objects_visibility_radio_buttons = new RadioButton[3];
        m_objects_visibility_radio_buttons[ObjectVisibility.ALWAYS_VISIBLE.getInt()] =
                findViewById(R.id.show_holes_always_radio_button);
        m_objects_visibility_radio_buttons[ObjectVisibility.INVISIBLE.getInt()] =
                findViewById(R.id.hide_holes_always_radio_button);
        m_objects_visibility_radio_buttons[ObjectVisibility.VISIBLE_ON_CONNECT.getInt()] =
                findViewById(R.id.show_holes_on_connect_radio_button);

        m_player_text_view_token_counter_left[PlayerType.PLAYER_0.getInt()] =
                (TextView)findViewById(R.id.player_token_counter);
        m_player_text_view_token_counter_left[PlayerType.PLAYER_1.getInt()]
                = (TextView)findViewById(R.id.opposite_token_counter);

        int base_diameter = m_size.x / 3;

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
        ConnectWheels(R.id.wheel2, R.id.wheel1, 15);
        ConnectWheels(R.id.wheel3, R.id.wheel2, 315);
        ConnectWheels(R.id.wheel4, R.id.wheel2, 15);
        ConnectWheels(R.id.wheel4, R.id.wheel3, 70);
        ConnectWheels(R.id.wheel5, R.id.wheel4, 307);
        ConnectWheels(R.id.wheel5, R.id.wheel3, 358);

        ConnectWheels(R.id.output, R.id.wheel5, 90);

        SetTurnDoneButtonLocation(R.id.wheel2, (int)0, (int)0);

        AddHole(R.id.input_token_queue_left, 90, PlayerType.PLAYER_0);
        AddHole(R.id.input_token_queue_left, 90, PlayerType.PLAYER_1);
        AddHole(R.id.input_token_queue_right, 270, PlayerType.PLAYER_0);
        AddHole(R.id.input_token_queue_right, 270, PlayerType.PLAYER_1);

        AddHole(R.id.wheel1, 30, PlayerType.PLAYER_0);
        AddHole(R.id.wheel1, 330, PlayerType.PLAYER_1);
        AddHoles(R.id.wheel2, 90, 3);
        AddHoles(R.id.wheel3, 90, 2);
        AddHoles(R.id.wheel4, 45, 4);
        AddHoles(R.id.wheel5, 90, 5);

        AddHole(R.id.output, 90, PlayerType.PLAYER_0);
        AddHole(R.id.output, 90, PlayerType.PLAYER_1);

        m_simple_stupid_ai = new SimpleStupidAI(m_wheels, this);
        m_smart_ai = new SmartAI(m_wheels, this);

        findViewById(R.id.wheels_layout).requestLayout();

        m_game_starting_now = true;
        m_wheel_finished_rotate_counter = m_wheels.length;

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        RestoreState();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            //if (mChatService == null) setupChat();
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        Wheel wheel = (Wheel)v;

        if (!wheel.GetAllowRotation()) {
            return true;
        }

        if (!m_allow_screen_touch) {
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

        if (m_two_players_game_runnig) {
            String message = String.valueOf(BLUETOOTH_MESSAGES.TURN_DONE) + "," + m_last_wheel_rotated;
            if (m_last_angle_rotated > 0 ) {
                if (m_last_angle_rotated_min < 0 ) {
                    message = message +","+String.valueOf(m_last_angle_rotated_min);
                }
                if (m_last_angle_rotated_max > m_last_angle_rotated ) {
                    message = message +","+String.valueOf(m_last_angle_rotated_max);
                }
            } else {
                if (m_last_angle_rotated_max > 0 ) {
                    message = message +","+String.valueOf(m_last_angle_rotated_max);
                }
                if (m_last_angle_rotated_min < m_last_angle_rotated ) {
                    message = message +","+String.valueOf(m_last_angle_rotated_min);
                }
            }
            message = message +","+String.valueOf(m_last_angle_rotated);

            sendMessage(message);
            return;
        }

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
        builder.setTitle(getString(R.string.start_new_game));
        builder.setPositiveButton(getString(R.string.confirm_player_0), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                m_player_type = PlayerType.PLAYER_0;
                StartNewGame();
            }
        });
        builder.setNegativeButton(getString(R.string.confirm_player_1), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                m_player_type = PlayerType.PLAYER_1;
                StartNewGame();
            }
        });
        builder.setNeutralButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // do nothing
            }
        });
        //builder.setIcon(R.drawable.new_game_icon); // TODO: Add this
        builder.show();
    }

    public void StartNewGame() {
        m_two_players_game_runnig = false;

        for (int i = 0; i < m_connectable_images.length; i++) {
            m_connectable_images[i].Reset();
        }

        m_last_wheel_rotated = m_wheels.length;
        m_last_angle_rotated = 0;
        m_last_angle_rotated_min = 0;
        m_last_angle_rotated_max= 0;
    }

    public void WheelFinishedRotating() {
        if (m_game_starting_now) {
            m_wheel_finished_rotate_counter--;
            if (m_wheel_finished_rotate_counter == 0) {
                m_game_starting_now = false;
                RestoreStatePart2();
            } else {
                return;
            }
        }

        if (m_two_players_game_runnig) {
            m_last_angle_rotation_index++;
            RotateByOtherPlayer();
        } else {
            EnableButtons(true);
        }
    }

    private void EnableButtons(Boolean enable) {
        findViewById(R.id.finish_turn_button).setEnabled(enable);
        findViewById(R.id.new_game_button).setEnabled(enable);
        findViewById(R.id.setting_button).setEnabled(enable);

        m_allow_screen_touch = enable;
    }

    public void ShowNumTokenLeft(PlayerType player_type, int num_token_left) {
        m_player_text_view_token_counter_left[player_type.getInt()].setText(String.valueOf(num_token_left));
    }

    public void onSettingClick(View view) {
        findViewById(R.id.main_game_layout).setVisibility(View.INVISIBLE);
        findViewById(R.id.setting_layout).setVisibility(View.VISIBLE);
    }

    private void RefreshVisibilityRadioButtons() {
        for (int i = 0 ; i < m_objects_visibility_radio_buttons.length; i++) {
            Boolean is_checked = (m_objects_visibility == ObjectVisibility.values()[i]);
            m_objects_visibility_radio_buttons[i].setChecked(is_checked);
        }
    }

    public ObjectVisibility GetObjectVisibility() { return m_objects_visibility; }

    private void RulesChanged() {
        for (ConnectableImage connectableImage : m_connectable_images ) {
            connectableImage.RulesChanged();
        }
    }

    public void onBackFromSettingClick(View view) {
        findViewById(R.id.main_game_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.setting_layout).setVisibility(View.INVISIBLE);

        for (int i = 0 ; i < m_objects_visibility_radio_buttons.length; i++) {
            if (m_objects_visibility_radio_buttons[i].isChecked()) {
                m_objects_visibility = ObjectVisibility.values()[i];
            }
        }

        RulesChanged();
    }

    @Override
    public void onBackPressed() {
        if (findViewById(R.id.setting_layout).getVisibility() == View.VISIBLE) {
            onBackFromSettingClick(null);
            return;
        }

        if (!m_allow_screen_touch) {
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

    public void EndGame(String string) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle(getString(R.string.game_over));
        builder.setPositiveButton(string, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                StartNewGame();
            }
        });
        //builder.setIcon(R.drawable.new_game_icon); // TODO: Add this
        builder.show();
    }

    public PlayerType GetPlayerType() {return m_player_type; }

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothChatService mChatService = null;
    private IncomingHandler mHandler = new IncomingHandler(this);

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    public void connect(View v) {
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

    public void discoverable(View v) {
        ensureDiscoverable();
    }

    private void RotateByOtherPlayer() {
        if (m_last_angle_rotation_index < m_last_angle_rotation_array.length) {
            int angle = m_last_angle_rotation_array[m_last_angle_rotation_index];
            if (m_last_angle_rotation_index != 0) {
                int prev_angle = m_last_angle_rotation_array[m_last_angle_rotation_index - 1];
                angle -= prev_angle;
            }
            m_wheels[m_last_wheel_rotated].AddRotation(angle);
        } else {
            m_last_angle_rotated = 0;
            m_last_angle_rotated_min = 0;
            m_last_angle_rotated_max = 0;
            //Allow the player use all wheels except the one used by the AI.
            if (m_last_wheel_rotated < m_wheels.length) {
                for (int i = 0; i < m_wheels.length; i++) {
                    m_wheels[i].SetAllowRotation(i != m_last_wheel_rotated);
                }
            }

            EnableButtons(true);
        }
    }

    public void HandleStartGameMessageFromOtherDevice() {
        onBackFromSettingClick(null);
        StartNewGame();
        m_two_players_game_runnig = true;
    }

    private void ParseMessage(String message) {
        String[] strArray = message.split(",");
        int[] intArray = new int[strArray.length];
        for(int i = 0; i < strArray.length; i++) {
            intArray[i] = Integer.parseInt(strArray[i]);
        }

        //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

        switch (intArray[0]) {
            case BLUETOOTH_MESSAGES.START_GAME:
                m_player_type = PlayerType.values()[1-intArray[1]];
                HandleStartGameMessageFromOtherDevice();
                break;
            case BLUETOOTH_MESSAGES.TURN_DONE:
                m_last_wheel_rotated = intArray[1];
                // ignore message type and wheel num.
                m_last_angle_rotation_array = new int[intArray.length - 2];
                System.arraycopy(intArray, 2, m_last_angle_rotation_array, 0, m_last_angle_rotation_array.length);
                m_last_angle_rotation_index = 0;
                RotateByOtherPlayer();
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case BluetoothChatService.MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                break;
            case BluetoothChatService.MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                ParseMessage(readMessage);
                break;
            case BluetoothChatService.MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(BluetoothChatService.DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                        + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case BluetoothChatService.MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }

    // The Handler that gets information back from the BluetoothChatService
    static class IncomingHandler extends Handler {
        private final WeakReference<MainActivity> m_activity;

        IncomingHandler(MainActivity activity) {
            m_activity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = m_activity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    };

    private void setupChat() {
        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BluetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device

                    if (mChatService == null) {
                        setupChat();
                    }

                    mChatService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void sendMessage(String message) {

        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    public void SendTwoPlayerGameMessage() {
        String message = String.valueOf(BLUETOOTH_MESSAGES.START_GAME) + ",";
        message += String.valueOf(m_player_type.getInt());
        sendMessage(message);
        StartNewGame();
        m_two_players_game_runnig = true;

        onBackFromSettingClick(null);
    }

    public void StartTwoPlayerGame(View v) {
        if (mChatService == null) {
            setupChat();
        }

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle(getString(R.string.start_two_player_game_title));
        builder.setPositiveButton(getString(R.string.confirm_player_0), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                m_player_type = PlayerType.PLAYER_0;
                SendTwoPlayerGameMessage();
            }
        });
        builder.setNegativeButton(getString(R.string.confirm_player_1), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                m_player_type = PlayerType.PLAYER_1;
                SendTwoPlayerGameMessage();
            }
        });
        builder.setNeutralButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // do nothing
            }
        });
        //builder.setIcon(R.drawable.new_game_icon); // TODO: Add this
        builder.show();
    }

    public void SendWheelMoveMessage(int wheel_num, int angle) {
        if (!m_two_players_game_runnig) {
            return;
        }

        if (m_last_wheel_rotated == wheel_num && m_last_angle_rotated == angle) {
            return;
        }

        m_last_wheel_rotated = wheel_num;
        m_last_angle_rotated += angle;
        if (m_last_angle_rotated < m_last_angle_rotated_min) {
            m_last_angle_rotated_min = m_last_angle_rotated;
        }
        if (m_last_angle_rotated > m_last_angle_rotated_max) {
            m_last_angle_rotated_max = m_last_angle_rotated;
        }
    }
}
