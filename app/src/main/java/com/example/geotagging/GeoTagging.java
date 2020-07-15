package com.example.geotagging;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Locale;

public class GeoTagging extends AppCompatActivity {
    private static final int REQUEST_LOCATION = 1;
    TextView showLocation;
    LocationManager locationManager;
    Double latitude, longitude;
    Double latitude2, longitude2;
    Button addressButton;
    TextView latLongTV;
    Button distanceButton;
    ImageButton reload;
    TextView distanceTv;
    LinearLayout enterAdd;
    String getcord;
    Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions( this,
                new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        Init();
        loadLocation();
    }

    public void Init(){

        showLocation = findViewById(R.id.showLocation);
        latLongTV = (TextView) findViewById(R.id.getCoordinate);
        addressButton = (Button) findViewById(R.id.btnGetCoordinate);
        distanceTv = (TextView) findViewById(R.id.getDistance);
        distanceButton = (Button) findViewById(R.id.btnGetDistance);
        reload = (ImageButton) findViewById(R.id.reloadBtn);
        enterAdd = (LinearLayout) findViewById(R.id.enterAddress);

        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                loadLocation();
            }
        });

        addressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                EditText editText = (EditText) findViewById(R.id.getAddress);
                String address = editText.getText().toString();
                GeocodingLocation locationAddress = new GeocodingLocation();
                locationAddress.getAddressFromLocation(address,
                        getApplicationContext(), new GeocoderHandler());
            }
        });

        distanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                double distanceinMiles = distance(latitude, longitude, latitude2, longitude2);
                distanceTv.setText(String.format("%.2f", milesToMeters(distanceinMiles)) + " km");
            }
        });
    }
    public void loadLocation(){
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            OnGPS();
        } else {
            getLocation();
        }
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
    public static double milesToMeters(double miles) {
        return miles / 0.00062137;
    }

    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new  DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(
                GeoTagging.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                GeoTagging.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (locationGPS != null) {
                double lat = locationGPS.getLatitude();
                double longi = locationGPS.getLongitude();
                latitude = lat;
                longitude = longi;
                GeocodingAddress locationAddress = new GeocodingAddress();
                locationAddress.getAddressFromLocation(latitude, longitude,
                        getApplicationContext(), new GeocoderHandler2());
                showLocation.setText("Your Coordinates: " + "\n" + "Latitude: " + String.valueOf(latitude) + "\n" + "Longitude: " + String.valueOf(longitude));
                reload.setVisibility(View.GONE);
                enterAdd.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "Unable to find your location.", Toast.LENGTH_SHORT).show();
                reload.setVisibility(View.VISIBLE);
                enterAdd.setVisibility(View.GONE);
            }
        }
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            latLongTV.setText("Coordinates : \n" + locationAddress);
            JSONArray jsonArray = null;
            try {
                jsonArray = new JSONArray(locationAddress);
                String[] strArr = new String[jsonArray.length()];

                latitude2 = Double.valueOf(jsonArray.getString(0));
                longitude2 = Double.valueOf(jsonArray.getString(1));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("stringAddress", String.valueOf(latitude2) + ' ' + String.valueOf(longitude2));
        }
    }
    private class GeocoderHandler2 extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            if (message.what == 1) {
                Bundle bundle = message.getData();
                locationAddress = bundle.getString("address");
            } else {
                locationAddress = null;
            }
            showLocation.append("\n\nYour Location:\n" + locationAddress);
        }
    }
}