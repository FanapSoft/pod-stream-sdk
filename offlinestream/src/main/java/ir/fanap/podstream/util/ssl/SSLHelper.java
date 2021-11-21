package ir.fanap.podstream.util.ssl;

import android.content.Context;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ir.fanap.podstream.util.Constants;

public class SSLHelper {
    File cart;

    public SSLHelper(Context context) {
        try {
            generateFile(Constants.CERT_FILE,context);
        } catch (Exception e) {

        }
    }

    public void generateFile(String sslCert, Context mContext) throws Exception {

        if (sslCert == null || sslCert.trim().isEmpty()) cart= null;

        InputStream inputStream1 =
                new ByteArrayInputStream(sslCert.getBytes());

        OutputStream out1 = null;

        try {

            File cTemp = new File(mContext.getFilesDir() + "/ca-cert");

            if(cTemp.exists())
            {
                cart= cTemp;
            }


            out1 = new FileOutputStream(mContext.getFilesDir() + "/ca-cert");

            copy(inputStream1, out1);


            File cert = new File(mContext.getFilesDir() + "/ca-cert");

            if (cert.exists()) {

                cart= cTemp;

            } else throw new Exception("Could not create ssl file!");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new Exception("FileNotFoundException! Could not create ssl file! " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("IOExeption! Could not create ssl file! " + e.getMessage());
        }
    }

    public File getCart() {
        return cart;
    }

    private void copy(InputStream inputStream1, OutputStream out1) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = inputStream1.read(buffer)) != -1) {
            out1.write(buffer, 0, read);
        }
        inputStream1.close();
        inputStream1 = null;
        out1.flush();
        out1.close();
        out1 = null;
    }

}
