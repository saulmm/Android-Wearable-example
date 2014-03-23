package saulmm.wear.example;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by wtf on 22/03/14.
 */
public class Tools {

    public static Bitmap loadBitmapAsset(Context context, String asset) {
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            is = context.getAssets().open(asset);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (is != null) {
            bitmap = BitmapFactory.decodeStream(is);
        }
        return bitmap;
    }
}
