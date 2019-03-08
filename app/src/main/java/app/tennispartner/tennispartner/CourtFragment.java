package app.tennispartner.tenispartner;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import app.tennispartner.tenispartner.helper.Helper;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.GeoPoint;
import org.imperiumlabs.geofirestore.GeoFirestore;
import org.imperiumlabs.geofirestore.GeoQuery;
import org.imperiumlabs.geofirestore.GeoQueryDataEventListener;
import org.jetbrains.annotations.NotNull;

public class CourtFragment extends SupportMapFragment implements
        OnMapReadyCallback,
        GeoQueryDataEventListener/*,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnInfoWindowLongClickListener*/ {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 86;
    private static final String TAG = CourtFragment.class.getSimpleName();
    private static final int PLACE_PICKER_REQUEST = 1;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;

    private static final float DEFAULT_ZOOM = 12.5f;

    private double radius;
    private GeoFirestore geoFirestore;

    private FirebaseUser currentUser;
    private double latitude;
    private double longitude;
    private MainActivity mainActivity;
    private FirebaseFirestore firestore;

    public static SupportMapFragment newInstance() {
        return new CourtFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mainActivity = (MainActivity) context;

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        geoFirestore = new GeoFirestore(firestore.collection("courts"));

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        radius = getResources().getInteger(R.integer.radius_default);

        // Create map
        getMapAsync(this);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mainActivity.getSupportActionBar() != null) mainActivity.getSupportActionBar().setTitle(R.string.court_activity_title);
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
        mMap.getUiSettings().setZoomControlsEnabled(true);
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

        try {
            if (mLocationPermissionGranted) {
                mFusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        if (location != null) {
                                            // Set the map's camera position to the current location of the device.
                                            mLastKnownLocation = location;
                                            latitude = mLastKnownLocation.getLatitude();
                                            longitude = mLastKnownLocation.getLongitude();
                                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                                    new LatLng(latitude, longitude), DEFAULT_ZOOM));

                                            GeoQuery geoQuery = geoFirestore.queryAtLocation(new GeoPoint(latitude, longitude), radius);
                                            geoQuery.removeAllListeners();
                                            geoQuery.addGeoQueryDataEventListener(CourtFragment.this);
                                        } else {
                                            Log.d(TAG, "Current location is null. Using defaults.");
                                            Toast.makeText(getActivity(), "Cannot get device location", Toast.LENGTH_SHORT).show();
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

        if (Helper.checkLocationPermission(getContext())) {
            mLocationPermissionGranted = true;
        }
    }


    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, @NotNull MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.court_main, menu);
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot user) {
                    if(user.get("admin") != null) {
                        if (user.getBoolean("admin")) {
                            MenuItem addCourtMenuItem = menu.findItem(R.id.court_add);
                            if(addCourtMenuItem != null) addCourtMenuItem.setVisible(true);
                        }
                    }
                }
            });
        }
        menu.findItem(R.id.user_filter).setVisible(false);
        super.onCreateOptionsMenu(menu,inflater);
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
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.court_add:
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult(builder.build(mainActivity), PLACE_PICKER_REQUEST);
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
    /*

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
    */
}
