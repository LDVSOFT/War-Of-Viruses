package net.ldvsoft.warofviruses;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Created by ldvsoft on 14.12.15.
 */
public class ServerNetworkPlayer extends Player {
    private WarOfVirusesServer server;

    public ServerNetworkPlayer(User user, User opponent, WarOfVirusesServer server, GameLogic.PlayerFigure figure) {
        this.user = user;
        this.server = server;
        this.ownFigure = figure;
        //Send client that game has started
        JsonObject message = new JsonObject();
        message.add(WoVProtocol.MY_FIGURE , new Gson().toJsonTree(figure));
        switch (figure) {
            case CROSS:
                message.add(WoVProtocol.CROSS_USER, new Gson().toJsonTree(user));
                message.add(WoVProtocol.ZERO_USER , new Gson().toJsonTree(opponent));
                break;
            case ZERO:
                message.add(WoVProtocol.ZERO_USER , new Gson().toJsonTree(user));
                message.add(WoVProtocol.CROSS_USER, new Gson().toJsonTree(opponent));
                break;
        }
        message.add(WoVProtocol.TURN_ARRAY, new JsonArray());
        server.sendToUser(user, WoVProtocol.GAME_LOADED, message);
    }

    @Override
    public void makeTurn() {
        //Nothing to do
    }

    @Override
    public void onGameStateChanged(GameEvent event) {
        JsonObject message = new JsonObject();
        message.add(WoVProtocol.EVENT, new Gson().toJsonTree(event));
        server.sendToUser(user, WoVProtocol.ACTION_TURN, message);
    }

    public void performMove(JsonObject message) {
        GameEvent event = new Gson().fromJson(message.get(WoVProtocol.EVENT), GameEvent.class);
        switch (event.type) {
            case TURN_EVENT:
                game.doTurn(this, event.getTurnX(), event.getTurnY());
                break;
            case GIVE_UP_EVENT:
                game.giveUp(this);
                break;
            case SKIP_TURN_EVENT:
                game.skipTurn(this);
                break;
        }
    }
}
