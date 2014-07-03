package com.SecUpwN.AIMSICD.drawer;

import android.widget.BaseAdapter;

import java.util.List;

public class DrawerMenuActivityConfiguration {

    private int mainLayout;
    private int drawerLayoutId;
    private int leftDrawerId;
    private int[] actionMenuItemsToHideWhenDrawerOpen;
    private List<NavDrawerItem> navItems;
    private int drawerOpenDesc;
    private int drawerCloseDesc;
    private BaseAdapter baseAdapter;

    public int getMainLayout() {
        return mainLayout;
    }

    public void setMainLayout(int mainLayout) {
        this.mainLayout = mainLayout;
    }

    public int getDrawerLayoutId() {
        return drawerLayoutId;
    }

    public void setDrawerLayoutId(int drawerLayoutId) {
        this.drawerLayoutId = drawerLayoutId;
    }

    public int getLeftDrawerId() {
        return leftDrawerId;
    }

    public void setLeftDrawerId(int leftDrawerId) {
        this.leftDrawerId = leftDrawerId;
    }

    public int[] getActionMenuItemsToHideWhenDrawerOpen() {
        return actionMenuItemsToHideWhenDrawerOpen;
    }

    public void setActionMenuItemsToHideWhenDrawerOpen(
            int[] actionMenuItemsToHideWhenDrawerOpen) {
        this.actionMenuItemsToHideWhenDrawerOpen = actionMenuItemsToHideWhenDrawerOpen;
    }

    public List<NavDrawerItem> getNavItems() {
        return navItems;
    }

    public void setNavItems(List<NavDrawerItem> navItems) {
        this.navItems = navItems;
    }

    public int getDrawerOpenDesc() {
        return drawerOpenDesc;
    }

    public void setDrawerOpenDesc(int drawerOpenDesc) {
        this.drawerOpenDesc = drawerOpenDesc;
    }

    public int getDrawerCloseDesc() {
        return drawerCloseDesc;
    }

    public void setDrawerCloseDesc(int drawerCloseDesc) {
        this.drawerCloseDesc = drawerCloseDesc;
    }

    public BaseAdapter getBaseAdapter() {
        return baseAdapter;
    }

    public void setBaseAdapter(BaseAdapter baseAdapter) {
        this.baseAdapter = baseAdapter;
    }
}