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
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
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
import com.puurva.findmetoo.uitls.SQLHelper;

import org.json.JSONObject;

public class NotificationsService extends FirebaseMessagingService {

    final String TAG = "NotificationsService";
//    String deviceID = null;
//    String activityID = null;
//    RequestStatus requestStatus = null;
//    NotificationType notificationType = null;

    private ActivityNotification activityNotification = null;
    private ActivityModel activityModel = null;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with GCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            try {
                JSONObject jObject = new JSONObject(remoteMessage.getData());
                if (jObject != null) {
                    if (jObject.has("FromDeviceId")) {
                        activityNotification = new ActivityNotification(jObject.get("FromDeviceId").toString(),
                                jObject.get("ActivityId").toString(),
                                RequestStatus.valueOf(jObject.get("NotificationRequestStatus").toString()),
                                NotificationType.valueOf(jObject.get("RequestNotificationType").toString()));
                    }
                    if (jObject.has("What")) {
                        activityModel = new ActivityModel(null, jObject.get("DeviceID").toString(),
                                jObject.get("What").toString(), jObject.get("description").toString(), jObject.get("When").toString(),
                                Double.parseDouble(jObject.get("Lat").toString()), Double.parseDouble(jObject.get("Long").toString()));
                    }
                }
//                deviceID = jObject.get("FromDeviceId").toString();
//                activityID = jObject.get("ActivityId").toString();
//                requestStatus = RequestStatus.valueOf(jObject.get("NotificationRequestStatus").toString());
//                notificationType = NotificationType.valueOf(jObject.get("RequestNotificationType").toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

//            if (/* Check if data needs to be processed by long running job */ true) {
//                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
////                scheduleJob();
//            } else {
//                // Handle message within 10 seconds
//                handleNow();
//            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            if(activityNotification != null || activityModel != null) {
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


    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
//        Class<?> intentClass = null;
//        switch (activityNotification.ActivityRequestStatus) {
//            case NEW:
//                intentClass = ProfileViewActivity.class;
//                break;
//            case ACCEPTED:
//            case REJECTED:
//                intentClass = MapsActivity.class;
//                break;
//            default:
//                intentClass = MapsActivity.class;
//                break;
//        }
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("ActivityNotification", activityNotification);
//        intent.putExtra("source", notificationType);
//        intent.putExtra("ActivityID", activityID);
//        intent.putExtra("RequestStatus", requestStatus);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

//        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0 /* Request code */, intent,
//                PendingIntent.FLAG_ONE_SHOT);

//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Alert")
//                .setCancelable(false)
//                .setMessage("Are you sure?")
//                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
////                        finish();
//                    }
//                })
//                .setNegativeButton("No", null);
//
//        builder.show();

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
