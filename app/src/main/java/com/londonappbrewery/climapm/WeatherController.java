package com.londonappbrewery.climapm;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.*;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class WeatherController extends AppCompatActivity {

    // Constants:
    int RequestCode = 123;
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    //final String WEATHER_URL = "http://api.weatherstack.com/current";
    // App ID to use OpenWeather data
    final String APP_ID = "7197475452b74a474443f858234e7b30";
    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;

    // TODO: Set LOCATION_PROVIDER here:
    String Location_Provider = LocationManager.GPS_PROVIDER;


    // Member Variables:
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;


    // TODO: Declare a LocationManager and a LocationListener here:
    LocationManager mLocationManager;
    LocationListener mLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);
        if (isOnline()) {
            //if Network Available

            // Linking the elements in the layout to Java code
            mCityLabel = (TextView) findViewById(R.id.locationTV);
            mWeatherImage = (ImageView) findViewById(R.id.weatherSymbolIV);
            mTemperatureLabel = (TextView) findViewById(R.id.tempTV);
            ImageButton changeCityButton = (ImageButton) findViewById(R.id.changeCityButton);


            // TODO: Add an OnClickListener to the changeCityButton here:
            changeCityButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent myIntent = new Intent(WeatherController.this, ChangeCity.class);
                    startActivity(myIntent);
                }
            });
        } else {
            try {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(WeatherController.this);

                alertDialog.setTitle("Info");
                alertDialog.setMessage("Internet not available, Please check your internet connectivity and try again");
                alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

                alertDialog.show();
            } catch (Exception e) {
                Log.d("Error", "Show Dialog: " + e.getMessage());
            }
        }


    }

    public boolean isOnline() {
        ConnectivityManager conMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        if (netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()) {
            Toast.makeText(this, "No Internet connection!", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }


    // TODO: Add onResume() here:
    public void onResume() {
        super.onResume();

        Log.d("clima", "ON RESUME() CLICKED");
        Intent bg1 = getIntent();
        String newCity = bg1.getStringExtra("city");
        if (newCity != null) {
            getWeatherForNewCity(newCity);
        } else {
            getWhetherForCurrentLocation();
        }
    }


    // TODO: Add getWeatherForNewCity(String city) here:
    public void getWeatherForNewCity(String city) {
        RequestParams params = new RequestParams();
        params.put("q", city);
        params.put("appid", APP_ID);
        networkCall(params);
    }

    // TODO: Add getWeatherForCurrentLocation() here:
    public void getWhetherForCurrentLocation() {
        Context context;
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {

                Log.d("clima", "onlocationChanged");
                String lat = String.valueOf(location.getLatitude());
                String longitute = String.valueOf(location.getLongitude());
                RequestParams params = new RequestParams();
                params.put("lat", lat);
                params.put("lon", longitute);
                params.put("appid", APP_ID);
                networkCall(params);

//                Log.d("clima", "lat"+ lat);
//                Log.d("clima", "long"+longitute);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("clima", "location disable");

            }

        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, RequestCode);
            }
            return;
        }
        mLocationManager.requestLocationUpdates(Location_Provider, MIN_TIME, MIN_DISTANCE, mLocationListener);

    }

    public void networkCall(RequestParams requestParams) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(WEATHER_URL, requestParams, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("clima", "respone  " + response.toString());
                //parsing of data is done here
                WeatherDataModel whData = WeatherDataModel.fromJson(response);
                updateUI(whData);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject responseString) {
                Log.d("clima", "FAIL RESPONSE " + throwable.toString());
                Toast.makeText(WeatherController.this, "Unknown Location", Toast.LENGTH_SHORT).show();

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RequestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("clima", "Permission Granted");
                getWhetherForCurrentLocation();
            } else
                Log.d("clima", "Permission Not Granted");

        }
    }

// TODO: Add letsDoSomeNetworking(RequestParams params) here:


    // TODO: Add updateUI() here:

    void updateUI(WeatherDataModel wheather) {
        mCityLabel.setText(wheather.getCity());
        mTemperatureLabel.setText(wheather.getTemprature());
        int rsid = getResources().getIdentifier(wheather.getIcon(), "drawable", getPackageName());
        mWeatherImage.setImageResource(rsid);
    }

    // TODO: Add onPause() here:


    @Override
    protected void onPause() {
        super.onPause();
        Log.d("clima", "onpuase() Clicked");
        if (mLocationManager != null)
            mLocationManager.removeUpdates(mLocationListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("clima", "onstop() CLICKED");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("clima", "ONSTART() CLICKED");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("clima", "ONDESTROY() CLICKED");
    }

//    public boolean isConnected() {
//        boolean connected = false;
//        try {
//            ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo nInfo = cm.getActiveNetworkInfo();
//            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
//            return connected;
//        } catch (Exception e) {
//            Log.e("Connectivity Exception", e.getMessage());
//        }
//        return connected;
//    }

}


