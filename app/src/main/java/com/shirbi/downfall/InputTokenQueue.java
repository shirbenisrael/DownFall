package com.shirbi.downfall;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import java.util.ArrayList;

public class InputTokenQueue extends ConnectableImage {

    public static final int MAX_TOKENS = 5;

    private ArrayList<Token> m_tokens;

    private Hole m_hole_out;

    Token.HORIZONTAL_ALIGNMENT m_horizontal_alignment;

    private void Init(Context context) {
        m_tokens = new ArrayList<Token>();
        m_hole_out = new Hole(context);
    }

    public InputTokenQueue(Context context) {
        super(context);
        Init(context);
    }

    public InputTokenQueue(Context context, AttributeSet attrs) {
        super(context, attrs);
        Init(context);
    }

    public void AddToken(Token token) {
        token.SetDiameter(m_hole_out.m_diameter);

        if (m_hole_out.HasResident()) {
            int angle = m_hole_out.GetBaseAngle();
            Token last_token;

            if (m_tokens.isEmpty()) {
                last_token = m_hole_out.GetResident();
            } else {
                last_token = m_tokens.get(m_tokens.size() - 1);
            }

            token.SetLocationNearOtherToken(last_token, m_horizontal_alignment, Token.VERTICAL_ALIGNMENT.TOP);
            m_tokens.add(token);
        } else {
            m_hole_out.SetResident(token);
            m_hole_out.SetAngle(0); // This will make the token shown */
        }

        token.Rotate(0);
    }

    public void TokenUsed() {
        if (m_tokens.isEmpty()) {
            return;
        }

        Token token = m_tokens.get(0);
        m_hole_out.SetResident(token);
        m_hole_out.SetAngle(0); // This will make the token shown */
        m_tokens.remove(0);

        int num_tokens_to_update = m_tokens.size();
        int angle = m_hole_out.GetBaseAngle();

        for (int i = 0; i < num_tokens_to_update; i++) {
            Token next_token = m_tokens.get(i);
            next_token.SetLocationNearOtherToken(token, m_horizontal_alignment, Token.VERTICAL_ALIGNMENT.TOP);
            token = next_token;
        }
    }

    public void AddHole(Hole hole, int base_angle) {
        m_hole_out = hole;
        hole.SetBaseAngle(this, base_angle);

        RelativeLayout relativeLayout = (RelativeLayout) this.getParent();
        relativeLayout.addView(hole);
        hole.SetAngle(0);
        hole.CheckConnection(m_connections);

        m_horizontal_alignment = base_angle < 180 ?
                Token.HORIZONTAL_ALIGNMENT.LEFT_EDJE :
                Token.HORIZONTAL_ALIGNMENT.RIGHT_EDJE;
    }
}
