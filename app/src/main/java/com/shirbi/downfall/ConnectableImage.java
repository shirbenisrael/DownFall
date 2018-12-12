package com.shirbi.downfall;

import android.content.Context;
import android.util.AttributeSet;

import java.util.HashSet;
import java.util.Set;

public abstract class ConnectableImage extends RotatableImage {
    protected Set<Connection> m_connections;

    private void Init() {
        m_connections = new HashSet<Connection>();
    }

    public ConnectableImage(Context context) {
        super(context);
        Init();
    }

    public ConnectableImage(Context context, AttributeSet attrs) {
        super(context, attrs);
        Init();
    }

    public abstract void AddHole(Hole hole, int base_angle);

    public void TokenUsed() {}
}
