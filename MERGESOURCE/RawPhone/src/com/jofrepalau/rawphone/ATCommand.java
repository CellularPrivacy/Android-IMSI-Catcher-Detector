package com.jofrepalau.rawphone;

/**
 * Created by lambchops on 27/01/14.
 */
public class ATCommand {

    static char[] buf = new char[4096]; //4k buffer 4*1024
    static char[] buf2 = new char[4096]; //4k buffer 4*1024

    /* Replace '\n' with '\r', aka `tr '\012' '\015'` */
    public static String tr_lf_cr(String szOriginal)
    {
        return szOriginal.replaceAll("\n", "\r");
    }

    public static String StripCR (String szOriginal)
    {
        return szOriginal.replaceAll("\r","");
    }

    public static boolean isFinalResult(char szResponse)
    {
        switch (szResponse)
        {

        }
        return false;
    }

}
