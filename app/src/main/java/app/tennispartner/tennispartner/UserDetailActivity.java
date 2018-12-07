package app.tennispartner.tennispartner;

import android.content.Intent;

import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.google.android.gms.tasks.OnSuccessListener;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import app.tennispartner.tennispartner.adapters.GameAdapter;
import app.tennispartner.tennispartner.databinding.ActivityUserDetailBinding;
import app.tennispartner.tennispartner.helper.Helper;
import app.tennispartner.tennispartner.helper.Login;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBirdException;

import java.util.Arrays;

public class UserDetailActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private app.tennispartner.tennispartner.models.User userInfo;
    private ActivityUserDetailBinding binding;
    private GameAdapter gameAdapter;
    private String userId;
    private boolean chatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidThreeTen.init(this);
        userId = getIntent().getStringExtra("userId");
        chatButton = getIntent().getBooleanExtra("chatButton", false);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_detail);
        binding.userListGames.setLayoutManager(new GridLayoutManager(this, 3));

        Toolbar toolbar = findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        ImageView avatar = findViewById(R.id.user_detail_avatar);
        FloatingActionButton fab = findViewById(R.id.chat_fab);

        if (!chatButton || (currentUser != null && currentUser.getUid().equals(userId))) fab.hide();

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                userInfo = documentSnapshot.toObject(app.tennispartner.tennispartner.models.User.class);
                binding.setUser(userInfo);
                Glide.with(getApplicationContext())
                        .load(userInfo != null ? userInfo.getAvatarUrl() : null)
                        .into(avatar);

                final String fullName = userInfo.getFirstName() + " " + userInfo.getLastName();
                binding.toolbarLayout.setTitle(fullName);
                binding.userDetailAgeValue.setText(String.valueOf(Helper.calculateAge(userInfo.getBirthday())));
                binding.userDetailLocationValue.setText(Helper.getCity(userInfo.getL().get(0), userInfo.getL().get(1), getApplicationContext()));

                if (chatButton) {
                    fab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (currentUser != null) {
                                GroupChannel.createChannelWithUserIds(Arrays.asList(currentUser.getUid(), userId), true, new GroupChannel.GroupChannelCreateHandler() {
                                    @Override
                                    public void onResult(GroupChannel groupChannel, SendBirdException e) {
                                        if (e != null) {    // Error.
                                            return;
                                        }
                                        Intent intent = new Intent(UserDetailActivity.this, GroupChatActivity.class);
                                        intent.putExtra("groupChatUrl", groupChannel.getUrl());
                                        startActivity(intent);
                                    }
                                });
                            } else {
                                Login.login(UserDetailActivity.this);
                            }

                        }
                    });
                }
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Login.loginResult(this, UserDetailActivity.class, requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}