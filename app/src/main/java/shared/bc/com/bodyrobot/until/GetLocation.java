package shared.bc.com.bodyrobot.until;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;

/**
 * @author goJee
 * @since 2018/3/22
 */

public class GetLocation {

    private final static String TAG = "Coordinate";

    private static class GetLocationHolder {
        final static GetLocation instance = new GetLocation();
    }

    public static GetLocation getInstance() {
        return GetLocationHolder.instance;
    }

    private GetLocation() {
    }

    private final static long MIN_TIME_INTERVAL = 5 * 60 * 1000L;
    private final static float MIN_DISTANCE = 1;

    private String mProvider;
    private String mCoordinate = "";
    private Criteria mCriteria = new Criteria();
    private boolean hasRequest = false;

    public void registerLocationUpdate(Context context) {
        mCoordinate = coordinate(context);
        if (mCoordinate.isEmpty()) {
            requestLocationUpdates(context);
        }
    }

    public void unregisterLocationUpdate(Context context) {
        if (hasRequest) {
            getLocationManager(context).removeUpdates(mLocationListener);
            hasRequest = false;
        }
    }

    public String getCoordinate() {
        return mCoordinate;
    }

    private String coordinate(Context context) {
        Location location = getBestLocation(context, mCriteria);
        return location == null ? "" : location.getLongitude() + "," + location.getLatitude();
    }

    private Location getBestLocation(Context context, Criteria criteria) {
        Location location;
        LocationManager manager = getLocationManager(context);

        if (criteria == null) {
            criteria = new Criteria();
        }
        mProvider = manager.getBestProvider(criteria, true);

        if (TextUtils.isEmpty(mProvider)) {
            //如果找不到最适合的定位，使用network定位
            mProvider = LocationManager.NETWORK_PROVIDER;
            location = getNetWorkLocation(context);
        } else {
            //高版本的权限检查
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            //获取最适合的定位方式的最后的定位权限
            location = manager.getLastKnownLocation(mProvider);
        }
        return location;
    }

    /**
     * network获取定位方式
     */
    private Location getNetWorkLocation(Context context) {
        Location location = null;
        LocationManager manager = getLocationManager(context);
        //高版本的权限检查
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Coordinate", "need to be granted permission");
            return null;
        }
        if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {//是否支持Network定位
            //获取最后的network定位信息
            location = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        return location;
    }

    private LocationManager getLocationManager(@NonNull Context context) {
        return (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    private void requestLocationUpdates(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocationManager(context).requestLocationUpdates(mProvider, MIN_TIME_INTERVAL, MIN_DISTANCE, mLocationListener);
            hasRequest = true;
        }
    }

    private LocationListener mLocationListener = new LocationListener() {

        // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        // Provider被enable时触发此函数，比如GPS被打开
        @Override
        public void onProviderEnabled(String provider) {

        }

        // Provider被disable时触发此函数，比如GPS被关闭
        @Override
        public void onProviderDisabled(String provider) {

        }

        //当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
        @Override
        public void onLocationChanged(Location location) {
            mCoordinate = location.getLongitude() + "," + location.getLatitude();
            Log.d(TAG, "onLocationChanged: " + mCoordinate);
        }
    };
}
