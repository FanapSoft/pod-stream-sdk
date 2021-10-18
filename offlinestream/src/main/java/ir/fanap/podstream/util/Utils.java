package ir.fanap.podstream.util;

import android.net.Uri;
import android.text.TextUtils;

import com.google.android.exoplayer2.util.Assertions;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Properties;

public class Utils {


    public static long byteArrayToLong(byte[] bytes) {
        try {
//            Log.e("te", "byteArrayToLong: " + bytes);
            long l = 0;
            for (int i = 0; i < 8; i++) {

                l <<= 8;

                l ^= (long) bytes[i] & 0xff;

            }
            return l;
        } catch (Exception e) {
            return 0;
        }
    }

    public static Properties getSslProperties(String brockerAddress, String sslPath) {

        final Properties propertiesProducer = new Properties();
        propertiesProducer.setProperty("bootstrap.servers", brockerAddress);
        propertiesProducer.setProperty("security.protocol", "SASL_SSL");
        propertiesProducer.setProperty("sasl.mechanisms", "PLAIN");
        propertiesProducer.setProperty("sasl.username", "rrrr");
        propertiesProducer.setProperty("sasl.password", "rrrr");
        propertiesProducer.setProperty("ssl.ca.location", sslPath);
        propertiesProducer.setProperty("ssl.key.password", "masoud68");

        return propertiesProducer;
    }



    private static RandomAccessFile openLocalFile(Uri uri) throws com.google.android.exoplayer2.upstream.FileDataSource.FileDataSourceException {
        try {
            return new RandomAccessFile(Assertions.checkNotNull(uri.getPath()), "r");
        } catch (FileNotFoundException e) {
            if (!TextUtils.isEmpty(uri.getQuery()) || !TextUtils.isEmpty(uri.getFragment())) {
                throw new com.google.android.exoplayer2.upstream.FileDataSource.FileDataSourceException(
                        String.format(
                                "uri has query and/or fragment, which are not supported. Did you call Uri.parse()"
                                        + " on a string containing '?' or '#'? Use Uri.fromFile(new File(path)) to"
                                        + " avoid this. path=%s,query=%s,fragment=%s",
                                uri.getPath(), uri.getQuery(), uri.getFragment()),
                        e);
            }
            throw new com.google.android.exoplayer2.upstream.FileDataSource.FileDataSourceException(e);
        }
    }
}
