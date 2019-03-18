package com.shirbi.downfall;

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

    public enum STAGE {
        STAGE_NONE,
        STAGE1,
        STAGE2,
        STAGE3,
        STAGE4,
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
                ShowMessage(R.string.tutorial_bad_order_2_3);
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

    private void Stage1() {
        m_stage = STAGE.STAGE1;
        m_activity.findViewById(R.id.input_token_queue_tutorial_right_layout).setVisibility(View.VISIBLE);
        m_activity.findViewById(R.id.wheel1_tutorial_layout).setVisibility(View.VISIBLE);
        m_activity.findViewById(R.id.wheel2_tutorial_layout).setVisibility(View.INVISIBLE);
        m_activity.findViewById(R.id.output_tutorial_layout).setVisibility(View.INVISIBLE);

        m_activity.SetPlayerType(PlayerType.PLAYER_0);
        m_activity.SetObjectVisibility(ObjectVisibility.INVISIBLE);

        for (ConnectableImage connectableImage : m_connectable_images) {
            connectableImage.RulesChanged();
        }

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
        ShowMessage(R.string.tutorial_fall_in_order);
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
}
