package shared.bc.com.bodyrobot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.xiaoti.robot.tzcaccess.StepEnums;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import shared.bc.com.bodyrobot.connect.HttpStep;
import shared.bc.com.bodyrobot.model.Report;
import shared.bc.com.bodyrobot.until.Constants;
import shared.bc.com.bodyrobot.until.IMEIUtils;
import shared.bc.com.bodyrobot.until.ZXingUtils;

/**
 * @author goJee
 * @since 2019/4/11
 */
public class ReportActivity extends AppCompatActivity {


    public static ReportActivity reporInstance;
    private final static int DelaySize = 30;
    private int mCounts = 0;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Handler handler = new Handler();
    private MediaPlayer mPlayer = null;
    private TextView textView;
    private TextView debugView;
    private ImageView guide_image;
    private ImageView image;
    private SharedPreferences sp;
    private String IMEI;

    private InputStream fileNames = null;
    private InputStream fileNames2 = null;
    private String json;
    private String result;
    private float height ;
    private float weight ;
    private float fat ;
    private String name;
    private Boolean debugging;
    public static final String URL = "server.51xiaoti.com";
    public static final String HTTP_PORT = "8764";
    public static final int TCP_PORT = 8871;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideNavigation();
        Log.e("test","开始页面!!!!!!!!!!!!!");
    }

    @Override
    protected void onResume() {
        connectReport();
        End();
        super.onResume();
    }
    private void connectReport(){
        initImei();
        Intent intent = getIntent();
        height = intent.getFloatExtra("height",height);
        weight = intent.getFloatExtra("weight",weight);
        fat = intent.getFloatExtra("fat",fat);
        name = intent.getStringExtra("imgName");
        debugging = intent.getBooleanExtra("debug",true);
        Log.e("身高",height+"");
        Report report = new Report();
        report.setHeight(BigDecimal.valueOf(height));
        report.setWeight(BigDecimal.valueOf(weight));
        report.setImpedance(BigDecimal.valueOf(fat));
        report.setImg(name);
        report.setDeviceCode(IMEI);
        Log.e("设备IMEI",IMEI);
        json = JSON.toJSONString(report);

        try {
            fileNames = getAssets().open("client.bks");
            fileNames2 = getAssets().open("client.bks");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void End(){
        String s;
        setContentView(R.layout.activity_end);
        textView = (TextView) findViewById(R.id.machie_end);
        debugView = (TextView) findViewById(R.id.equipment_debugging_report);
        image = (ImageView) findViewById(R.id.image_log);
        guide_image = (ImageView) findViewById(R.id.image_end);
        guide_image.setImageDrawable(getResources().getDrawable(R.drawable.image_back_end));
        if(debugging == false && height !=0 && weight !=0 && fat !=0 && name !=null){
            s =  "身高:"+height+"cm"+"\n体重:"+weight+"kg"+"\n生物阻抗:"+fat+"\n图片编码:"+name;
            debugView.setText(s);
        }else{
            s = "";
            debugView.setText(s);
        }

        mPlayer = MediaPlayer.create(this, R.raw.report);
//      设置音频
//      播放音频
        mPlayer.start();
        thread.start();
        thread3.start();
        mCounts = 0;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                int remain = DelaySize - mCounts++;
                if (remain > 0) {
                    String text = String.format("%s%s",
                            remain,
                            "");
                    textView.setText(text+"s");
                    mHandler.postDelayed(this, 1000);
                }else{
                    handler.post(runnableUi);
                }
            }
        });
    }

    public void httpReport(String json){
        try {
            String url1 = "http://"+URL+"/api/report/v2";
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, json);

            final Request request = new Request.Builder()
                    .url(url1)
                    .post(body)
                    .build();
            OkHttpClient client = new OkHttpClient();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("HTTP","二维码http连接失败");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.e("HTTP","http连接成功");
                    String json = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        String success = jsonObject.getString("success");
                        String message = jsonObject.getString("message");
                        int code = jsonObject.getInt("code");
                        if ("true".equals(success) && code == 200) {
                            result = jsonObject.getString("result");
                            handler.post(runnableUi2);
                        } else {
                            Log.e("message" + code, message);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch(NullPointerException e){
                        e.printStackTrace();
                    }

                }
            });
        }finally{

        }
    }

    private void initImei() {
        sp = getSharedPreferences(Constants.SharedPref, MODE_PRIVATE);
        IMEI = sp.getString(Constants.SP_IMEI, "");
        String imei = IMEIUtils.getImei(this);
        if (!imei.isEmpty()) {
            sp.edit().putString(Constants.SP_IMEI, imei).apply();
            if (!imei.equals(IMEI)) {
                IMEI = imei;
            }
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

    Thread thread=new Thread(new Runnable() {
        @Override
        public void run() {
            httpReport(json);

        }
    });
    Thread thread3=new Thread(new Runnable() {
        @Override
        public void run() {
            HttpStep.httpRetained(String.valueOf (StepEnums.QR_CODE_STEP.getValue()),IMEI);

        }
    });
    public void Image(){
        Bitmap bitmaplog = BitmapFactory.decodeResource(getResources(), R.drawable.image_log);
        Bitmap bitmap = ZXingUtils.createQRImage(result , 200,200,bitmaplog);
        image.setImageBitmap(bitmap);
    }
    public void Start(){
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        System.gc();
        finish();
    }
    Runnable   runnableUi=new  Runnable(){
        @Override
        public void run() {
            //更新界面
            Start();
        }

    };
    Runnable   runnableUi2=new  Runnable(){
        @Override
        public void run() {
            //更新界面
            Image();
        }

    };
}
