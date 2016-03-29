/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.ui.drawer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DrawerMenuSection implements NavDrawerItem {

    private static final int SECTION_TYPE = 0;
    private int id;
    private String label;

    private DrawerMenuSection() {
    }

    public static DrawerMenuSection create(int id, String label) {
        DrawerMenuSection section = new DrawerMenuSection();
        section.setId(id);
        section.setLabel(label);
        return section;
    }

    @Override
    public int getType() {
        return SECTION_TYPE;
    }

    public void setIconId(int icon) {
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public boolean updateActionBarTitle() {
        return false;
    }
}
