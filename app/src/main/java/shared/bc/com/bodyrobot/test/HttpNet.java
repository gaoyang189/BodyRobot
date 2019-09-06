package shared.bc.com.bodyrobot.test;

import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpNet {
    /**
     * 下载apk
     */

    public static void httpDownLoadApk(String url, okhttp3.Callback callback) {
        // 打印日志
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)//单位是秒
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);

    }
}
