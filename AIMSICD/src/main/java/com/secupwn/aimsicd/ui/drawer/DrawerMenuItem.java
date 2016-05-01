/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.ui.drawer;

import android.support.annotation.DrawableRes;

import com.secupwn.aimsicd.R;
import com.secupwn.aimsicd.constants.DrawerMenu.ID.APPLICATION;
import com.secupwn.aimsicd.constants.DrawerMenu.ID.DATABASE_SETTINGS;
import com.secupwn.aimsicd.constants.DrawerMenu.ID.MAIN;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DrawerMenuItem implements NavDrawerItem {

    public static final int ITEM_TYPE = 1;

    private int id;
    private String label;
    @DrawableRes
    private int iconId;
    private boolean updateActionBarTitle;
    private boolean showInfoButton;

    private DrawerMenuItem() {
    }

    public static DrawerMenuItem create(int pMenuId, String pLabel, int pIconDrawableId, boolean pUpdateActionBarTitle) {
        return create(pMenuId, pLabel, pIconDrawableId, pUpdateActionBarTitle, true);
    }


    public static DrawerMenuItem create(int pMenuId, String pLabel, int pIconDrawableId, boolean pUpdateActionBarTitle, boolean pIsShowInfoButton) {
        DrawerMenuItem item = new DrawerMenuItem();
        item.setId(pMenuId);
        item.setLabel(pLabel);
        item.setIconId(pIconDrawableId);
        item.setUpdateActionBarTitle(pUpdateActionBarTitle);
        item.setShowInfoButton(pIsShowInfoButton);
        return item;
    }

    @Override
    public int getType() {
        return ITEM_TYPE;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean updateActionBarTitle() {
        return isUpdateActionBarTitle();
    }

    /**
     *
     * @return Returns a string that describes the menu item
     */
    public int getHelpStringId() {

        switch (id) {

            case MAIN.PHONE_SIM_DETAILS:
                return R.string.help_main_phone_sim_details;

            case MAIN.NEIGHBORING_CELLS:
                return R.string.help_main_neighboring_cells;

            case MAIN.DB_VIEWER:
                return R.string.help_main_database_viewer;

            case MAIN.ANTENNA_MAP_VIEW:
                return R.string.help_main_antenna_map_view;

            case MAIN.AT_COMMAND_INTERFACE:
                return R.string.help_main_at_command_interface;

            case DATABASE_SETTINGS.RESET_DB:
                return R.string.help_settings_reset_db;

            case APPLICATION.DOWNLOAD_LOCAL_BTS_DATA:
                return R.string.help_app_download_local_bts;

            case APPLICATION.ADD_GET_OCID_API_KEY:
                return R.string.help_app_add_get_ocid_api_key;

            case APPLICATION.UPLOAD_LOCAL_BTS_DATA:
                return R.string.help_app_upload_local_bts;

            case APPLICATION.QUIT:
                return R.string.help_app_quit;

            default:
                return R.string.empty;
        }
    }
}
