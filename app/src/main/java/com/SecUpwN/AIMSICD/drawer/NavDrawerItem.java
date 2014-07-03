package com.SecUpwN.AIMSICD.drawer;

public interface NavDrawerItem {
    public int getId();
    public String getLabel();
    public void setLabel(String label);
    public void setIcon(int icon);
    public int getType();
    public boolean isEnabled();
    public boolean updateActionBarTitle();
}