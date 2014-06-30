package com.SecUpwN.AIMSICD.fragments;

import com.SecUpwN.AIMSICD.AIMSICD.NavDrawerItem;

import android.content.Context;

public class DrawerMenuItem implements NavDrawerItem {

    public static final int ITEM_TYPE = 1 ;

    private int id ;
    private String label ;
    private int icon ;
    private boolean updateActionBarTitle ;

    private DrawerMenuItem() {
    }

    public static DrawerMenuItem create( int id, String label, String icon, boolean updateActionBarTitle, Context context ) {
        DrawerMenuItem item = new DrawerMenuItem();
        item.setId(id);
        item.setLabel(label);
        item.setIcon(context.getResources().getIdentifier( icon, "drawable", context.getPackageName()));
        item.setUpdateActionBarTitle(updateActionBarTitle);
        return item;
    }

    @Override
    public int getType() {
        return ITEM_TYPE;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean updateActionBarTitle() {
        return this.updateActionBarTitle;
    }

    public void setUpdateActionBarTitle(boolean updateActionBarTitle) {
        this.updateActionBarTitle = updateActionBarTitle;
    }
}