Android-Wearable-example
========================

### Preview Video

- [Video](https://drive.google.com/file/d/0B62SZ3WRM2R2SVZRMTlseW1mWGM/edit?usp=sharing)


### Motivation

After the recent launch of 'wear sdk' for 'wearables' I have  decided to make a small proof of concept showing how it works this sdk, I have decided to integrate well with a small project in AppEngine, showing so, google cloud endpoints.

### Configuring the development environment 

First of all, you need to have Android sdk installed also need to be part of the Preview for wear developers to download the [Android Wear Preview](http://developer.android.com/wear/preview/signup.html) application that will serve our linking our device with the _wearable_ emulator, then from the SDK Manager, you need to be installed the SDK tools reivision 22.6 or higher version, you must also install the Android system image ARM EABI Wear v7a system Image, also make sure the packets _Google Android Support Library_ and the _Google Repository_ are installed.

Now yes and we can create our image emulator 'wearable' I do not want to focus too much on this part as it is excellently detailed [here](http://developer.android.com/wear/preview/start.html)

We have to have our usb device connected to your machine, and running the emulator, fire the following command:

```adb -d forward tcp:5601 tcp:5601```

With this, we will have our device linked with the emulator, adding the appropriate libraries to the project we are ready to start:

```javscript
dependencies {
    compile 'com.koushikdutta.ion:ion:1.2.4'
    compile 'com.android.support:appcompat-v7:+'
    compile 'com.android.support:support-v4:19.0.+'
    compile fileTree(dir: 'libs', include: ['*.jar'])
    compile files('libs/wearable-preview-support.jar')
}
```


To not make it too complex, I decided to create only one activity, remember that when an Android device such as a phone or a tablet, is connected to a wearable device , all notifications are shared between devices in wearable , all notifications appear as letters in the stream context .

The project will run as follows, in the activity of the device (phone or tablet), you have a button , when pressed will raise a notification is displayed on the wearable, such notice will be a menu with different pages, in the last we have a button that when pressed will ask us our name, we will give voice to the wearable (if there is an emulator that enter with the keyboard), the name will be sent to a simple api in AppEngine , and greeting us AppEngine return a message, what fun right?


. Here the apk file:
https://drive.google.com/file/d/0B62SZ3WRM2R2Snpmcm5DTFNxUGs/edit?usp=sharing


To create pages, what we do is add a series of notifications to a list, then the list will use it to add it to the principal notification that will be used to display the set of pages in the wearable .

```java

     
        // The notifications will be joined in a list as pages in a menu
        ArrayList<Notification> notificationPages = new ArrayList<Notification>();

        // Each page is a notification
        for(int i = 1; i < titles.length; i++) {
            NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
            style.setBigContentTitle(titles[i]); // title
            style.bigText(descs[i]); // content
            style.setSummaryText("");

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setSmallIcon(R.drawable.adt);
            builder.setStyle(style);
            notificationPages.add(builder.build());
        }

```

Initially, we need a BroadcastReceiver to handle the events that come to us from wearable for example when the user enters text, (when we ask the name), this BroadcastReceiver will be recorded at the beginning of the activity. 

The first paragraph of code talk about it, we need a PendingIntent that will work for BroadCast to capture events of wearable text input [PendingIntents](http://developer.android.com/reference/android/app/PendingIntent.html)

The second paragraph is more focused on the notification itself, setting its title, content, etc., in the last paragraph, we can see that the builder of notification is used to create actual notification of wearable, now adding the preconfigured pages and some parameters to indicate that the last card is where you will enter your name.

``` java
        Intent talkIntent = new Intent(ACTION_RESPONSE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, talkIntent,
        PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_CANCEL_CURRENT);

        // Main notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setLargeIcon(notificationBackground); // fondo
        builder.setContentTitle(titles[0]); // titulo
        builder.setContentText(descs[0]); // contenido
        
        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.drawable.ic_launcher);

        Notification notification = new WearableNotifications.Builder(builder)
            .setMinPriority().addRemoteInputForContentIntent(new    RemoteInput.Builder(EXTRA_REPLY)
            .setLabel("Tu nombre ? ").build())
            .addPages(notificationPages)
            .build();

        mNotificationManager.notify(WEARABLE_MENU, notification);.
``` 

This is the method triggered by the receiver, after the user has entered his name in the last card, thanks to the [Ion library](https://github.com/koush/ion) by [Koushik Dutta](https://github.com/koush), I make an http request to my endpoint of Google App Engine, with the name I get the wearable, the callback will be ````appEngineCallback()````

```java

    /**
     * Method trigered by the receiver
     * @param intent
     */
    private void processName(Intent intent) {
        String text = intent.getStringExtra(EXTRA_REPLY);

        if (text != null && !text.equals("")) {
            JsonObject jsonBody = new JsonObject();
            jsonBody.addProperty("message", text);

            Ion.with(this, APP_ENGINE_API)
                .setJsonObjectBody(jsonBody).asJsonObject()
                .setCallback(appEngineCallback);
        }
    }
```

The ````showAppEngineResultNotification()``` method from callback receives the http request, the message server, which is shown in the wearable by a further notification

```java
    /**
     * The callback is called by the Ion Library when the post request ends
     */
    FutureCallback<JsonObject> appEngineCallback = new FutureCallback<JsonObject>() {
        @Override
        public void onCompleted(Exception e, JsonObject result) {
            if (result != null) {
                showAppEngineResultNotification(result.get("message") + "");
            }
        }
    };


    /**
     * Show a notification with the AppEngine Request response
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
````
### AppEngine

Respect AppEngine has been developed with a small api writted python:

```python
import endpoints
from protorpc import messages
from protorpc import remote

package = 'Hello'

class GDGWord(messages.Message):
    """GDGWord that stores a message."""
    message = messages.StringField(1)

@endpoints.api(name='gdgwearableapi', version='v1')
class GDGApi(remote.Service):
    """GDG API v1."""

    MULTIPLY_METHOD_RESOURCE = endpoints.ResourceContainer(GDGWord)

    @endpoints.method(MULTIPLY_METHOD_RESOURCE, GDGWord,
                      path='greetmeber', http_method='POST',
                      name='wear.greetgdgmember')

    def greetings_multiply(self, request):
        return GDGWord(message= "Hello: %s, is nice to see you :D" % request.message)


APPLICATION = endpoints.ap
```
