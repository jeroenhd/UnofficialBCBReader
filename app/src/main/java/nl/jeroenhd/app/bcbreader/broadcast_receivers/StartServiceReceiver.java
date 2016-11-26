package nl.jeroenhd.app.bcbreader.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Starts the intent service when the device boots and when the time changes
 */
public class StartServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        // Find out why we're receiving a broadcast
        String action = intent.getAction();

        Log.d("UpdateOnReceive", "Received an intent! Processing...");

        // This can happen some times according to stack overflow
        // Probably some app sending out random Intents
        // If the action is null, just return
        if (action == null) {
            Log.e("UpdateOnReceive", "Somehow a null intent was received");
            return;
        }

        // If the action is not an action we should handle, log an error and return
        if (!action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.e("UpdateOnReceive", "Invalid action was passed to the StartServiceReceiver (why did it trigger???)");
            return;
        }

        UpdateEventReceiver.setupAlarm(context);
    }
}
