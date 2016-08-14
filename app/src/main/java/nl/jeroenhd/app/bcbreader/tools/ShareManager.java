package nl.jeroenhd.app.bcbreader.tools;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

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
        Uri pictureUri = getBitmapUri(context, image);
        Intent shareIntent = new Intent();

        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);
        shareIntent.putExtra(Intent.EXTRA_STREAM, pictureUri);
        shareIntent.setType("image/*");
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
    public static String getStupidPhrase() {
        // Stolen from the BCB site
        String[] stupidPhrases =
                new String[]{"protect this cat at all costs",
                        "This BCB page is the cat’s pajamas! It left me feline purretty great!",
                        "*screaming internally* DAISY.......",
                        "If I had to rate this page of Bittersweet Candy Bowl it would be canine out of ten!",
                        "You know, if you guys aren’t reading Bittersweet Candy Bowl then it’s claws for concern!",
                        "Stop purrcrastinating and read this Bittersweet Candy Bowl page! It’s mewtiful!",
                        "Reading this Bittersweet Candy Bowl page will claws you to to experience true mewphoria!",
                        "★ ★ LUCY ABUSE MASTERPOST ★ ★",
                        "I still think BCB’s Mike is highly problematic.",
                        "protect this cat at all costs",
                        "Whenever I have a ruff day Bittersweet Candy Bowl is always there to cheer me pup!",
                        "aesthetic",
                        "omG AUGUSTUS i’m cry",
                        "daisy is a precious cinnamon roll too sweet too pure for this world",
                        "LOOK AT HIM.... MY BEAUTIFUL TRASH BF",
                };

        // The documentation says this number is always positive
        // It is not.
        // That's what you get when you use Java
        int index = new Random().nextInt(stupidPhrases.length);
        return stupidPhrases[index];
    }
}
