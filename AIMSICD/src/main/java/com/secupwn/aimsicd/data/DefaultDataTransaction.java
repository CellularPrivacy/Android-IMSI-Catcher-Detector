package com.secupwn.aimsicd.data;

import com.secupwn.aimsicd.data.model.DefaultLocation;
import com.secupwn.aimsicd.data.model.GpsLocation;
import com.secupwn.aimsicd.data.model.SmsDetectionString;

import io.realm.Realm;

public class DefaultDataTransaction implements Realm.Transaction {
    @Override
    public void execute(Realm realm) {

        long count = realm.where(SmsDetectionString.class).count();
        if (count == 0) {
            SmsDetectionString smsDetectionString1 = realm.createObject(SmsDetectionString.class);
            smsDetectionString1.setDetectionString("Received short message type 0");
            smsDetectionString1.setSmsType("TYPE0");

            SmsDetectionString smsDetectionString2 = realm.createObject(SmsDetectionString.class);
            smsDetectionString2.setDetectionString("Received voice mail indicator clear SMS shouldStore=false");
            smsDetectionString2.setSmsType("MWI");

            SmsDetectionString smsDetectionString3 = realm.createObject(SmsDetectionString.class);
            smsDetectionString3.setDetectionString("SMS TP-PID:0 data coding scheme: 24");
            smsDetectionString3.setSmsType("FLASH");

            SmsDetectionString smsDetectionString4 = realm.createObject(SmsDetectionString.class);
            smsDetectionString4.setDetectionString("isTypeZero=true");
            smsDetectionString4.setSmsType("TYPE0");

            SmsDetectionString smsDetectionString5 = realm.createObject(SmsDetectionString.class);
            smsDetectionString5.setDetectionString("incoming msg. Mti 0 ProtocolID 0 DCS 0x04 class -1");
            smsDetectionString5.setSmsType("WAPPUSH");

            SmsDetectionString smsDetectionString6 = realm.createObject(SmsDetectionString.class);
            smsDetectionString6.setDetectionString("SMS TP-PID:0 data coding scheme: 4");
            smsDetectionString6.setSmsType("WAPPUSH");
        }

        storeDefaultLocation(realm, 412, "Afghanistan", 34.5167, 69.1833);
        storeDefaultLocation(realm, 276, "Albania", 41.3275, 19.8189);
        storeDefaultLocation(realm, 603, "Algeria", 36.7631, 3.05056);
        storeDefaultLocation(realm, 213, "Andorra", 42.5, 1.51667);
        storeDefaultLocation(realm, 631, "Angola", -8.83833, 13.2344);
        storeDefaultLocation(realm, 365, "Anguilla", 18.2167, -63.05);
        storeDefaultLocation(realm, 344, "Antigua And Barbuda", 17.1167, -61.85);
        storeDefaultLocation(realm, 722, "Argentina", -34.5875, -58.6725);
        storeDefaultLocation(realm, 283, "Armenia", 40.1833, 44.5);
        storeDefaultLocation(realm, 363, "Aruba", 12.5167, -70.0333);
        storeDefaultLocation(realm, 505, "Australia", -35.2833, 149.217);
        storeDefaultLocation(realm, 232, "Austria", 48.2, 16.3667);
        storeDefaultLocation(realm, 364, "Bahamas", 25.0833, -77.35);
        storeDefaultLocation(realm, 426, "Bahrain", 26.2361, 50.5831);
        storeDefaultLocation(realm, 470, "Bangladesh", 23.7231, 90.4086);
        storeDefaultLocation(realm, 342, "Barbados", 13.1, -59.6167);
        storeDefaultLocation(realm, 257, "Belarus", 53.9, 27.5667);
        storeDefaultLocation(realm, 206, "Belgium", 50.8333, 4.33333);
        storeDefaultLocation(realm, 702, "Belize", 17.25, -88.7667);
        storeDefaultLocation(realm, 616, "Benin", 6.48333, 2.61667);
        storeDefaultLocation(realm, 350, "Bermuda", 32.2942, -64.7839);
        storeDefaultLocation(realm, 736, "Bolivia", -19.0431, -65.2592);
        storeDefaultLocation(realm, 218, "Bosnia And Herzegovina", 43.85, 18.3833);
        storeDefaultLocation(realm, 652, "Botswana", -24.6464, 25.9119);
        storeDefaultLocation(realm, 724, "Brazil", -16.2119, -44.4308);
        storeDefaultLocation(realm, 348, "British Virgin Islands", 18.4167, -64.6167);
        storeDefaultLocation(realm, 528, "Brunei", 4.88333, 114.933);
        storeDefaultLocation(realm, 284, "Bulgaria", 42.6833, 23.3167);
        storeDefaultLocation(realm, 613, "Burkina Faso", 12.3703, -1.52472);
        storeDefaultLocation(realm, 642, "Burundi", -3.37778, 29.3667);
        storeDefaultLocation(realm, 456, "Cambodia", 11.55, 104.917);
        storeDefaultLocation(realm, 624, "Cameroon", 3.86667, 11.5167);
        storeDefaultLocation(realm, 302, "Canada", 45.4167, -75.7);
        storeDefaultLocation(realm, 625, "Cape Verde", 14.9167, -23.5167);
        storeDefaultLocation(realm, 346, "Cayman Islands", 19.3, -81.3833);
        storeDefaultLocation(realm, 623, "Central African Republic", 4.36667, 18.5833);
        storeDefaultLocation(realm, 622, "Chad", 12.1167, 15.05);
        storeDefaultLocation(realm, 730, "Chile", -33.45, -70.6667);
        storeDefaultLocation(realm, 460, "China", 39.9289, 116.388);
        storeDefaultLocation(realm, 461, "China", 39.9289, 116.388);
        storeDefaultLocation(realm, 732, "Colombia", 4.6, -74.0833);
        storeDefaultLocation(realm, 654, "Comoros", -11.7042, 43.2403);
        storeDefaultLocation(realm, 629, "Congo", -4.25917, 15.2847);
        storeDefaultLocation(realm, 548, "Cook Islands", -21.2, -159.767);
        storeDefaultLocation(realm, 712, "Costa Rica", 9.93333, -84.0833);
        storeDefaultLocation(realm, 612, "Cote Divoire", 6.81667, -5.28333);
        storeDefaultLocation(realm, 219, "Croatia", 45.8, 16);
        storeDefaultLocation(realm, 368, "Cuba", 23.1319, -82.3642);
        storeDefaultLocation(realm, 280, "Cyprus", 35.1667, 33.3667);
        storeDefaultLocation(realm, 230, "Czech Republic", 50.0833, 14.4667);
        storeDefaultLocation(realm, 238, "Denmark", 55.6667, 12.5833);
        storeDefaultLocation(realm, 638, "Djibouti", 11.595, 43.1481);
        storeDefaultLocation(realm, 366, "Dominica", 15.3, -61.4);
        storeDefaultLocation(realm, 370, "Dominican Republic", 18.4667, -69.9);
        storeDefaultLocation(realm, 740, "Ecuador", -0.216667, -78.5);
        storeDefaultLocation(realm, 602, "Egypt", 30.05, 31.25);
        storeDefaultLocation(realm, 706, "El Salvador", 13.7, -89.2);
        storeDefaultLocation(realm, 627, "Equatorial Guinea", 3.35, 8.66667);
        storeDefaultLocation(realm, 248, "Estonia", 59.4339, 24.7281);
        storeDefaultLocation(realm, 636, "Ethiopia", 9.03333, 38.7);
        storeDefaultLocation(realm, 288, "Faroe Islands", 62.0167, -6.76667);
        storeDefaultLocation(realm, 542, "Fiji", -18.1333, 178.417);
        storeDefaultLocation(realm, 244, "Finland", 60.1756, 24.9342);
        storeDefaultLocation(realm, 208, "France", 48.8667, 2.33333);
        storeDefaultLocation(realm, 742, "French Guiana", 4.93333, -52.3333);
        storeDefaultLocation(realm, 547, "French Polynesia", -17.5333, -149.567);
        storeDefaultLocation(realm, 628, "Gabon", 0.383333, 9.45);
        storeDefaultLocation(realm, 607, "Gambia", 13.4531, -16.5775);
        storeDefaultLocation(realm, 282, "Georgia", 41.7178, 44.7844);
        storeDefaultLocation(realm, 262, "Germany", 52.5167, 13.4);
        storeDefaultLocation(realm, 620, "Ghana", 5.55, -0.216667);
        storeDefaultLocation(realm, 266, "Gibraltar", 36.1333, -5.35);
        storeDefaultLocation(realm, 202, "Greece", 37.9833, 23.7333);
        storeDefaultLocation(realm, 290, "Greenland", 64.1833, -51.75);
        storeDefaultLocation(realm, 352, "Grenada", 14.6, -61.0833);
        storeDefaultLocation(realm, 704, "Guatemala", 14.6333, -90.5167);
        storeDefaultLocation(realm, 234, "Guernsey", 49.45, -2.53333);
        storeDefaultLocation(realm, 611, "Guinea", 9.50917, -13.7122);
        storeDefaultLocation(realm, 632, "Guinea-Bissau", 11.85, -15.5833);
        storeDefaultLocation(realm, 738, "Guyana", 6.8, -58.1667);
        storeDefaultLocation(realm, 372, "Haiti", 18.5392, -72.335);
        storeDefaultLocation(realm, 708, "Honduras", 14.1, -87.2167);
        storeDefaultLocation(realm, 454, "Hong Kong", 22.2833, 114.15);
        storeDefaultLocation(realm, 216, "Hungary", 47.5, 19.0833);
        storeDefaultLocation(realm, 274, "Iceland", 64.15, -21.95);
        storeDefaultLocation(realm, 404, "India", 28.6, 77.2);
        storeDefaultLocation(realm, 510, "Indonesia", -6.16889, 106.822);
        storeDefaultLocation(realm, 432, "Iran", 35.6719, 51.4244);
        storeDefaultLocation(realm, 418, "Iraq", 33.3386, 44.3939);
        storeDefaultLocation(realm, 272, "Ireland", 53.3331, -6.24889);
        storeDefaultLocation(realm, 243, "Isle Of Man", 54.15, -4.48333);
        storeDefaultLocation(realm, 222, "Italy", 41.9, 12.4833);
        storeDefaultLocation(realm, 338, "Jamaica", 18, -76.8);
        storeDefaultLocation(realm, 440, "Japan", 35.6861, 139.753);
        storeDefaultLocation(realm, 441, "Japan", 35.6861, 139.753);
        storeDefaultLocation(realm, 234, "Jersey", 49.1833, -2.1);
        storeDefaultLocation(realm, 416, "Jordan", 31.95, 35.9333);
        storeDefaultLocation(realm, 401, "Kazakhstan", 51.1811, 71.4278);
        storeDefaultLocation(realm, 639, "Kenya", -1.28333, 36.8167);
        storeDefaultLocation(realm, 545, "Kiribati", -0.883333, 169.533);
        storeDefaultLocation(realm, 419, "Kuwait", 29.3697, 47.9783);
        storeDefaultLocation(realm, 457, "Laos", 17.9667, 102.6);
        storeDefaultLocation(realm, 247, "Latvia", 56.95, 24.1);
        storeDefaultLocation(realm, 415, "Lebanon", 33.8719, 35.5097);
        storeDefaultLocation(realm, 651, "Lesotho", -29.3167, 27.4833);
        storeDefaultLocation(realm, 618, "Liberia", 6.31056, -10.8047);
        storeDefaultLocation(realm, 606, "Libya", 32.8925, 13.18);
        storeDefaultLocation(realm, 295, "Liechtenstein", 47.1333, 9.51667);
        storeDefaultLocation(realm, 246, "Lithuania", 54.6833, 25.3167);
        storeDefaultLocation(realm, 270, "Luxembourg", 49.6117, 6.13);
        storeDefaultLocation(realm, 455, "Macau", 22.2, 113.55);
        storeDefaultLocation(realm, 294, "Macedonia", 42, 21.4333);
        storeDefaultLocation(realm, 646, "Madagascar", -18.9167, 47.5167);
        storeDefaultLocation(realm, 650, "Malawi", -13.9833, 33.7833);
        storeDefaultLocation(realm, 502, "Malaysia", 3.16667, 101.7);
        storeDefaultLocation(realm, 472, "Maldives", -12.7794, 45.2272);
        storeDefaultLocation(realm, 610, "Mali", 12.65, -8);
        storeDefaultLocation(realm, 278, "Malta", 35.8997, 14.5147);
        storeDefaultLocation(realm, 551, "Marshall Islands", 7.1, 171.383);
        storeDefaultLocation(realm, 609, "Mauritania", 18.1, -15.95);
        storeDefaultLocation(realm, 617, "Mauritius", -20.1667, 57.5);
        storeDefaultLocation(realm, 334, "Mexico", 19.4342, -99.1386);
        storeDefaultLocation(realm, 550, "Micronesia", 6.91667, 158.15);
        storeDefaultLocation(realm, 259, "Moldova", 47.005, 28.8578);
        storeDefaultLocation(realm, 428, "Mongolia", 47.9167, 106.917);
        storeDefaultLocation(realm, 354, "Montserrat", 16.7, -62.2167);
        storeDefaultLocation(realm, 604, "Morocco", 34.0333, -6.83333);
        storeDefaultLocation(realm, 643, "Mozambique", -25.9653, 32.5892);
        storeDefaultLocation(realm, 649, "Namibia", -22.57, 17.0836);
        storeDefaultLocation(realm, 429, "Nepal", 27.7167, 85.3167);
        storeDefaultLocation(realm, 204, "Netherlands", 52.0833, 4.3);
        storeDefaultLocation(realm, 546, "New Caledonia", -22.2667, 166.45);
        storeDefaultLocation(realm, 530, "New Zealand", -41.3, 174.783);
        storeDefaultLocation(realm, 710, "Nicaragua", 12.1508, -86.2683);
        storeDefaultLocation(realm, 614, "Niger", 13.5167, 2.11667);
        storeDefaultLocation(realm, 621, "Nigeria", 9.08333, 7.53333);
        storeDefaultLocation(realm, 555, "Niue", -19.0167, -169.917);
        storeDefaultLocation(realm, 467, "North Korea", 39.0194, 125.755);
        storeDefaultLocation(realm, 242, "Norway", 59.9167, 10.75);
        storeDefaultLocation(realm, 422, "Oman", 23.6133, 58.5933);
        storeDefaultLocation(realm, 410, "Pakistan", 33.7, 73.1667);
        storeDefaultLocation(realm, 552, "Palau", 7.34056, 134.471);
        storeDefaultLocation(realm, 714, "Panama", 8.96667, -79.5333);
        storeDefaultLocation(realm, 537, "Papua New Guinea", -9.46472, 147.193);
        storeDefaultLocation(realm, 744, "Paraguay", -25.2667, -57.6667);
        storeDefaultLocation(realm, 716, "Peru", -12.05, -77.05);
        storeDefaultLocation(realm, 515, "Philippines", 14.5833, 121);
        storeDefaultLocation(realm, 260, "Poland", 52.25, 21);
        storeDefaultLocation(realm, 268, "Portugal", 38.7167, -9.13333);
        storeDefaultLocation(realm, 427, "Qatar", 25.2867, 51.5333);
        storeDefaultLocation(realm, 647, "Reunion", -20.8667, 55.4667);
        storeDefaultLocation(realm, 226, "Romania", 44.4333, 26.1);
        storeDefaultLocation(realm, 250, "Russia", 55.7522, 37.6156);
        storeDefaultLocation(realm, 635, "Rwanda", -1.95361, 30.0606);
        storeDefaultLocation(realm, 356, "Saint Kitts And Nevis", 17.3, -62.7167);
        storeDefaultLocation(realm, 358, "Saint Lucia", 14, -61);
        storeDefaultLocation(realm, 308, "Saint Pierre And Miquelon", 46.7667, -56.1833);
        storeDefaultLocation(realm, 360, "Saint Vincent And The Grenadines", 13.1333, -61.2167);
        storeDefaultLocation(realm, 549, "Samoa", -13.8333, -171.733);
        storeDefaultLocation(realm, 626, "Sao Tome And Principe", 0.333333, 6.73333);
        storeDefaultLocation(realm, 420, "Saudi Arabia", 24.6408, 46.7728);
        storeDefaultLocation(realm, 608, "Senegal", 14.6667, -17.4333);
        storeDefaultLocation(realm, 633, "Seychelles", -4.61667, 55.45);
        storeDefaultLocation(realm, 619, "Sierra Leone", 8.49, -13.2342);
        storeDefaultLocation(realm, 525, "Singapore", 1.29306, 103.856);
        storeDefaultLocation(realm, 231, "Slovakia", 48.15, 17.1167);
        storeDefaultLocation(realm, 540, "Solomon Islands", -9.43333, 159.95);
        storeDefaultLocation(realm, 637, "Somalia", 2.06667, 45.3667);
        storeDefaultLocation(realm, 655, "South Africa", -33.9167, 18.4167);
        storeDefaultLocation(realm, 450, "South Korea", 37.5664, 127);
        storeDefaultLocation(realm, 214, "Spain", 40.4, -3.68333);
        storeDefaultLocation(realm, 413, "Sri Lanka", 6.90278, 79.9083);
        storeDefaultLocation(realm, 634, "Sudan", 15.5881, 32.5342);
        storeDefaultLocation(realm, 746, "Suriname", 5.83333, -55.1667);
        storeDefaultLocation(realm, 653, "Swaziland", -26.3167, 31.1333);
        storeDefaultLocation(realm, 240, "Sweden", 59.3333, 18.05);
        storeDefaultLocation(realm, 228, "Switzerland", 46.9167, 7.46667);
        storeDefaultLocation(realm, 417, "Syria", 33.5, 36.3);
        storeDefaultLocation(realm, 466, "Taiwan", 121.45, 25.0167);
        storeDefaultLocation(realm, 436, "Tajikistan", 38.56, 68.7739);
        storeDefaultLocation(realm, 640, "Tanzania", -6.8, 39.2833);
        storeDefaultLocation(realm, 520, "Thailand", 13.75, 100.517);
        storeDefaultLocation(realm, 615, "Togo", 6.13194, 1.22278);
        storeDefaultLocation(realm, 539, "Tonga", -21.1333, -175.2);
        storeDefaultLocation(realm, 374, "Trinidad And Tobago", 10.65, -61.5167);
        storeDefaultLocation(realm, 605, "Tunisia", 36.8028, 10.1797);
        storeDefaultLocation(realm, 286, "Turkey", 39.935, 32.8642);
        storeDefaultLocation(realm, 438, "Turkmenistan", 37.95, 58.3833);
        storeDefaultLocation(realm, 376, "Turks And Caicos Islands", 21.4667, -71.1333);
        storeDefaultLocation(realm, 553, "Tuvalu", -8.51667, 179.217);
        storeDefaultLocation(realm, 641, "Uganda", 0.316667, 32.5833);
        storeDefaultLocation(realm, 255, "Ukraine", 50.4333, 30.5167);
        storeDefaultLocation(realm, 424, "United Arab Emirates", 24.4667, 54.3667);
        storeDefaultLocation(realm, 430, "United Arab Emirates", 24.4667, 54.3667);
        storeDefaultLocation(realm, 431, "United Arab Emirates", 24.4667, 54.3667);
        storeDefaultLocation(realm, 235, "United Kingdom", 51.5, -0.116667);
        storeDefaultLocation(realm, 234, "United Kingdom", 51.5, -0.116667);
        storeDefaultLocation(realm, 312, "United States of America", 77.0367, 38.8951);
        storeDefaultLocation(realm, 316, "United States of America", 77.0367, 38.8951);
        storeDefaultLocation(realm, 310, "United States of America", 77.0367, 38.8951);
        storeDefaultLocation(realm, 311, "United States of America", 77.0367, 38.8951);
        storeDefaultLocation(realm, 313, "United States of America", 77.0367, 38.8951);
        storeDefaultLocation(realm, 314, "United States of America", 77.0367, 38.8951);
        storeDefaultLocation(realm, 315, "United States of America", 77.0367, 38.8951);
        storeDefaultLocation(realm, 748, "Uruguay", -34.8581, -56.1708);
        storeDefaultLocation(realm, 434, "Uzbekistan", 41.3167, 69.25);
        storeDefaultLocation(realm, 541, "Vanuatu", -17.7333, 168.317);
        storeDefaultLocation(realm, 734, "Venezuela", 10.5, -66.9167);
        storeDefaultLocation(realm, 452, "Vietnam", 21.0333, 105.85);
        storeDefaultLocation(realm, 421, "Yemen", 15.3547, 44.2067);
        storeDefaultLocation(realm, 220, "Yugoslavia", 44.8119, 20.4656);
        storeDefaultLocation(realm, 645, "Zambia", -15.4167, 28.2833);
        storeDefaultLocation(realm, 648, "Zimbabwe", -17.8333, 31.05);

    }

    private void storeDefaultLocation(Realm realm, int mobileCountryCode, String country, double latitude, double longitude) {

        DefaultLocation location = realm.where(DefaultLocation.class)
                .equalTo("mobileCountryCode", mobileCountryCode)
                .equalTo("country", country)
                .findFirst();

        if (location == null) {
            location = realm.createObject(DefaultLocation.class);
        }

        location.setCountry(country);
        location.setMobileCountryCode(mobileCountryCode);

        if (location.getGpsLocation() == null) {
            location.setGpsLocation(realm.createObject(GpsLocation.class));
        }

        location.getGpsLocation().setLatitude(latitude);
        location.getGpsLocation().setLongitude(longitude);
    }
}
