package shared.bc.com.bodyrobot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import org.opencv.objdetect.CascadeClassifier;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import shared.bc.com.bodyrobot.connect.HttpStep;
import shared.bc.com.bodyrobot.until.Constants;
import shared.bc.com.bodyrobot.until.IMEIUtils;

/**
 * UVCCamera use demo
 * <p>
 * Created by jiangdongguo on 2017/9/30.
 */

public class USBCameraActivity extends AppCompatActivity implements CameraDialog.CameraDialogParent, CameraViewInterface.Callback {
    private static final String TAG = "Debug";
    public static USBCameraActivity instance = null;
    private UVCCameraHelper mCameraHelper = null;
    private CameraViewInterface mUVCCameraView;
    private UVCCameraTextureView cameraView;
    private boolean isRequest;
    private boolean isPreview;
    private SurfaceHolder mSurfaceHolder = null;

    public String IMEI = "IMEI";
    private SharedPreferences sp;

    private TextView debugView;
    private Handler handler = new Handler();
    private ImageView imageView;
    private Button button;
    private ObjectDetector mFaceDetector;

    private CascadeClassifier mCascadeClassifier;

    private ByteArrayOutputStream baos;
    private float[] infomation = new float[3];
    private String picPath;
    private String name;
    private byte[] rawImage;
    private Bitmap bitmap;
    private Boolean debugging;
    public int state;

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
                Log.e("连接的摄像头id",device.getDeviceId()+"");
            }
        }

        @Override
        public void onDisConnectDev(UsbDevice device) {
            showShortMsg("disconnecting");
        }

    };
    private Object Canvas;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        state = 5;
        Intent intent = getIntent();
        infomation = intent.getFloatArrayExtra("information");
        debugging = intent.getBooleanExtra("debug",true);
        instance = this;
        hideNavigation();
        findview();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(state == 5) {
                    state = 0;
                    handler.post(runnableUi2);
                }
            }
        },10000);
    }

    public void findview(){
        setContentView(R.layout.activity_usbcamera);
        mUVCCameraView = findViewById(R.id.camera_view);
        debugView = findViewById(R.id.equipment_debugging_usb);
            face();
    }

    public void face(){
        String s;
        if(debugging == false && infomation[0] !=0 && infomation[1] !=0 && infomation[2] !=0){
            s =  "身高:"+infomation[0]+"cm"+"\n体重:"+infomation[1]+"kg"+"\n生物阻抗:"+infomation[2]+"\n图片编码:";
            debugView.setText(s);
        }else{
            s = "";
            debugView.setText(s);
        }
        mUVCCameraView.setCallback(this);
        mCameraHelper = UVCCameraHelper.getInstance();
        mCameraHelper.initUSBMonitor(this, mUVCCameraView, listener);
        findFace();
    }

    public void findFace(){
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
                Log.e("拍照!", "找到脸部数量:" + findFaceNum);
                if (findFaceNum != 0) {
                    if(state == 5) {
                        state = 0;
                        taskPhoto();
                        Log.e("拍照成功!", "找到脸部数量:" + findFaceNum);
                        handler.post(runnableUi);
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
        int a = mFaceDetector.detectObject(dst,mObject).length;

        src.release();
        temp.release();
        dst.release();

        return a;
    }

    public void taskPhoto(){
        String path = UVCCameraHelper.ROOT_PATH + "/Xiaoti Robot";
        File file = new File(path);
        if(!file.exists()){
            file.mkdirs();
        }
        name = System.currentTimeMillis()+"";
        if (mCameraHelper == null || !mCameraHelper.isCameraOpened()) {
            showShortMsg("sorry,camera open failed");
        }
        String picPath = UVCCameraHelper.ROOT_PATH + "/Xiaoti Robot/" + name
                + UVCCameraHelper.SUFFIX_JPEG;
        mCameraHelper.capturePicture(picPath, new AbstractUVCCameraHandler.OnCaptureListener() {
            @Override
            public void onCaptureResult(String path) {
                Log.e(TAG,"save path：" + path);
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

    @Override
    protected void onStart() {
        super.onStart();
        // step.2 register USB event broadcast
        if (mCameraHelper != null) {
            mCameraHelper.registerUSB();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // step.3 unregister USB event broadcast
        Log.e("USB","断开Camera");
        if (mCameraHelper != null) {
            mCameraHelper.unregisterUSB();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FileUtils.releaseFile();
        // step.4 release uvc camera resources
        if (mCameraHelper != null) {
            mUVCCameraView.DestroyedSurface();
            mCameraHelper.release();
        }
    }

    private void showShortMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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

    public void toReport(){
        Intent intent = new Intent(this, ReportActivity.class);
        intent.putExtra("height", infomation[0]);
        intent.putExtra("weight", infomation[1]);
        intent.putExtra("fat", infomation[2]);
        intent.putExtra("imgName", name);
        intent.putExtra("debug", debugging);
        startActivity(intent);
        finish();
    }

    //缺失页
    public void failure(){
        state =0;
        Intent intent = new Intent(this, FailureActivity.class);
        startActivity(intent);
        initImei();
        HttpStep.httpRetained(String.valueOf(StepEnums.FACE_STEP.getValue()),IMEI);
        finish();

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

        //********** TEST ***********//
//        IMEI = "355099048440815";
        Log.d(TAG, "IMEI: " + IMEI);
    }

    Runnable   runnableUi=new  Runnable(){
        @Override
        public void run() {
            //更新界面
            toReport();
        }

    };

    Runnable   runnableUi2=new  Runnable(){
        @Override
        public void run() {
            //更新界面
            failure();
        }

    };
}