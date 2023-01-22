package bjfu.it.yhz.odometer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ServiceConnection connection;
    private OdometerService odometerService;
    private boolean bound = false;
    private final int PERMISSION_REQUEST_CODE = 698;

    private double distance = 0.0;
    private double longitude = 0.0;

    private String time = "";

    Handler handler = new Handler();

    Runnable runable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // ServiceConnection用来监控与服务的连接
        connection = new ServiceConnection() {
            // Android系统创建连接,会调用此方法,IBinder与绑定服务通信
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                OdometerService.OdometerBinder binder = (OdometerService.OdometerBinder) service;
                odometerService = binder.getOdometer();
                odometerService.setDistanceMeters(distance);
                bound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                bound = false;
            }
        };
        displayDistance();
    }


    private void displayDistance(){
        final TextView distanceView = findViewById(R.id.distance);
        final TextView locView = findViewById(R.id.locView);
        final TextView timeView = findViewById(R.id.timeView);
        runable = new Runnable() {
            @Override
            public void run() {
                if(bound && odometerService != null){
                    distance = odometerService.getDistance();
                    Location location = odometerService.getCurrentLocation();
                    if(location != null){
                        longitude = odometerService.getCurrentLocation().getLongitude();
                        time =  getGpsLocalTime( odometerService.getCurrentLocation().getTime() );
                    }
                    Log.e("TTTTTT","bound is yes");
                }else{
                    Log.i("AAAAA","bound is no");
                }
                Log.w("bbbbb",String.valueOf(bound));

                String distanceStr = String.format(Locale.getDefault(),"%1$,.2fM", distance);
                Log.w("distance",distanceStr);
                distanceView.setText(distanceStr);
                String longitudeStr = String.format(Locale.getDefault(),"%1$,.6f ",longitude);
                locView.setText(longitudeStr);
                timeView.setText(time);

                handler.postDelayed(this,1000);
            }
        };
        handler.post(runable);
    }

    @SuppressLint("SimpleDateFormat")
    private static String getGpsLocalTime(long gpsTime){
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(gpsTime);
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String datestring = df.format(calendar.getTime());

        return datestring;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 检查权限
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        }else{
            Intent intent = new Intent(this,OdometerService.class);
            bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }
    }

    public void start(View view){
//        if(ContextCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    PERMISSION_REQUEST_CODE);
//        }else{
//            Intent intent = new Intent(this,OdometerService.class);
//            bindService(intent, connection, Context.BIND_AUTO_CREATE);
//            bound = true;
//        }
        bound = true;
    }

    public void stop(View view){
//        if(bound){
//            unbindService(connection);
//            bound = false;
//        }
        bound = false;
    }


    @Override
    protected void onStop(){
        super.onStop();
        if(bound){
            unbindService(connection);
            bound = false;
        }
        handler.removeCallbacks(runable);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(this, OdometerService.class);
                    bindService(intent, connection, Context.BIND_AUTO_CREATE);
                } else {
                    Toast.makeText(this, "Location permission required", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble("distance",distance);
        outState.putDouble("longitude",longitude);
        outState.putString("time",time);
        Log.e("AAAaaA",String.valueOf(longitude));
        Log.e("BBBBbb",String.valueOf(distance));
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        distance = savedInstanceState.getDouble("distance");
        longitude = savedInstanceState.getDouble("longitude");
        Log.e("RRRRR",String.valueOf(longitude));
        Log.e("RRRRR",String.valueOf(distance));
        time = savedInstanceState.getString("time");
    }
}