package shared.bc.com.bodyrobot;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author goJee
 * @since 2019/4/11
 */
public class FailureActivity extends AppCompatActivity {
    Intent intent;
    private final static int DelaySize = 3;
    private int mCounts = 0;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private MediaPlayer mPlayer = null;
    private TextView textView;
    private ImageView guide_image;
    private Handler handler = new Handler();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_failure);
        hideNavigation();
        Failure();
    }

    public void Failure(){
        setContentView(R.layout.activity_failure);
        textView = (TextView) findViewById(R.id.machie_failure);
        guide_image = (ImageView) findViewById(R.id.image_failure);
        guide_image.setImageDrawable(getResources().getDrawable(R.drawable.image_back_failure));


        mPlayer = MediaPlayer.create(this, R.raw.failure);
//      设置音频
//      播放音频
        mPlayer.start();
        mCounts = 0;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                int remain = DelaySize - mCounts++;
                if (remain > 0) {
                    String text = String.format("%s%s",
                            remain,
                            "");
                    textView.setText(text);
                    mHandler.postDelayed(this, 1000);
                }else{
                    handler.post(runnableUi);
                }
            }
        });
    }

    public void hideNavigation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }
    public void Start(){
        intent = new Intent(this,HomeActivity.class);
        startActivity(intent);
        finish();
    }
    Runnable   runnableUi=new  Runnable(){
        @Override
        public void run() {
            //更新界面
            Start();
        }

    };
}
