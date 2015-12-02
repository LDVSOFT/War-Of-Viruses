package net.ldvsoft.warofviruses;

import org.json.JSONException;
import org.json.JSONObject;

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
    protected static final JSONObject JSON_RESULT_FAILURE = new JSONObject().put(RESULT, RESULT_FAILURE);

    private static final String DEFAULT_CONFIG_FILE = "/etc/war-of-viruses-server.conf";

    private Logger logger;

    private Properties config = new Properties();

    private GCMHandler gcmHandler;

    public String getSetting(String name, String defaultValue) {
        return config.getProperty(name, defaultValue);
    }

    /**
     * Process incoming via GCM message from client.
     * If simple answer is required, returns an answer.
     * May return null, but then client will get generic answer.
     * @param message Message from the client.
     * @return (Optional) Answer to client.
     */
    public JSONObject processGCM(JSONObject message) {
        try {
            String action = message.getJSONObject("data").getString(ACTION);
            switch (action) {
                case ACTION_PING:
                    return processPing(message);
                default:
                    return null;
            }
        } catch (JSONException e) {
            logger.log(Level.WARNING, "Wrong message format", e);
            return null;
        }
    }

    private JSONObject processPing(JSONObject message) {
        String sender = message.getString("from");
        String messageId = message.getString("message_id");

        JSONObject answer = new JSONObject()
                .put(RESULT, RESULT_SUCCESS)
                .put(PING_ID, messageId);

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
