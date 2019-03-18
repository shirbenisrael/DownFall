package com.shirbi.downfall;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

public class InputTokenQueue extends ConnectableImage implements SlideToken {

    public static final int MAX_TOKENS = 5;

    private TokenList[] m_tokens = new TokenList[PlayerType.NUM_PLAYERS];

    Token.HORIZONTAL_ALIGNMENT m_horizontal_alignment;

    int m_last_token_num[] = new int[PlayerType.NUM_PLAYERS];

    private void Init(Context context) {
        String strings[];
        strings = new String[2];

        for (int i = 0; i < m_tokens.length; i++) {
            m_tokens[i] = new TokenList();
        }

        for (int i = 0; i < PlayerType.NUM_PLAYERS; i++) {
            m_last_token_num[i] = MAX_TOKENS;
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

    private void AddToken(Token token, int num_moves) {
        Hole hole = MatchHoleToToke(token);
        TokenList tokens_list = MatchTokenList(token);

        token.Register();

        token.RulesChanged();

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

        token.QueueAnimation(num_moves, animation_direction, this );
    }

    public void SetLastToken(PlayerType player_type, int token_num) {
        m_last_token_num[player_type.getInt()] = token_num;
    }

    public void TokenStoppedMoving(Token token) {
        int final_token_number = m_last_token_num[token.GetPlayerType().getInt()];
        int token_number = token.GetNumber();
        if (token_number == final_token_number) {
            return;
        }

        Token new_token = new Token(m_activity);

        int tokens_int_queue = m_tokens[token.GetPlayerType().getInt()].size() + 1; // include the hole

        int new_number = token_number < final_token_number ? token_number + 1 : token_number -1;
        new_token.SetType(token.GetColor(), new_number );
        new_token.SetPlayerType(token.GetPlayerType());
        AddToken(new_token, MAX_TOKENS - tokens_int_queue);
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
        ClearAllTokens();

        Token token = new Token(m_activity);

        Token.COLOR color =
                (m_horizontal_alignment == Token.HORIZONTAL_ALIGNMENT.LEFT_EDJE) ?
                        Token.COLOR.COLOR_1 :
                        Token.COLOR.COLOR_2;

        token.SetType(color, 1);
        AddToken(token, MAX_TOKENS);

        token = new Token(m_activity);
        token.SetType(color, 1);
        token.SetPlayerType(PlayerType.PLAYER_1);
        AddToken(token, MAX_TOKENS);
    }

    public void ClearAllTokens() {
        super.Reset();

        for (int i = 0; i < m_tokens.length; i++) {
            for (Token token : m_tokens[i]) {
                token.RemoveFromParentView();
            }
            m_tokens[i].clear();
        }
    }

    public void AddTokenToPlayer(PlayerType player_type, int token_num) {
        Token token = new Token(m_activity);

        Token.COLOR color =
                (m_horizontal_alignment == Token.HORIZONTAL_ALIGNMENT.LEFT_EDJE) ?
                        Token.COLOR.COLOR_1 :
                        Token.COLOR.COLOR_2;

        token.SetType(color, token_num);
        token.SetPlayerType(player_type);
        AddToken(token, MAX_TOKENS);
    }

    // Ignore the one in the hole
    public int GetNumTokensInQueue(PlayerType player_type) {
        return m_tokens[player_type.getInt()].size();
    }

    public void StoreState(SharedPreferences.Editor editor) {
        for (Hole hole : m_holes) {
            String str = (m_activity.getString(R.string.input_queue_tokens))
                    + String.valueOf(m_horizontal_alignment) + hole.GetPlayerType().getInt();

            int tokens_int_queue = m_tokens[hole.GetPlayerType().getInt()].size();
            int token_in_hole = hole.GetResident() == null ? 0 : 1;
            editor.putInt(str, tokens_int_queue + token_in_hole);
        }
    }

    public void RestoreStatePart2(SharedPreferences sharedPref) {
        super.Reset();

        for (Hole hole : m_holes) {
            String str = (m_activity.getString(R.string.input_queue_tokens))
                    + String.valueOf(m_horizontal_alignment) + hole.GetPlayerType().getInt();

            int num_tokens_to_add = sharedPref.getInt(str, MAX_TOKENS);

            if (num_tokens_to_add == 0 || num_tokens_to_add > 5) {
                continue;
            }

            Token token = new Token(m_activity);

            Token.COLOR color =
                    (m_horizontal_alignment == Token.HORIZONTAL_ALIGNMENT.LEFT_EDJE) ?
                            Token.COLOR.COLOR_1 :
                            Token.COLOR.COLOR_2;

            token.SetType(color, 6 - num_tokens_to_add);
            token.SetPlayerType(hole.GetPlayerType());
            AddToken(token, MAX_TOKENS);
        }
    }

    public void RulesChanged() {
        super.RulesChanged();

        for (Token token : m_tokens[m_activity.GetPlayerType().getOppositeInt()]) {
            token.RulesChanged();
        }
    }
}
