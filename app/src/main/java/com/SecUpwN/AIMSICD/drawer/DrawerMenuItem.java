/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.drawer;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.constants.DrawerMenu.ID.APPLICATION;
import com.SecUpwN.AIMSICD.constants.DrawerMenu.ID.MAIN;
import com.SecUpwN.AIMSICD.constants.DrawerMenu.ID.SETTINGS;
import com.SecUpwN.AIMSICD.constants.DrawerMenu.ID.TRACKING;

public class DrawerMenuItem implements NavDrawerItem {

    public static final int ITEM_TYPE = 1;

    private int mId;
    private String mLabel;
    private int mIconId;
    private boolean mUpdateActionBarTitle;
    private boolean mIsShowInfoButton;

    private DrawerMenuItem() {
    }

    public static DrawerMenuItem create(int pMenuId, String pLabel, int pIconDrawableId, boolean pUpdateActionBarTitle) {
        return create(pMenuId, pLabel, pIconDrawableId, pUpdateActionBarTitle, true);
    }


    public static DrawerMenuItem create(int pMenuId, String pLabel, int pIconDrawableId, boolean pUpdateActionBarTitle, boolean pIsShowInfoButton) {
        DrawerMenuItem item = new DrawerMenuItem();
        item.setId(pMenuId);
        item.setLabel(pLabel);
        item.setmIconId(pIconDrawableId);
        item.setUpdateActionBarTitle(pUpdateActionBarTitle);
        item.setIsShowInfoButton(pIsShowInfoButton);
        return item;
    }

    @Override
    public int getType() {
        return ITEM_TYPE;
    }

    public int getId() {
        return mId;
    }

    void setId(int pId) {
        mId = pId;
    }

    public String getLabel() {
        return mLabel;
    }

    public void setLabel(String pLabel) {
        mLabel = pLabel;
    }

    public int getIconId() {
        return mIconId;
    }

    public void setmIconId(int pIcon) {
        mIconId = pIcon;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean updateActionBarTitle() {
        return mUpdateActionBarTitle;
    }

    void setUpdateActionBarTitle(boolean pUpdateActionBarTitle) {
        mUpdateActionBarTitle = pUpdateActionBarTitle;
    }

    public boolean isShowInfoButton() {
        return mIsShowInfoButton;
    }

    public void setIsShowInfoButton(boolean pIsShowInfoButton) {
        mIsShowInfoButton = pIsShowInfoButton;
    }

    /**
     *
     * @return Returns a string that describes the menu item
     */
    public int getHelpStringId() {

        switch (mId) {

            case MAIN.PHONE_SIM_DETAILS:
                return R.string.help_main_phone_sim_details;

            case MAIN.CURRENT_TREAT_LEVEL:
                return R.string.help_main_current_threat_level;

            case MAIN.ACD:
                return R.string.help_main_acd;

            case MAIN.DB_VIEWER:
                return R.string.help_main_database_viewer;

            case MAIN.ANTENNA_MAP_VIEW:
                return R.string.help_main_antenna_map_view;

            case MAIN.AT_COMMAND_INTERFACE:
                return R.string.help_main_at_command_interface;

            case TRACKING.TOGGLE_CELL_TRACKING:
                return R.string.help_tracking_toggle_cell_tracking;

            case TRACKING.TOGGLE_ATTACK_DETECTION:
                return R.string.help_tracking_toggle_attack_detection;

            case SETTINGS.PREFERENCES:
                return R.string.help_settings_preferences;

            case SETTINGS.BACKUP_DB:
                return R.string.help_settings_backup_db;

            case SETTINGS.RESTORE_DB:
                return R.string.help_settings_restore_db;

            case SETTINGS.RESET_DB:
                return R.string.help_settings_reset_db;

            case SETTINGS.EXPORT_DB_TO_CVS:
                return R.string.help_settings_export_db_to_csv;

            case SETTINGS.IMPORT_DB_FROM_CVS:
                return R.string.help_settings_import_db_from_csv;

            case APPLICATION.ADD_GET_OCID_API_KEY:
                return R.string.help_app_add_get_ocid_api_key;

            case APPLICATION.ABOUT:
                return R.string.help_app_about;

            case APPLICATION.DOWNLOAD_LOCAL_BTS_DATA:
                return R.string.help_app_download_local_bts;

            case APPLICATION.UPLOAD_LOCAL_BTS_DATA:
                return R.string.help_app_upload_local_bts;

            case APPLICATION.SEND_DEBUGGING_LOG:
                return R.string.help_app_debugging;

            case APPLICATION.QUIT:
                return R.string.help_app_quit;

            default:
                return R.string.empty;
        }
    }
}