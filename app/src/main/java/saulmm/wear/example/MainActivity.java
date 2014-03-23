package saulmm.wear.example;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preview.support.v4.app.NotificationManagerCompat;
import android.preview.support.wearable.notifications.RemoteInput;
import android.preview.support.wearable.notifications.WearableNotifications;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

import static android.util.Log.d;

public class MainActivity extends ActionBarActivity {
    public static int WEARABLE_MENU= 13;
    public static int APP_ENGINE_RESULT_NOTIFICATION = 14;

    private static final String ACTION_RESPONSE = "saulmm.wear.example.REPLY";
    public static final String EXTRA_REPLY = "reply";

    private BroadcastReceiver mReceiver;
    private NotificationManagerCompat mNotificationManager;

    // UI


    /**
     * Podría usar el sistema de google endpoints integrado en Android Studio, pero por simplicidad
     * y por querer hacer el ejemplo enfatizado al 'wear sdk' hago la peticion directamente.
     *
     * para mas info: https://developers.google.com/appengine/docs/java/endpoints/
     */
    public static final String APP_ENGINE_API = "https://gdgweartest.appspot.com/_ah/api/gdgwearableapi/v1/greetmeber";
    private ProgressDialog pDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNotificationManager = NotificationManagerCompat.from(this);

        // Receiver que recibe los intents del speech del wearable
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                processName(intent);
            }
        };

        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Registro del receiver
        registerReceiver(mReceiver, new IntentFilter(ACTION_RESPONSE));
    }

    /**
     * Metodo para inicializar el layout del dispositivo móvil, no del wearable, los elementos gráficos
     * del wearable se gestionan con notificaciones.
     *
     * Las notificaciones de android apararecen en el stream principal y forman el nucleo de la experiencia de Android Wear .
     * Muchas de las principales Android Design guidelines para notificacioens se aplican en Android Wear.
     */
    private void initUI() {
         pDialog = new ProgressDialog(this);
        pDialog.setIndeterminate(true);
        pDialog.setMessage("Haciendo peticion a AppEngine");

        Button startButton = (Button) findViewById(R.id.start);

        final AlertDialog.Builder dialog = new AlertDialog.Builder(this)
            .setTitle("Wearable")
            .setMessage("El menú se debería de estar mostrando en el wearable")
            .setPositiveButton("Aceptar", null);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAppEngineMenu();
                dialog.show();
            }
        });
    }

    /**
     * Construye la notificacion a modo de menu para el wearable, en base a los titulos
     * y descripciones almacenados en un array en values/string.xml, las notificaciones
     * se encadenan en una lista a modo de páginas, luego se crea la notificacion final
     * albergando dichas páginas y mediante el NotificationManager se lanza al dispositivo.
     *
     * Al mostrar la notificación el wearable automáticamente
     * mostrará la notificción a modo de menu.
     *
     * Las acciones se mandan como PendingIntent, en este caso, se crea un pending intent
     * con un intent que redirige a la actividad NotificationReceiver, que es la encargada de
     * mandar la petición a AppEngine.
     */
    private void showAppEngineMenu() {
        String [] titles = this.getResources().getStringArray(R.array.titulos);
        String [] descs = this.getResources().getStringArray(R.array.descs);

        // Las notificaciones se encadenan en una lista a modo de páginas de un menu
        ArrayList<Notification> notificationPages = new ArrayList<Notification>();

        // Cada pagina es una notificacion
        for(int i = 1; i < titles.length; i++) {
            NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
            style.setBigContentTitle(titles[i]); // titulo
            style.bigText(descs[i]); // contenido
            style.setSummaryText("");

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setSmallIcon(R.drawable.adt);
            builder.setStyle(style);
            notificationPages.add(builder.build());
        }



        Bitmap notificationBackground = Bitmap.createScaledBitmap(
                Tools.loadBitmapAsset(this, "background.png"),
                512, 748, false);

        // Pending intent a modo de broadcast, sera recogido por el mReceiver, registrado previamente
        Intent talkIntent = new Intent(ACTION_RESPONSE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, talkIntent,
        PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_CANCEL_CURRENT);

        // Notificacion principal
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setLargeIcon(notificationBackground); // fondo
        builder.setContentTitle(titles[0]); // titulo
        builder.setContentText(descs[0]); // contenido

        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.drawable.ic_launcher);

        // Se usa el wearable sdk para crear la notificación paginada.
        Notification notification = new WearableNotifications.Builder(builder)
            .setMinPriority().addRemoteInputForContentIntent(new RemoteInput.Builder(EXTRA_REPLY)
            .setLabel("Tu nombre ? ").build())
            .addPages(notificationPages)
            .build();

        // El NotificationManager lanza la notificación al dispositivo, y por consiguiente, al wearable.
        mNotificationManager.notify(WEARABLE_MENU, notification);
    }


    /**
     * Metodo disparado por el receiver
     * @param intent, Intent con los datos del speech en el wearable
     */
    private void processName(Intent intent) {
        String text = intent.getStringExtra(EXTRA_REPLY);

        if (text != null && !text.equals("")) {
            JsonObject jsonBody = new JsonObject();
            jsonBody.addProperty("message", text);

            // Ion es quien hace el post a la api en appengine
            pDialog.show();
            Ion.with(this, APP_ENGINE_API)
                .setJsonObjectBody(jsonBody).asJsonObject()
                .setCallback(appEngineCallback);
        }
    }

    /**
     * El callback es llamado por Ion cuando termina la petición post contra AppEngine
     */
    FutureCallback<JsonObject> appEngineCallback = new FutureCallback<JsonObject>() {
        @Override
        public void onCompleted(Exception e, JsonObject result) {
            pDialog.dismiss();

            if (result != null) {
                showAppEngineResultNotification(result.get("message") + "");
            }
        }
    };


    /**
     * Muestra una nueva notificación con la respuesta de AppEngine
     * @param content, respuesta de appengine
     */
    private void showAppEngineResultNotification(String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        Bitmap notificationBackground = Bitmap.createScaledBitmap(
                Tools.loadBitmapAsset(this, "appengine.png"),
                280, 280, false);

        builder.setLargeIcon(notificationBackground); // fondo
        builder.setContentTitle("Respuesta..."); // titulo de la notificacion
        builder.setContentText(content); // contenido de la notificacion
        builder.setSmallIcon(R.drawable.ic_launcher);

        Notification notification = new WearableNotifications.Builder(builder)
                .build();

        mNotificationManager.notify(14, notification);
    }
}
