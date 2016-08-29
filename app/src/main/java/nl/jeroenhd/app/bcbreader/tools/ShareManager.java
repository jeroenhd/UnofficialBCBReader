package nl.jeroenhd.app.bcbreader.tools;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import nl.jeroenhd.app.bcbreader.R;
import nl.jeroenhd.app.bcbreader.data.SuperSingleton;

/**
 * A class to easily share an image with some text
 */

public class ShareManager {

    /**
     * Share an image and add a tag line
     *
     * @param context          The context to share from
     * @param image            The image to share
     * @param message          The message to add to the image
     * @param shareIntentTitle The title of the intent picker
     */
    private static void ShareImageWithText(Context context, Bitmap image, String message, String shareIntentTitle) {
        Intent shareIntent = new Intent();

        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, message);
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);

        // Have sharing the full image depend on a user preference
        // Most chat apps already preview pages
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("share_upload_image", false)) {
            Uri pictureUri = getBitmapUri(context, image);
            shareIntent.putExtra(Intent.EXTRA_STREAM, pictureUri);
        }
        shareIntent.setType("image/png");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(shareIntent, shareIntentTitle));
    }

    /**
     * Share an online image and add a tag line.
     * Warning: this method is asynchronous! It adds an element to the Volley queue.
     *
     * @param context          The context to share from
     * @param url              The URL of the image to share
     * @param message          The message to add to the image
     * @param shareIntentTitle The title of the intent picker
     * @param errorListener    An error listener in case something goes wrong downloading the image
     */
    public static void ShareImageWithText(final Context context, String url, final String message, final String shareIntentTitle, final Response.ErrorListener errorListener) {
        SuperSingleton superSingleton = SuperSingleton.getInstance(context);
        superSingleton.getImageLoader().get(url, new ImageLoader.ImageListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Call our parents
                errorListener.onErrorResponse(error);
            }

            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                ShareImageWithText(context, response.getBitmap(), message, shareIntentTitle);
            }
        });
    }

    /**
     * Store a BitmapDrawable and create a share URI.
     *
     * @param bitmap The bitmap to share
     * @return An URI pointing to the drawable, which is shareable with other apps
     */
    private static Uri getBitmapUri(Context context, Bitmap bitmap) {
        if (bitmap == null)
            return null;

        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "bcbshare_" + System.currentTimeMillis() + ".png");
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return Uri.fromFile(file);
    }

    /**
     * Get a stupid pun/comment the user should replace with something they want
     * @return A string, probably a pun
     */
    public static String getStupidPhrase(Context context) {
        String[] stupidPhrases = context.getResources().getStringArray(R.array.stupid_sharing_phrases);

        // The documentation says this number is always positive
        // It is not.
        // That's what you get when you use Java
        int index = new Random().nextInt(stupidPhrases.length);
        return stupidPhrases[index];
    }
}
