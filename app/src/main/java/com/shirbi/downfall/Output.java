package com.shirbi.downfall;

import android.content.Context;
import android.util.AttributeSet;

public class Output extends ConnectableImage {
    private MainActivity m_activity;
    int m_player_num_tokens_left;
    int m_opposite_num_tokens_left;

    public Output(Context context) {
        super(context);
        m_activity = (MainActivity)context;
    }

    public Output(Context context, AttributeSet attrs) {
        super(context, attrs);
        m_activity = (MainActivity)context;
    }

    @Override
    public void AddHole(Hole hole, int base_angle) {
        super.AddHole(hole, base_angle);

        hole.CheckConnection(m_connections);
    }

    public void TokenEntered(Hole hole) {

        hole.GetResident().OutputAnimation();
        hole.SetResident(null);

        if (hole.GetOppositeSide()) {
            m_opposite_num_tokens_left--;
            m_activity.ShowNumTokenLeft(true, m_opposite_num_tokens_left);
        } else {
            m_player_num_tokens_left--;
            m_activity.ShowNumTokenLeft(false, m_player_num_tokens_left);
        }
    }

    public void Reset() {
        m_opposite_num_tokens_left = 10;
        m_activity.ShowNumTokenLeft(true, m_opposite_num_tokens_left);
        m_player_num_tokens_left = 10;
        m_activity.ShowNumTokenLeft(false, m_player_num_tokens_left);
    }
}
