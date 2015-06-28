package com.SecUpwN.AIMSICD.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marvin Arnold on 9/06/15.
 */
public class OCIDCSV  extends ArrayList<OCIDCSV.OCIDCSVLine> {

    public OCIDCSV() {
        super();
    }

    public void add(String[] newLine) {
        add(new OCIDCSVLine(newLine));
    }

    public class OCIDCSVLine {
        private final String[] ocidCell;

        public OCIDCSVLine(String[] ocidCell) {
            this.ocidCell = ocidCell;
        }

        public double getGPSLat() {
            return truncateDouble(this.ocidCell[0], 5);
        }

        public double getGPSLon() {
            return truncateDouble(this.ocidCell[1], 5);
        }

        public int getMCC() {
            return Integer.parseInt(this.ocidCell[2]);
        }

        public int getMNC() {
            return Integer.parseInt(this.ocidCell[3]);
        }

        public int getLAC() {
            return Integer.parseInt(this.ocidCell[4]);
        }

        public int getCID() {
            return Integer.parseInt(this.ocidCell[5]);
        }

        /**
         * Average signal in [dBm]
         * @return
         */
        public int getAvgSig() {
            return Integer.parseInt(this.ocidCell[6]);
        }

        /**
         * Average range in [m]
         * @return
         */
        public int getAvgRange() {
            return Integer.parseInt(this.ocidCell[7]);
        }

        public int getSamples() {
            return Integer.parseInt(this.ocidCell[8]);
        }

        public int isGPSExact() {
            return Integer.parseInt(this.ocidCell[9]);
        }

        public String getRAT() {
            return String.valueOf(this.ocidCell[10]);
        }

        public double truncateDouble(String d, int numDecimal) {
            return  truncateDouble(Double.parseDouble(d), numDecimal);
        }

        public double truncateDouble(double d, int numDecimal) {
            String s = String.format("%." + Integer.toString(numDecimal) +"f", d);
            NumberFormat nf = NumberFormat.getInstance();
            double number = nf.parse(s).doubleValue();
            return number;
        }
    }
}
