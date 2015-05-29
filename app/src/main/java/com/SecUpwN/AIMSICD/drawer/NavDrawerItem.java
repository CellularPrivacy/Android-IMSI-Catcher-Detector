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