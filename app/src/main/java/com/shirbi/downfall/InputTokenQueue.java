package com.shirbi.downfall;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import java.util.ArrayList;

public class InputTokenQueue extends ConnectableImage implements SlideToken {

    public static final int MAX_TOKENS = 5;

    private class TokenList extends  ArrayList<Token> {};

    private TokenList[] m_tokens = new TokenList[PlayerType.NUM_PLAYERS];

    Token.HORIZONTAL_ALIGNMENT m_horizontal_alignment;

    private void Init(Context context) {
        String strings[];
        strings = new String[2];

        for (int i = 0; i < m_tokens.length; i++) {
            m_tokens[i] = new TokenList();
        }
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
            if (hole.GetPlayerType() == token.GetPlayerType()) {
                return hole;
            }
        }

        return null;
    }

    // Return token list according token player.
    private TokenList MatchTokenList(Token token) {
        return m_tokens[token.GetPlayerType().getInt()];
    }

    private TokenList MatchHoleToTokenList(Hole hole) {
        return m_tokens[hole.GetPlayerType().getInt()];
    }

    private void AddToken(Token token) {
        Hole hole = MatchHoleToToke(token);
        TokenList tokens_list = MatchTokenList(token);

        token.SetDiameter(hole.m_diameter);

        if (hole.HasResident()) {
            tokens_list.add(token);
        } else {
            hole.SetResident(token);
        }

        token.Rotate(0);
        token.SetStartingLocationSlidingToHole(hole, m_horizontal_alignment);

        Token.HORIZONTAL_ALIGNMENT animation_direction =
                (m_horizontal_alignment == Token.HORIZONTAL_ALIGNMENT.LEFT_EDJE) ?
                        Token.HORIZONTAL_ALIGNMENT.RIGHT_EDJE :
                        Token.HORIZONTAL_ALIGNMENT.LEFT_EDJE;

        token.QueueAnimation(6 - token.GetNumber(),animation_direction, this );
    }

    public void TokenStoppedMoving(Token token) {
        if (token.GetNumber() == 5) {
            return;
        }

        Token new_token = new Token(m_activity);

        new_token.SetType(token.GetColor(), token.GetNumber()+ 1 );
        new_token.SetPlayerType(token.GetPlayerType());
        AddToken(new_token);
    }

    public void TokenUsed(Hole hole) {
        TokenList tokens_list = MatchHoleToTokenList(hole);

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

        for (int i = 0; i < m_tokens.length; i++) {
            for (Token token : m_tokens[i]) {
                ((ViewGroup) (token.getParent())).removeView(token);
            }
            m_tokens[i].clear();
        }
        Token token = new Token(m_activity);

        Token.COLOR color =
                (m_horizontal_alignment == Token.HORIZONTAL_ALIGNMENT.LEFT_EDJE) ?
                        Token.COLOR.COLOR_1 :
                        Token.COLOR.COLOR_2;

        token.SetType(color, 1);
        AddToken(token);

        token = new Token(m_activity);
        token.SetType(color, 1);
        token.SetPlayerType(PlayerType.AI_PLAYER);
        AddToken(token);
    }

    // Ignore the one in the hole
    public int GetNumTokensInQueue(PlayerType player_type) {
        return m_tokens[player_type.getInt()].size();
    }
}
