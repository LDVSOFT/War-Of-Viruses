package net.ldvsoft.warofviruses;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by ldvsoft on 24.11.15.
 */
public class GCMHandler extends SmackCcsClient {
    protected Logger logger = Logger.getLogger(GCMHandler.class.getName());
    protected WarOfVirusesServer server;

    public GCMHandler(WarOfVirusesServer server) throws XMPPException, IOException, SmackException {
        this.server = server;

        String projectId = server.getSetting("google.projectId");
        String apiKey = server.getSetting("google.apiKey");
        String gcmServer = server.getSetting("gcm.server");
        int gcmPort = Integer.parseInt(server.getSetting("gcm.port"));
        connect(projectId, apiKey, gcmServer, gcmPort);
    }

    @Override
    public boolean sendDownstreamMessage(String jsonRequest) {
        try {
            return super.sendDownstreamMessage(jsonRequest);
        } catch (SmackException.NotConnectedException e) {
            logger.log(Level.WARNING, "Why connection is closed?", e);
            return false;
        }
    }

    @Override
    protected void handleUpstreamMessage(JsonObject message) {
        super.handleUpstreamMessage(message);
        JsonObject answer = server.processGCM(message);
        if (answer != null) {
            sendDownstreamMessage(SmackCcsClient.createJsonMessage(
                    message.get("from").getAsString(),
                    nextMessageId(),
                    answer,
                    null,
                    null,
                    false,
                    "high"
            ));
        }
    }

    public void stop() {
        disconnect();
    }
}
