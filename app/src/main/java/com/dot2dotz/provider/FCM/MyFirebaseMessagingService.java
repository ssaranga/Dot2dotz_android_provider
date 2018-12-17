package com.dot2dotz.provider.FCM;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.dot2dotz.provider.Activity.MainActivity;
import com.dot2dotz.provider.Activity.SplashScreen;
import com.dot2dotz.provider.R;
import com.dot2dotz.provider.Utilities.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;

public class MyFirebaseMessagingService extends FirebaseMessagingService {


    private static final String TAG = "MyFirebaseMsgService";
    Utilities utils = new Utilities();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData() != null) {
            utils.print(TAG, "From: " + remoteMessage.getFrom());
            utils.print(TAG, "Notification Message Body: " + remoteMessage.getData());
            //Calling method to generate notification
            sendNotification(remoteMessage.getData().get("message"));
        } else {
            utils.print(TAG, "FCM Notification failed");
        }
    }

    //This method is only generating push notification
    //It is same as we did in earlier posts
    private void sendNotification(String messageBody) {
        String CHANNEL_ID = "1";
        CharSequence name = getString(R.string.app_name);
        int importance;
        if (!Utilities.isAppIsInBackground(getApplicationContext())) {
            try {
                // app is in foreground, broadcast the push message
                utils.print(TAG, "foreground");
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("push", true);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                        PendingIntent.FLAG_ONE_SHOT);

                NotificationCompat.Builder notificationBuilder;

                if (Build.VERSION.SDK_INT < 26) {
                    notificationBuilder = new NotificationCompat.Builder(this);
                    notificationBuilder.setContentTitle(getString(R.string.app_name))
                            .setContentText(messageBody)
                            .setAutoCancel(true)
                            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))
                            .setPriority(NotificationManager.IMPORTANCE_HIGH)
                            .setContentIntent(pendingIntent);

                    notificationBuilder.setSmallIcon(getNotificationIcon(notificationBuilder), 1);

                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    notificationManager.notify(0, notificationBuilder.build());
                } else {
                    importance = NotificationManager.IMPORTANCE_DEFAULT;
                    notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
                    NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                    mChannel.enableVibration(true);
                    mChannel.enableLights(true);
                    mChannel.setLightColor(Color.RED);
                    mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                    mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                    mChannel.setDescription(getResources().getString(R.string.app_name));

                    notificationBuilder.setContentTitle(getString(R.string.app_name))
                            .setContentText(messageBody)
                            .setAutoCancel(true)
                            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))
                            .setPriority(NotificationManager.IMPORTANCE_HIGH)
                            .setChannelId(CHANNEL_ID)
                            .setVisibility(Notification.VISIBILITY_PUBLIC)
                            .setContentIntent(pendingIntent).build();

                    notificationBuilder.setSmallIcon(getNotificationIcon(notificationBuilder), 1);

                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.createNotificationChannel(mChannel);

                    notificationManager.notify(0, notificationBuilder.build());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            utils.print(TAG, "background");
            // app is in background, show the notification in notification tray
            if (messageBody.equalsIgnoreCase("New Incoming delivery")) {//New Incoming delivery
                try {
                    Intent intent = new Intent(this, SplashScreen.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("push", true);
                    startActivity(intent);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                            PendingIntent.FLAG_ONE_SHOT);

                    Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    NotificationCompat.Builder notificationBuilder;
                    if (Build.VERSION.SDK_INT < 26) {
                        notificationBuilder = new NotificationCompat.Builder(this)
                                .setContentTitle(getString(R.string.app_name))
                                .setContentText(messageBody)
                                .setAutoCancel(true)
                                .setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alert_tone))
                                .setContentIntent(pendingIntent);

                        notificationBuilder.setSmallIcon(getNotificationIcon(notificationBuilder), 1);

                        NotificationManager notificationManager =
                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                        notificationManager.notify(0, notificationBuilder.build());
                    } else {
                        importance = NotificationManager.IMPORTANCE_DEFAULT;
                        notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
                        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                        mChannel.enableVibration(true);
                        mChannel.enableLights(true);
                        mChannel.setLightColor(Color.RED);
                        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                        mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                        mChannel.setDescription(getResources().getString(R.string.app_name));
                        notificationBuilder.setContentTitle(getString(R.string.app_name))
                                .setContentText(messageBody)
                                .setAutoCancel(true)
                                .setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alert_tone))
                                .setVisibility(Notification.VISIBILITY_PUBLIC)
                                .setContentIntent(pendingIntent)
                                .setChannelId(CHANNEL_ID).build();

                        notificationBuilder.setSmallIcon(getNotificationIcon(notificationBuilder), 1);

                        NotificationManager notificationManager =
                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.createNotificationChannel(mChannel);
                        notificationManager.notify(0, notificationBuilder.build());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("push", true);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                            PendingIntent.FLAG_ONE_SHOT);
                    NotificationCompat.Builder notificationBuilder;

                    if (Build.VERSION.SDK_INT < 26) {
                        notificationBuilder = new NotificationCompat.Builder(this)
                                .setContentTitle(getString(R.string.app_name))
                                .setContentText(messageBody)
                                .setAutoCancel(true)
                                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                                .setContentIntent(pendingIntent);

                        notificationBuilder.setSmallIcon(getNotificationIcon(notificationBuilder), 1);

                        NotificationManager notificationManager =
                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                        notificationManager.notify(0, notificationBuilder.build());
                    } else {
                        importance = NotificationManager.IMPORTANCE_DEFAULT;
                        notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
                        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                        mChannel.enableVibration(true);
                        mChannel.enableLights(true);
                        mChannel.setLightColor(Color.RED);
                        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                        mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                        mChannel.setDescription(getResources().getString(R.string.app_name));
                        notificationBuilder.setContentTitle(getString(R.string.app_name))
                                .setContentText(messageBody)
                                .setAutoCancel(true)
                                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                                .setChannelId(CHANNEL_ID)
                                .setVisibility(Notification.VISIBILITY_PUBLIC)
                                .setContentIntent(pendingIntent).build();

                        notificationBuilder.setSmallIcon(getNotificationIcon(notificationBuilder), 1);

                        NotificationManager notificationManager =
                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.createNotificationChannel(mChannel);
                        notificationManager.notify(0, notificationBuilder.build());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int getNotificationIcon(NotificationCompat.Builder notificationBuilder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
            return R.drawable.notification_white;
        } else {
            return R.mipmap.ic_launcher;
        }
    }


//FOR FUTURE REFERENCE

//    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
//
//    private NotificationUtils notificationUtils;
//
//    @Override
//    public void onMessageReceived(RemoteMessage remoteMessage) {
//        utils.print(TAG, "From: " + remoteMessage.getFrom());
//
//        if (remoteMessage == null)
//            return;
//
//        // Check if message contains a notification payload.
//        if (remoteMessage.getNotification() != null) {
//            utils.print(TAG, "Notification Body: " + remoteMessage.getNotification().getBody());
//            handleNotification(remoteMessage.getNotification().getBody());
//        }
//
//        // Check if message contains a data payload.
//        if (remoteMessage.getData().size() > 0) {
//            utils.print(TAG, "Data Payload: " + remoteMessage.getData().toString());
//
//            try {
//                JSONObject json = new JSONObject(remoteMessage.getData().toString());
//                handleDataMessage(json);
//            } catch (Exception e) {
//                utils.print(TAG, "Exception: " + e.getMessage());
//            }
//        }
//    }
//
//    private void handleNotification(String message) {
//        if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
//            // app is in foreground, broadcast the push message
//            Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
//            pushNotification.putExtra("message", message);
//            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
//
//            // play notification sound
//            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
//            notificationUtils.playNotificationSound();
//        }else{
//            // If the app is in background, firebase itself handles the notification
//        }
//    }
//
//    private void handleDataMessage(JSONObject json) {
//        utils.print(TAG, "push json: " + json.toString());
//
//        try {
//            JSONObject data = json.getJSONObject("data");
//
//            String title = data.getString("title");
//            String message = data.getString("message");
//            boolean isBackground = data.getBoolean("is_background");
//            String imageUrl = data.getString("image");
//            String timestamp = data.getString("timestamp");
//            JSONObject payload = data.getJSONObject("payload");
//
//            utils.print(TAG, "title: " + title);
//            utils.print(TAG, "message: " + message);
//            utils.print(TAG, "isBackground: " + isBackground);
//            utils.print(TAG, "payload: " + payload.toString());
//            utils.print(TAG, "imageUrl: " + imageUrl);
//            utils.print(TAG, "timestamp: " + timestamp);
//
//
//            if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
//                // app is in foreground, broadcast the push message
//                Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
//                pushNotification.putExtra("message", message);
//                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
//
//                // play notification sound
//                NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
//                notificationUtils.playNotificationSound();
//            } else {
//                // app is in background, show the notification in notification tray
//                Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
//                resultIntent.putExtra("message", message);
//
//                // check for image attachment
//                if (TextUtils.isEmpty(imageUrl)) {
//                    showNotificationMessage(getApplicationContext(), title, message, timestamp, resultIntent);
//                } else {
//                    // image is present, show notification with image
//                    showNotificationMessageWithBigImage(getApplicationContext(), title, message, timestamp, resultIntent, imageUrl);
//                }
//            }
//        } catch (JSONException e) {
//            utils.print(TAG, "Json Exception: " + e.getMessage());
//        } catch (Exception e) {
//            utils.print(TAG, "Exception: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Showing notification with text only
//     */
//    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
//        notificationUtils = new NotificationUtils(context);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
//    }
//
//    /**
//     * Showing notification with text and image
//     */
//    private void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent, String imageUrl) {
//        notificationUtils = new NotificationUtils(context);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl);
//    }
}