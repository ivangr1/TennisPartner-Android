package app.tennispartner.tenispartner;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import app.tennispartner.tenispartner.helper.AboutDialog;
import app.tennispartner.tenispartner.helper.Helper;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import app.tennispartner.tenispartner.helper.FilterDialog;
import app.tennispartner.tenispartner.helper.Login;

import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;

import static app.tennispartner.tenispartner.helper.Login.RC_SIGN_IN;
import static app.tennispartner.tenispartner.helper.Login.RC_SIGN_IN_CONTINUE;
import static app.tennispartner.tenispartner.helper.Login.isPhoneUserWithoutName;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        BottomNavigationView.OnNavigationItemSelectedListener,
        PartnerFragment.OnFragmentInteractionListener {

    private static final int REQUEST_INVITE = 95;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String PARTNER_FRAGMENT = "partner_fragment";
    private static final String COURT_FRAGMENT = "court_fragment";

    private FragmentManager fragmentManager;
    private Fragment currentFragment;

    private NavigationView mNavigationView;
    private FirebaseUser currentUser;

    private View hView;
    private ImageView nav_header_avatar;
    private TextView nav_header_name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        fragmentManager = getSupportFragmentManager();

        Fragment fragment = fragmentManager.findFragmentByTag(PARTNER_FRAGMENT);
        if (fragment == null) {
            fragment = PartnerFragment.newInstance();
        }
        replaceFragment(fragment, PARTNER_FRAGMENT);

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Main drawer setup
        DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        hView = mNavigationView.getHeaderView(0);
        nav_header_avatar = hView.findViewById(R.id.nav_header_avatar);
        nav_header_name = hView.findViewById(R.id.nav_header_name);

        // Bottom navigation setup
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // If the user signed in with phone but didn't enter additional info, alert him and redirect
        if(isPhoneUserWithoutName())
            new AlertDialog.Builder(this)
                    .setTitle(R.string.login_dialog_title)
                    .setMessage(R.string.login_dialog_description)
                    .setPositiveButton(R.string.login_dialog_continue, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivityForResult(new Intent(MainActivity.this, LoginActivity.class), RC_SIGN_IN_CONTINUE);
                        }
                    })
                    .setCancelable(false)
                    .create()
                    .show();

        mNavigationView.setCheckedItem(R.id.partner);

        String photoUrl = null;
        int photoInt = R.drawable.ic_user;
        String displayName = getString(R.string.login_text);
        hView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login.login(MainActivity.this);
            }
        });

        mNavigationView.getMenu().findItem(R.id.logout).setVisible(false);
        mNavigationView.getMenu().findItem(R.id.inbox).setVisible(false);

        if (currentUser != null && !isPhoneUserWithoutName()) {
            photoUrl = currentUser.getPhotoUrl().toString();
            displayName = currentUser.getDisplayName();
            hView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, UserDetailActivity.class);
                    intent.putExtra("userId", currentUser.getUid());
                    startActivity(intent);
                }
            });
            mNavigationView.getMenu().findItem(R.id.logout).setVisible(true);
            mNavigationView.getMenu().findItem(R.id.inbox).setVisible(true);

            // Show all unread messages number
            SendBird.getTotalUnreadMessageCount(new GroupChannel.GroupChannelTotalUnreadMessageCountHandler() {
                @Override
                public void onResult(int totalUnreadMessageCount, SendBirdException e) {
                    if (e != null) return;
                        TextView unreadMessages = (TextView) mNavigationView.getMenu().findItem(R.id.inbox).getActionView();
                        if(totalUnreadMessageCount > 0) {
                            unreadMessages.setText(String.valueOf(totalUnreadMessageCount));
                        } else {
                            unreadMessages.setBackground(null);
                        }
                }
            });
        }
        // Get currentUser
        Glide.with(this)
                .load((currentUser != null) ? photoUrl : photoInt)
                .apply(RequestOptions.circleCropTransform())
                .into(nav_header_avatar);
        nav_header_name.setText(displayName);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN || requestCode == RC_SIGN_IN_CONTINUE) {
            Login.loginResult(this, MainActivity.class, requestCode, resultCode, data);
        }
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.user_filter) {
            openFilterDialog(null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.partner) {
            PartnerFragment fragment = (PartnerFragment) fragmentManager.findFragmentByTag(PARTNER_FRAGMENT);
            if (fragment == null) {
                fragment = (PartnerFragment) PartnerFragment.newInstance();
            }
            replaceFragment(fragment, PARTNER_FRAGMENT);
        }

        if (id == R.id.courts) {
            CourtFragment fragment = (CourtFragment) fragmentManager.findFragmentByTag(COURT_FRAGMENT);
            if (fragment == null) {
                fragment = (CourtFragment) CourtFragment.newInstance();
            }
            replaceFragment(fragment, COURT_FRAGMENT);
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

        if (id == R.id.about) {
            DialogFragment aboutDialog = new AboutDialog();
            aboutDialog.show(getSupportFragmentManager(), "about_dialog_user");
        }

        if (id == R.id.invite_friends) {
            inviteFriends(null);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawers();
        return true;
    }


    public void loadDatabase() {
        DatabaseLoader.loadDatabase();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private void replaceFragment(@NonNull Fragment fragment, @NonNull String tag) {
        if (!fragment.equals(currentFragment)) {
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.container_main, fragment, tag)
                    .addToBackStack(null)
                    .commit();
            currentFragment = fragment;
        }
    }

    public void inviteFriends(View view) {
        Helper.inviteFriends(this);
    }

    public void openFilterDialog(View view) {
        DialogFragment filterDialog = new FilterDialog();
        filterDialog.show(getSupportFragmentManager(), "filter_dialog_user");
    }
}
