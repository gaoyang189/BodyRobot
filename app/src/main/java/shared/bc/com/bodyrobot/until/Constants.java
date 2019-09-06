package shared.bc.com.bodyrobot.until;

/**
 * @author goJee
 * @since 2017/12/5
 */

public class Constants {

    //******************************* HTTP ******************************//

    /**
     * 服务器地址.
     */
    public static String sServer = "http://xmyc28.com/web/CMS/api";
    public static String UrlGetAds;
    public static String UrlMachineNo;
    public static String UrlUpdateMachineStatus;
    public static String UrlMachineConfig;
    public static String UrlQRCodeUrl;
    public static String UrlCheckForUpdates;

    public final static int Post_What_getAds = 1;
    public final static int Post_What_getMachineNo = 2;
    public final static int Post_What_updateMachineStatus = 3;
    public final static int Post_What_getMachineConfig = 4;
    public final static int Post_What_getQRCodeUrl = 5;
    public final static int Post_What_checkForUpdates = 6;

    public final static int Download_What_Apk = 10;

    public final static String Post_Params_machine_sn = "machine_sn";
    public final static String Post_Params_coordinate = "coordinate";
    public final static String Post_Params_status = "status";
    public final static String Post_Params_on_off = "on_off";
    public final static String Post_Params_version = "version";

    public static String UrlDownloadApk = "http://oss.ucdl.pp.uc.cn/fs01/union_pack/Wandoujia_110644_web_direct_binded.apk?x-oss-process=udf/pp-udf,Jjc3LiMnJ3FxdnJ1fnE=";


    //******************************* SharedPreferences ******************************//
    public final static String SharedPref = "SharedPref";
    public final static String SP_IMEI = "IMEI";
    public final static String SP_MachineNo = "MachineNo";
    public final static String SP_Server = "Server";

    public static void config() {
        UrlGetAds = sServer + "/get-ads";
        UrlMachineNo = sServer + "/get-machine-no";
        UrlUpdateMachineStatus = sServer + "/update-machine-status";
        UrlMachineConfig = sServer + "/get-machine-config";
        UrlQRCodeUrl = sServer + "/get-qrcode-url";
        UrlCheckForUpdates = sServer + "/check-for-updates";
    }

    static {
        config();
    }
}
