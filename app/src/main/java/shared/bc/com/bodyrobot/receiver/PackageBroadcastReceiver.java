package shared.bc.com.bodyrobot.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import shared.bc.com.bodyrobot.HomeActivity;


/**
 * @author goJee
 * @since 2018/3/27
 */

public class PackageBroadcastReceiver extends BroadcastReceiver {

    private static final String ACTION = "android.intent.action.PACKAGE_REPLACED";

    @Override
    public void onReceive(Context context, Intent intent) {

            Intent mainActIntent = new Intent(context, HomeActivity.class);
            mainActIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mainActIntent);
    }
}
