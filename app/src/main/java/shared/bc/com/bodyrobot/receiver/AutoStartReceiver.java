package shared.bc.com.bodyrobot.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import shared.bc.com.bodyrobot.HomeActivity;

/**
 * @author goJee
 * @since 2019/4/16
 */
public class AutoStartReceiver extends BroadcastReceiver {

    private static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {

            Intent mainActIntent = new Intent(context, HomeActivity.class);  // 要启动的Activity
            mainActIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mainActIntent);


    }
}
