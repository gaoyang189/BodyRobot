package shared.bc.com.bodyrobot;

import android.app.Application;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.tencent.bugly.crashreport.CrashReport;
import com.yanzhenjie.nohttp.NoHttp;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

import android_serialport_api.SerialPort;
import android_serialport_api.sample.SerialApplication;
import shared.bc.com.bodyrobot.until.CrashHandler;


/**
 * @author goJee
 * @since 2017/12/5
 */

public class MyApplication extends Application implements SerialApplication {

    private CrashHandler mCrashHandler;
    @Override
    public void onCreate() {
//        mCrashHandler = CrashHandler.getInstance();
//        mCrashHandler.init(getApplicationContext(), getClass());
        configNoHttp();
        configLogger();
        configBugly();
        super.onCreate();
    }

    private void configNoHttp() {
        com.yanzhenjie.nohttp.Logger.setDebug(false);
        NoHttp.Config config = new NoHttp.Config();
        config.setConnectTimeout(4 * 1000);
        config.setReadTimeout(4 * 1000);
        NoHttp.initialize(this, config); // NoHttp默认初始化。
    }

    private void configLogger() {
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(false)  // (Optional) Whether to show thread info or not. Default true
                .methodCount(0)         // (Optional) How many method line to show. Default 2
                .methodOffset(3)        // (Optional) Skips some method in/system/bin/suvokes in stack trace. Default 5
//                .logStrategy(customLog) // (Optional) Changes the log strategy to print out. Default LogCat
                .tag("BodyRobot")   // (Optional) Custom tag for each log. Default PRETTY_LOGGER
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy) {
            @Override
            public boolean isLoggable(int priority, String tag) {
                return BuildConfig.DEBUG;
            }
        });
        Logger.addLogAdapter(new DiskLogAdapter());
    }
    private void configBugly() {
        CrashReport.initCrashReport(getApplicationContext(), "3e3c5f6f99", false);
    }
    private SerialPort mSerialPort = null;

    @Override
    public SerialPort getSerialPort(String path) throws SecurityException, IOException, InvalidParameterException {
        if (mSerialPort == null) {
            /* Read serial port parameters */
//            String path;
            int baudrate;
//            path = "/dev/ttyS2";
            baudrate = 9600;
            /* Check parameters */
            if ((path.length() == 0) || (baudrate == -1)) {
                throw new InvalidParameterException();
            }

            /* Open the serial port */
            mSerialPort = new SerialPort(new File(path), baudrate, 0);

        }
        return mSerialPort;
    }

    @Override
    public  void changeSerialPort(){
        mSerialPort = null;
    }

    @Override
    public void closeSerialPort() {
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }
}
