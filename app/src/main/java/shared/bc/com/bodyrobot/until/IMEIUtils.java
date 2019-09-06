package shared.bc.com.bodyrobot.until;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.TypedValue;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author goJee
 * @since 2018/3/21
 */

public class IMEIUtils {

    public static void showToast(Context context, int resId) {
        showToast(context, context.getString(resId));
    }

    public static void showToast(Context context, CharSequence text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void keepScreenBright(Activity activity) {
        int keepScreenOn = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        activity.getWindow().setFlags(keepScreenOn, keepScreenOn);
    }

    public static String getImei(Context context) {
        String imei = "";
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephony == null) {
            System.err.println("telephony == null");
            return imei;
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            System.err.println("android.permission.READ_PHONE_STATE is not PERMISSION_GRANTED");
            return imei;
        }
        try {
            Class<?> telephonyClass = Class.forName(telephony.getClass().getName());
            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getImei = telephonyClass.getMethod("getImei", parameter);
            Object[] objects = new Object[1];
            objects[0] = 0;
            Object obj = getImei.invoke(telephony, objects);
            if (obj != null) {
                imei = obj.toString();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return imei;
    }

    public static float dp2px(Context context, float dpValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpValue, context.getResources().getDisplayMetrics());
    }

    /**
     * 得到SD卡根目录.
     */
    public static File getRootPath(Context context) {
        if (sdCardIsAvailable()) {
            return Environment.getExternalStorageDirectory(); // 取得sdcard文件路径
        } else {
            return context.getFilesDir();
        }
    }

    /**
     * SD卡是否可用.
     */
    public static boolean sdCardIsAvailable() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File sd = new File(Environment.getExternalStorageDirectory().getPath());
            return sd.canWrite();
        } else
            return false;
    }

    public static boolean isApkFile(String filePath) {
        return filePath.endsWith(".apk")
                && new File(filePath).exists();
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        NetworkInfo active = manager.getActiveNetworkInfo();
        return active != null && active.isConnected();
    }

    /**
     * 获取版本号
     */
    public static int getVersionCode(Context context) {
        PackageInfo packageInfo = null;
        try {
            PackageManager pm = context.getPackageManager();
            packageInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return packageInfo != null ? packageInfo.versionCode : -1;
    }

    /**
     * 获取版本号
     */
    public static String getVersionName(Context context) {
        PackageInfo packageInfo = null;
        try {
            PackageManager pm = context.getPackageManager();
            packageInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return packageInfo != null ? packageInfo.versionName : "";
    }

    public static String getPhoneSimSerialNumber(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        StringBuilder sb = new StringBuilder();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            return tm == null ? "" : tm.getSimSerialNumber();
        }
        return "";
    }

}
