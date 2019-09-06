package shared.bc.com.bodyrobot;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

/**
 * Created Zjc by Administrator on 2018/11/11/011.
 */

public class MainActivity extends Activity implements View.OnClickListener {
    private Button mBtnTake;


    private float[] infomation = {100,50,50};
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mBtnTake = (Button) findViewById(R.id.btn_update);
        mBtnTake.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String cmd = "su -c reboot";
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}