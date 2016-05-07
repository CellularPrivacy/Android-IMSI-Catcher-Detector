/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.ui.drawer;

import android.content.Context;
import android.widget.BaseAdapter;

import com.secupwn.aimsicd.R;
import com.secupwn.aimsicd.adapters.DrawerMenuAdapter;
import com.secupwn.aimsicd.constants.DrawerMenu;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

@Getter
public class DrawerMenuActivityConfiguration {

    private int mainLayout;
    private int drawerLayoutId;
    private int leftDrawerId;
    private int[] actionMenuItemsToHideWhenDrawerOpen;
    private List<NavDrawerItem> navItems;
    private BaseAdapter baseAdapter;

    private DrawerMenuActivityConfiguration(Builder pBuilder) {
        mainLayout = pBuilder.mMainLayout;
        drawerLayoutId = pBuilder.mDrawerLayoutId;
        leftDrawerId = pBuilder.mLeftDrawerId;
        actionMenuItemsToHideWhenDrawerOpen = pBuilder.mActionMenuItemsToHideWhenDrawerOpen;
        navItems = pBuilder.mNavItems;
        baseAdapter = pBuilder.mBaseAdapter;
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
            menu.add(DrawerMenuItem.create(DrawerMenu.ID.MAIN.PHONE_SIM_DETAILS, mContext.getString(R.string.device_info), R.drawable.ic_action_phone, true));
            menu.add(DrawerMenuItem.create(DrawerMenu.ID.MAIN.NEIGHBORING_CELLS, mContext.getString(R.string.cell_info_title), R.drawable.cell_tower, true));
            menu.add(DrawerMenuItem.create(DrawerMenu.ID.MAIN.DB_VIEWER, mContext.getString(R.string.db_viewer), R.drawable.ic_action_storage, true));
            menu.add(DrawerMenuItem.create(DrawerMenu.ID.MAIN.ANTENNA_MAP_VIEW, mContext.getString(R.string.map_view), R.drawable.ic_action_map, false));
            menu.add(DrawerMenuItem.create(DrawerMenu.ID.MAIN.AT_COMMAND_INTERFACE, mContext.getString(R.string.at_command_title), R.drawable.ic_action_computer, true));

            //Section Settings
            menu.add(DrawerMenuSection.create(DrawerMenu.ID.SECTION_DATABASE_SETTINGS, mContext.getString(R.string.database_settings)));
            menu.add(DrawerMenuItem.create(DrawerMenu.ID.DATABASE_SETTINGS.RESET_DB, mContext.getString(R.string.clear_database), R.drawable.ic_action_delete_database, false));

            //Section Application
            menu.add(DrawerMenuSection.create(DrawerMenu.ID.SECTION_APPLICATION, mContext.getString(R.string.application)));
            menu.add(DrawerMenuItem.create(DrawerMenu.ID.APPLICATION.DOWNLOAD_LOCAL_BTS_DATA, mContext.getString(R.string.get_opencellid), R.drawable.stat_sys_download_anim0, false));
            menu.add(DrawerMenuItem.create(DrawerMenu.ID.APPLICATION.UPLOAD_LOCAL_BTS_DATA, mContext.getString(R.string.upload_bts), R.drawable.stat_sys_upload_anim0, false));
            menu.add(DrawerMenuItem.create(DrawerMenu.ID.APPLICATION.QUIT, mContext.getString(R.string.quit), R.drawable.ic_action_remove, false));
            mNavItems = menu;

            if (mMainLayout == 0) {
                this.mainLayout(R.layout.activity_main);
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
