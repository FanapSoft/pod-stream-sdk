package ir.fanap.podstream.Util;

import android.util.Log;

public class Utils {


    public static long byteArrayToLong(byte[] bytes) {
       try{
           Log.e("te", "byteArrayToLong: "+bytes );
           long l = 0;
           for (int i=0; i<8; i++) {

               l <<= 8;

               l ^= (long) bytes[i] & 0xff;

           }
           return l;
       }catch (Exception e){
           return 0;
       }
    }
}
