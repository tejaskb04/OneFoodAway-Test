package com.example.onefoodaway;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.Nullable;
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
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements MapboxMap.OnMyLocationChangeListener {

    private final String API_KEY = "pk.eyJ1IjoidGVqYXNrYjA0IiwiYSI6ImNqNWxmOTE4ZjJ0bGoycW82YXp4OThyMjMifQ.PkokQMomWDhJiz1aq8TuUA";
    private final String GOOGE_PLACES_API_KEY = "AIzaSyAH008n41rXGsO2oYtJgZduebNYwN127_I";
    private final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100;
    private MapView mapView;
    private boolean locationFound = false;
    private int radius = 5000;

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
            while (!locationFound) {
                mapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(MapboxMap mapboxMap) {
                        mapboxMap.setMyLocationEnabled(true);
                        Location location = new Location(mapboxMap.getMyLocation());
                        if (location != null) {
                            locationFound = true;
                        }
                    }
                });
            }
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
                    displayNearbyLocations(mapboxMap.getMyLocation().getLatitude(),
                            mapboxMap.getMyLocation().getLongitude());
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    while (!locationFound) {
                        mapView.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(MapboxMap mapboxMap) {
                                mapboxMap.setMyLocationEnabled(true);
                                Location location = new Location(mapboxMap.getMyLocation());
                                if (location != null) {
                                    locationFound = true;
                                }
                            }
                        });
                    }
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
                            displayNearbyLocations(mapboxMap.getMyLocation().getLatitude(),
                                    mapboxMap.getMyLocation().getLongitude());
                        }
                    });
                } else {
                    // STUB
                }
                return;
            }
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
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(new LatLng(mapboxMap.getMyLocation()))
                                .build();
                        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1500);
                    }
                });
                return true;
            }
            case R.id.radius: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Set Radius"); // Create Custom Title
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

    private void displayNearbyLocations(double lat, double lng) {
        System.out.println("hi");
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
        jsonObjectRequest.setShouldCache(false);
        AppController.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    private void parseUrl(JSONObject data) {
        String name = null;
        String snippet = null;
        double lat, lng;
        try {
            JSONArray jsonArray = data.getJSONArray("results");
            if (data.getString("status").equalsIgnoreCase("OK")) {
                mapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(MapboxMap mapboxMap) {
                        mapboxMap.clear();
                    }
                });
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject place = jsonArray.getJSONObject(i);
                    if (!place.isNull("name")) {
                        name = place.getString("name");
                    }
                    if (!place.isNull("rating") && !place.isNull("price_level")) {
                        snippet = "Rating: " + place.getString("rating") + " " + "Price Level: "
                                + place.getString("price_level");
                    }
                    lat = place.getJSONObject("geometry").getJSONObject("location")
                            .getDouble("lat");
                    lng = place.getJSONObject("geometry").getJSONObject("location")
                            .getDouble("lng");
                    final MarkerViewOptions markerViewOptions = new MarkerViewOptions()
                            .title(name)
                            .snippet(snippet)
                            .position(new LatLng(lat, lng));
                    mapView.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(MapboxMap mapboxMap) {
                            mapboxMap.addMarker(markerViewOptions);
                        }
                    });
                }
            }
        } catch (JSONException e) {
            // Show Error Message
        }
    }

    @Override
    public void onMyLocationChange(@Nullable final Location location) {
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                if (location != null) {
                    mapboxMap.easeCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(),
                            location.getLongitude())));
                }
                displayNearbyLocations(location.getLatitude(), location.getLongitude());
            }
        });
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

/*
TODO:
*/

/*
BUGS:
    1. mapView sometimes loads before user location is found
    2. Camera does not center with user
    3. Changing radius of Nearby Food Locations Search not functional
    4. New markers do not appear when in range and old markers do not disappear when out of range
*/
