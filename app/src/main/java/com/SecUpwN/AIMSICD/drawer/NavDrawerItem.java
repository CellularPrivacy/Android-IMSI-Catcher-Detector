/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.drawer;

public interface NavDrawerItem {
    public int getId();
    public String getLabel();
    public void setLabel(String label);
    public void setmIconId(int icon);
    public int getType();
    public boolean isEnabled();
    public boolean updateActionBarTitle();
}