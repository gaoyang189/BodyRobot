package shared.bc.com.bodyrobot.connect;

import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import shared.bc.com.bodyrobot.ReportActivity;

/**
 * @author goJee
 * @since 2019/5/28
 */
public class HttpStep {
    private static OkHttpClient client = new OkHttpClient();

    public static void httpRetained(String stepEnums,String IMEI){
        try {
            String url2 = "http://"+ ReportActivity.reporInstance.URL+"/api/tzc/step";
            RequestBody formBody = new FormBody.Builder()
                    .add("deviceCode", IMEI)
                    .add("step", stepEnums)
                    .build();

            final Request request = new Request.Builder()
                    .url(url2)
                    .post(formBody)
                    .build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("连接服务器失败","");
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String json = response.body().string();
                        JSONObject jsonObject = new JSONObject(json);
                        String success = jsonObject.getString("success");
                        String message = jsonObject.getString("message");
                        int code = jsonObject.getInt("code");
                        if("true".equals(success)&&"200".equals(code)){
                            Log.d("已收到返回信息",message);
                        }else{
                            Log.d("信息返回失败",message);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        }finally{
        }
    }
}
