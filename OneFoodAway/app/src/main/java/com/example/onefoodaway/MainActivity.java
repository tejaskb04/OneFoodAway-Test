package com.example.onefoodaway;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.api.geocoding.v5.MapboxGeocoding;

public class MainActivity extends AppCompatActivity {
    private final String API_KEY = "pk.eyJ1IjoidGVqYXNrYjA0IiwiYSI6ImNqNWxmOTE4ZjJ0bGoycW82YXp4OThyMjMifQ.PkokQMomWDhJiz1aq8TuUA";
    private final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100;
    private MapView mapView;
    private int radius = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, API_KEY);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        else {
            final int INTERVAL = 1000;
            final Handler handler = new Handler();
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    mapView.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(MapboxMap mapboxMap) {
                            mapboxMap.getUiSettings().setCompassEnabled(false);
                            mapboxMap.setMyLocationEnabled(true);
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(new LatLng(mapboxMap.getMyLocation()))
                                    .zoom(13)
                                    .bearing(270)
                                    .tilt(20)
                                    .build();
                            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 5000);
                        }
                    });
                }
            };
            handler.postAtTime(runnable, System.currentTimeMillis() + INTERVAL);
            handler.postDelayed(runnable, INTERVAL);
        }
        MapboxGeocoding mapboxGeocoding = new MapboxGeocoding.Builder()
                .setAccessToken(Mapbox.getAccessToken())
                .setLocation("1600 Pennsylvania Ave NW")
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.recenter: {
                mapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(MapboxMap mapboxMap) {
                        mapboxMap.getUiSettings().setCompassEnabled(false);
                        mapboxMap.setMyLocationEnabled(true);
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(new LatLng(mapboxMap.getMyLocation()))
                                .build();
                        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 500);
                    }
                });
                return true;
            }
            case R.id.radius: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Set Radius");
                final EditText input = new EditText(this);
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            radius = Integer.parseInt(input.getText().toString());
                        }
                        catch (NumberFormatException e) {
                            Toast toast = Toast.makeText(MainActivity.this, "Invalid Input", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // STUB
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    final int INTERVAL = 1000;
                    final Handler handler = new Handler();
                    final Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            mapView.getMapAsync(new OnMapReadyCallback() {
                                @Override
                                public void onMapReady(MapboxMap mapboxMap) {
                                    mapboxMap.getUiSettings().setCompassEnabled(false);
                                    mapboxMap.setMyLocationEnabled(true);
                                    CameraPosition cameraPosition = new CameraPosition.Builder()
                                            .target(new LatLng(mapboxMap.getMyLocation()))
                                            .zoom(13)
                                            .bearing(270)
                                            .tilt(20)
                                            .build();
                                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 5000);
                                }
                            });
                        }
                    };
                    handler.postAtTime(runnable, System.currentTimeMillis() + INTERVAL);
                    handler.postDelayed(runnable, INTERVAL);
                } else {
                    // RIP
                }
                return;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
