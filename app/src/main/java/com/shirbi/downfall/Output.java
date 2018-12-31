package com.shirbi.downfall;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.ViewGroup;

public class Output extends ConnectableImage implements SlideToken {
    int m_player_num_tokens_left[] = new int[PlayerType.NUM_PLAYERS];

    public Output(Context context) {
        super(context);
    }

    public Output(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void AddHole(Hole hole, int base_angle) {
        super.AddHole(hole, base_angle);

        hole.CheckConnection(m_connections);
    }

    public void TokenEntered(Hole hole) {
        Token token = hole.GetResident();
        token.setVisibility(VISIBLE);
        token.QueueAnimation(5, Token.HORIZONTAL_ALIGNMENT.LEFT_EDJE, this);
        hole.SetResident(null);

        m_player_num_tokens_left[hole.GetPlayerType().getInt()]--;
        m_activity.ShowNumTokenLeft(hole.GetPlayerType(),
                m_player_num_tokens_left[hole.GetPlayerType().getInt()]);
    }

    public void TokenStoppedMoving(Token token) {
        ((ViewGroup)(token.getParent())).removeView(token);
    }

    public void Reset() {
        for (int i = 0; i < m_player_num_tokens_left.length; i++) {
            m_player_num_tokens_left[i] = 10;
            m_activity.ShowNumTokenLeft(PlayerType.values()[i], m_player_num_tokens_left[i]);
        }
    }

    public void StoreState(SharedPreferences.Editor editor) {
        for (Hole hole : m_holes) {
            int player_num = hole.GetPlayerType().getInt();

            String str = (m_activity.getString(R.string.output_queue_tokens)) + player_num;

            editor.putInt(str, m_player_num_tokens_left[player_num]);
        }
    }

    public void RestoreState(SharedPreferences sharedPref) {
        for (Hole hole : m_holes) {
            int player_num = hole.GetPlayerType().getInt();

            String str = (m_activity.getString(R.string.output_queue_tokens)) + player_num;

            m_player_num_tokens_left[player_num] = sharedPref.getInt(str, 10);

            m_activity.ShowNumTokenLeft(hole.GetPlayerType(),  m_player_num_tokens_left[player_num]);
        }
    }
}
