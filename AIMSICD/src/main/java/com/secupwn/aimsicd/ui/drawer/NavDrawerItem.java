/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.ui.drawer;

import android.support.annotation.DrawableRes;

public interface NavDrawerItem {
    int getId();
    String getLabel();
    void setLabel(String label);
    void setIconId(@DrawableRes int icon);
    int getType();
    boolean isEnabled();
    boolean updateActionBarTitle();
}
