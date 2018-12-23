package com.shirbi.downfall;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import java.util.ArrayList;

public class InputTokenQueue extends ConnectableImage {

    public static final int MAX_TOKENS = 5;

    private ArrayList<Token> m_tokens;
    private ArrayList<Token> m_tokens_opposite;

    Token.HORIZONTAL_ALIGNMENT m_horizontal_alignment;

    private void Init(Context context) {
        m_tokens = new ArrayList<Token>();
        m_tokens_opposite = new ArrayList<Token>();
    }

    public InputTokenQueue(Context context) {
        super(context);
        Init(context);
    }

    public InputTokenQueue(Context context, AttributeSet attrs) {
        super(context, attrs);
        Init(context);
    }

    // Find hole with same player as token.
    private Hole MatchHoleToToke(Token token) {
        for (Hole hole : m_holes) {
            if (hole.GetOppositeSide() == token.GetOppositeSide()) {
                return hole;
            }
        }

        return null;
    }

    // Return token list according token player.
    private ArrayList<Token> MatchTokenList(Token token) {
        return token.GetOppositeSide() ? m_tokens_opposite : m_tokens;
    }

    private ArrayList<Token> MatchHoleToTokenList(Hole hole) {
        return hole.GetOppositeSide() ? m_tokens_opposite : m_tokens;
    }

    public void AddToken(Token token) {
        Hole hole = MatchHoleToToke(token);
        ArrayList<Token> tokens_list = MatchTokenList(token);

        token.SetDiameter(hole.m_diameter);

        if (hole.HasResident()) {
            int angle = hole.GetBaseAngle();
            Token last_token;

            if (tokens_list.isEmpty()) {
                last_token = hole.GetResident();
            } else {
                last_token = tokens_list.get(tokens_list.size() - 1);
            }

            token.SetLocationNearOtherToken(last_token, m_horizontal_alignment, Token.VERTICAL_ALIGNMENT.TOP);
            tokens_list.add(token);
        } else {
            hole.SetResident(token);
            hole.SetAngle(0); // This will make the token shown */
        }

        token.Rotate(0);
    }

    public void TokenUsed(Hole hole) {
        ArrayList<Token> tokens_list = MatchHoleToTokenList(hole);

        if (tokens_list.isEmpty()) {
            return;
        }

        Token token = tokens_list.get(0);
        hole.SetResident(token);
        hole.SetAngle(0); // This will make the token shown */
        tokens_list.remove(0);

        int num_tokens_to_update = tokens_list.size();
        int angle = hole.GetBaseAngle();

        for (int i = 0; i < num_tokens_to_update; i++) {
            Token next_token = tokens_list.get(i);
            next_token.SetLocationNearOtherToken(token, m_horizontal_alignment, Token.VERTICAL_ALIGNMENT.TOP);
            token = next_token;
        }
    }

    public void AddHole(Hole hole, int base_angle) {
        super.AddHole(hole, base_angle);

        hole.CheckConnection(m_connections);

        m_horizontal_alignment = base_angle < 180 ?
                Token.HORIZONTAL_ALIGNMENT.LEFT_EDJE :
                Token.HORIZONTAL_ALIGNMENT.RIGHT_EDJE;
    }

    public void Reset() {
        super.Reset();

        for (Token token : m_tokens) {
            ((ViewGroup) (token.getParent())).removeView(token);
        }
        m_tokens.clear();

        for (Token token : m_tokens_opposite) {
            ((ViewGroup) (token.getParent())).removeView(token);
        }
        m_tokens_opposite.clear();
    }
}
