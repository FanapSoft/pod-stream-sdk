package ir.fanap.podstreamsdkexample;

import android.app.Activity;

import ir.fanap.podstream.Entity.FileSetup;

public interface MainConstract {
    interface View{
        void onFileReady();

        void isLoading(boolean isloading);

        void hasError(String error);
    }

    interface Presenter{
        void init();
        void prepare(FileSetup fileSetup);
        void destroy();
    }
}
