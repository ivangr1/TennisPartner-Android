package app.tennispartner.tenispartner;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import app.tennispartner.tenispartner.adapters.UserAdapter;
import app.tennispartner.tenispartner.helper.Helper;
import app.tennispartner.tenispartner.models.SharedView;
import app.tennispartner.tenispartner.models.User;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.imperiumlabs.geofirestore.GeoQuery;
import org.imperiumlabs.geofirestore.GeoQueryDataEventListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PartnerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class PartnerFragment extends Fragment implements GeoQueryDataEventListener {

    private OnFragmentInteractionListener mListener;

    private RecyclerView mUserListRecyclerView;
    private LinearLayout mProgressView;
    private LinearLayout emptyDataView;
    private SwipeRefreshLayout mySwipeRefreshLayout;
    private int radius;
    private double lat;
    private double lon;
    private GeoFirestore geoFirestore;
    private GeoQuery geoQuery;
    private List<User> users;
    private UserAdapter userAdapter;

    private FirebaseUser currentUser;
    private FusedLocationProviderClient mFusedLocationClient;
    private MainActivity mainActivity;
    private SharedView viewModel;

    public PartnerFragment() {
        // Required empty public constructor
    }

    static Fragment newInstance() {
        return new PartnerFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mainActivity = (MainActivity) context;

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        geoFirestore = new GeoFirestore(firestore.collection("users"));
        users = new ArrayList<>();

        SharedPreferences sharedPref = mainActivity.getPreferences(Context.MODE_PRIVATE);
        radius = sharedPref.getInt(getString(R.string.partner_radius), getResources().getInteger(R.integer.radius_default));

        viewModel = ViewModelProviders.of(mainActivity).get(SharedView.class);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel.getRadius().observe(this, integer -> {
            radius = integer;
            refreshLayout(true);
        });
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentPartner = inflater.inflate(R.layout.fragment_partner, container, false);

        mUserListRecyclerView = fragmentPartner.findViewById(R.id.userListItems);
        //mUserListRecyclerView.setHasFixedSize(true);

        emptyDataView = fragmentPartner.findViewById(R.id.empty_data_view);
        ScrollView mContent = fragmentPartner.findViewById(R.id.content);
        SeekBar mDistanceSeekBar = fragmentPartner.findViewById(R.id.distanceSeekBar);
        mySwipeRefreshLayout = fragmentPartner.findViewById(R.id.swiperefresh);
        Button ask_location_button = fragmentPartner.findViewById(R.id.ask_location_button);

        // Placeholder UI setup
        mProgressView = fragmentPartner.findViewById(R.id.loading_view);
        LinearLayout mEmptyLocationView = fragmentPartner.findViewById(R.id.empty_location);

        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshLayout(false);
                    }
                }
        );

        return fragmentPartner;
    }



    @Override
    public void onResume() {
        super.onResume();

        if(mainActivity.getSupportActionBar() != null) mainActivity.getSupportActionBar().setTitle(R.string.partner_activity_title);

        showProgress(true);

        // Get currentUser location
        if (Helper.checkLocationPermission(mainActivity)) {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                lat = location.getLatitude();
                                lon = location.getLongitude();
                                GeoPoint currentLocation = new GeoPoint(lat, lon);
                                if (currentUser != null) {
                                    // Update currentUser location
                                    geoFirestore.setLocation(currentUser.getUid(), currentLocation);
                                }
                                users.clear();
                                geoQuery = geoFirestore.queryAtLocation(currentLocation, radius);
                                geoQuery.removeAllListeners();
                                geoQuery.addGeoQueryDataEventListener(PartnerFragment.this);
                            } else {
                                Toast.makeText(getContext(), "Cannot get device location", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    private void refreshLayout(boolean showLoading) {
            if(showLoading) showProgress(true);
            users.clear();
            geoQuery = geoFirestore.queryAtLocation(new GeoPoint(lat, lon), radius);
            geoQuery.addGeoQueryDataEventListener(this);
            if (userAdapter != null) userAdapter.notifyDataSetChanged();
            mySwipeRefreshLayout.setRefreshing(false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void setOnPartnerSelectedListener(Activity activity) {
        mListener = (OnFragmentInteractionListener) activity;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onDocumentEntered(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {
        //Toast.makeText(mainActivity, "Document entered: " + documentSnapshot.getId(), Toast.LENGTH_SHORT).show();
        try {
            // Don't show the current currentUser in the list
            if (currentUser != null) {
                if (documentSnapshot.getId().equals(currentUser.getUid())) return;
            }
            User user = documentSnapshot.toObject(User.class);
            if (user != null && !users.contains(user)) {
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

        geoQuery.removeGeoQueryEventListener(this);

        if (users.isEmpty()) {
            mUserListRecyclerView.setVisibility(View.GONE);
            emptyDataView.setVisibility(View.VISIBLE);
        } else {
            mUserListRecyclerView.setVisibility(View.VISIBLE);
            emptyDataView.setVisibility(View.GONE);
            userAdapter = new UserAdapter(users, mainActivity);
            mUserListRecyclerView.setAdapter(userAdapter);
            mUserListRecyclerView.setLayoutManager(new LinearLayoutManager(mainActivity));
            userAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onGeoQueryError(Exception e) {
        Log.d("GEO_QUERY_ERROR", e.getLocalizedMessage());
    }

    private void showProgress(boolean b) {
        Helper.showProgress(b, mySwipeRefreshLayout, mProgressView, mainActivity);
    }
}
