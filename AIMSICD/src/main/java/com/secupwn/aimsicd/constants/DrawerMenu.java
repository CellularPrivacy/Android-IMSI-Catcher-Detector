/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.constants;

/**
 * Constants for Menu
 */
public class DrawerMenu {

    /**
     * Constants of id for NavDrawerItem
     * <p>Relates to {@link com.secupwn.aimsicd.ui.drawer.NavDrawerItem#getId()}<br />
     */
    public static class ID {

        /**
         * Constants of section of menu
         */
        public static final int SECTION_MAIN = 10;
        public static final int SECTION_DATABASE_SETTINGS = 30;
        public static final int SECTION_APPLICATION = 40;

        /**
         * Constants of item of 'main' section of menu
         */
        public static class MAIN {

            public static final int NEIGHBORING_CELLS = 100;
            public static final int PHONE_SIM_DETAILS = 110;
            public static final int ALL_CURRENT_CELL_DETAILS = 120;
            public static final int DB_VIEWER = 130;
            public static final int ANTENNA_MAP_VIEW = 140;
            public static final int AT_COMMAND_INTERFACE = 150;

        }

        /**
         * Constants of item of 'settings' section of menu
         */
        public static class DATABASE_SETTINGS {
            public static final int RESET_DB = 330; //Reset DataBase
        }

        /**
         * Constants of item of 'application' section of menu
         */
        public static class APPLICATION {
            public static final int DOWNLOAD_LOCAL_BTS_DATA = 400;  //Download Local BST Data FIXME     Is this should be "Download Local OCID Data" ?
            public static final int UPLOAD_LOCAL_BTS_DATA = 410;    //Upload Local BST Data
            public static final int ADD_GET_OCID_API_KEY = 420;     // Add/Get OCID API key
            public static final int FAQ = 450;                      // TODO Help/FAQ
            public static final int QUIT = 460;                     //Quit

        }

    }

    /**
     * The constant indicates the number of types of menu items
     */
    public static final int COUNT_OF_MENU_TYPE = 2;

}
