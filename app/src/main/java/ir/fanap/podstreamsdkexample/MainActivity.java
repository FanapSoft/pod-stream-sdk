package ir.fanap.podstreamsdkexample;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.podstreamsdkexample.R;

import ir.fanap.podstream.Entity.FileSetup;

public class MainActivity extends AppCompatActivity implements MainConstract.View {

    //    TextView ext_error;
    MainConstract.Presenter presenter;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    public void init() {
        progressBar = findViewById(R.id.progressBar);
        presenter = new MainPresenter(this, this);
        FileSetup file = new FileSetup.Builder().
                build(
                        "7936886af8064418b01e97f57c377734",
                        "A4LGHTOURUCII46D"
                );
        //A4LGHTOURUCII46D
        presenter.prepare(file);
    }

    @Override
    public void onFileReady() {

    }

    void showLoading() {
        if (progressBar.getVisibility() != View.VISIBLE)
            progressBar.setVisibility(View.VISIBLE);
        Log.e("loadti", "showLoading: " + System.currentTimeMillis());

    }

    void hideLoading() {
        if (progressBar.getVisibility() == View.VISIBLE)
            progressBar.setVisibility(View.INVISIBLE);
        Log.e("loadti", "hideLoading: " + System.currentTimeMillis());
    }

    @Override
    public void isLoading(boolean isloading) {
        if (isloading) {
            showLoading();
        } else {
            hideLoading();
        }

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