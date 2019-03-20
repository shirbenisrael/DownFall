package com.shirbi.downfall;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.view.View;
import android.widget.TextView;

public class Tutorial {
    protected MainActivity m_activity;
    protected int m_base_diameter;
    ConnectableImage m_connectable_images[];
    Wheel m_wheels[];
    InputTokenQueue m_input;
    Output m_output;
    TextView m_message_text_view;

    static private final int SECOND_WHEEL_PLAYER_FIRST_ANGLE = 45;
    static private final int SECOND_WHEEL_NUM_HOLES = 4;

    public enum STAGE {
        STAGE_NONE,
        STAGE1,
        STAGE2,
        STAGE3,
        STAGE4,
        STAGE5,
        STAGE6,
        STAGE7,
        STAGE8,
    }

    private STAGE m_stage;

    public STAGE GetStage() {
        return m_stage;
    }

    Tutorial(MainActivity activity, int base_diameter) {
        m_activity = activity;
        m_base_diameter = base_diameter;

        m_input = (InputTokenQueue) (m_activity.findViewById(R.id.input_token_queue_tutorial_right));
        m_wheels = new Wheel[2];
        m_wheels[0] = (Wheel) (m_activity.findViewById(R.id.wheel1_tutorial));
        m_wheels[1] = (Wheel) (m_activity.findViewById(R.id.wheel2_tutorial));
        m_output = (Output) (m_activity.findViewById(R.id.output));

        m_connectable_images = new ConnectableImage[4];
        m_connectable_images[0] = m_input;
        m_connectable_images[1] = m_wheels[0];
        m_connectable_images[2] = m_wheels[1];
        m_connectable_images[3] = m_output;

        m_message_text_view = (TextView) m_activity.findViewById(R.id.tutorial_bottom_text);

        ArrangeImages();
    }

    private void ArrangeImages() {
        m_stage = STAGE.STAGE_NONE;

        m_activity.SetConnectableImageDiameter(R.id.input_token_queue_tutorial_right, m_base_diameter * 1);
        m_activity.SetConnectableImageDiameter(R.id.wheel1_tutorial, m_base_diameter * 1);
        m_activity.SetConnectableImageDiameter(R.id.wheel2_tutorial, m_base_diameter * 3 / 2);
        m_activity.SetConnectableImageDiameter(R.id.output_tutorial, m_base_diameter * 1);

        m_activity.SetTokenQueueLocation(R.id.input_token_queue_tutorial_right, m_base_diameter * 2, 0);

        m_activity.ConnectWheelToInputQueue(R.id.wheel1_tutorial, R.id.input_token_queue_tutorial_right, 90);

        m_activity.SetWheelLocation(R.id.wheel1_tutorial, m_base_diameter, 30);

        m_wheels[0].SetWheelNum(0);
        m_wheels[1].SetWheelNum(1);

        m_activity.ConnectWheels(R.id.wheel2_tutorial, R.id.wheel1_tutorial, 0);
        m_activity.ConnectWheels(R.id.output_tutorial, R.id.wheel2_tutorial, 90);

        m_activity.AddHole(R.id.input_token_queue_tutorial_right, 270, PlayerType.PLAYER_0);
        m_activity.AddHole(R.id.input_token_queue_tutorial_right, 270, PlayerType.PLAYER_1);

        m_activity.AddHole(R.id.wheel1_tutorial, 270, PlayerType.PLAYER_0);
        m_activity.AddHole(R.id.wheel1_tutorial, 240, PlayerType.PLAYER_1);

        m_activity.AddHoles(R.id.wheel2_tutorial, 45, 4);

        m_activity.AddHole(R.id.output_tutorial, 90, PlayerType.PLAYER_0);
        m_activity.AddHole(R.id.output_tutorial, 90, PlayerType.PLAYER_1);

        for (int i = 0; i < m_wheels.length; i++) {
            m_wheels[i].setOnTouchListener(m_activity);
            m_wheels[i].DisableTwoDirectionLimitation();
        }
    }

    public void WheelFinishedRotating() {
        switch (m_stage) {
            case STAGE6:
                Stage7();
                break;
            case STAGE7:
                Stage8();
                break;
            default:
                break;
        }
    }

    public void TokenExit(Token token) {
        switch (m_stage) {
            case STAGE4:
                if (token.GetNumber() == 3) {
                    if (token.GetPreviousToken() == null) {
                        Stage5();
                    }
                }
                break;
            case STAGE5:
                if (token.GetNumber() == 5) {
                    if (token.GetPreviousToken() == null) {
                        Stage6();
                    }
                }
            case STAGE8:
                if (token.GetPlayerType() == PlayerType.PLAYER_1) {
                    ShowErrorMessage(R.string.tutorial_opponent_token_fall);
                    Stage6();
                }
            default:
                break;
        }
    }

    // This will be called when output think that game is end.
    // It can happen only because of bad order.
    public void GameEnd() {
        m_input.ClearAllTokens();
        m_wheels[0].Reset();
        m_wheels[1].Reset();
        m_output.Reset();

        switch (m_stage) {
            case STAGE4:
                Stage4();
                ShowErrorMessage(R.string.tutorial_bad_order_2_3);
                break;
            case STAGE5:
                Stage5();
                ShowErrorMessage(R.string.tutorial_bad_order_4_5);
                break;
            default:
                break;
        }
    }

    public void WheelRotated(int wheel_num) {
        Wheel wheel = m_wheels[wheel_num];

        switch (m_stage) {
            case STAGE_NONE:
                break;
            case STAGE1:
                if (wheel.GetNumTokens(PlayerType.PLAYER_0) == 1) {
                    Stage2();
                }
                break;
            case STAGE2:
                // wheel 0 with no token, or wheel 1 with one token.
                if (wheel_num == wheel.GetNumTokens(PlayerType.PLAYER_0)) {
                    Stage3();
                }
                break;
            case STAGE3:
                if (wheel_num == 1 && wheel.GetNumTokens(PlayerType.PLAYER_0) == 0) {
                    Stage4();
                }
                break;
        }
    }

    private void SetObjectVisibility(ObjectVisibility objectVisibility) {
        m_activity.SetObjectVisibility(objectVisibility);
        for (ConnectableImage connectableImage : m_connectable_images) {
            connectableImage.RulesChanged();
        }
    }

    private void Stage1() {
        m_stage = STAGE.STAGE1;
        m_activity.findViewById(R.id.input_token_queue_tutorial_right_layout).setVisibility(View.VISIBLE);
        m_activity.findViewById(R.id.wheel1_tutorial_layout).setVisibility(View.VISIBLE);
        m_activity.findViewById(R.id.wheel2_tutorial_layout).setVisibility(View.INVISIBLE);
        m_activity.findViewById(R.id.output_tutorial_layout).setVisibility(View.INVISIBLE);

        m_activity.SetPlayerType(PlayerType.PLAYER_0);
        SetObjectVisibility(ObjectVisibility.INVISIBLE);

        ((ConnectableImage) m_activity.findViewById(R.id.wheel1_tutorial)).Reset();
        ((ConnectableImage) m_activity.findViewById(R.id.wheel2_tutorial)).Reset();

        m_input.ClearAllTokens();
        m_input.SetLastToken(PlayerType.PLAYER_0, 1);
        m_input.AddTokenToPlayer(PlayerType.PLAYER_0, 1);

        ShowMessage(R.string.tutorial_rotate_wheel_to_queue);
    }

    private void Stage2() {
        m_stage = STAGE.STAGE2;
        m_activity.findViewById(R.id.wheel2_tutorial_layout).setVisibility(View.VISIBLE);
        ShowMessage(R.string.tutorial_connect_wheels);
    }

    private void Stage3() {
        m_stage = STAGE.STAGE3;
        m_activity.findViewById(R.id.output_tutorial_layout).setVisibility(View.VISIBLE);
        ShowMessage(R.string.tutorial_rotate_wheel_to_output);
    }

    private void Stage4() {
        m_stage = STAGE.STAGE4;
        m_input.SetLastToken(PlayerType.PLAYER_0, 3);
        m_input.AddTokenToPlayer(PlayerType.PLAYER_0, 2);
        ShowMessage(R.string.tutorial_fall_in_order_2_3);
    }

    private void Stage5() {
        m_stage = STAGE.STAGE5;
        m_input.SetLastToken(PlayerType.PLAYER_0, 4);
        m_input.AddTokenToPlayer(PlayerType.PLAYER_0, 5);
        ShowMessage(R.string.tutorial_fall_in_order_4_5);
    }

    private void Stage6() {
        m_stage = STAGE.STAGE6;
        m_input.ClearAllTokens();
        m_wheels[0].Reset();
        m_wheels[1].Reset();
        m_output.Reset();
        SetObjectVisibility(ObjectVisibility.ALWAYS_VISIBLE);
        m_input.SetLastToken(PlayerType.PLAYER_1, 1);
        m_input.AddTokenToPlayer(PlayerType.PLAYER_1, 1);
        for (int i = 0; i < m_wheels.length; i++) {
            m_wheels[i].SetAllowRotation(false);
        }
        ShowMessage(R.string.tutorial_opponent_token);

        // This will put the player on top.
        int angle = SECOND_WHEEL_PLAYER_FIRST_ANGLE - m_wheels[1].GetCurrentAngle();
        // This will put the opponent hole on top.
        angle += 180 / SECOND_WHEEL_NUM_HOLES;
        m_wheels[1].AddRotation(angle);
    }

    public void Stage7() {
        m_stage = STAGE.STAGE7;
        m_wheels[0].AddRotation(720);
    }

    public void Stage8() {
        m_stage = STAGE.STAGE8;
        for (int i = 0; i < m_wheels.length; i++) {
            m_wheels[i].SetAllowRotation(true);
        }
        m_input.SetLastToken(PlayerType.PLAYER_0, 1);
        m_input.AddTokenToPlayer(PlayerType.PLAYER_0, 1);
    }

    public void Show() {
        m_activity.findViewById(R.id.main_game_layout).setVisibility(View.INVISIBLE);
        m_activity.findViewById(R.id.tutorial_layout).setVisibility(View.VISIBLE);

        Stage1();
    }

    public void Hide() {
        m_activity.findViewById(R.id.main_game_layout).setVisibility(View.VISIBLE);
        m_activity.findViewById(R.id.tutorial_layout).setVisibility(View.INVISIBLE);
        m_stage = STAGE.STAGE_NONE;
    }

    private void ShowMessage(int id) {
        m_message_text_view.setText(id);
    }

    private void ShowErrorMessage(int id) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(m_activity, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(m_activity);
        }

        builder.setMessage(id);
        builder.setNeutralButton(R.string.confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        //builder.setIcon(R.drawable.some_icon); // TODO: Add this
        builder.show();
    }
}
