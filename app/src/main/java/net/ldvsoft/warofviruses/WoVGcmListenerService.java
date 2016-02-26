package net.ldvsoft.warofviruses;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.UUID;

public class WoVGcmListenerService extends GcmListenerService {

    private static final String TAG = "WoVGcmListenerService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.i(TAG, "From: " + from);
        Log.i(TAG, "Message: " + data.toString());
        String action = (String) data.get(WoVProtocol.ACTION);
        Intent intent = new Intent();
        intent.putExtra(WoVPreferences.BUNDLE, data);
        switch (action) {
            case WoVProtocol.ACTION_TURN:
                intent.setAction(WoVPreferences.TURN_BROADCAST);
                break;
            case WoVProtocol.GAME_LOADED:
                intent.setAction(WoVPreferences.MAIN_BROADCAST);
                break;
        }
        sendBroadcast(intent);
    }

    public static void sendGcmMessage(Context context, String action, JsonObject data) {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        String id = "m-" + UUID.randomUUID().toString();
        Bundle message = new Bundle();
        message.putString(WoVProtocol.ACTION, action);
        if (data != null) {
            message.putString(WoVProtocol.DATA, data.toString());
        }
        try {
            Log.d(TAG, "SENDING " + action + " TO " + context.getString(R.string.gcm_defaultSenderId) + " ID " + id);
            gcm.send(context.getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com", id, (long) 0, message);
            Log.d(TAG, "OVER?");
        } catch (IOException e) {
            Log.wtf(TAG, "Something really wrong:", e);
        }
    }

}