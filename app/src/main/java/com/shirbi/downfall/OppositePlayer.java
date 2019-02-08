package com.shirbi.downfall;

public abstract class OppositePlayer {
    protected Wheel m_wheels[];
    protected MainActivity m_activity;

    OppositePlayer( Wheel wheels[], MainActivity activity) {
        m_wheels = wheels;
        m_activity = activity;
    }

    public abstract int Run();
}
