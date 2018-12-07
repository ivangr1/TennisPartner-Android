package com.ivangr.tennispartner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.bitmap.BitmapDrawableResource;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataApi;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.ivangr.tennispartner.helper.FilterDialog;
import com.ivangr.tennispartner.helper.Helper;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.imperiumlabs.geofirestore.GeoQuery;
import org.imperiumlabs.geofirestore.GeoQueryDataEventListener;

import java.util.HashMap;
import java.util.Map;

public class CourtActivity extends AppCompatActivity implements OnMapReadyCallback,
        GeoQueryDataEventListener, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnInfoWindowLongClickListener {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 86;
    private static final String TAG = CourtActivity.class.getSimpleName();
    private static final int PLACE_PICKER_REQUEST = 1;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;

    /**
     * Customizing the info window contents.
     */
    /*class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private GeoDataClient mGeoDataClient;
        private final View mContents;
        private final TextView addressUi;
        private final TextView phoneUi;
        private final TextView websiteUi;
        private final TextView attributionsUi;

        CustomInfoWindowAdapter() {
            mContents = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
            addressUi = ((TextView) mContents.findViewById(R.id.custom_info_address));
            phoneUi = ((TextView) mContents.findViewById(R.id.custom_info_phone));
            websiteUi = ((TextView) mContents.findViewById(R.id.custom_info_website));
            attributionsUi = ((TextView) mContents.findViewById(R.id.custom_info_attributions));
            mGeoDataClient = Places.getGeoDataClient(getApplicationContext());
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            String title = marker.getTitle();
            TextView titleUi = ((TextView) mContents.findViewById(R.id.custom_info_title));
            titleUi.setText((title != null) ? title : "");

            mGeoDataClient.getPlaceById(marker.getSnippet()).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                @Override
                public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                    if (task.isSuccessful()) {
                        PlaceBufferResponse places = task.getResult();
                        Place myPlace = places.get(0);

                        addressUi.setText((myPlace.getAddress() != null) ? myPlace.getAddress() : "");
                        phoneUi.setText((myPlace.getPhoneNumber() != null) ? myPlace.getPhoneNumber() : "");
                        websiteUi.setText((myPlace.getWebsiteUri() != null) ? myPlace.getWebsiteUri().toString() : "");
                        attributionsUi.setText((myPlace.getAttributions() != null) ? myPlace.getAttributions() : "");

                        Log.i(TAG, "Place found: " + myPlace.getName());
                        places.release();
                        return mContents;
                    } else {
                        Log.e(TAG, "Place not found.");
                    }
                }
            });
        }


        private void render(Marker marker, View view) {
            String title = marker.getTitle();
            TextView titleUi = ((TextView) view.findViewById(R.id.custom_info_title));
            titleUi.setText((title != null) ? title : "");

            mGeoDataClient.getPlaceById(marker.getSnippet()).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                @Override
                public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                    if (task.isSuccessful()) {
                        PlaceBufferResponse places = task.getResult();
                        Place myPlace = places.get(0);

                        addressUi.setText((myPlace.getAddress() != null) ? myPlace.getAddress() : "");
                        phoneUi.setText((myPlace.getPhoneNumber() != null) ? myPlace.getPhoneNumber() : "");
                        websiteUi.setText((myPlace.getWebsiteUri() != null) ? myPlace.getWebsiteUri().toString() : "");
                        attributionsUi.setText((myPlace.getAttributions() != null) ? myPlace.getAttributions() : "");

                        Log.i(TAG, "Place found: " + myPlace.getName());
                        places.release();
                    } else {
                        Log.e(TAG, "Place not found.");
                    }
                }
            });
        }
    }*/
    private static final float DEFAULT_ZOOM = 13f;

    private double radius = 20;
    private GeoFirestore geoFirestore;

    private CollectionReference geoFirestoreRef;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_court);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        geoFirestoreRef = firestore.collection("courts");
        geoFirestore = new GeoFirestore(geoFirestoreRef);

        radius = getResources().getInteger(R.integer.radius_default);

        if (getCallingActivity() != null) getSupportActionBar().setTitle(R.string.court_title);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnInfoWindowLongClickListener(this);

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
    }


    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                mFusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        if (location != null) {
                                            // Set the map's camera position to the current location of the device.
                                            mLastKnownLocation = location;
                                            latitude = mLastKnownLocation.getLatitude();
                                            longitude = mLastKnownLocation.getLongitude();
                                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                                    new LatLng(latitude, longitude), DEFAULT_ZOOM));

                                            GeoQuery geoQuery = geoFirestore.queryAtLocation(new GeoPoint(latitude, longitude), radius);
                                            geoQuery.removeAllListeners();
                                            geoQuery.addGeoQueryDataEventListener(CourtActivity.this);
                                        } else {
                                            Log.d(TAG, "Current location is null. Using defaults.");
                                            Toast.makeText(CourtActivity.this, "Cannot get device location", Toast.LENGTH_SHORT).show();
                                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0, 0), 0));
                                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                                        }
                                    }
                                }
                        );
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (Helper.checkLocationPermission(this)) {
            mLocationPermissionGranted = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.court_main, menu);
        if (currentUser == null) menu.findItem(R.id.court_add).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.court_add:
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
                return true;

            case R.id.court_map_style_normal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;

            case R.id.court_map_style_hybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;

            default:
                // If we got here, the currentUser's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                savePlace(place);
            }
        }
    }

    private void savePlace(Place place) {
        if (mMap == null) {
            return;
        }
        Map<String, Object> court = new HashMap<>();
        court.put("name", place.getName());
        //court.put("address", place.getAddress());
        court.put("phone", place.getPhoneNumber());
        //court.put("attributions", place.getAttributions());
        LatLng latlng = place.getLatLng();

        firestore.collection("courts").document(place.getId())
                .set(court)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        geoFirestore.setLocation(place.getId(), new GeoPoint(latlng.latitude, latlng.longitude));
                        Snackbar.make(findViewById(R.id.map), R.string.court_succesfully_added, Snackbar.LENGTH_LONG);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    @Override
    public void onDocumentEntered(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {
        mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.tennis_court_marker))
                .title(documentSnapshot.getString("name"))
                .snippet((documentSnapshot.getString("phone") != null) ? documentSnapshot.getString("phone") : "")
                .position(new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude())));
    }

    @Override
    public void onDocumentExited(DocumentSnapshot documentSnapshot) {

    }

    @Override
    public void onDocumentMoved(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {

    }

    @Override
    public void onDocumentChanged(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {

    }

    @Override
    public void onGeoQueryReady() {

    }

    @Override
    public void onGeoQueryError(Exception e) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (getCallingActivity() != null) {
            int[] dateTime = getIntent().getIntArrayExtra("dateTime");
            Intent intent = new Intent();
            intent.putExtra("dateTime", dateTime);
            intent.putExtra("court", marker.getTitle());
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void onInfoWindowLongClick(Marker marker) {
        dialPhoneNumber(marker.getSnippet());
    }

    public void dialPhoneNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
