package com.shirbi.downfall;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

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
        token.ShowOnExit();
        token.QueueAnimation(5, Token.HORIZONTAL_ALIGNMENT.LEFT_EDJE, this);
        hole.SetResident(null);
    }

    public void TokenStoppedMoving(Token token) {
        token.RemoveFromParentView();
        token.SetOwnerWheel(null);
        token.Unregister();

        int player_num = token.GetPlayerType().getInt();

        m_player_num_tokens_left[player_num]--;
        m_activity.ShowNumTokenLeft(token.GetPlayerType(), m_player_num_tokens_left[player_num]);

        if (m_player_num_tokens_left[player_num] == 0) {
            String string;
            if (token.GetPlayerType() == m_activity.GetPlayerType()) {
                string = m_activity.getString(R.string.win);
            } else {
                string = m_activity.getString(R.string.lose);
            }
            m_activity.EndGame(string);
        }

        if (token.GetPreviousToken() != null) {
            String string;
            if (token.GetPlayerType() == m_activity.GetPlayerType()) {
                string = m_activity.getString(R.string.lose_order);
            } else {
                string = m_activity.getString(R.string.win_order);
            }
            m_activity.EndGame(string);
        }
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

    public int GetWheelNum() {
        return 5;
    }
}
