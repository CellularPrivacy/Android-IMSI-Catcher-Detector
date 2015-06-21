/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.utils;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 *  Description:    TODO: please add this...
 */
public class StackOverflowXmlParser {
    // We don't use namespaces
    private static final String ns = null;

    public List<Cell> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readCells(parser);
        } finally {
            in.close();
        }
    }

    private List<Cell> readCells(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Cell> cells = new ArrayList<>();

        parser.require(XmlPullParser.START_TAG, ns, "rsp");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("cell")) {
                cells.add(readCell(parser));
            } else {
                skip(parser);
            }
        }
        return cells;
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
    // to their respective "read" methods for processing. Otherwise, skips the tag.
    private Cell readCell(XmlPullParser parser) throws XmlPullParserException, IOException {
        Cell cell = new Cell();
        parser.require(XmlPullParser.START_TAG, ns, "cell");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name) {
                case "lat":
                    cell.setLat(readDouble(parser));
                    break;
                case "lon":
                    cell.setLon(readDouble(parser));
                    break;
                case "mcc":
                    cell.setMCC(readInt(parser));
                    break;
                case "mnc":
                    cell.setMNC(readInt(parser));
                    break;
                case "cellid":
                    cell.setCID(readInt(parser));
                    break;
                case "lac":
                    cell.setLAC(readInt(parser));
                    break;
                default:
                    skip(parser);
                    break;
            }
        }
        return cell;
    }

    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    // For tags containing double values.
    private double readDouble(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return Double.valueOf(result);
    }

    // For tags containing double values.
    private int readInt(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return Integer.valueOf(result);
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

}
