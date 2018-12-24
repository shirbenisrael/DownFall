package com.shirbi.downfall;

public abstract class OppositePlayer {
    protected Wheel m_wheels[];

    OppositePlayer( Wheel wheels[]) {
        m_wheels = wheels;
    }

    public abstract int Run();
}
