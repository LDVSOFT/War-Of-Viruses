package net.ldvsoft.warofviruses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Сева on 13.12.2015.
 */
public class ClientNetworkPlayer extends Player {
    private Context context;
    private GoogleCloudMessaging gcm = new GoogleCloudMessaging();
    private BroadcastReceiver receiver;

    public ClientNetworkPlayer(User user, GameLogic.PlayerFigure ownFigure, Context context) {
        this.user = user;
        this.ownFigure = ownFigure;
        this.context = context;
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String data = intent.getBundleExtra(WoVProtocol.GAME_BUNDLE).getString(WoVProtocol.DATA);
                JsonObject jsonData = (JsonObject) new JsonParser().parse(data);
                GameEvent event = GameEvent.deserialize(new Gson().fromJson(jsonData.get(WoVProtocol.TURN_TYPE), int.class),
                        new Gson().fromJson(jsonData.get(WoVProtocol.TURN_X), int.class),
                        new Gson().fromJson(jsonData.get(WoVProtocol.TURN_Y), int.class));
                switch (event.type) {
                    case TURN_EVENT:
                        game.doTurn(ClientNetworkPlayer.this, event.getTurnX(), event.getTurnY());
                        break;
                    case GIVE_UP_EVENT:
                        game.giveUp(ClientNetworkPlayer.this);
                        break;
                    case SKIP_TURN_EVENT:
                        game.skipTurn(ClientNetworkPlayer.this);
                        break;
                }
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
        data.putInt(WoVProtocol.TURN_TYPE, event.getEventTypeAsInt());
        data.putInt(WoVProtocol.TURN_X, event.getTurnX());
        data.putInt(WoVProtocol.TURN_Y, event.getTurnY());
        try {
            gcm.send(context.getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com", id, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
