/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.constants;

/**
 * Constants for Menu
 */
public class DrawerMenu {

    /**
     * Constants of id for NavDrawerItem
     * <p>Relates to {@link com.SecUpwN.AIMSICD.drawer.NavDrawerItem#getId()}<br />
     */
    public static class ID {


        /**
         * Constants of section of menu
         */
        public static final int SECTION_MAIN = 10;
        public static final int SECTION_TRACKING = 20;
        public static final int SECTION_SETTINGS = 30;
        public static final int SECTION_APPLICATION = 40;

        /**
         * Constants of item of 'main' section of menu
         */
        public static class MAIN {

            public static final int CURRENT_TREAT_LEVEL = 100; //Current Threat Level
            public static final int PHONE_SIM_DETAILS = 110; //Phone/SIM Details
            public static final int ACD = 120; //All Current Cell Details (ACD)
            public static final int DB_VIEWER = 130; //Database Viewer
            public static final int ANTENNA_MAP_VIEW = 140; // Antenna Map View
            public static final int AT_COMMAND_INTERFACE = 150; //AT Command Interface

        }

        /**
         * Constants of item of 'tracking' section of menu
         */
        public static class TRACKING {

            public static final int TOGGLE_ATTACK_DETECTION = 200; //Toggle Attack Detection
            public static final int TOGGLE_CELL_TRACKING = 210; //Toggle Cell Tracking
            public static final int TRACK_FEMTOCELL = 220; //Track Femtocell

        }

        /**
         * Constants of item of 'settings' section of menu
         */
        public static class SETTINGS {

            public static final int PREFERENCES = 300;
            public static final int BACKUP_DB = 310; //Backup DataBase
            public static final int RESTORE_DB = 320; //Restore DataBase
            public static final int RESET_DB = 330; //Reset DataBase
            //TODO need to implement
            public static final int EXPORT_DB_TO_CVS = 340; //Export DataBase to CSV files
            //TODO need to implement
            public static final int IMPORT_DB_FROM_CVS = 350; //Import DataBase from CSV file(s)

        }

        /**
         * Constants of item of 'application' section of menu
         */
        public static class APPLICATION {
            //FIXME     Is this should be "Download Local OCID Data" ?
            public static final int DOWNLOAD_LOCAL_BTS_DATA = 400; //Download Local BST Data
            public static final int UPLOAD_LOCAL_BTS_DATA = 410; //Upload Local BST Data
            //TODO need to implement
            public static final int ADD_GET_OCID_API_KEY = 420; // Add/Get OCID API key
            public static final int ABOUT = 430; //About AIMSICD
            public static final int SEND_DEBUGGING_LOG = 440; //Debugging
            //TODO need to implement
            public static final int FAQ = 450; // Help/FAQ
            public static final int QUIT = 460; //Quit

        }

    }

    /**
     * The constant indicates the number of types of menu items
     */
    public final static int COUNT_OF_MENU_TYPE = 2;

}
