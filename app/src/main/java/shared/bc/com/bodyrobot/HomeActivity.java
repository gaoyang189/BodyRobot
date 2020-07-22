package shared.bc.com.bodyrobot;


import android.Manifest;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.usb.UsbDevice;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jiangdg.usbcamera.UVCCameraHelper;
import com.jiangdg.usbcamera.utils.FileUtils;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.common.AbstractUVCCameraHandler;
import com.serenegiant.usb.widget.CameraViewInterface;
import com.serenegiant.usb.widget.UVCCameraTextureView;
import com.xiaoti.robot.tzcaccess.StepEnums;

import org.opencv.ObjectDetector;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;

import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android_serialport_api.sample.SerialApplication;
import android_serialport_api.sample.SerialPortActivity;
import it.sauronsoftware.cron4j.Scheduler;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import shared.bc.com.bodyrobot.connect.HttpStep;
import shared.bc.com.bodyrobot.connect.XTClient;
import shared.bc.com.bodyrobot.receiver.AutoInstallService;
import shared.bc.com.bodyrobot.test.UpdateApk;
import shared.bc.com.bodyrobot.until.Constants;
import shared.bc.com.bodyrobot.until.DeleteImage;
import shared.bc.com.bodyrobot.until.IMEIUtils;
import shared.bc.com.bodyrobot.until.SharedProtocol;


/**
 * @author goJee
 * @since 2019/3/28
 */
public class HomeActivity extends SerialPortActivity implements CameraDialog.CameraDialogParent, CameraViewInterface.Callback {


    private int state = 0;
    public String IMEI = "IMEI";

    public int num = 0;
    private SharedPreferences sp;
    private float[] information = new float[3];
    private float[] informationfat = new float[9];
    private List<String> list = new ArrayList();
    private static final String TAG = "SerialPortActivity";

    private GifImageView gifView;
    private MediaPlayer mPlayer = null;
    private SharedProtocol mAgreement;
    private ImageView guide_image;
    private TextView textView;
    private TextView cHeight;
    private TextView debugView;
    private TextView calibrationHeight;
    private TextView calibrationWeight;
    private ListView listView;

    private int mCounts = 0;

    private final static int DelaySize = 3;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Handler handler = new Handler();
    private Boolean debug = true;
    public static HomeActivity instance = null;
    private XTClient thread = null;

    private UVCCameraHelper mCameraHelper = null;
    private CameraViewInterface mUVCCameraView;
    private UVCCameraTextureView cameraView;
    private boolean isRequest;
    private boolean isPreview;
    private ObjectDetector mFaceDetector;
    private String name;
    private byte[] rawImage;
    private Bitmap bitmap;
    private ByteArrayOutputStream baos;
    private int m = 0;


    static {
        System.loadLibrary("opencv_java3");
    }

    private UVCCameraHelper.OnMyDevConnectListener listener = new UVCCameraHelper.OnMyDevConnectListener() {

        @Override
        public void onAttachDev(UsbDevice device) {
            if (mCameraHelper == null || mCameraHelper.getUsbDeviceCount() == 0) {
                showShortMsg("check no usb camera");
                return;
            }
            // request open permission
            if (!isRequest) {
                isRequest = true;
                if (mCameraHelper != null) {
                    mCameraHelper.requestPermission(0);
                }
            }
        }

        @Override
        public void onDettachDev(UsbDevice device) {
            // close camera
            if (isRequest) {
                isRequest = false;
                mCameraHelper.closeCamera();
                showShortMsg(device.getDeviceName() + " is out");
            }
        }

        @Override
        public void onConnectDev(UsbDevice device, boolean isConnected) {
            if (!isConnected) {
                showShortMsg("fail to connect,please check resolution params");
                isPreview = false;
            } else {
                isPreview = true;
                showShortMsg("connecting");
                Log.e("连接的摄像头id", device.getDeviceId() + "");
            }
        }

        @Override
        public void onDisConnectDev(UsbDevice device) {
            showShortMsg("disconnecting");
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogcatHelper.getInstance(this).start();
        initImei();
        checkPromission();
        super.onCreate(savedInstanceState);
        instance = this;
        AutoInstallService.callback = null;
        Task();
        mAgreement = new SharedProtocol(mProtocolListener);
        thread2.start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                UpdateApk.UpdateVersion(HomeActivity.this);
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                httpCode(UpdateApk.getVersionCode(HomeActivity.this), getAppVersionName(HomeActivity.this));
            }
        }).start();
        thread3.start();
        thread4.start();
        thread5.start();
    }

    //
//
//
    private void Task() {
        Scheduler scheduler = new Scheduler();
        // Schedules the task, once every minute.
        scheduler.schedule("* * * * *", new DeleteImage());
        // Starts the scheduler.
        scheduler.start();
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
//
//        //********** TEST ***********//
////        IMEI = "355099048440815";
//        Log.d(TAG, "IMEI: " + IMEI);
    }

    @Override
    protected void onDataReceived(byte[] buffer, int size) {
        byte[] data = new byte[size];
        System.arraycopy(buffer, 0, data, 0, size);
        if (mAgreement != null) {
            mAgreement.handleWithData(data);
        }
    }

    @Override
    protected SerialApplication getAppDelegate() {
        return (MyApplication) getApplication();
    }

    //  引导页
    public void Guide() {
        setContentView(R.layout.activity_home);
        hideNavigation();
        debugView = (TextView) findViewById(R.id.equipment_debugging);
        calibrationHeight = (TextView) findViewById(R.id.calibration_height);
        debugView.setText("版本号:" + getAppVersionName(HomeActivity.this) + "-release");
        calibrationHeight.setText("设备编号:" + IMEI);
        state = 0;
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

////      设置音频
//        mPlayer = MediaPlayer.create(this, R.raw.guide);
////      播放音频
//        mPlayer.start();
//        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer arg0) {
//                if(state == 0) {
//                    Timer timer = new Timer();
//                    timer.schedule(new TimerTask() {
//                        @Override
//                        public void run() {
//                            mPlayer.start();
//                        }
//                    }, 5000);
//                }
//            }
//        });
        calibrationHeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String data = "FA87010187";
                byte[] sendData = hex2byte(data);
                writeBytes(sendData);
                CalibrationHeight();
            }

        });
        debugView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (debug) {
                    debugView.setTextColor(Color.parseColor("#FF0000"));
                    debug = false;
                } else {
                    debugView.setTextColor(Color.parseColor("#FFFFFF"));
                    debug = true;
                }
            }

        });

    }
    //准备检测

    public void calibrationWeight(View v) {
        String data = "FA86010087";
        byte[] sendData = hex2byte(data);
        writeBytes(sendData);
    }

    public static byte[] hex2byte(String hex) {
        String digital = "0123456789ABCDEF";
        String hex1 = hex.replace(" ", "");
        char[] hex2char = hex1.toCharArray();
        byte[] bytes = new byte[hex1.length() / 2];
        byte temp;
        for (int p = 0; p < bytes.length; p++) {
            temp = (byte) (digital.indexOf(hex2char[2 * p]) * 16);
            temp += digital.indexOf(hex2char[2 * p + 1]);
            bytes[p] = (byte) (temp & 0xff);
        }
        return bytes;
    }

    private void Start() {
        if (state == 0) {
            setContentView(R.layout.activity_start);
            state = -1;
            textView = (TextView) findViewById(R.id.machie_tv);
            guide_image = (ImageView) findViewById(R.id.image_start);
            guide_image.setImageDrawable(getResources().getDrawable(R.drawable.image_back_guide));

            mPlayer = MediaPlayer.create(this, R.raw.start);
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
                    } else {
                        if (state == -1) {
                            Height();
                        }
                    }
                }
            });
        }

    }

    private void Height() {
        setContentView(R.layout.activity_height);
        String s;
        state = 1;
        try {
            gifView = (GifImageView) findViewById(R.id.height_gif);
            debugView = (TextView) findViewById(R.id.equipment_debugging_height);
            debugView = (TextView) findViewById(R.id.equipment_debugging_height);
            GifDrawable gifFromResource = new GifDrawable(getResources(), R.drawable.image_back_height);
            gifView.setImageDrawable(gifFromResource);
            if (debug == false) {
                s = "身高:\n体重:\n生物阻抗:\n图片编码:";
                debugView.setText(s);
            } else {
                s = "";
                debugView.setText(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //      设置音频
        mPlayer = MediaPlayer.create(this, R.raw.height);
//      播放音频
        mPlayer.start();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (state == 1) {
                    handler.post(runnableUi5);
                }
            }
        }, 11000);


    }

    private void Weight() {
        if (state == 1) {
            String s;
            setContentView(R.layout.activity_weight);
            debugView = (TextView) findViewById(R.id.equipment_debugging_weight);
            state = 2;
            if (debug == false && information[0] != 0) {
                s = "身高:" + information[0] + "cm" + "\n体重:\n生物阻抗:\n图片编码:";
                debugView.setText(s);
            } else {
                s = "";
                debugView.setText(s);
            }
            try {
                gifView = (GifImageView) findViewById(R.id.weight_gif);
                GifDrawable gifFromResource = new GifDrawable(getResources(), R.drawable.image_back_weight);
                gifView.setImageDrawable(gifFromResource);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //      设置音频
            mPlayer = MediaPlayer.create(this, R.raw.weight);
//      播放音频
            mPlayer.start();
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (state == 2 || state == 0) {
                        handler.post(runnableUi5);
                    }
                }
            }, 10000);
        }
    }

    private void Fat() {
        if (state == 2) {
            String s;
            setContentView(R.layout.activity_fat);
            state = 3;
            debugView = (TextView) findViewById(R.id.equipment_debugging_fat);
            if (debug == false && information[0] != 0 && information[1] != 0) {
                s = "身高:" + information[0] + "cm" + "\n体重:" + information[1] + "kg" + "\n生物阻抗:\n图片编码:";
                debugView.setText(s);
            } else {
                s = "";
                debugView.setText(s);
            }
            try {
                gifView = (GifImageView) findViewById(R.id.fat_gif);
                GifDrawable gifFromResource = new GifDrawable(getResources(), R.drawable.image_back_fat);
                gifView.setImageDrawable(gifFromResource);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //      设置音频
            mPlayer = MediaPlayer.create(this, R.raw.fat);
//      播放音频
            mPlayer.start();
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (state == 3) {
//                        handler.post(runnableUi5);
                        information[2] = 500;
                        handler.post(runnableUi6);
                    }
                }
            }, 8000);
        }

    }

    private void Facial() {
        if (state == 3) {
            String s;
            setContentView(R.layout.activity_facial);
            textView = (TextView) findViewById(R.id.machie_facial);
            mUVCCameraView = findViewById(R.id.camera_view);
            state = 4;
            debugView = (TextView) findViewById(R.id.equipment_debugging_facial);
            if (debug == false && information[0] != 0 && information[1] != 0 && information[2] != 0) {
                s = "身高:" + information[0] + "cm" + "\n体重:" + information[1] + "kg" + "\n生物阻抗:" + information[2] + "\n图片编码:";
                debugView.setText(s);
            } else {
                s = "";
                debugView.setText(s);
            }
            try {
                gifView = (GifImageView) findViewById(R.id.facial_gif);
                GifDrawable gifFromResource = new GifDrawable(getResources(), R.drawable.image_back_facial);
                gifView.setImageDrawable(gifFromResource);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //      设置音频
            mPlayer = MediaPlayer.create(this, R.raw.facial);
//      播放音频
            mPlayer.start();
            face();
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
                        if (remain == 1) {
                            mHandler.postDelayed(this, 100);
                        } else {
                            mHandler.postDelayed(this, 700);
                        }
                    } else {
                        if (state == 4) {
//                            if(m == 1) {
//                                mCameraHelper.updateResolution(1280, 720);
//                                m=0;
//                            }else{
//                                m=1;
//                                mCameraHelper.updateResolution(640, 480);
//                            }
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    // 休眠500ms，等待Camera创建完毕
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    // 开启预览
                                    mCameraHelper.startPreview(mUVCCameraView);
                                }
                            }).start();

                        }
                    }
                }
            });

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (state == 4) {
//                        try {
//                            mCameraHelper.stopPreview();
//                        }catch (Exception e){
//                            e.printStackTrace();
//                        }
                        handler.post(runnableUi5);
                    }
                }
            }, 13000);
        }
    }

    public void face() {
        if (mCameraHelper == null) {
            mUVCCameraView.setCallback(this);
            mCameraHelper = UVCCameraHelper.getInstance();
            mCameraHelper.initUSBMonitor(this, mUVCCameraView, listener);
            if (mCameraHelper != null) {
                mCameraHelper.registerUSB();
            }
        }
        findFace();
    }


    public void findFace() {
        mCameraHelper.setOnPreviewFrameListener(new AbstractUVCCameraHandler.OnPreViewResultListener() {
            @Override
            public void onPreviewResult(byte[] data) {
                int width = mCameraHelper.getPreviewWidth();
                int height = mCameraHelper.getPreviewHeight();

                BitmapFactory.Options newOpts = new BitmapFactory.Options();
                newOpts.inJustDecodeBounds = true;
                YuvImage yuvimage = new YuvImage(
                        data,
                        ImageFormat.NV21,
                        width,
                        height,
                        null);
                baos = new ByteArrayOutputStream();
                yuvimage.compressToJpeg(new Rect(0, 0, width, height), 100, baos);// 80--JPG图片的质量[0-100],100最高
                rawImage = baos.toByteArray();
                //将rawImage转换成bitmap
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                bitmap = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length, options);
                Matrix matrix = new Matrix();
                matrix.setRotate(90);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
//                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,options);
                int findFaceNum = convert2Grey(bitmap);
                if (findFaceNum != 0) {
                    if (state == 4) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        state = -3;
                        taskPhoto();

                        Log.e("拍照成功!", "找到脸部数量:" + findFaceNum);
                        handler.post(runnableUiReport);
                    }
                }
            }
        });
    }

    private int convert2Grey(Bitmap bitmap) {
        mFaceDetector = new ObjectDetector(getApplicationContext(), R.raw.lbpcascade_frontalface, 3, 0.2F, 0.2F, new Scalar(255, 0, 0, 255));
        Mat src = new Mat();//Mat是OpenCV的一种图像格式
        Mat temp = new Mat();
        Mat dst = new Mat();
        Utils.bitmapToMat(bitmap, src);
        Imgproc.cvtColor(src, temp, Imgproc.COLOR_RGB2BGR);
        Imgproc.cvtColor(temp, dst, Imgproc.COLOR_BGR2GRAY);
        Utils.matToBitmap(dst, bitmap);
        MatOfRect mObject = new MatOfRect();
        int a = mFaceDetector.detectObject(dst, mObject).length;

        src.release();
        temp.release();
        dst.release();

        return a;
    }

    public void taskPhoto() {
        String path = UVCCameraHelper.ROOT_PATH + "/Xiaoti Robot";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        name = System.currentTimeMillis() + "";
        if (mCameraHelper == null || !mCameraHelper.isCameraOpened()) {
            showShortMsg("sorry,camera open failed");
        }
        String picPath = UVCCameraHelper.ROOT_PATH + "/Xiaoti Robot/" + name
                + UVCCameraHelper.SUFFIX_JPEG;
        mCameraHelper.capturePicture(picPath, new AbstractUVCCameraHandler.OnCaptureListener() {
            @Override
            public void onCaptureResult(String path) {
                Log.e(TAG, "save path：" + path);
            }
        });
    }

    private void checkPromission() {
        if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
        }
        if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, 1);
        }
    }

    @Override
    public USBMonitor getUSBMonitor() {
        return mCameraHelper.getUSBMonitor();
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (canceled) {
            showShortMsg("取消操作");
        }
    }


    @Override
    public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
        if (!isPreview && mCameraHelper.isCameraOpened()) {
            mCameraHelper.startPreview(mUVCCameraView);
            isPreview = true;
        }
    }

    @Override
    public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {

    }

    @Override
    public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
        if (isPreview && mCameraHelper.isCameraOpened()) {
            //delete by lzg
            mCameraHelper.stopPreview();
            //end lzg
            isPreview = false;
        }
    }

    public void toReport() {
        Intent intent = new Intent(this, ReportActivity.class);
        intent.putExtra("height", information[0]);
        intent.putExtra("weight", information[1]);
        intent.putExtra("fat", information[2]);
        intent.putExtra("imgName", name);
        intent.putExtra("debug", debug);
        startActivity(intent);
    }

    private void USBCamera() {

        state = 5;
        Intent intent = new Intent(this, USBCameraActivity.class);
        intent.putExtra("information", information);
        intent.putExtra("debug", debug);
        Log.d("", information.toString());
        startActivity(intent);
    }


    //缺失页
    private void failure() {
        if (state != 10 && state != 11 && state != 12 && state != 13 && state != 0) {
            if (state != -2) {
                Intent intent = new Intent(this, FailureActivity.class);
                startActivity(intent);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        switchhttp();
                        state = -2;
                    }
                }).start();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
//        Facial();
//        Start();
        Guide();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            mCameraHelper.stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // step.3 unregister USB event broadcast
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("USB", "断开Camera");
        if (mCameraHelper != null) {
            mCameraHelper.unregisterUSB();
        }
        FileUtils.releaseFile();
        // step.4 release uvc camera resources
        if (mCameraHelper != null) {
            mUVCCameraView.DestroyedSurface();
            mCameraHelper.release();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void finish() {
        /**
         * 记住不要执行此句 super.finish(); 因为这是父类已经实现了改方法
         * 设置该activity永不过期，即不执行onDestroy()

         */
        moveTaskToBack(true);
    }

    private void CalibrationHeight() {
        state = 20;
        setContentView(R.layout.activity_calibration_height);
        list.clear();
        listView = findViewById(R.id.textlist);
        cHeight = findViewById(R.id.height_calibration);
        String text = "已进入身高标定模式,请放上身高模型!!";
        cHeight.setText(text);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(HomeActivity.this, android.R.layout.simple_list_item_1, list);
                listView.setAdapter(adapter);
                mHandler.postDelayed(this, 1000);
            }
        });
    }

    private void HeightEnd() {

        setContentView(R.layout.activity_calibration_height);
        cHeight = findViewById(R.id.height_calibration);
        String text = "身高标定已完成,即将退出!!";
        cHeight.setText(text);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Guide();
                    }
                });
            }
        }, 3000);

    }

    private void Calibration() {
        state = 10;
        setContentView(R.layout.activity_weight_calibration);
        calibrationWeight = findViewById(R.id.weight_calibration);
        String text = "已进入标中模式,请拿开秤上重物!";
        calibrationWeight.setText(text);
    }

    private void Weight50() {
        if (state == 10) {
            state = 11;
            String text = "请放上50kg物品!";
            calibrationWeight.setText(text);
        }


    }

    private void Weight100() {
        if (state == 11) {
            state = 12;
            String text = "请放上100kg物品!";
            calibrationWeight.setText(text);
        }


    }

    private void Weight150() {
        if (state == 12) {
            state = 13;
            String text = "请放上150kg物品!";
            calibrationWeight.setText(text);
        }


    }

    private void WeightEnd() {
        if (state == 13) {
            String text = "标定完毕,退出标重模式!";
            calibrationWeight.setText(text);
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Guide();
                        }
                    });
                }
            }, 3000);
        }

    }

    private SharedProtocol.Listener mProtocolListener;

    {
        mProtocolListener = new SharedProtocol.Listener() {

            @Override
            public void locked(int status) {
                handler.post(runnableUi);
            }

            @Override
            public void temporary(float weight) {
                Log.d(TAG, "weigh");
                if (weight == 0) {
                    if (state == -1 || state == 1 || state == 2 || state == 3 || state == 4) {
//                        try {
//                            mCameraHelper.stopPreview();
//                        }catch (Exception e){
//                            e.printStackTrace();
//                        }
                        handler.post(runnableUi5);
                    }
                    if (state == 5) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (USBCameraActivity.instance.state == 0) return;
                                USBCameraActivity.instance.failure();
                            }
                        });
                    }
                }
            }

            @Override
            public void Weight(float weight) {
                information[1] = weight;
            }

            @Override
            public void height(float height, int status) {
                if (status == 1 && state != 0) {
                    information[0] = height;
                    handler.post(runnableUi3);
                }
                if (status == 0 && state == 0) {
                    information[0] = height;
                    handler.post(runnableUi);
                }
                if (status == 0 && state == 20) {
                    if (list.size() > 10) {
                        list.remove(list.get(0));
                    }
                    String test = "身高:" + height;
                    list.add(test);
                }
            }

            @Override
            public void FatStart(int status) {
                if (status == 0) {
                    handler.post(runnableUi4);
                }
            }

            @Override
            public void Fat(float[] fat) {
                System.arraycopy(fat, 0, informationfat, 0, fat.length);
                information[2] = fat[1] * 10;
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.post(runnableUi6);
            }

            @Override
            public void FatTimeOut() {
//                handler.post(runnableUi5);
            }

            @Override
            public void weightCalibration() {
                handler.post(runnableUi8);
            }

            @Override
            public void weight50() {
                handler.post(runnableUi9);
            }

            @Override
            public void weight100() {
                handler.post(runnableUi10);
            }

            @Override
            public void weight150() {
                handler.post(runnableUi11);
            }

            @Override
            public void weightEnd() {
                handler.post(runnableUi12);
            }

            @Override
            public void calibrationHeight() {
                handler.post(runnableUi13);
            }

            @Override
            public void on() {

            }

            @Override
            public void off() {
                Log.e("收到关闭app的命令", "开始关闭app");
//                System.exit(0);
            }
        };
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

    private void TcpConnect() {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (XTClient.channel == null || !XTClient.channel.isActive()) {
                    if (thread != null) {
                        thread.interrupt();
                        Log.e("TCP", "正在重连!!!!!!!!!!!!");
                        if (num > 9) {
                            num = 0;
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    showShortMsg("TCP重连失败,app即将重启!");
                                }
                            });
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
//                            reSetApp();
                        }
                        num++;
                    }
                    Log.e("TCP", "正在连接!!!!!!!!!!!!!!");
                    thread = new XTClient(HomeActivity.this);
                    thread.start();
                }
            }
        }, 1000, 10000);


    }

    private void showShortMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void reSetApp() {
        Intent intent = getPackageManager()
                .getLaunchIntentForPackage(getApplication().getPackageName());
        PendingIntent restartIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 10, restartIntent);
        System.exit(0);
    }

    public void httpCode(int code, String version) {
        try {
            String url1 = "http://" + ReportActivity.reporInstance.URL + "/api/updateVersion";
            RequestBody formBody = new FormBody.Builder()
                    .add("deviceCode", IMEI)
                    .add("versionName", version)
                    .build();

            final Request request = new Request.Builder()
                    .url(url1)
                    .post(formBody)
                    .build();
            OkHttpClient client = new OkHttpClient();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("版本号", "发送失败!!!!!");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d("版本号", "发送成功!!!!!!!");
                }
            });
        } finally {

        }
    }

    Thread thread2 = new Thread(new Runnable() {
        @Override
        public void run() {
            TcpConnect();
        }
    });

    public void switchhttp() {
        switch (state) {
            case -1:
                HttpStep.httpRetained(String.valueOf(StepEnums.FIRST_STEP.getValue()), IMEI);
                break;
            case 1:
                HttpStep.httpRetained(String.valueOf(StepEnums.HEIGHT_STEP.getValue()), IMEI);
                break;
            case 2:
                HttpStep.httpRetained(String.valueOf(StepEnums.WEIGHT_STEP.getValue()), IMEI);
                break;
            case 3:
                HttpStep.httpRetained(String.valueOf(StepEnums.FAT_STEP.getValue()), IMEI);
                break;
            case 4:
                HttpStep.httpRetained(String.valueOf(StepEnums.FACE_STEP.getValue()), IMEI);
                break;
            case 5:
                HttpStep.httpRetained(String.valueOf(StepEnums.FACE_STEP.getValue()), IMEI);
                break;
        }
    }

    //安装辅助app
    Thread thread3 = new Thread(new Runnable() {
        @Override
        public void run() {
            if (getAppList("com.xiaoti.startxiaoti")) return;
            writeApp();
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!getAppList("com.xiaoti.startxiaoti")) return;
                    Intent intent = getPackageManager().getLaunchIntentForPackage("com.xiaoti.startxiaoti");
                    if (intent != null) {
                        intent.putExtra("type", "110");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                    System.gc();
                    cancel();
                }
            }, 1000, 500);
        }
    });
    Thread thread4 = new Thread(new Runnable() {
        @Override
        public void run() {
            setTopApp();
        }
    });


    Thread thread5 = new Thread(new Runnable() {
        @Override
        public void run() {
            String packageName = "com.xiaoti.startthexiaoti";
            if (!getAppList(packageName)) return;
            try {
                execCmd("pm uninstall " + packageName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });

    public static String execCmd(String cmd) throws Exception {
        StringBuilder result = new StringBuilder();

        Process process = null;
        BufferedReader bufrIn = null;
        BufferedReader bufrError = null;

        try {
            // 执行命令, 返回一个子进程对象（命令在子进程中执行）
            process = Runtime.getRuntime().exec("/system/bin/su");

            String cmd1 = cmd + "\n"
                    + "exit\n";
            process.getOutputStream().write(cmd1.getBytes());

            // 方法阻塞, 等待命令执行完成（成功会返回0）
            process.waitFor();

            // 获取命令执行结果, 有两个结果: 正常的输出 和 错误的输出（PS: 子进程的输出就是主进程的输入）
            bufrIn = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            bufrError = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"));

            // 读取输出
            String line = null;
            while ((line = bufrIn.readLine()) != null) {
                result.append(line).append('\n');
            }
            while ((line = bufrError.readLine()) != null) {
                result.append(line).append('\n');
            }

        } finally {
            closeStream(bufrIn);
            closeStream(bufrError);

            // 销毁子进程
            if (process != null) {
                process.destroy();
            }
        }

        // 返回执行结果
        return result.toString();
    }

    private static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (Exception e) {
                // nothing
            }
        }
    }

    private void setTopApp() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                isRunning();
            }
        }, 1000, 5000);
    }

    private void isRunning() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : list) {
            if (processInfo.processName.equals("shared.bc.com.bodyrobot")) {
                if (processInfo.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    Intent intent = this.getPackageManager().getLaunchIntentForPackage("shared.bc.com.bodyrobot");
                    startActivity(intent);

                }
            }
        }
    }

    private void writeApp() {
        InputStream is = null;
        try {
            is = getAssets().open("StartXiaoti.apk");
            File file = new File(Environment.getExternalStorageDirectory(), "StartXiaoti.apk");
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            byte[] temp = new byte[1024];
            int i = 0;
            while ((i = is.read(temp)) > 0) {
                fos.write(temp, 0, i);
            }
            fos.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/StartXiaoti.apk"), "application/vnd.android.package-archive");
        startActivity(intent);
    }

    private boolean getAppList(String pack) {
        PackageManager pm = getPackageManager();
        // Return a List of all packages that are installed on the device.
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        for (PackageInfo packageInfo : packages) {
            // 判断系统/非系统应用
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) // 非系统应用
            {
                if (packageInfo.packageName.equals(pack)) {
                    return true;
                }
            }
        }
        return false;
    }


    Runnable runnableUi = new Runnable() {
        @Override
        public void run() {
//            boolean isPlaying = false;
//            try {
//                isPlaying = mPlayer.isPlaying();
//            }
//            catch (IllegalStateException e) {
//                mPlayer = null;
//                mPlayer = new MediaPlayer();
//            }
//            if(isPlaying) {
//                mPlayer.stop();
//                mPlayer.release();
//            }
            //更新界面
            Start();
        }

    };
    Runnable runnableUi3 = new Runnable() {
        @Override
        public void run() {
            boolean isPlaying = false;
            try {
                isPlaying = mPlayer.isPlaying();
            } catch (IllegalStateException e) {
                mPlayer = null;
                mPlayer = new MediaPlayer();
            } catch (Exception e) {
                mPlayer = null;
                mPlayer = new MediaPlayer();
            }
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
                mPlayer.release();
            }
            //更新界面
            Weight();
        }

    };
    Runnable runnableUi4 = new Runnable() {
        @Override
        public void run() {
            boolean isPlaying = false;
            try {
                isPlaying = mPlayer.isPlaying();
            } catch (IllegalStateException e) {
                mPlayer = null;
                mPlayer = new MediaPlayer();
            } catch (Exception e) {
                mPlayer = null;
                mPlayer = new MediaPlayer();
            }
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
                mPlayer.release();
            }
            //更新界面
            Fat();
        }

    };
    Runnable runnableUi5 = new Runnable() {
        @Override
        public void run() {
            boolean isPlaying = false;
            try {
                isPlaying = mPlayer.isPlaying();
            } catch (IllegalStateException e) {
                mPlayer = null;
                mPlayer = new MediaPlayer();
            } catch (Exception e) {
                mPlayer = null;
                mPlayer = new MediaPlayer();
            }
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
                mPlayer.release();
            }
            //更新界面
            failure();

        }

    };
    //
//    Thread thread3 = new Thread(new Runnable() {
//        @Override
//        public void run() {
//            switchhttp();
//        }
//    });
    Runnable runnableUi6 = new Runnable() {
        @Override
        public void run() {
            boolean isPlaying = false;
            try {
                isPlaying = mPlayer.isPlaying();
            } catch (IllegalStateException e) {
                mPlayer = null;
                mPlayer = new MediaPlayer();
            } catch (Exception e) {
                mPlayer = null;
                mPlayer = new MediaPlayer();
            }
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
                mPlayer.release();
            }
            //更新界面
            Facial();
        }

    };
    Runnable runnableUi7 = new Runnable() {
        @Override
        public void run() {
            //更新界面
//            USBCamera();
            face();
        }

    };
    Runnable runnableUi8 = new Runnable() {
        @Override
        public void run() {
            boolean isPlaying = false;
            try {
                isPlaying = mPlayer.isPlaying();
            } catch (IllegalStateException e) {
                mPlayer = null;
                mPlayer = new MediaPlayer();
            } catch (Exception e) {
                mPlayer = null;
                mPlayer = new MediaPlayer();
            }
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
                mPlayer.release();
            }
            //更新界面
            Calibration();
        }

    };
    Runnable runnableUi9 = new Runnable() {
        @Override
        public void run() {
            //更新界面
            Weight50();
        }

    };
    Runnable runnableUi10 = new Runnable() {
        @Override
        public void run() {
            //更新界面
            Weight100();
        }

    };
    Runnable runnableUi11 = new Runnable() {
        @Override
        public void run() {
            //更新界面
            Weight150();
        }

    };
    Runnable runnableUi12 = new Runnable() {
        @Override
        public void run() {
            //更新界面
            WeightEnd();
        }

    };

    Runnable runnableUi13 = new Runnable() {
        @Override
        public void run() {
            //更新界面
            HeightEnd();
        }

    };

    Runnable runnableUiReport = new Runnable() {
        @Override
        public void run() {
            //更新界面
            toReport();
        }

    };
}