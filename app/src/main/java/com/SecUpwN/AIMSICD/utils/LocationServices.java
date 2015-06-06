/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.utils;

import android.os.AsyncTask;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class LocationServices {

    private static void writeData(OutputStream out, int cid, int lac, int mnc, int mcc)
            throws IOException
    {
        DataOutputStream dataOutputStream = new DataOutputStream(out);
        dataOutputStream.writeShort(0x0E); // Fct code

        dataOutputStream.writeInt(0); // requesting 8 byte session
        dataOutputStream.writeInt(0);

        dataOutputStream.writeShort(0); // country code string
        dataOutputStream.writeShort(0); // client descriptor string
        dataOutputStream.writeShort(0); // version tag string

        dataOutputStream.writeByte(0x1B); // Fct code

        dataOutputStream.writeInt(0); // MNC?
        dataOutputStream.writeInt(0); // MCC?
        dataOutputStream.writeInt(3); // RAT  = Radio Access Type (3=GSM, 5=UMTS)

        dataOutputStream.writeShort(0); // length of provider name

        // provider name string
        dataOutputStream.writeInt(cid); // CID
        dataOutputStream.writeInt(lac); // LAC
        dataOutputStream.writeInt(mnc); // MNC
        dataOutputStream.writeInt(mcc); // MCC
        dataOutputStream.writeInt(-1);  // always -1
        dataOutputStream.writeInt(0);   // rx level

        dataOutputStream.flush();
    }

    public static class LocationAsync extends AsyncTask<Integer, Void, float[]> {
        public AsyncResponse delegate=null;

        @Override
        protected float[] doInBackground(Integer... params) {
            /*try {
                String mmapUrl = "http://www.google.com/glm/mmap";

                URL url = new URL(mmapUrl);
                HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                httpConn.setRequestMethod("POST");
                httpConn.setDoOutput(true);
                httpConn.connect();

                OutputStream outputStream = httpConn.getOutputStream();
                writeData(outputStream,
                        params[0], //CID
                        params[1], //LAC
                        params[2], //MNC
                        params[3]); //MCC

                InputStream inputStream = httpConn.getInputStream();
                DataInputStream dataInputStream = new DataInputStream(inputStream);

                dataInputStream.readShort();
                dataInputStream.readByte();

                int code = dataInputStream.readInt();
                if (code == 0) {
                    float latitude = dataInputStream.readInt() / 1000000f;
                    float longitude = dataInputStream.readInt() / 1000000f;
                    return new float[] {latitude, longitude};
                }

            } catch (IOException e) {
                e.printStackTrace();
            }*/

            return new float[] {0.0f,0.0f};
        }

        @Override
        protected void onPostExecute(float[] floats) {
            delegate.processFinish(floats);
        }
    }
}
