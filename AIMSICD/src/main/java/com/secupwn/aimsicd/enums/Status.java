package com.secupwn.aimsicd.enums;

import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;

import com.secupwn.aimsicd.R;

import lombok.Getter;

@Getter
public enum Status {
    /**
     * Grey
     */
    IDLE(R.string.status_idle, R.color.material_grey_400),
    /**
     * Green
     */
    OK(R.string.status_ok, R.color.material_light_green_A700),
    /**
     * Yellow
     */
    MEDIUM(R.string.status_medium, R.color.material_yellow_A700),
    /**
     * Orange
     */
    HIGH(R.string.status_high, R.color.material_orange_A700),
    /**
     * Red
     */
    DANGER(R.string.status_danger, R.color.material_red_A700),
    /**
     * Black
     */
    SKULL(R.string.status_skull, R.color.material_black);

    @StringRes
    private int name;

    @ColorRes
    private int color;

    Status(@StringRes int name, @ColorRes int color) {
        this.name = name;
        this.color = color;
    }
}
