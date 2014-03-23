package saulmm.wear.example;

import android.app.Activity;
import android.app.Notification;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preview.support.v4.app.NotificationManagerCompat;
import android.preview.support.wearable.notifications.WearableNotifications;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONObject;

import static android.util.Log.d;


/**
 * Created by wtf on 22/03/14.
 */
public class NotificationReceiver extends Activity {
    private NotificationManagerCompat mNotificationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView text = new TextView(this);
        text.setText("Example...");

        setContentView(text);
    }


    public void showNotification(String content) {
        mNotificationManager = NotificationManagerCompat.from(this);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
//
//        Bitmap notificationBackground = Bitmap.createScaledBitmap(
//                Tools.loadBitmapAsset(this, "appengine.png"),
//                280, 280, false);
//
//        builder.setLargeIcon(notificationBackground); // fondo
        builder.setContentTitle("Respuesta..."); // titulo de la notificacion
        builder.setContentText(content); // contenido de la notificacion
        builder.setSmallIcon(R.drawable.ic_launcher);

        Notification notification = new WearableNotifications.Builder(builder)
                .build();

        mNotificationManager.notify(14, notification);
    }
}
