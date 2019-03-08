package app.tennispartner.tenispartner.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import app.tennispartner.tenispartner.MainActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import app.tennispartner.tenispartner.GroupChatActivity;
import app.tennispartner.tenispartner.R;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.shadow.com.google.gson.JsonObject;
import com.sendbird.android.shadow.com.google.gson.JsonParser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class FirebaseInstanceIdService extends FirebaseMessagingService {

    private final static String TAG = FirebaseInstanceIdService.class.getSimpleName();
    private static final String CHANNEL_ID = "Messages";

    @Override
    public void onNewToken(String token) {
        SendBird.registerPushTokenForCurrentUser(token, new SendBird.RegisterPushTokenWithStatusHandler() {
            @Override
            public void onRegistered(SendBird.PushTokenRegistrationStatus ptrs, SendBirdException e) {
                if (e != null) {
                    Toast.makeText(FirebaseInstanceIdService.this, "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (ptrs == SendBird.PushTokenRegistrationStatus.PENDING) {
                    // Try registering the token after a connection has been successfully established.
                }
            }
        });
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String sendBird = null;
        JsonObject payload = null;
        sendBird = (String) remoteMessage.getData().get("sendbird");
        if (sendBird != null) {
            payload = new JsonParser().parse(sendBird).getAsJsonObject();
        }
        sendNotification(this, payload);
    }

    private void sendNotification(Context context, JsonObject payload) {

        String message = payload.get("message").getAsString();
        String channelUrl = payload.getAsJsonObject("channel").get("channel_url").getAsString();
        String senderUrl = payload.getAsJsonObject("sender").get("profile_url").getAsString();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= 26) {  // Build.VERSION_CODES.O
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, "Messages", NotificationManager.IMPORTANCE_HIGH);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(mChannel);
            }
        }

        GroupChannel.getChannel(channelUrl, new GroupChannel.GroupChannelGetHandler() {
            @Override
            public void onResult(GroupChannel groupChannel, SendBirdException e) {
                if (e != null) {    // Error!
                    return;
                }
                // Create an Intent for the activity you want to start
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                Intent resultIntent;
                if(currentUser != null) {
                    resultIntent = new Intent(getApplicationContext(), GroupChatActivity.class);
                    resultIntent.putExtra("groupChatUrl", channelUrl);
                } else {
                    resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                }
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Glide.with(context)
                        .asBitmap()
                        .load(senderUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                                        .setSmallIcon(R.drawable.ic_squash_rackets)
                                        .setColor(getResources().getColor(R.color.colorPrimary))
                                        .setLargeIcon(resource)
                                        .setContentTitle(TextUtils.getGroupChannelTitle(groupChannel))
                                        .setContentText(message)
                                        .setAutoCancel(true)
                                        .setSound(defaultSoundUri)
                                        .setPriority(Notification.PRIORITY_MAX)
                                        .setContentIntent(pendingIntent);

                                if (notificationManager != null) {
                                    notificationManager.notify(channelUrl.hashCode(), mBuilder.build());
                                }
                            }
                        });
            }
        });
    }

}
