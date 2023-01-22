package bjfu.it.yhz.odometer;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModel;


public class OdometerService extends Service{

    /**
     * 内部类Binder
     * Binder对象定义编程接口供客户端与服务进行交互，如获得OdometerService的引用
     */
    public class OdometerBinder extends Binder {
        OdometerService getOdometer() {
            return OdometerService.this;
        }
    }

    private IBinder binder = new OdometerBinder();
    private double distanceMeters = 0.0;
    private LocationManager locManager;
    private LocationListener locListener;
    private Location lastLocation;

    @Override
    public void onCreate() {
        super.onCreate();

        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String provider = locManager.getBestProvider(new Criteria(), true);
        locListener = new LocationListener() {

            @Override
            public void onLocationChanged(@NonNull Location location) {
                if(lastLocation == null){
                    lastLocation = location;
                }
                distanceMeters += location.distanceTo(lastLocation);
                lastLocation = location;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderDisabled(@NonNull String provider) {}
            @Override
            public void onProviderEnabled(@NonNull String provider) {}
        };

        // 检查权限
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locManager.requestLocationUpdates(provider,1000,1,locListener);
        }
    }


    public Location getCurrentLocation(){
        return lastLocation;
    }

    private boolean isGpsAble(LocationManager lm){
        return lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)?true:false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locManager != null && locListener != null){
            locManager.removeUpdates(locListener);
        }
        locManager = null;
        locListener = null;
    }

    // 回调方法 Return the communication channel to the service.
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public double getDistance(){
        return distanceMeters;
    }

    public void setDistanceMeters(double distanceMeters){
        this.distanceMeters = distanceMeters;
    }
}