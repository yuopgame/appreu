package styleru.it_lab.reaschedule.Services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import styleru.it_lab.reaschedule.MainMenuActivity;
import styleru.it_lab.reaschedule.R;

public class SchGcmListenerService extends  GcmListenerService{

    public static final String DEBUG_TAG = "GcmListenerServiceDEBUG";

    @Override
    public void onMessageReceived(String from, Bundle data)
    {
        String message = data.getString("message");
        Log.i(DEBUG_TAG, "Received message from: " + from);
        Log.i(DEBUG_TAG, "The Message is: " + message);

        sendNotification(message);
    }

    private void sendNotification(String message) {
        Intent intent = new Intent(this, MainMenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_light)
                .setContentTitle("Сообщение из облака!")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

}
