package shared.bc.com.bodyrobot.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.serenegiant.usb.widget.CameraViewInterface;

import pl.droidsonroids.gif.GifImageView;
import shared.bc.com.bodyrobot.ReportActivity;

/**
 * @author goJee
 * @since 2019/4/12
 */
public class TestActivity  extends AppCompatActivity {
    Button button;
    UpdateBean updateBean = new UpdateBean();

    private CameraViewInterface mUVCCameraView;
    private GifImageView gifView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_facial);
//
//        mUVCCameraView = findViewById(R.id.camera_view);
//
//        try {
//            gifView = (GifImageView) findViewById(R.id.facial_gif);
//            GifDrawable gifFromResource = new GifDrawable(getResources(), R.drawable.image_back_facial);
//            gifView.setImageDrawable(gifFromResource);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        button = (Button) findViewById(R.id.btn_update);
//        toReport();
//        UpdateApk.UpdateVersion(TestActivity.this);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //版本更新
//                UpdateApk.UpdateVersion(TestActivity.this);
//            }
//        });
    }

    @Override
    protected void onResume() {
        toReport();
        super.onResume();
    }
//    //
    public void toReport(){
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
