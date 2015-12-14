package net.ldvsoft.warofviruses;

import com.google.gson.JsonObject;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static net.ldvsoft.warofviruses.WoVProtocol.ACTION;
import static net.ldvsoft.warofviruses.WoVProtocol.ACTION_PING;
import static net.ldvsoft.warofviruses.WoVProtocol.PING_ID;
import static net.ldvsoft.warofviruses.WoVProtocol.RESULT;
import static net.ldvsoft.warofviruses.WoVProtocol.RESULT_FAILURE;
import static net.ldvsoft.warofviruses.WoVProtocol.RESULT_SUCCESS;

public final class WarOfVirusesServer {
    protected static final JsonObject JSON_RESULT_FAILURE = new JsonObject();

    static {
        JSON_RESULT_FAILURE.addProperty(RESULT, RESULT_FAILURE);
    }

    private static final String DEFAULT_CONFIG_FILE = "/etc/war-of-viruses-server.conf";

    private Logger logger;

    private Properties config = new Properties();

    private GCMHandler gcmHandler;

    public String getSetting(String name) {
        return config.getProperty(name, "");
    }

    /**
     * Process incoming via GCM message from client.
     * If simple answer is required, returns an answer.
     * May return null, but then client will get generic answer.
     * @param message Message from the client.
     * @return (Optional) Answer to client.
     */
    public JsonObject processGCM(JsonObject message) {
        String action = message.get("data").getAsJsonObject().get(ACTION).getAsString();
        switch (action) {
            case ACTION_PING:
                return processPing(message);
            default:
                return null;
        }
    }

    private JsonObject processPing(JsonObject message) {
        String sender = message.get("from").getAsString();
        String messageId = message.get("message_id").getAsString();

        JsonObject answer = new JsonObject();
        answer.addProperty(RESULT, RESULT_SUCCESS);
        answer.addProperty(PING_ID, messageId);

        gcmHandler.sendDownstreamMessage(SmackCcsClient.createJsonMessage(
                        sender,
                        gcmHandler.nextMessageId(),
                        answer,
                        null,
                        null,
                        false,
                        "high")
        );
        return null;
    }

    public void run() {
        String configFile = System.getenv("CONFIG");
        if (configFile == null) {
            configFile = DEFAULT_CONFIG_FILE;
        }

        logger = Logger.getLogger(WarOfVirusesServer.class.getName());
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream(configFile));
            config.load(new FileReader(configFile));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Cannot open config file or it's contents are wrong.", e);
            System.exit(1);
        }
        logger = Logger.getLogger(WarOfVirusesServer.class.getName());

        try {
            gcmHandler = new GCMHandler(this);

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    gcmHandler.stop();
                }
            }));

            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Server failed", e);
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        WarOfVirusesServer instance = new WarOfVirusesServer();
        instance.run();
    }
}
