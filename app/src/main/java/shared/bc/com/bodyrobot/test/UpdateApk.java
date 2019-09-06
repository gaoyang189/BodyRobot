package shared.bc.com.bodyrobot.test;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import shared.bc.com.bodyrobot.HomeActivity;

import static java.lang.Thread.sleep;

public class UpdateApk {

    private static UpdateBean updateBean = new UpdateBean();
    private static String url0 = "http://server.51xiaoti.com/api/version";
    private static Handler mHandler = new Handler(Looper.getMainLooper());

    public static int UpdateVersion(final Context context) {
        connect(context);

        return 0;
    }
    public static void connect(final Context context){
            final Request request = new Request.Builder()
                    .url(url0)
                    .build();
            OkHttpClient client = new OkHttpClient();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                    Log.d("","");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String json = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        String json0 = jsonObject.getString("result");
                        JSONObject jsonObject0 = new JSONObject(json0);
                        int code = jsonObject0.getInt("versionCode");
                        String ota = jsonObject0.getString("ota");
                        String versionname = jsonObject0.getString("versionName");
                        updateBean.setMessage("更新啦");
                        updateBean.setTitle("立即更新");
                        updateBean.setUrl(ota);
                        updateBean.setVersionCode(code);
                        updateBean.setVersionName(versionname);
                        mHandler.post(new Runnable(){
                            @Override
                            public void run() {
                                int nowCode = getVersionCode(context);//手机端的版本
                                int newCode = updateBean.getVersionCode();
                                if (nowCode < newCode) {//小于最新版本号
                                    checkPermission(context);
                                } else {
                                    Log.e("MA", "已经是最新版本");
//            ToastUtils.showMessage("已经是最新的版本");
                                }
                                Log.d("","");
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
    }

    public static void checkPermission(final Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions((HomeActivity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1000);
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.REQUEST_INSTALL_PACKAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                //申请WRITE_EXTERNAL_STORAGE权限
                ActivityCompat.requestPermissions((HomeActivity) context, new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES},
                        1000);
                return;
            }
            return;
        } else {
//            showUpdateDialog(context,updateBean);
            downFile(updateBean.getUrl(), context);
        }
    }

    public static void showUpdateDialog(final Context context, final UpdateBean updateBean) {
        final UpdateDialog updateDialog = new UpdateDialog(context);
        updateDialog.setData(updateBean, true, new UpdateDialogOperate() {
            @Override
            public void executeCancel(String text) {
                updateDialog.cancel();
            }

            @Override
            public void executeCommit(String text) {
                downFile(updateBean.getUrl(), context);
                updateDialog.cancel();
            }
        });
        updateDialog.show();
    }

    //下载进度
    private static ProgressDialog progressDialog;

    public static void downFile(final String url, final Context context) {
        try {
            progressDialog = new ProgressDialog(context);    //进度条，在下载的时候实时更新进度，提高用户友好度
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setTitle("正在下载");
            progressDialog.setMessage("请稍后。。。");
            progressDialog.setProgress(0);
            progressDialog.setCancelable(false);//点击返回键，禁止退出
            progressDialog.show();
        }catch(Exception e){
            e.printStackTrace();
        }

        HttpNet.httpDownLoadApk(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //下载失败
                downFial(context);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //下载。。。
                InputStream is = null;//输入流
                FileOutputStream fos = null;//输出流
                try {
                    is = response.body().byteStream();//获取输入流
                    long total = response.body().contentLength();//获取文件大小
                    setMax(total, context);//为progressDialog设置大小
                    File file = null;
                    if (is != null) {
                        file = new File(Environment.getExternalStorageDirectory(), "app-release.apk");// 设置路径
                        fos = new FileOutputStream(file);
                        byte[] buf = new byte[1024];
                        int ch = -1;
                        int process = 0;
                        while ((ch = is.read(buf)) != -1) {
                            fos.write(buf, 0, ch);
                            process += ch;
                            downLoading(process, context);       //这里就是关键的实时更新进度了！
                        }
                    }
                    fos.flush();
                    // 下载完成
                    if (fos != null) {
                        fos.close();
                    }
                    sleep(1000);
                    downSuccess(context, file);
                } catch (Exception e) {
                    e.printStackTrace();
                    downFial(context);
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                    }
                }
            }
        });

    }

    /**
     * 进度条实时更新
     *
     * @param i
     */
    public static void downLoading(final int i, Context context) {
        ((HomeActivity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.setProgress(i);
            }
        });
    }

    public static void downSuccess(final Context context, final File file) {
        //安装
        ((HomeActivity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                try {
                    sleep(1000);
                    installApk(file, context);

//                    Reboot();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public static void installApk(File file, Context context) {

        Intent intent = new Intent();
        //执行动作
        intent.setAction(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT > 23) { //判读版本是否在7.0以上
            //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
            Uri apkUri =
                    FileProvider.getUriForFile(context, "com.ma.update.fileprovider", file);
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
//      Attempt to invoke virtual   android.content.res.XmlResourceParser
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file),
                    "application/vnd.android.package-archive");
        }
        //执行的数据类型
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

    }
    public static void Reboot(){
        String cmd = "su -c reboot";
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void downFial(final Context context) {
        ((HomeActivity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.cancel();
                Toast.makeText(context, "更新失败", Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void setMax(final long total, Context context) {
        ((HomeActivity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.setMax((int) total);
            }
        });
    }

    public static int getVersionCode(Context context) {
        int versionCode = 0;
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            versionCode = packageInfo.versionCode;
            return versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }


}
