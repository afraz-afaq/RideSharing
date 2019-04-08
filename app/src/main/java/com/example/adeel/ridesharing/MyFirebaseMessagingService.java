package com.example.adeel.ridesharing;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Afraz on 2/9/2019.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseMessagingServce";
    String notificationTitle = null, notificationBody = null;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {



        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            notificationTitle = remoteMessage.getNotification().getTitle();
            notificationBody = remoteMessage.getNotification().getBody();
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        sendNotification(notificationTitle, notificationBody);
    }


    private void sendNotification(String notificationTitle, String notificationBody) {
        Intent intent  = new Intent(this, MainActivity.class);
        if (notificationTitle.equals("New Request"))
        {
            intent.putExtra("request","request");

        }
        else if(notificationTitle.equals("Request Accepted")){
            intent.putExtra("accepted","accepted");
        }
        else if(notificationTitle.equals("Ride Canceled")){
            intent.putExtra("canceled","canceled");
        }
        else if(notificationTitle.equals("Request Canceled")){
            intent.putExtra("request","request");
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setAutoCancel(true)   //Automatically delete the notification
                .setSmallIcon(R.mipmap.ic_launcher) //Notification icon
                .setContentIntent(pendingIntent)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setSound(defaultSoundUri);


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int id = (int) System.currentTimeMillis();
        notificationManager.notify(id, notificationBuilder.build());
    }
}
