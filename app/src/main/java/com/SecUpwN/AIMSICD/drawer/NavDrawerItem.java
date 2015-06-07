/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.drawer;

public interface NavDrawerItem {
    int getId();
    String getLabel();
    void setLabel(String label);
    void setmIconId(int icon);
    int getType();
    boolean isEnabled();
    boolean updateActionBarTitle();
}