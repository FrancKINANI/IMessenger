package I.imessenger.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

import I.imessenger.R;
import I.imessenger.activities.ChatActivity;
import I.imessenger.activities.MainActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // Token update is handled in Login/MainActivity usually, but good to have here if needed.
        // For now, we rely on the app updating it when the user logs in.
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = remoteMessage.getNotification() != null ? remoteMessage.getNotification().getTitle() : "New Message";
        String body = remoteMessage.getNotification() != null ? remoteMessage.getNotification().getBody() : "You have a new message";
        
        // Handle data payload if present (often used for background execution)
        if (remoteMessage.getData().size() > 0) {
            // Handle data
            String senderName = remoteMessage.getData().get("userName");
            if (senderName != null) title = senderName;
            
            String message = remoteMessage.getData().get("message");
            if (message != null) body = message;
        }

        sendNotification(title, body);
    }

    private void sendNotification(String title, String body) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        String channelId = "chat_message";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_send) // Use existing icon or app icon
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Chat Messages", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(new Random().nextInt(), builder.build());
    }
}
