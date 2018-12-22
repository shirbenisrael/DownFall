package com.shirbi.downfall;

import android.content.Context;
import android.util.AttributeSet;

public class Output extends ConnectableImage {
    Context m_context;

    public Output(Context context) {
        super(context);
        m_context = context;
    }

    public Output(Context context, AttributeSet attrs) {
        super(context, attrs);
        m_context = context;
    }

    @Override
    public void AddHole(Hole hole, int base_angle) {
        super.AddHole(hole, base_angle);

        hole.CheckConnection(m_connections);
    }

    public void TokenEntered(Hole hole) {
        hole.GetResident().OutputAnimation();
        hole.SetResident(null);
    }
}
