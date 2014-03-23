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


##### Under construction
Meanwhile: https://docs.google.com/document/d/17ShYiByxmJoAHYSVfYJqKAxTrYWflKsVjgZCuxAMPWg/edit#
