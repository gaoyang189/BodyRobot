package shared.bc.com.bodyrobot.receiver;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * 辅助服务自动安装APP，该服务在单独进程中允许
 */
public class AutoInstallService extends AccessibilityService {
    private final static String TAG = "Accessibility";

    private SparseBooleanArray booleanArray = new SparseBooleanArray();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        AccessibilityNodeInfo nodeInfo = event.getSource();
        if (nodeInfo != null) {
            int eventType = event.getEventType();
            if (eventType== AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
                    eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                if (!booleanArray.get(event.getWindowId())) {
                    boolean handled = iterateNodesAndHandle(nodeInfo);
                    if (handled) {
                        booleanArray.put(event.getWindowId(), true);

                    }
            }
            }
        }
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    protected void onServiceConnected() {
        Log.d(TAG, "无障碍服已开启");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "无障碍服务已关闭");
        return super.onUnbind(intent);
    }

    private boolean iterateNodesAndHandle(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo != null) {
            int childCount = nodeInfo.getChildCount();
            if ("android.widget.Button".equals(nodeInfo.getClassName())) {
                String nodeContent = nodeInfo.getText().toString();
                Log.d(TAG, "content is " + nodeContent);
                if ("安装".equals(nodeContent)
                        ||"完成".equals(nodeContent)
                        || "确定".equals(nodeContent)
                        || "打开".equals(nodeContent)) {
                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);

                    /*
                     * 智能安装Apk后，App重置，服务中止，无法自动处理“完成”窗口，
                     * 会在下一次需要升级安装时弹出“完成”窗口，这样导致此次升级无法进行。
                     */
                    if ("完成".equals(nodeContent) || "打开".equals(nodeContent)) {
                        if (callback != null) {
                            callback.invoke();
                        }
                    }
                    return true;
                }
            } else if ("android.widget.ScrollView".equals(nodeInfo.getClassName())) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            }
            for (int i = 0; i < childCount; i++) {
                AccessibilityNodeInfo childNodeInfo = nodeInfo.getChild(i);
                if (iterateNodesAndHandle(childNodeInfo)) {
                    return true;
                }
            }
        }
        return false;
    }

    public interface Callback {
        void invoke();
    }

    public static Callback callback;
}