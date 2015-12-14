package net.ldvsoft.warofviruses;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Сева on 13.12.2015.
 */
public class ClientNetworkPlayer extends Player {
    private Context context;
    private GoogleCloudMessaging gcm;
    private BroadcastReceiver receiver;

    ClientNetworkPlayer(User user, GameLogic.PlayerFigure ownFigure, Context context) {
        this.user = user;
        this.ownFigure = ownFigure;
        this.context = context;
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle data = intent.getBundleExtra(WoVPreferences.TURN_BUNDLE);
                GameEvent.deserialize(data.getInt(WoVProtocol.TURN_TYPE),
                        data.getInt(WoVProtocol.TURN_X), data.getInt(WoVProtocol.TURN_Y));
            }
        };
        context.registerReceiver(receiver, new IntentFilter(WoVPreferences.TURN_BROADCAST));
    }

    @Override
    public void makeTurn() {
        //nothing to do
    }

    @Override
    public void onGameStateChanged(GameEvent event) {
        Bundle data = new Bundle();
        data.putString(WoVProtocol.ACTION, WoVProtocol.ACTION_TURN);
        String id = UUID.randomUUID().toString();
        try {
            gcm.send(context.getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com", id, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
