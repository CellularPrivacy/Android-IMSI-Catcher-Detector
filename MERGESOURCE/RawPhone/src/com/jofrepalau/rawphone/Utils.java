package com.jofrepalau.rawphone;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;
import com.jofrepalau.rawphone.cmdprocessor.CMDProcessor;
import com.stericson.RootTools.RootTools;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class Utils {

    public static void CheckUtils(Context ctx)
    {
        final String szChmod;
        final Context ctxInstance = ctx;

        // Check chmod utils
        if (RootTools.checkUtil("chmod"))
            szChmod = "chmod";
        else
        {
            if (RootTools.checkUtil("busybox") && RootTools.hasUtil("chmod", "busybox"))
                szChmod = "busybox chmod";
            else if (RootTools.checkUtil("toolbox") && RootTools.hasUtil("chmod", "toolbox"))
                szChmod = "toolbox chmod";
            else
                szChmod = "";
        }

        // Check for microcom applet
        if (!RootTools.checkUtil("microcom"))
        {
            AlertDialog.Builder mMicrocom = new AlertDialog.Builder(ctx);
            mMicrocom.setTitle("RawPhone - Microcom applet");
            mMicrocom.setMessage(ctx.getResources().getString(R.string.text_no_microcom));
            mMicrocom.setPositiveButton(ctx.getResources().getString(R.string.text_ok), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    if (CopyAssetsToLocal(ctxInstance, "microcom","microcom"))
                    {
                        boolean bOperationSucceed;
                        // Copy to system
                        String szPathName = GetBasePath(ctxInstance);
                        if (!szPathName.endsWith("/"))
                            szPathName = szPathName + "/";

                        bOperationSucceed = InstallBinary(szPathName, "microcom", szChmod);

                        CharSequence text = "Microcom applet successfully installed!";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(ctxInstance, text, duration);
                        toast.show();
                    }
                }
            });
            mMicrocom.setNegativeButton(ctx.getResources().getString(R.string.text_cancel), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            });
            mMicrocom.show();
        }

    }

    // Copy assets to local
    private static boolean CopyAssetsToLocal(Context ctx, String szSourceName, String szDstName)
    {
        String szBasePath = GetBasePath(ctx);
        if (szBasePath.equals("")) return false;
        szDstName = szBasePath + "/" + szDstName;

        InputStream myInput;
        OutputStream myOutput;
        String outFileName = szDstName;
        try
        {
            File hfOutput = new File(szDstName);
            if (hfOutput.exists()) hfOutput.delete();

            myOutput = new FileOutputStream(outFileName);
            myInput = ctx.getAssets().open(szSourceName);
            byte[] tBuffer = new byte[4096];  /* 4K page size */
            int nLength;
            while ((nLength = myInput.read(tBuffer)) > 0)
                myOutput.write(tBuffer, 0, nLength);
            myOutput.flush();
            myInput.close();
            myOutput.close();
        }
        catch (Exception e)
        {
            Log.e("RawPhone_Utils", "CopyAssetsToLocal() failed, msg = " + e.getMessage());
            return false;
        }

        return true;
    }

    // Get application data path
    private static String GetBasePath(Context ctx)
    {
        Context cont = ctx.getApplicationContext();
        String szBasePath = "";
        if (cont != null)
        {
            // No try catch the cont != null will prevent a possible NPE here
            if (cont.getFilesDir().exists())
                szBasePath = cont.getFilesDir().getAbsolutePath();
            else if (!cont.getFilesDir().mkdirs())
                szBasePath = "";
        }
        else
        {
            szBasePath = "";
        }

        return szBasePath;
    }

    private static boolean InstallBinary(String szBinaryPath, String szBinaryName,String szChmod)
    {
        boolean mOperationSucceeded;

        mOperationSucceeded = CMDProcessor.runSuCommand("mount -o rw,remount /system").success();
        mOperationSucceeded &= CMDProcessor.runSuCommand("cp " + szBinaryPath + szBinaryName + " /system/xbin/" + szBinaryName).success();
        mOperationSucceeded &= CMDProcessor.runSuCommand(szChmod + " 755 /system/xbin/" + szBinaryName).success();
        mOperationSucceeded &= CMDProcessor.runSuCommand("sync").success();
        mOperationSucceeded &= CMDProcessor.runSuCommand("mount -o ro,remount /system").success();

        return mOperationSucceeded;
    }

    private static String ByteToString(byte[] baByteArray)
    {
        if (baByteArray == null) return null;
        try
        {
            String szResult = new String(baByteArray, "ASCII");
            szResult = String.copyValueOf(szResult.toCharArray(), 0, baByteArray.length);
            return szResult;
        }
        catch (UnsupportedEncodingException e)
        {
            return null;
        }
    }

    private static String[] ByteArrayToStringArray(byte[] baByteArray, int nDataLength)
    {
        if (baByteArray == null) return null;
        if (nDataLength <= 0) return null;
        if (nDataLength > baByteArray.length) return null;

        // Replace all unvisible chars to '.'
        for (int i = 0; i < nDataLength; i++)
        {
            if ((baByteArray[i] == 0x0D) || (baByteArray[i] == 0x0A))
            {
                baByteArray[i] = 0;
                continue;
            }
            if (baByteArray[i] < 0x20) baByteArray[i] = 0x2E;
            if (baByteArray[i] > 0x7E) baByteArray[i] = 0x2E;
        }

        // Split and convert to string
        List<String> lstString = new ArrayList<String>();
        for (int i = 0; i < nDataLength; i++)
        {
            if (baByteArray[i] == 0) continue;
            int nBlockLength = -1;
            for (int j = i + 1; j < nDataLength; j++)
            {
                if (baByteArray[j] == 0)
                {
                    nBlockLength = j - i;
                    break;
                }
            }
            if (nBlockLength == -1) nBlockLength = nDataLength - i;
            byte[] baBlockData = new byte[nBlockLength];
            System.arraycopy(baByteArray, i, baBlockData, 0, nBlockLength);
            lstString.add(ByteToString(baBlockData));
            i += nBlockLength;
        }

        if (lstString.size() <= 0) return null;
        String[] szResult = new String[lstString.size()];
        lstString.toArray(szResult);
        return szResult;
    }
}
