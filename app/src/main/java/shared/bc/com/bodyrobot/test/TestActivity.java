package shared.bc.com.bodyrobot.test;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.serenegiant.usb.widget.CameraViewInterface;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import shared.bc.com.bodyrobot.R;
import shared.bc.com.bodyrobot.ReportActivity;

/**
 * @author goJee
 * @since 2019/4/12
 */
public class TestActivity extends AppCompatActivity {
    Button button;
    UpdateBean updateBean = new UpdateBean();


    private TextView debugView;
    private TextView calibrationHeight;
    private TextView calibrationWeight;

    private CameraViewInterface mUVCCameraView;
    private GifImageView gifView;


    public String IMEI = "IMEI";
    private Boolean debug = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hideNavigation();
        toReport();
    }

    private void Guide() {
        setContentView(R.layout.activity_home);
        debugView = (TextView) findViewById(R.id.equipment_debugging);
        calibrationHeight = (TextView) findViewById(R.id.calibration_height);
        debugView.setText("版本号:" + getAppVersionName(TestActivity.this) + "-release");
        calibrationHeight.setText("设备编号:" + IMEI);
        gifView = (GifImageView) findViewById(R.id.gif);
        if (!debug) {
            debugView.setTextColor(Color.parseColor("#FF0000"));
        }
        try {
            GifDrawable gifFromResource = new GifDrawable(getResources(), R.drawable.image_back_start);
            gifView.setImageDrawable(gifFromResource);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void start() {
        String s;
        setContentView(R.layout.activity_facial);
        debugView = (TextView) findViewById(R.id.equipment_debugging_facial);
        s = "身高:" + "cm" + "\n体重:" + "kg" + "\n生物阻抗:" + "\n图片编码:";
        debugView.setText(s);
        try {
            gifView = (GifImageView) findViewById(R.id.facial_gif);
            GifDrawable gifFromResource = new GifDrawable(getResources(), R.drawable.image_back_facial);
            gifView.setImageDrawable(gifFromResource);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public static String getAppVersionName(Context context) {
        String appVersionName = "";
        try {
            PackageInfo packageInfo = context.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            appVersionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("", e.getMessage());
        }
        return appVersionName;
    }

    //    @Override
//    protected void onResume() {
//        toReport();
//        super.onResume();
//    }
////    //
    public void toReport() {
        Intent intent = new Intent(this, ReportActivity.class);
        intent.putExtra("height", 180);
        intent.putExtra("weight", 50);
        intent.putExtra("fat", 800);
        intent.putExtra("imgName", "1567251327989");
//        intent.putExtra("imgName", "0000");
        intent.putExtra("debug", false);
        startActivity(intent);
//        finish();
    }

}
