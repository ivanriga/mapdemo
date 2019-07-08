package com.example.mapdemo;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;

    // The entry points to the Places API.
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;
    private boolean mInternetPermissionGranted;
    private boolean mSaveTraceLocation;
  //  private boolean mLocationReady;
    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // Used for selecting the current place.
//    private static final int M_MAX_ENTRIES = 5;
//    private String[] mLikelyPlaceNames;
//    private String[] mLikelyPlaceAddresses;
//    private String[] mLikelyPlaceAttributions;
//    private LatLng[] mLikelyPlaceLatLngs;
//
//    private long ChangedTime;
    private long ChangedGPSTime = 1000;

    private Menu mmenu;
    ArrayList serverPoints = new ArrayList();
 //   ArrayList tracePoints = new ArrayList();
    private String serverIP = "http://mapdemo.atwebpages.com";

    PolylineOptions lineTraceOptions = new PolylineOptions();
    MarkerOptions optionsCurrent = new MarkerOptions();
    LatLng latLon_current ;
    private boolean mServerRoute;
    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {

            for (Location location : locationResult.getLocations()) {
                SetLocationResult(location);

            }
        };

    };
    private Marker markerCurrent;
    private Polyline polylineTrace;
    private Polyline ServerPolyLine;
    private Marker markerServer;
    private int iLasServerId=0;
    private boolean mServerLocation;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        serverIP = sp.getString("ip_text", "http://mapdemo.atwebpages.com");

    }

    @Override
    protected void onResume() {
        super.onResume();
        markerServer=null;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        serverIP = sp.getString("ip_text", "http://mapdemo.atwebpages.com");
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    /**
     * Sets up the options menu.
     * @param menu The options menu.
     * @return Boolean.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.current_place_menu, menu);
        mmenu = menu;
        return true;
    }

    /**
     * Handles a click on the menu option to get a place.
     * @param item The menu item to handle.
     * @return Boolean.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.option_start_trace) {
            mSaveTraceLocation = true;

            item.setVisible(false);
            MenuItem itemStop = mmenu.findItem(R.id.option_stop_trace);
            itemStop.setVisible(true);
            Toast.makeText(this,"Trace start",Toast.LENGTH_SHORT).show();
      //
        }
        if (id == R.id.option_stop_trace) {
            mSaveTraceLocation = false;
            item.setVisible(false);
            MenuItem itemStart = mmenu.findItem(R.id.option_start_trace);
            Toast.makeText(this,"Trace stop",Toast.LENGTH_SHORT).show();
            itemStart.setVisible(true);
        }
        if (id == R.id.option_show_server) {
            item.setVisible(false);
            mServerRoute = true;
            MenuItem itemAlt = mmenu.findItem(R.id.option_hide_server);
            itemAlt.setVisible(true);
            Toast.makeText(this,"Server  start",Toast.LENGTH_SHORT).show();
            StartTrace();    GetServerValues();
        }
        if (id == R.id.option_hide_server) {
            item.setVisible(false);
            mServerRoute = false;
            if (markerServer!=null) {

                markerServer = null;
                iLasServerId=0;
            }
            MenuItem itemAlt = mmenu.findItem(R.id.option_show_server);
            itemAlt.setVisible(true);
            Toast.makeText(this,"Server  stop",Toast.LENGTH_SHORT).show();
        }
        if (id == R.id.action_settings) {
            Intent i = new Intent(MapsActivity.this, SettingsActivity.class);
            startActivity(i);
            return true;
        }
        return true;
    }
    private void StartTrace() {
        // mMap.clear();
        mServerLocation = true;
        Timer waitingGPSTimer = new Timer();

        waitingGPSTimer.schedule(new TimerTask() {


            public void run() {
                if (mServerLocation && mServerRoute) {

                    GetServerValues();
                }
            }
        }, ChangedGPSTime, ChangedGPSTime);

    }
    private void GetServerValues() {
        mServerLocation=false;
        OkHttpClient client = new OkHttpClient();
        String url = serverIP + "/get_positions.php?user_id=1&id="+iLasServerId;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        final String myResponce = response.body().string();

                        MapsActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                FillRouteOnMap(myResponce);

                            }
                        });


                    }
                    else
                    {
                        mServerLocation=true;
                    }
                } catch (Exception ex1) {
                    ex1.printStackTrace();
                }
            }
        });
    }

    private void FillRouteOnMap(String myResponce) {
        JSONArray jsonArr = null;
        try {
            jsonArr = new JSONArray(myResponce);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (jsonArr.length()>0) {
            serverPoints.clear();
            double lan = 0;
            double lon = 0;
            // for (int i = 0; i < jsonArr.length(); i++) {
            for (int i = jsonArr.length() - 1; i >= 0; i--) {
                JSONObject jsonObj = null;
                try {
                    jsonObj = jsonArr.getJSONObject(i);
                    lan = jsonObj.getDouble("latitude");
                    lon = jsonObj.getDouble("longitude");
                    LatLng point = new LatLng(lan, lon);
                    iLasServerId = jsonObj.getInt("id");
                    serverPoints.add(point);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
            PolylineOptions lineOptions = new PolylineOptions();


            if (ServerPolyLine == null) {
                lineOptions.addAll(serverPoints);
                lineOptions.width(2);
                lineOptions.color(Color.GREEN);
                lineOptions.geodesic(true);

                ServerPolyLine = mMap.addPolyline(lineOptions);
            }
            else
            {
                List<LatLng> _list = ServerPolyLine.getPoints();
                _list.addAll(serverPoints);
                ServerPolyLine.setPoints(_list);
            }
            LatLng serverLast = new LatLng(lan, lon);
            if (markerServer == null) {
                MarkerOptions optionsServerMarker = new MarkerOptions();

                // Setting the position of the marker
                optionsServerMarker.position(serverLast);


                optionsServerMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.s_marker));


                // Add new marker to the Google Map Android API V2
                markerServer = mMap.addMarker(optionsServerMarker);
            } else {
                markerServer.setPosition(serverLast);
            }
        }
        mServerLocation=true;
    }
    public void requestLocationUpdates() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(ChangedGPSTime);
        mLocationRequest.setFastestInterval(ChangedGPSTime);
     //   mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        // Prompt the user for permission.
        getLocationPermission();
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
        requestLocationUpdates();
    }
    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {

            if (mLocationPermissionGranted) {
           //     mMap.setMyLocationEnabled(false);
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();

                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {

                        if (task.isSuccessful()) {

                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();



                            // Setting the position of the marker

                            LatLng latLon_current = new LatLng(mLastKnownLocation.getLatitude(),  mLastKnownLocation.getLongitude());

                          if (  lineTraceOptions.getPoints().isEmpty())
                          {
                              optionsCurrent.icon(BitmapDescriptorFactory.fromResource(R.drawable.g_marker));
                              optionsCurrent.position( latLon_current);
                              optionsCurrent.visible(true);
                               markerCurrent =        mMap.addMarker(optionsCurrent);

                              lineTraceOptions.add(latLon_current);
                              lineTraceOptions.width(6);
                              lineTraceOptions.color(Color.BLUE);
                              lineTraceOptions.geodesic(true);
                              polylineTrace = mMap.addPolyline(lineTraceOptions);

                          }
                           else {

                              optionsCurrent.position(latLon_current);


                              lineTraceOptions.add(latLon_current);
                          }
                         //   mMap.clear();

                         mMap. moveCamera(CameraUpdateFactory.newLatLng(latLon_current));

                          mMap.animateCamera( CameraUpdateFactory.zoomTo( DEFAULT_ZOOM) );

                         //   mMap.addMarker(optionsCurrent);


                         //  mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                               //     new LatLng(mLastKnownLocation.getLatitude(),
                                     //       mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));

                      //     UploadCurrentPosition(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude());
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                        //    mMap.moveCamera(CameraUpdateFactory
                               //     .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));

                        }
                    }
                });
            }
        } catch (SecurityException e)  {

            Log.e("Exception: %s", e.getMessage());
        }
        finally {
           //
        }
    }

    /**
     * Upload to server current coordinates
     * @param latitude
     * @param longitude
     */
    private void UploadCurrentPosition(double latitude, double longitude) {
    //    mLocationReady=false;
        OkHttpClient client = new OkHttpClient();
        String url = serverIP+"/upload_position.php?latitude="+latitude
                +"&longitude="+longitude+"&user_id=1";

            Request request = new Request.Builder()
                    .url(url)
                    .build();

           client.newCall(request).enqueue(new Callback() {

               @Override
               public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    e.printStackTrace();
                   //mLocationReady=true;
               }

               @Override
               public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()){
                    String myResponce = response.body().string();
                //    Toast.makeText(MapsActivity.this.getApplicationContext(),myResponce,Toast.LENGTH_LONG).show();

                }
                //   mLocationReady=true;
               }
           });

    }

    /**
     * work on new location
     * @param location
     */
    private void SetLocationResult(Location location) {
        Log.i("MainActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
        LatLng latLon_current = new LatLng(location.getLatitude(),  location.getLongitude());
        markerCurrent.setPosition(latLon_current);
        lineTraceOptions.add(latLon_current);
        List<LatLng> curr_list = polylineTrace.getPoints();
        curr_list.add(latLon_current);
        polylineTrace.setPoints(curr_list);
     //   mMap. moveCamera(CameraUpdateFactory.newLatLng(latLon_current));
        if ( mSaveTraceLocation) {
            UploadCurrentPosition(location.getLatitude(), location.getLongitude());
        }
    }
    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }



    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                mLocationPermissionGranted = false;
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
                updateLocationUI();
            }
        }

    }
       /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
              //  mMap.setMyLocationEnabled(true);
              //  mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
}
