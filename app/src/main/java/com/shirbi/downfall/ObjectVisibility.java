package com.shirbi.downfall;

import android.view.View;

public enum ObjectVisibility {
    ALWAYS_VISIBLE(0), INVISIBLE(1), VISIBLE_ON_CONNECT(2);

    private final int m_visibility;

    ObjectVisibility(int visibility) {
        this.m_visibility = visibility;
    }

    public int getInt() {
        return this.m_visibility;
    }

    public void SetOnView(View view) {
        switch (this) {
            case INVISIBLE:
                view.setVisibility(View.INVISIBLE);
                break;

            case ALWAYS_VISIBLE:
                view.setVisibility(View.VISIBLE);
                break;

            case VISIBLE_ON_CONNECT:
                view.setVisibility(View.INVISIBLE);
                break;
        }
    }
}
