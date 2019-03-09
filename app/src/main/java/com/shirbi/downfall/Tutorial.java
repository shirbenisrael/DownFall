package com.shirbi.downfall;

import android.view.View;

public class Tutorial {
    protected MainActivity m_activity;
    protected int m_base_diameter;
    ConnectableImage m_connectable_images[];

    Tutorial(MainActivity activity, int base_diameter) {
        m_activity = activity;
        m_base_diameter = base_diameter;

        m_connectable_images = new ConnectableImage[4];
        m_connectable_images[0] = ((ConnectableImage)m_activity.findViewById(R.id.input_token_queue_tutorial_right));
        m_connectable_images[1] = ((ConnectableImage)m_activity.findViewById(R.id.wheel1_tutorial));
        m_connectable_images[2] = ((ConnectableImage)m_activity.findViewById(R.id.wheel2_tutorial));
        m_connectable_images[3] = ((ConnectableImage)m_activity.findViewById(R.id.output));

        ArrangeImages();
    }

    private void ArrangeImages() {
        m_activity.SetConnectableImageDiameter(R.id.input_token_queue_tutorial_right, m_base_diameter * 1);
        m_activity.SetConnectableImageDiameter(R.id.wheel1_tutorial, m_base_diameter * 4 / 3);
        m_activity.SetConnectableImageDiameter(R.id.wheel2_tutorial, m_base_diameter * 3 / 2);
        m_activity.SetConnectableImageDiameter(R.id.output_tutorial, m_base_diameter * 1);

        m_activity.SetTokenQueueLocation(R.id.input_token_queue_tutorial_right, m_base_diameter * 2, 0);

        m_activity.ConnectWheelToInputQueue(R.id.wheel1_tutorial, R.id.input_token_queue_tutorial_right, 90);

        m_activity.SetWheelLocation(R.id.wheel1_tutorial, m_base_diameter,30 );
        m_activity.ConnectWheels(R.id.wheel2_tutorial, R.id.wheel1_tutorial, 0);
        m_activity.ConnectWheels(R.id.output_tutorial, R.id.wheel2_tutorial, 90);

        m_activity.AddHole(R.id.input_token_queue_tutorial_right, 270, PlayerType.PLAYER_0);
        m_activity.AddHole(R.id.input_token_queue_tutorial_right, 270, PlayerType.PLAYER_1);

        m_activity.AddHole(R.id.wheel1_tutorial, 30, PlayerType.PLAYER_0);
        m_activity.AddHole(R.id.wheel1_tutorial, 330, PlayerType.PLAYER_1);

        m_activity.AddHoles(R.id.wheel2_tutorial, 45, 4);

        m_activity.AddHole(R.id.output_tutorial, 90, PlayerType.PLAYER_0);
        m_activity.AddHole(R.id.output_tutorial, 90, PlayerType.PLAYER_1);

        ((ConnectableImage)m_activity.findViewById(R.id.wheel1_tutorial)).setOnTouchListener(m_activity);
        ((ConnectableImage)m_activity.findViewById(R.id.wheel2_tutorial)).setOnTouchListener(m_activity);
    }

    private void Stage1() {
        m_activity.findViewById(R.id.input_token_queue__tutorial_right_layout).setVisibility(View.VISIBLE);
        m_activity.findViewById(R.id.wheel1_tutorial_layout).setVisibility(View.VISIBLE);
        m_activity.findViewById(R.id.wheel2_tutorial_layout).setVisibility(View.INVISIBLE);
        m_activity.findViewById(R.id.output_tutorial_layout).setVisibility(View.INVISIBLE);

        m_activity.SetPlayerType(PlayerType.PLAYER_0);
        m_activity.SetObjectVisibility(ObjectVisibility.INVISIBLE);

        for (ConnectableImage connectableImage : m_connectable_images ) {
            connectableImage.RulesChanged();
        }
    }

    public void Show() {
        m_activity.findViewById(R.id.main_game_layout).setVisibility(View.INVISIBLE);
        m_activity.findViewById(R.id.tutorial_layout).setVisibility(View.VISIBLE);

        Stage1();
    }

    public void Hide() {
        m_activity.findViewById(R.id.main_game_layout).setVisibility(View.VISIBLE);
        m_activity.findViewById(R.id.tutorial_layout).setVisibility(View.INVISIBLE);
    }
}
