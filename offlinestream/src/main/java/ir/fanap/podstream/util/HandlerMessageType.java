package ir.fanap.podstream.util;



import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class HandlerMessageType {


    private final int ackType;

    public HandlerMessageType(int ackType) {
        this.ackType = ackType;
    }


    @IntDef({
            ActConstants.MESSAGE_PLAYER_INIT,
            ActConstants.MESSAGE_PLAYER_RELEASE,
            ActConstants.MESSAGE_PLAYER_PREAPARE_TO_PLAY,
            ActConstants.MESSAGE_ERROR
    })



    @Retention(RetentionPolicy.SOURCE)
    public @interface ActConstants {

        int MESSAGE_PLAYER_INIT = 0;
        int MESSAGE_PLAYER_RELEASE = 1;
        int MESSAGE_PLAYER_PREAPARE_TO_PLAY = 2;
        int MESSAGE_ERROR = 3;




    }
}
