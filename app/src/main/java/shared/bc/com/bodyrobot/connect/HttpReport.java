package shared.bc.com.bodyrobot.connect;

import android.util.Log;

import com.jiangdg.usbcamera.UVCCameraHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import shared.bc.com.bodyrobot.model.Report;

/**
 * @author goJee
 * @since 2019/7/3
 */
public class HttpReport {

    private static HttpReport httpReport;

    public static HttpReport getInstance() {
        if (httpReport == null) {
            httpReport = new HttpReport();
        }
        return httpReport;
    }

    public void httpUploadPictures(Report report,String url){
        String url1 = "http://"+url+"/api/report/v1";
        String imagePath = UVCCameraHelper.ROOT_PATH + "/Xiaoti Robot/"+report.getImg()+".jpg";
        File file = new File(imagePath);
        RequestBody image = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("height",String.valueOf(report.getHeight()))
                .addFormDataPart("weight", String.valueOf(report.getWeight()))
                .addFormDataPart("impedance", String.valueOf(report.getImpedance()))
                .addFormDataPart("file", imagePath, image)
                .addFormDataPart("deviceCode",report.getDeviceCode())
                .build();

        final Request request = new Request.Builder()
                .url(url1)
                .post(requestBody)
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


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }
}
