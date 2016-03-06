package com.secupwn.aimsicd.enums;

import android.support.annotation.StringRes;

import com.secupwn.aimsicd.R;

public enum Status {
    /**
     * Grey
     */
    IDLE(R.string.status_idle),
    /**
     * Green
     */
    OK(R.string.status_ok),
    /**
     * Yellow
     */
    MEDIUM(R.string.status_medium),
    /**
     * Orange
     */
    HIGH(R.string.status_high),
    /**
     * Red
     */
    DANGER(R.string.status_danger),
    /**
     * Black
     */
    SKULL(R.string.status_skull);

    @StringRes
    private int name;

    Status(@StringRes int name) {
        this.name = name;
    }

    public int getName() {
        return name;
    }
}
