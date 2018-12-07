package app.tennispartner.tennispartner.helper;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import app.tennispartner.tennispartner.GroupChatActivity;
import app.tennispartner.tennispartner.R;
import app.tennispartner.tennispartner.UserDetailActivity;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.shadow.com.google.gson.JsonElement;
import com.sendbird.android.shadow.com.google.gson.JsonObject;
import com.sendbird.android.shadow.com.google.gson.JsonParser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

public class FirebaseInstanceIdService extends FirebaseMessagingService {

    private final static String TAG = FirebaseInstanceIdService.class.getSimpleName();
    private static final String CHANNEL_ID = "Messages";

    @Override
    public void onNewToken(String token) {
        SendBird.registerPushTokenForCurrentUser(token, new SendBird.RegisterPushTokenWithStatusHandler() {
            @Override
            public void onRegistered(SendBird.PushTokenRegistrationStatus ptrs, SendBirdException e) {
                if (e != null) {
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
        String message = remoteMessage.getData().get("message");
        JsonElement payload = new JsonParser().parse(remoteMessage.getData().get("sendbird"));
        sendNotification(message, payload);
    }

    private void sendNotification(String message, JsonElement payload) {
        JsonObject chatDetails = payload.getAsJsonObject();
        String channelUrl = chatDetails.getAsJsonObject("channel").get("channel_url").getAsString();
        String senderUrl = chatDetails.getAsJsonObject("sender").get("profile_url").getAsString();
        //String senderName = chatDetails.getAsJsonObject("sender").get("name").getAsString();

        GroupChannel.getChannel(channelUrl, new GroupChannel.GroupChannelGetHandler() {
            @Override
            public void onResult(GroupChannel groupChannel, SendBirdException e) {
                if (e != null) {    // Error!
                    return;
                }
                Context context = getApplicationContext();
                // Create an Intent for the activity you want to start
                Intent resultIntent = new Intent(getApplicationContext(), GroupChatActivity.class);
                resultIntent.putExtra("groupChatUrl", channelUrl);
                // Create the TaskStackBuilder and add the intent, which inflates the back stack
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                stackBuilder.addNextIntentWithParentStack(resultIntent);
                // Get the PendingIntent containing the entire back stack
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                Glide.with(context)
                        .asBitmap()
                        .load(senderUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                                        .setSmallIcon(R.drawable.ic_squash_rackets)
                                        .setLargeIcon(resource)
                                        .setContentTitle(TextUtils.getGroupChannelTitle(groupChannel))
                                        .setContentText(message)
                                        .setContentIntent(resultPendingIntent)
                                        .setAutoCancel(true);

                                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                                notificationManager.notify(channelUrl.hashCode(), mBuilder.build());
                            }
                        });
            }
        });
    }

}
