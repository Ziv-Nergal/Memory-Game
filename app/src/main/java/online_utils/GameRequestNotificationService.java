package online_utils;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class GameRequestNotificationService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if(remoteMessage.getData().size() > 0 && remoteMessage.getNotification() != null) {

            Intent intent = new Intent("GameRequestAction");

            intent.putExtra("friend_id", remoteMessage.getData().get("friend_id"));
            intent.putExtra("friend_user_name", remoteMessage.getData().get("friend_user_name"));
            intent.putExtra("card_number", remoteMessage.getData().get("card_number"));
            intent.putExtra("card_res_id", remoteMessage.getData().get("card_res_id"));

            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }
}
