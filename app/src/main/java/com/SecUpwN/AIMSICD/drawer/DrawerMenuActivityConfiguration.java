/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.drawer;

import android.content.Context;
import android.widget.BaseAdapter;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.adapters.DrawerMenuAdapter;
import com.SecUpwN.AIMSICD.constants.DrawerMenu;

import java.util.ArrayList;
import java.util.List;

public class DrawerMenuActivityConfiguration {

    private int mMainLayout;
    private int mDrawerLayoutId;
    private int mLeftDrawerId;
    private int[] mActionMenuItemsToHideWhenDrawerOpen;
    private List<NavDrawerItem> mNavItems;
    private BaseAdapter mBaseAdapter;

    private DrawerMenuActivityConfiguration(Builder pBuilder) {
        mMainLayout = pBuilder.mMainLayout;
        mDrawerLayoutId = pBuilder.mDrawerLayoutId;
        mLeftDrawerId = pBuilder.mLeftDrawerId;
        mActionMenuItemsToHideWhenDrawerOpen = pBuilder.mActionMenuItemsToHideWhenDrawerOpen;
        mNavItems = pBuilder.mNavItems;
        mBaseAdapter = pBuilder.mBaseAdapter;
    }

    public int getMainLayout() {
        return mMainLayout;
    }

    public int getDrawerLayoutId() {
        return mDrawerLayoutId;
    }

    public int getLeftDrawerId() {
        return mLeftDrawerId;
    }

    public int[] getActionMenuItemsToHideWhenDrawerOpen() {
        return mActionMenuItemsToHideWhenDrawerOpen;
    }

    public List<NavDrawerItem> getNavItems() {
        return mNavItems;
    }

    public BaseAdapter getBaseAdapter() {
        return mBaseAdapter;
    }

    public static class Builder {

        private final Context mContext;
        private int mMainLayout;
        private int mDrawerLayoutId;
        private int mLeftDrawerId;
        private int[] mActionMenuItemsToHideWhenDrawerOpen;
        private List<NavDrawerItem> mNavItems;
        private int mDrawerOpenDesc;
        private int mDrawerCloseDesc;
        private BaseAdapter mBaseAdapter;

        public Builder(Context pContext) {
            mContext = pContext;
        }

        public Builder mainLayout(int pMainLayout) {
            mMainLayout = pMainLayout;
            return this;
        }

        public Builder drawerLayoutId(int pDrawerLayoutId) {
            mDrawerLayoutId = pDrawerLayoutId;
            return this;
        }

        public Builder leftDrawerId(int pLeftDrawerId) {
            mLeftDrawerId = pLeftDrawerId;
            return this;
        }

        public Builder drawerOpenDesc(int pDrawerOpenDesc) {
            mDrawerOpenDesc = pDrawerOpenDesc;
            return this;
        }

        public Builder drawerCloseDesc(int pDrawerCloseDesc) {
            mDrawerCloseDesc = pDrawerCloseDesc;
            return this;
        }

        public DrawerMenuActivityConfiguration build() {

            List<NavDrawerItem> menu = new ArrayList<>();

            //Section Main
            menu.add(DrawerMenuSection.create(DrawerMenu.ID.SECTION_MAIN, mContext.getString(R.string.main)));
            menu.add(DrawerMenuItem.create(DrawerMenu.ID.MAIN.PHONE_SIM_DETAILS, mContext.getString(R.string.device_info), R.drawable.ic_action_phone, true));           // Phone/SIM Details
            menu.add(DrawerMenuItem.create(DrawerMenu.ID.MAIN.CURRENT_TREAT_LEVEL, mContext.getString(R.string.cell_info_title), R.drawable.cell_tower, true));            // Cell Information (Neighboring cells etc)
            menu.add(DrawerMenuItem.create(DrawerMenu.ID.MAIN.ACD, mContext.getString(R.string.cell_lookup), R.drawable.stat_sys_download_anim0, false));  // Lookup "All Current Cell Details (ACD)"
            menu.add(DrawerMenuItem.create(DrawerMenu.ID.MAIN.DB_VIEWER, mContext.getString(R.string.db_viewer), R.drawable.ic_action_storage, true));           // Database Viewer
            menu.add(DrawerMenuItem.create(DrawerMenu.ID.MAIN.ANTENNA_MAP_VIEW, mContext.getString(R.string.map_view), R.drawable.ic_action_map, false));               // Antenna Map Viewer
            menu.add(DrawerMenuItem.create(DrawerMenu.ID.MAIN.AT_COMMAND_INTERFACE, mContext.getString(R.string.at_command_title), R.drawable.ic_action_computer, true));   // AT Command Interface

            //Section Tracking
            menu.add(DrawerMenuSection.create(DrawerMenu.ID.SECTION_TRACKING, mContext.getString(R.string.tracking)));
            menu.add(DrawerMenuItem.create(DrawerMenu.ID.TRACKING.TOGGLE_ATTACK_DETECTION, mContext.getString(R.string.toggle_attack_detection), R.drawable.untrack_cell, false));    // Toggle "Attack Detection"
            menu.add(DrawerMenuItem.create(DrawerMenu.ID.TRACKING.TOGGLE_CELL_TRACKING, mContext.getString(R.string.toggle_cell_tracking), R.drawable.untrack_cell, false));      // Toggle "Cell Tracking"

            //Section Settings
            menu.add(DrawerMenuSection.create(DrawerMenu.ID.SECTION_SETTINGS, mContext.getString(R.string.settings)));
            menu.add(DrawerMenuItem.create(DrawerMenu.ID.SETTINGS.PREFERENCES, mContext.getString(R.string.preferences), R.drawable.ic_action_settings, false));            // Preferences
            menu.add(DrawerMenuItem.create(DrawerMenu.ID.SETTINGS.BACKUP_DB, mContext.getString(R.string.backup_database), R.drawable.ic_action_import_export, false));   // Backup Database
            menu.add(DrawerMenuItem.create(DrawerMenu.ID.SETTINGS.RESTORE_DB, mContext.getString(R.string.restore_database), R.drawable.ic_action_import_export, false));  // Restore Database
            menu.add(DrawerMenuItem.create(DrawerMenu.ID.SETTINGS.RESET_DB, mContext.getString(R.string.clear_database), R.drawable.ic_action_delete_database, false));  // Reset Database

            //Section Application
            menu.add(DrawerMenuSection.create(DrawerMenu.ID.SECTION_APPLICATION, mContext.getString(R.string.application)));
            menu.add(DrawerMenuItem.create(DrawerMenu.ID.APPLICATION.DOWNLOAD_LOCAL_BTS_DATA, mContext.getString(R.string.get_opencellid), R.drawable.stat_sys_download_anim0, false));   // "Download Local BTS data"
            menu.add(DrawerMenuItem.create(DrawerMenu.ID.APPLICATION.UPLOAD_LOCAL_BTS_DATA, mContext.getString(R.string.upload_bts), R.drawable.stat_sys_upload_anim0, false));      // "Upload Local BTS data"
            menu.add(DrawerMenuItem.create(DrawerMenu.ID.APPLICATION.ABOUT, mContext.getString(R.string.about_aimsicd), R.drawable.ic_action_about, true));         // About
            menu.add(DrawerMenuItem.create(DrawerMenu.ID.APPLICATION.SEND_DEBUGGING_LOG, mContext.getString(R.string.send_logs), R.drawable.ic_action_computer, false));         // Debugging
            menu.add(DrawerMenuItem.create(DrawerMenu.ID.APPLICATION.QUIT, mContext.getString(R.string.quit), R.drawable.ic_action_remove, false));                // Quit
            mNavItems = menu;

            if (mMainLayout == 0) {
                this.mainLayout(R.layout.main);
            }

            if (mDrawerLayoutId == 0) {
                this.drawerLayoutId(R.id.drawer_layout);
            }

            if (mLeftDrawerId == 0) {
                this.leftDrawerId(R.id.left_drawer);
            }

            if (mDrawerOpenDesc == 0) {
                this.drawerOpenDesc(R.string.drawer_open);
            }

            if (mDrawerCloseDesc == 0) {
                this.drawerCloseDesc(R.string.drawer_close);
            }

            if (mBaseAdapter == null) {
                mBaseAdapter = new DrawerMenuAdapter(mContext, R.layout.drawer_item, menu);
            }

            return new DrawerMenuActivityConfiguration(this);
        }

    }
}