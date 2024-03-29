package com.puurva.findmetoo.Activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.puurva.findmetoo.Enums.NotificationType;
import com.puurva.findmetoo.Enums.RequestStatus;
import com.puurva.findmetoo.R;
import com.puurva.findmetoo.ServiceInterfaces.model.ActivityModel;
import com.puurva.findmetoo.ServiceInterfaces.model.DeviceModel;
import com.puurva.findmetoo.ServiceInterfaces.model.ActivityNotification;
import com.puurva.findmetoo.uitls.CommonUtility;
import com.puurva.findmetoo.uitls.Global;

import org.json.JSONObject;

public class NotificationsService extends FirebaseMessagingService {

    final String TAG = "NotificationsService";
//    String deviceID = null;
//    String activityID = null;
//    RequestStatus requestStatus = null;
//    NotificationType notificationType = null;

    private ActivityNotification activityNotification = null;
    private ActivityModel activityModel = null;
    private String notificationId = null;

    /**
     * Called when message is received.
     *
     
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getData().size() > 0) {
            try {
                JSONObject jObject = new JSONObject(remoteMessage.getData());
                if (jObject != null) {
                    if (jObject.has("NotificationId")) {
                        notificationId = jObject.get("NotificationId").toString();
                    }

                    if (jObject.has("FromDeviceId")) {
                        activityNotification = new ActivityNotification(jObject.get("FromDeviceId").toString(),
                                jObject.get("ActivityId").toString(),
                                RequestStatus.valueOf(jObject.get("NotificationRequestStatus").toString()),
                                NotificationType.valueOf(jObject.get("RequestNotificationType").toString()));
                    }
                    if (jObject.has("What")) {
                        activityModel = new ActivityModel(jObject.get("ActivityID").toString(), jObject.get("DeviceID").toString(),
                                jObject.get("What").toString(), jObject.get("description").toString(), jObject.get("When").toString(),
                                Double.parseDouble(jObject.get("Lat").toString()), Double.parseDouble(jObject.get("Long").toString()));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            if(activityNotification != null || activityModel != null || notificationId != null) {
                sendNotification(remoteMessage.getNotification().getBody());
        }
//            Toast.makeText(this, remoteMessage.getNotification().getBody(), Toast.LENGTH_SHORT).show();
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]


    // [START on_new_token]

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        if(!Global.has_device_registered) {
            sendRegistrationToServer(token);
        }
    }
    // [END on_new_token]

    /**
     * Schedule a job using FirebaseJobDispatcher.
     */
//    private void scheduleJob() {
//        // [START dispatch_job]
//        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
//        Job myJob = dispatcher.newJobBuilder()
//                .setService(MyJobService.class)
//                .setTag("my-job-tag")
//                .build();
//        dispatcher.schedule(myJob);
//        // [END dispatch_job]
//    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    /**
     * Persist token to third-party servers.
     * <p>
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        String androidId = CommonUtility.GetDeviceId();

        DeviceModel deviceModel = new DeviceModel(androidId, "", android.os.Build.VERSION.RELEASE, token);
        CommonUtility.RegisterDevice(deviceModel);
    }

    private PendingIntent createOnDismissedIntent(Context context, ActivityNotification activityNotification) {
        Intent intent = new Intent(context, NotificationDismissedReceiver.class);
        intent.putExtra("NotificationId", 0);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context.getApplicationContext(),
                        0, intent, 0);
        return pendingIntent;
    }

    private void sendNotification(String messageBody) {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("ActivityNotification", activityNotification);
        if(activityModel != null) {
            intent.putExtra("ActivityOfNotification", activityModel.ActivityID);
        }
        if(notificationId != null) {
            intent.putExtra("NotificationId", notificationId);
        }
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(getApplicationContext(), channelId)
                        .setSmallIcon(R.drawable.findmetoo_logo)
                        .setContentTitle(getString(R.string.fcm_message))
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .setDeleteIntent(createOnDismissedIntent(getApplicationContext(), activityNotification))
                        .setPriority(NotificationManager.IMPORTANCE_HIGH);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
