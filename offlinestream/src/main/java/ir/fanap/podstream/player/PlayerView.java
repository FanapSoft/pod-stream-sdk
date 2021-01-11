package ir.fanap.podstream.player;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.SurfaceView;
import android.view.TextureView;

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;


public class PlayerView extends TextureView {

    public PlayerView(@NonNull Context context) {
        super(context);
    }

    public PlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
