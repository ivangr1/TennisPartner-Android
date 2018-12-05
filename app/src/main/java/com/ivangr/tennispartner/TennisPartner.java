package com.ivangr.tennispartner;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

public class TennisPartner extends MultiDexApplication {

    private static final String CHANNEL_ID = "Messages";

    // Called when the application is starting, before any other application objects have been created.
    // Overriding this method is totally optional!
    @Override
    public void onCreate() {
        super.onCreate();

        Context context = getApplicationContext();

        SendBird.init("42C504AB-D68E-4636-8C4B-A766E513DB44", context);

        // If user is logged connect to chat
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            SendBird.connect(currentUser.getUid(), new SendBird.ConnectHandler() {
                @Override
                public void onConnected(com.sendbird.android.User user, SendBirdException e) {
                    if (e != null) {    // Error.
                        return;
                    }

                    FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            String token = task.getResult().getToken();
                            if (token == null) return;

                            SendBird.registerPushTokenForCurrentUser(FirebaseInstanceId.getInstance().getToken(),
                                    new SendBird.RegisterPushTokenWithStatusHandler() {
                                        @Override
                                        public void onRegistered(SendBird.PushTokenRegistrationStatus status, SendBirdException e) {
                                        }
                                    });
                        }
                    });
                }
            });
        }

        createNotificationChannel();


    }

    // This is called when the overall system is running low on memory,
    // and would like actively running processes to tighten their belts.
    // Overriding this method is totally optional!
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}