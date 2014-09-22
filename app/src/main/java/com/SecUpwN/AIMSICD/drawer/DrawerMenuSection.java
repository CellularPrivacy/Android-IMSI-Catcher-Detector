package com.SecUpwN.AIMSICD.drawer;

public class DrawerMenuSection implements NavDrawerItem {

    private static final int SECTION_TYPE = 0;
    private int id;
    private String label;
    private int icon;

    private DrawerMenuSection() {
    }

    public static DrawerMenuSection create( int id, String label ) {
        DrawerMenuSection section = new DrawerMenuSection();
        section.setId(id);
        section.setLabel(label);
        return section;
    }

    @Override
    public int getType() {
        return SECTION_TYPE;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean updateActionBarTitle() {
        return false;
    }
}