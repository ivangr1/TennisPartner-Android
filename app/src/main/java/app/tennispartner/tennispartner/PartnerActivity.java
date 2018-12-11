package app.tennispartner.tennispartner;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;
import app.tennispartner.tennispartner.adapters.UserAdapter;
import app.tennispartner.tennispartner.helper.FilterDialog;
import app.tennispartner.tennispartner.helper.Helper;
import app.tennispartner.tennispartner.helper.Login;
import app.tennispartner.tennispartner.models.User;
import com.sendbird.android.SendBird;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.imperiumlabs.geofirestore.GeoQuery;
import org.imperiumlabs.geofirestore.GeoQueryDataEventListener;

import java.util.ArrayList;
import java.util.List;

import static app.tennispartner.tennispartner.helper.Helper.MY_PERMISSIONS_REQUEST_LOCATION;
import static app.tennispartner.tennispartner.helper.Helper.isGooglePlayServicesAvailable;
import static app.tennispartner.tennispartner.helper.Login.RC_SIGN_IN;
import static app.tennispartner.tennispartner.helper.Login.RC_SIGN_IN_CONTINUE;
import static app.tennispartner.tennispartner.helper.Login.isPhoneUser;

public class PartnerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        GeoQueryDataEventListener, FilterDialog.NoticeDialogListener {

    private static final int REQUEST_INVITE = 95;
    private static final String TAG = PartnerActivity.class.getSimpleName();

    private LinearLayout mProgressView;
    private NavigationView navigationView;
    private ImageView nav_header_avatar;
    private TextView nav_header_name;
    private ScrollView mContent;
    private SeekBar mDistanceSeekBar;
    private int radius;
    private DrawerLayout mDrawerLayout;
    private RecyclerView mUserListRecyclerView;
    double lat;
    double lon;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser;
    FusedLocationProviderClient mFusedLocationClient;
    private GeoFirestore geoFirestore;
    private GeoQuery geoQuery;
    private FirebaseFirestore firestore;
    private List<User> users = new ArrayList<>();
    private UserAdapter userAdapter;
    private TextView emptyDataView;
    private SharedPreferences sharedPref;
    private View hView;
    private SwipeRefreshLayout mySwipeRefreshLayout;
    private Button ask_location_button;
    private LinearLayout emptyLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partner);

        currentUser = mAuth.getCurrentUser();

        // If the user got out of LoginActivity without finishing, redirect him back
        if (currentUser != null && (currentUser.getDisplayName() == null || currentUser.getDisplayName().isEmpty())) {
            startActivity(new Intent(this, LoginActivity.class));
        }

        firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mUserListRecyclerView = findViewById(R.id.userListItems);
        mUserListRecyclerView.setHasFixedSize(true);

        emptyLocation = findViewById(R.id.empty_location);
        emptyDataView = findViewById(R.id.empty_data_view);
        mProgressView = findViewById(R.id.loading_view);
        mContent = findViewById(R.id.content);
        mDistanceSeekBar = findViewById(R.id.distanceSeekBar);
        mySwipeRefreshLayout = findViewById(R.id.swiperefresh);
        ask_location_button = findViewById(R.id.ask_location_button);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.partner_activity_title);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        hView = navigationView.getHeaderView(0);
        nav_header_avatar = hView.findViewById(R.id.nav_header_avatar);
        nav_header_name = hView.findViewById(R.id.nav_header_name);

        // Get currentUser location
        if (isGooglePlayServicesAvailable(this)) {
            if (Helper.checkLocationPermission(this)) {
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(PartnerActivity.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    lat = location.getLatitude();
                                    lon = location.getLongitude();

                                    if (currentUser != null) {
                                        // Update currentUser location
                                        geoFirestore.setLocation(currentUser.getUid(), new GeoPoint(lat, lon));
                                    }
                                    geoFirestore.queryAtLocation(new GeoPoint(lat, lon), radius)
                                            .addGeoQueryDataEventListener(PartnerActivity.this);
                                    showProgress(true);
                                } else {
                                    Toast.makeText(PartnerActivity.this, "Cannot get device location", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        }

        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshLayout();
                    }
                }
        );
        FloatingActionButton fab = findViewById(R.id.addUsersActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isGooglePlayServicesAvailable(PartnerActivity.this)) {
                    Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                            .setMessage(getString(R.string.invitation_message))
                            //.setDeepLink(Uri.parse(getString(R.string.invitation_deep_link)))
                            //.setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
                            .setCallToActionText(getString(R.string.invitation_cta))
                            .build();
                    startActivityForResult(intent, REQUEST_INVITE);
                }
            }
        });
    }

    private void refreshLayout() {
        if (userAdapter != null) {
            users.clear();
            geoQuery = geoFirestore.queryAtLocation(new GeoPoint(lat, lon), radius);
            geoQuery.addGeoQueryDataEventListener(this);
            userAdapter.notifyDataSetChanged();
            mySwipeRefreshLayout.setRefreshing(false);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.d(TAG, "onActivityResult: sent invitation " + id);
                }
            } else {
                // Sending failed or it was canceled, show failure message to the currentUser
                Log.d(TAG, "Error!");
            }
        } else if (requestCode == RC_SIGN_IN || requestCode == RC_SIGN_IN_CONTINUE) {
            Login.loginResult(this, PartnerActivity.class, requestCode, resultCode, data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        CollectionReference geoFirestoreRef = firestore.collection("users");
        geoFirestore = new GeoFirestore(geoFirestoreRef);

        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        radius = sharedPref.getInt(getString(R.string.partner_radius), getResources().getInteger(R.integer.radius_default));

        currentUser = mAuth.getCurrentUser();
        navigationView.setCheckedItem(R.id.partner);

        String photoUrl = null;
        int photoInt = R.drawable.ic_user;
        String displayName = getString(R.string.login_text);
        hView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login.login(PartnerActivity.this);
            }
        });

        navigationView.getMenu().findItem(R.id.logout).setVisible(false);
        navigationView.getMenu().findItem(R.id.inbox).setVisible(false);

        if (currentUser != null && !isPhoneUser()) {
            photoUrl = currentUser.getPhotoUrl().toString();
            displayName = currentUser.getDisplayName();
            hView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PartnerActivity.this, UserDetailActivity.class);
                    intent.putExtra("userId", currentUser.getUid());
                    startActivity(intent);
                }
            });
            navigationView.getMenu().findItem(R.id.logout).setVisible(true);
            navigationView.getMenu().findItem(R.id.inbox).setVisible(true);
        }
        // Get currentUser
        Glide.with(this)
                .load((currentUser != null) ? photoUrl : photoInt)
                .apply(RequestOptions.circleCropTransform())
                .into(nav_header_avatar);
        nav_header_name.setText(displayName);
    }

    private void showSnackbar(int error) {
        View rootView = this.getWindow().getDecorView().findViewById(android.R.id.content);
        Snackbar.make(rootView, error, Snackbar.LENGTH_SHORT);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        if (currentUser == null) menu.findItem(R.id.user_messages).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.user_messages:
                startActivity(new Intent(this, ChatListActivity.class));
                return true;
            case R.id.user_filter:
                DialogFragment filterDialog = new FilterDialog();
                filterDialog.show(getSupportFragmentManager(), "filter_dialog_user");
                return true;

            default:
                // If we got here, the currentUser's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.courts) {
            startActivity(new Intent(this, CourtActivity.class));
        }

        if (id == R.id.inbox) {
            startActivity(new Intent(this, ChatListActivity.class));
        }

        if (id == R.id.logout) {
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            // currentUser is now signed out
                            SendBird.disconnect(new SendBird.DisconnectHandler() {
                                @Override
                                public void onDisconnected() {
                                    recreate();
                                }
                            });
                        }
                    });
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawers();
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        emptyLocation.setVisibility(View.GONE);
                        showProgress(true);

                        //Request location updates:
                        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                        mFusedLocationClient.getLastLocation()
                                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(@NonNull Location location) {
                                        if (location != null) {
                                            lat = location.getLatitude();
                                            lon = location.getLongitude();
                                            if (userAdapter != null) users.clear();
                                            sharedPref = PartnerActivity.this.getPreferences(Context.MODE_PRIVATE);
                                            radius = sharedPref.getInt(getString(R.string.partner_radius), getResources().getInteger(R.integer.radius_default));
                                            geoQuery = geoFirestore.queryAtLocation(new GeoPoint(lat, lon), radius);
                                            geoQuery.addGeoQueryDataEventListener(PartnerActivity.this);
                                        }
                                    }
                                });
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    showProgress(false);
                    ask_location_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(PartnerActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    MY_PERMISSIONS_REQUEST_LOCATION);
                        }
                    });
                    emptyLocation.setVisibility(View.VISIBLE);

                }
            }

        }
    }


    public void loadDatabase(View view) {
        DatabaseLoader.loadDatabase();
    }

    @Override
    public void onDocumentEntered(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {
        try {
            // Don't show the current currentUser in the list
            if (currentUser != null) {
                if (documentSnapshot.getId().equals(currentUser.getUid())) return;
            }
            User user = documentSnapshot.toObject(User.class);
            if (user != null) {
                user.setId(documentSnapshot.getId());
                users.add(user);
            }
        } catch (NullPointerException e) {
            Log.d("DOCUMENT_ERROR", e.getLocalizedMessage());
        }
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
        showProgress(false);
        //geoQuery.removeGeoQueryEventListener(this);

        if (users.isEmpty()) {
            mUserListRecyclerView.setVisibility(View.GONE);
            emptyDataView.setVisibility(View.VISIBLE);
        } else {
            mUserListRecyclerView.setVisibility(View.VISIBLE);
            emptyDataView.setVisibility(View.GONE);
            userAdapter = new UserAdapter(users, this);
            mUserListRecyclerView.setAdapter(userAdapter);
            mUserListRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        }
    }

    @Override
    public void onGeoQueryError(Exception e) {
        Log.d("QUERY_ERROR", e.getLocalizedMessage());
    }

    @Override
    public void onDialogPositiveClick(int radius) {
        this.radius = radius;
        showProgress(true);
        if (userAdapter != null) {
            users.clear();
            geoQuery = geoFirestore.queryAtLocation(new GeoPoint(lat, lon), radius);
            geoQuery.addGeoQueryDataEventListener(this);
            userAdapter.notifyDataSetChanged();
        }
    }

    private void showProgress(boolean b) {
        Helper.showProgress(b, mySwipeRefreshLayout, mProgressView, this);
    }
}
