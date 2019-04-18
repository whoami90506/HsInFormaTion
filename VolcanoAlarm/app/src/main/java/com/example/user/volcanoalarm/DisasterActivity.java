package com.example.user.volcanoalarm;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormat;

public class DisasterActivity extends AppCompatActivity {

    //const
    public final DecimalFormat mFormat = new DecimalFormat("##########.#");

    //View
    Button nextButton;
    TextView alarmText, distanceText, strengthText;

    //intent
    Double mDisasterLat = 25.194298, mDisasterLng = 121.560478;
    Double mUserLat = 25.025914, mUserLng = 121.526604;
    Double mDistance = 0.0;
    int mType;
    Double mStrength = 4d;

    //Location
    LocationManager mLocationManager;
    LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.v("MyTag", "DisasterActivity/mLocationListener/onLocationChanged");
            mUserLat = location.getLatitude();
            mUserLng = location.getLongitude();
            mDistance = QueryUtils.getDistance(mUserLat, mUserLng, mDisasterLat, mDisasterLng);
            String temp = getResources().getString(R.string.distance) + " " + String.valueOf(mFormat.format(mDistance)) + getResources().getString(R.string.km);
            distanceText.setText(temp);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    //Vibration
    Vibrator mVibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_disaster);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) actionbar.hide();

        //receive data
        Intent intent = getIntent();
        mDisasterLat = intent.getDoubleExtra("disasterLat", 0);
        mDisasterLng = intent.getDoubleExtra("disasterLng", 0);
        mUserLat = intent.getDoubleExtra("userLat", 0);
        mUserLng = intent.getDoubleExtra("userLng", 0);
        mDistance = intent.getDoubleExtra("distance", 0);
        mType = intent.getIntExtra("type", 0);
        mStrength = intent.getDoubleExtra("strength", 0d);

        //view
        alarmText = (TextView)findViewById(R.id.disaster_alarm_textView);
        distanceText = (TextView)findViewById(R.id.disaster_distance_textView);
        strengthText = (TextView)findViewById(R.id.disaster_strength_textView);
        nextButton = (Button)findViewById(R.id.disaster_next_button);

        String temp = getResources().getString(R.string.distance) + " " + String.valueOf(mFormat.format(mDistance)) + getResources().getString(R.string.km);
        distanceText.setText(temp);
        switch(mType){
            case(QueryUtils.Volcano):
                alarmText.setText(R.string.volcanoAlarm);
                temp = getResources().getString(R.string.strength) + ": " + String.valueOf(mStrength);
                strengthText.setText(temp);

                ((ConstraintLayout)findViewById(R.id.background)).setBackgroundColor(Color.argb(255,255,63,63));
                alarmText.setTextColor(Color.WHITE);
                distanceText.setTextColor(Color.WHITE);
                strengthText.setTextColor(Color.WHITE);
                break;

            case(QueryUtils.Earthquake):
                alarmText.setText(R.string.earthquakeAlarm);
                temp = getResources().getString(R.string.magnitude) + ": " + String.valueOf(mStrength);
                strengthText.setText(temp);

                ((ConstraintLayout)findViewById(R.id.background)).setBackgroundColor(Color.argb(255,255,255-63,0));
                alarmText.setTextColor(Color.BLACK);
                distanceText.setTextColor(Color.BLACK);
                strengthText.setTextColor(Color.BLACK);
                break;

            default:
                Log.v("MyTagError", "DisasterActivity/onCreate/view/wrong mType: " + mType);
                alarmText.setText(R.string.Alarm);
                temp = getResources().getString(R.string.strength) + ": " + String.valueOf(mStrength);
                strengthText.setText(temp);

                ((ConstraintLayout)findViewById(R.id.background)).setBackgroundColor(Color.BLACK);
                alarmText.setTextColor(Color.WHITE);
                distanceText.setTextColor(Color.WHITE);
                strengthText.setTextColor(Color.WHITE);
        }
        Typeface type = Typeface.createFromAsset(getResources().getAssets(),"fonts/microsoft.ttf");
        alarmText.setTypeface(type);
        distanceText.setTypeface(type);
        strengthText.setTypeface(type);
        nextButton.setTypeface(type);


        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(DisasterActivity.this, MainActivity.class);
                startActivity(intent1);
                DisasterActivity.this.finish();
            }
        });


        //location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(mLocationManager.GPS_PROVIDER, 0, 0, mLocationListener);

        //vibration
        mVibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
        mVibrator.vibrate(5000);

        //Media
        MediaPlayer m = MediaPlayer.create(DisasterActivity.this, R.raw.warning_sound);
        m.start();
    }

    @Override
    public void onPause() {
        mLocationManager.removeUpdates(mLocationListener);
        super.onPause();
    }

    public void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }
        mLocationManager.requestLocationUpdates(mLocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
    }

    @Override
    public void onDestroy() {
        mLocationManager.removeUpdates(mLocationListener);
        super.onDestroy();
    }
}
