package ir.fanap.podstreamsdkexample;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.podstreamsdkexample.R;

import ir.fanap.podstream.Entity.FileSetup;

public class MainActivity extends AppCompatActivity implements MainConstract.View {

    //    TextView ext_error;
    MainConstract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    public void init() {
        //ext_error = findViewById(R.id.ext_error);
        presenter = new MainPresenter(this, this);
        FileSetup file = new FileSetup.Builder().
                build(
                        "7936886af8064418b01e97f57c377734",
                        "5313E3G5GK3HCP1I"
                );
        presenter.prepare(file);
    }

    @Override
    public void onFileReady() {

    }

    @Override
    public void isLoading(boolean isloading) {

    }

    @Override
    public void hasError(String error) {
        Log.e("TAG", "hasError: " + error);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //  ext_error.append(error + "------\n");
            }
        });

        // Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.destroy();
    }
}