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

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private final String API_KEY = "pk.eyJ1IjoidGVqYXNrYjA0IiwiYSI6ImNqNWxmOTE4ZjJ0bGoycW82YXp4OThyMjMifQ.PkokQMomWDhJiz1aq8TuUA";
    private final String GOOGE_PLACES_API_KEY = "MY_KEY";
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
    }

    private void displayNearbyLocations(double lat, double lng) {
        StringBuilder stringBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        stringBuilder.append("location=").append(lat).append(",").append(lng);
        stringBuilder.append("&radius=").append(radius);
        stringBuilder.append("&types=").append("bar|cafe|meal_delivery|meal_takeaway|restaurant");
        stringBuilder.append("&sensor=true");
        stringBuilder.append("&key=").append(GOOGE_PLACES_API_KEY);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(stringBuilder.toString(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        parseUrl(jsonObject);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Show Error Message
                    }
                });
    }

    private void parseUrl(JSONObject data) {
        String id = "";
        String placeId = "";
        String name = null;
        String reference = "";
        String icon = "";
        String vicinity = null;
        double lat, lng;
        try {
            JSONArray jsonArray = data.getJSONArray("data");
            if (data.getString("status").equalsIgnoreCase("OK")) {
                mapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(MapboxMap mapboxMap) {
                        mapboxMap.clear();
                    }
                });
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject place = jsonArray.getJSONObject(i);
                    id = place.getString("id");
                    placeId = place.getString("place_id");
                    if (!place.isNull("name")) {
                        name = place.getString("name");
                    }
                    if (!place.isNull("vicinity")) {
                        vicinity = place.getString("vicinity");
                    }
                    lat = place.getJSONObject("geometry").getJSONObject("location")
                            .getDouble("latitude");
                    lng = place.getJSONObject("geometry").getJSONObject("location")
                            .getDouble("longitude");
                    reference = place.getString("reference");
                    icon = place.getString("icon");
                    // Create Marker
                }
            }
        } catch (JSONException e) {
            // Show Error Message
        }
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
                    // STUB
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
