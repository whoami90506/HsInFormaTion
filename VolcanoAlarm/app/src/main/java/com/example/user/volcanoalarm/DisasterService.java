package com.example.user.volcanoalarm;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

import static com.example.user.volcanoalarm.QueryUtils.getDistance;
import static java.lang.Math.asin;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.System.currentTimeMillis;

public class DisasterService extends Service {
    //const

    public final int mRequestInterval = 1000;
    public final double mForceInterval = 999999 * 1000;
    public final int mLargestDistance = 500;

    //Request
    double mDuration = 0;

    //UserLocation
    Location mUserLocation;
    Double mUserLat = 25.025914, mUserLng = 121.526604;

    //Disasier
    int mType = 0;
    double mStrength = 4d;
    Double mDisasterLat = 25.17611, mDisasterLng = 121.52138;
    Double mDistance = QueryUtils.getDistance(mUserLat, mUserLng, mDisasterLat, mDisasterLng);

    //muti-thread
    final Runnable mCheckDisasterRunnable = new Runnable() {
        @Override
        public void run() {
            Log.v("MyTag", "DisasterService/mCheckDisasterRunnable/run/init");
            String request = QueryUtils.makeHTTPRequest(QueryUtils.MainUrl + "/trigger");
            Log.v("MyTag", "DisasterService/mCheckDisasterRunnable/run/request len:" + String.valueOf(request.length()) + " content: " + request);

            boolean validRequest = false;
            if (!request.equals("")){
                //get warning
                try {
                    JSONObject obj = new JSONObject(request);
                    mType = obj.getInt("status");
                    if(mType != 0) {
                        mDisasterLat = obj.getDouble("lat");
                        mDisasterLng = obj.getDouble("long");
                        mStrength = obj.getDouble("level");
                        validRequest = true;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.v("MyTag", "DisasterService/mCheckDisasterRunnable/run: fail to read JSON : " + e);
                }

                //get distance && userPosition
                if(validRequest) {
                    Log.v("MyTag", "DisasterService/mCheckDisasterRunnable/run/validRequest");
                    getDeviceLocation();
                    if (mUserLocation != null) {
                        Log.v("MyTag", "DisasterService/mCheckDisasterRunnable/run/getUserLocation");
                        mUserLat = mUserLocation.getLatitude();
                        mUserLng = mUserLocation.getLongitude();
                    }
                    mDistance = getDistance(mUserLat, mUserLng, mDisasterLat, mDisasterLng);
                    Log.v("MyTag", "DisasterService/mCheckDisasterRunnable/run : distance : " + String.valueOf(mDistance));

                    //intent
                    if (mDistance < mLargestDistance) {
                        Log.v("MyTag", "DisasterService/mCheckDisasterRunnable/run : distance < mLargestDistance_" + String.valueOf(mLargestDistance));
                        showAlarm();
                        return;
                    }
                }

            }

            //Force Open
            if(currentTimeMillis() - mDuration > mForceInterval){
                Log.v("MyTag", "DisasterService/mCheckDisasterRunnable/run/Force open");
                mType = 0;
                showAlarm();
                return;
            }

            //keep running
            if(mThreadIsRunning){
                Log.v("MyTag", "DisasterService/mCheckDisasterRunnable/run/keep running");
                mCheckDisasterHandler.postDelayed(this, mRequestInterval);
            }

        }
    };
    HandlerThread mCheckDisasterThread;
    Handler mCheckDisasterHandler;
    boolean mThreadIsRunning = false;

    public void showAlarm(){
        Intent intent = new Intent(DisasterService.this, DisasterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("disasterLat", mDisasterLat);
        intent.putExtra("disasterLng", mDisasterLng);
        intent.putExtra("userLat", mUserLat);
        intent.putExtra("userLng", mUserLng);
        intent.putExtra("type", mType);
        intent.putExtra("strength", mStrength);
        intent.putExtra("distance", mDistance);
        startActivity(intent);

        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("MyTag", "DisasterService/onStartCommand/init");

        int ans = super.onStartCommand(intent, flags, startId);
        mThreadIsRunning = true;
        mDuration = currentTimeMillis();
        mCheckDisasterThread = new HandlerThread("mCheckDisasterRunnable");
        mCheckDisasterThread.start();
        mCheckDisasterHandler = new Handler(mCheckDisasterThread.getLooper());
        mCheckDisasterHandler.post(mCheckDisasterRunnable);
        return ans;
    }

    @Override
    public void onDestroy(){
        Log.v("MyTag", "DisasterService/onDestroy/init");
        mThreadIsRunning = false;
        super.onDestroy();
    }

    private void getDeviceLocation(){
        Log.d("MyTag", "DisasterService/getDeviceLocation: getting the devices current location");

         FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
         boolean locationPermissionsGranted = true;

        try{
            if(locationPermissionsGranted){

                final Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            mUserLocation =  (Location) task.getResult();
                            Log.d("MyTag", "DisasterService/getDeviceLocation/onComplete: found location + lat: " + String.valueOf(mUserLocation.getLatitude()) + " lng: " + String.valueOf(mUserLocation.getLongitude()));

                        }else{
                            Log.d("MyTag", "DisasterService/getDeviceLocation/onComplete: current location is null");
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e("MyTag", "DisasterService/getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }
}
