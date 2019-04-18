package com.example.user.volcanoalarm;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import static android.R.attr.data;
import static java.lang.Math.PI;
import static java.lang.Math.asin;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

/**
 * Created by user on 2017/7/21.
 */

//the helper function relating to requesting nad receiving the data from Azure
public final class QueryUtils {
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();
    private  QueryUtils(){
    }

    //public member
    public static final String MainUrl = "http://114.34.123.174:5000";
    public static final int Volcano = 1;
    public static final int Earthquake = 2;


    //public function
    public static String makeHTTPRequest(String urls) {
        URL url = createUrl(urls);
        String jsonResponse = "";
        if (url == null){
            return jsonResponse;
        }
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try{
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(100000);
            urlConnection.setConnectTimeout(150000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            if (urlConnection.getResponseCode() == 200){
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            }
            else{
                Log.e("MyTag", "Error response code:"+ urlConnection.getResponseCode());
            }
        }
        catch (IOException e ){
            Log.e(LOG_TAG, "Problem retrieving from the earthquake JSON results", e);
        }finally {
            if (urlConnection != null){
                urlConnection.disconnect();
            }
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return jsonResponse;
    }


    public static double getDistance(Double rlat1, Double rlng1, Double rlat2, Double rlng2){
        double lat1 = rlat1 / 180 * PI;
        double lng1 = rlng1 / 180 * PI;
        double lat2 = rlat2 / 180 * PI;
        double lng2 = rlng2 / 180 * PI;

        final Double earthR = 6371d;
        Double dlat = lat2 - lat1, dlng = lng2 - lng1;

        return 2*earthR*asin(sqrt(sin(dlat/2)*sin(dlat/2) + cos(lng1)*cos(lng2)*sin(dlng/2)*sin(dlng/2) ) );
    }

    //private function
    private static String readFromStream(InputStream inputStream) throws IOException{
        StringBuilder output = new StringBuilder();
        if (inputStream != null){
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while ( line !=  null){
                output.append(line);
                line = reader.readLine();
            }
        }
        return  output.toString();
    }

    private  static  URL createUrl (String stringUrl){
        URL url = null;
        try{
            url = new URL(stringUrl);
        }
        catch (MalformedURLException e){
            Log.e(LOG_TAG, "Problem building the URL", e);
        }
        return url;
    }
}

