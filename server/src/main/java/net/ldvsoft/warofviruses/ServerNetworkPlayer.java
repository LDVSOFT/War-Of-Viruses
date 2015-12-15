package net.ldvsoft.warofviruses;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Created by ldvsoft on 14.12.15.
 */
public class ServerNetworkPlayer extends Player {
    private static final Gson gson = new Gson();

    private final User opponent;
    private WarOfVirusesServer server;

    public ServerNetworkPlayer(User user, User opponent, WarOfVirusesServer server, GameLogic.PlayerFigure figure) {
        this.user = user;
        this.opponent = opponent;
        this.server = server;
        this.ownFigure = figure;
    }

    @Override
    public void makeTurn() {
        //Nothing to do
    }

    @Override
    public void onGameStateChanged(GameEvent event) {
        JsonObject message = new JsonObject();
        message.add(WoVProtocol.EVENT, gson.toJsonTree(event));
        server.sendToUser(user, WoVProtocol.ACTION_TURN, message);
    }

    @Override
    public void setGame(Game game) {
        super.setGame(game);
        //Send client that game has started
        JsonObject message = new JsonObject();
        message.add(WoVProtocol.MY_FIGURE , gson.toJsonTree(ownFigure));
        message.add(WoVProtocol.GAME_ID, gson.toJsonTree(game.getGameId()));
        switch (ownFigure) {
            case CROSS:
                message.add(WoVProtocol.CROSS_USER, gson.toJsonTree(user));
                message.add(WoVProtocol.ZERO_USER , gson.toJsonTree(opponent));
                break;
            case ZERO:
                message.add(WoVProtocol.ZERO_USER , gson.toJsonTree(user));
                message.add(WoVProtocol.CROSS_USER, gson.toJsonTree(opponent));
                break;
        }
        message.add(WoVProtocol.TURN_ARRAY, new JsonArray());
        server.sendToUser(user, WoVProtocol.GAME_LOADED, message);
    }

    public void performMove(JsonObject message) {
        GameEvent event = gson.fromJson(message.get(WoVProtocol.EVENT), GameEvent.class);
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
