package com.SecUpwN.AIMSICD.utils;

import com.SecUpwN.AIMSICD.R;

/**
 * Class that holds and returns the correct icon based on requested icon format and
 * current system status.
 *
 * @author Tor Henning Ueland
 */
public class Icon {
    public enum Type {
        FLAT,
        SENSE,
        WHITE,
    }

    public static int getIcon(Type t) {
        switch(t) {
            case FLAT:
                switch (Status.getStatus()) {
                    case IDLE:
                        return R.drawable.flat_idle;

                    case NORMAL:
                        return R.drawable.flat_ok;

                    case MEDIUM:
                        return R.drawable.flat_medium;

                    case ALARM:
                        return R.drawable.flat_danger;

                    default:
                        return R.drawable.flat_idle;
                }

            case SENSE:
                switch (Status.getStatus()) {
                    case IDLE:
                        return R.drawable.sense_idle;

                    case NORMAL:
                        return R.drawable.sense_ok;

                    case MEDIUM:
                        return R.drawable.sense_medium;

                    case ALARM:
                        return R.drawable.sense_danger;

                    default:
                        return R.drawable.sense_idle;
                }

            case WHITE:
                switch (Status.getStatus()) {
                    case IDLE:
                        return R.drawable.white_idle;

                    case NORMAL:
                        return R.drawable.white_ok;

                    case MEDIUM:
                        return R.drawable.white_medium;

                    case ALARM:
                        return R.drawable.white_danger;

                    default:
                        return R.drawable.white_idle;
                }
        }
        return -1;
    }
}
