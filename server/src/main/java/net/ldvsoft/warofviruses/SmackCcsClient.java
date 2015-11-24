package net.ldvsoft.warofviruses;

import org.json.JSONException;
import org.json.JSONObject;

import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.DefaultExtensionElement;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.StringUtils;
import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocketFactory;

/**
 * Sample Smack implementation of a client for GCM Cloud Connection Server. This
 * code can be run as a standalone CCS client.
 * <p/>
 * <p>For illustration purposes only.
 */
public class SmackCcsClient {

    private static final Logger logger = Logger.getLogger("SmackCcsClient");

    private static final String GCM_SERVER = "gcm.googleapis.com";
    private static final int GCM_PORT = 5235;

    private static final String GCM_ELEMENT_NAME = "gcm";
    private static final String GCM_NAMESPACE = "google:mobile:data";

    static {
        ProviderManager.addExtensionProvider(GCM_ELEMENT_NAME, GCM_NAMESPACE, new ExtensionElementProvider<ExtensionElement>() {
            @Override
            public DefaultExtensionElement parse(XmlPullParser parser, int initialDepth) throws org.xmlpull.v1.XmlPullParserException,
                    IOException {
                String json = parser.nextText();
                return new GcmPacketExtension(json);
            }
        });
    }

    /**
     * Project Id, given by Google APIs
     */
    private String projectId;

    /**
     * Indicates whether the connection is in draining state, which means that it
     * will not accept any new downstream messages.
     */
    protected volatile boolean connectionDraining = false;

    /**
     * XMPP connection to CCS
     */
    private XMPPTCPConnection connection;

    /**
     * Creates a JSON encoded GCM message.
     *
     * @param to             RegistrationId of the target device (Required).
     * @param messageId      Unique messageId for which CCS sends an
     *                       "ack/nack" (Required).
     * @param payload        Message content intended for the application. (Optional).
     * @param collapseKey    GCM collapse_key parameter (Optional).
     * @param timeToLive     GCM time_to_live parameter (Optional).
     * @param delayWhileIdle GCM delay_while_idle parameter (Optional).
     * @return JSON encoded GCM message.
     */
    public static String createJsonMessage(String to, String messageId,
                                           JSONObject payload, String collapseKey, Long timeToLive,
                                           Boolean delayWhileIdle) {
        JSONObject message = new JSONObject();
        message.put("to", to);
        if (collapseKey != null) {
            message.put("collapse_key", collapseKey);
        }
        if (timeToLive != null) {
            message.put("time_to_live", timeToLive);
        }
        if (delayWhileIdle != null && delayWhileIdle) {
            message.put("delay_while_idle", true);
        }
        message.put("message_id", messageId);
        message.put("data", payload);
        return message.toString();
    }

    /**
     * Creates a JSON encoded ACK message for an upstream message received
     * from an application.
     *
     * @param to        RegistrationId of the device who sent the upstream message.
     * @param messageId messageId of the upstream message to be acknowledged to CCS.
     * @return JSON encoded ack.
     */
    protected static String createJsonAck(String to, String messageId) {
        JSONObject message = new JSONObject();
        message.put("message_type", "ack");
        message.put("to", to);
        message.put("message_id", messageId);
        return message.toString();
    }

    //TODO Remove that
//    public static void main(String[] args) throws Exception {
//
//        SmackCcsClient ccsClient = new SmackCcsClient();
//
//        ccsClient.connect(projectId, apiKey);
//
//        // Send a sample hello downstream message to a device.
//        String messageId = ccsClient.nextMessageId();
//        JSONObject payload = new JSONObject();
//        payload.put("Message", "Ahha, it works!");
//        payload.put("CCS", "Dummy Message");
//        payload.put("EmbeddedMessageId", messageId);
//        String collapseKey = "sample";
//        Long timeToLive = 10000L;
//        String message = createJsonMessage(YOUR_PHONE_REG_ID, messageId, payload,
//                collapseKey, timeToLive, true);
//
//        ccsClient.sendDownstreamMessage(message);
//        logger.info("Message sent.");
//
//        //crude loop to keep connection open for receiving messages
//        while (2 + 2 == 2 * 2) {
//        }
//    }

    /**
     * Sends a downstream message to GCM.
     *
     * @return true if the message has been successfully sent.
     */
    public boolean sendDownstreamMessage(String jsonRequest) throws
            NotConnectedException {
        if (!connectionDraining) {
            send(jsonRequest);
            return true;
        }
        logger.info("Dropping downstream message since the connection is draining");
        return false;
    }

    /**
     * Returns a random message id to uniquely identify a message.
     * <p/>
     * <p>Note: This is generated by a pseudo random number generator for
     * illustration purpose, and is not guaranteed to be unique.
     */
    public String nextMessageId() {
        return "m-" + UUID.randomUUID().toString();
    }

    /**
     * Sends a packet with contents provided.
     */
    protected void send(String jsonRequest) throws NotConnectedException {
        Stanza request = new GcmPacketExtension(jsonRequest).toPacket();
        connection.sendStanza(request);
    }

    /**
     * Handles an upstream data message from a device application.
     * <p/>
     * <p>This sample echo server sends an echo message back to the device.
     * Subclasses should override this method to properly process upstream messages.
     */
    protected void handleUpstreamMessage(JSONObject jsonObject) {
        // PackageName of the application that sent this message.
        String category = (String) jsonObject.get("category");
        String from = (String) jsonObject.get("from");
        JSONObject payload = jsonObject.getJSONObject("data");
        payload.put("ECHO", "Application: " + category);

        // Send an ECHO response back
        String echo = createJsonMessage(from, nextMessageId(), payload,
                "echo:CollapseKey", null, false);

        try {
            sendDownstreamMessage(echo);
        } catch (NotConnectedException e) {
            logger.log(Level.WARNING, "Not connected anymore, echo message is not sent", e);
        }

    }

    /**
     * Handles an ACK.
     * <p/>
     * <p>Logs a INFO message, but subclasses could override it to
     * properly handle ACKs.
     */
    protected void handleAckReceipt(JSONObject jsonObject) {
        String messageId = jsonObject.getString("message_id");
        String from = jsonObject.getString("from");
        logger.log(Level.INFO, "handleAckReceipt() from: " + from + ",messageId: " + messageId);
    }

    /**
     * Handles a NACK.
     * <p/>
     * <p>Logs a INFO message, but subclasses could override it to
     * properly handle NACKs.
     */
    protected void handleNackReceipt(JSONObject jsonObject) {
        String messageId = jsonObject.getString("message_id");
        String from = jsonObject.getString("from");
        logger.log(Level.INFO, "handleNackReceipt() from: " + from + ",messageId: " + messageId);
    }

    protected void handleControlMessage(JSONObject jsonObject) {
        logger.log(Level.INFO, "handleControlMessage(): " + jsonObject);
        String controlType = jsonObject.getString("control_type");
        if ("CONNECTION_DRAINING".equals(controlType)) {
            connectionDraining = true;
        } else {
            logger.log(Level.INFO, "Unrecognized control type: %s. This could happen if new features are " + "added to the CCS protocol.",
                    controlType);
        }
    }

    /**
     * Connects to GCM Cloud Connection Server using the supplied credentials.
     *
     * @param senderId Your GCM project number
     * @param apiKey   API Key of your project
     */
    public void connect(String senderId, String apiKey)
            throws XMPPException, IOException, SmackException {
        projectId = senderId;
        XMPPTCPConnectionConfiguration config =
                XMPPTCPConnectionConfiguration.builder()
                        .setServiceName(GCM_SERVER)
                        .setHost(GCM_SERVER)
                        .setCompressionEnabled(false)
                        .setPort(GCM_PORT)
                        .setConnectTimeout(30000)
                        .setSecurityMode(SecurityMode.disabled)
                        .setSendPresence(false)
                        .setSocketFactory(SSLSocketFactory.getDefault())
                        .build();

        connection = new XMPPTCPConnection(config);

        //disable Roster as I don't think this is supported by GCM
        Roster roster = Roster.getInstanceFor(connection);
        roster.setRosterLoadedAtLogin(false);

        logger.info("Connecting...");
        connection.connect();

        connection.addConnectionListener(new LoggingConnectionListener());

        // Handle incoming packets
        connection.addAsyncStanzaListener(new MyStanzaListener(), new MyStanzaFilter());

        // Log all outgoing packets
        connection.addPacketInterceptor(new MyStanzaInterceptor(), new MyStanzaFilter());

        connection.login(senderId + "@gcm.googleapis.com", apiKey);

    }

    /**
     * XMPP Packet Extension for GCM Cloud Connection Server.
     */
    private static final class GcmPacketExtension extends DefaultExtensionElement {

        private final String json;

        public GcmPacketExtension(String json) {
            super(GCM_ELEMENT_NAME, GCM_NAMESPACE);
            this.json = json;
        }

        public String getJson() {
            return json;
        }

        @Override
        public String toXML() {
            return String.format("<%s xmlns=\"%s\">%s</%s>",
                    GCM_ELEMENT_NAME, GCM_NAMESPACE,
                    StringUtils.escapeForXML(json), GCM_ELEMENT_NAME);
        }

        public Stanza toPacket() {
            Message message = new Message();
            message.addExtension(this);
            return message;
        }
    }

    private static final class LoggingConnectionListener
            implements ConnectionListener {

        @Override
        public void connected(XMPPConnection xmppConnection) {
            logger.info("Connected.");
        }

        @Override
        public void authenticated(XMPPConnection connection, boolean resumed) {
            //Do nothing
        }


        @Override
        public void reconnectionSuccessful() {
            logger.info("Reconnecting..");
        }

        @Override
        public void reconnectionFailed(Exception e) {
            logger.log(Level.INFO, "Reconnection failed.. ", e);
        }

        @Override
        public void reconnectingIn(int seconds) {
            logger.log(Level.INFO, "Reconnecting in %d secs", seconds);
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            logger.info("Connection closed on error.");
        }

        @Override
        public void connectionClosed() {
            logger.info("Connection closed.");
        }
    }

    private class MyStanzaFilter implements StanzaFilter {

        @Override
        public boolean accept(Stanza stanza) {
            if (stanza.getTo() != null)
                if (stanza.getTo().startsWith(projectId))
                    return true;
            return false;
        }
    }

    private class MyStanzaListener implements StanzaListener {

        @Override
        public void processPacket(Stanza packet) {
            logger.log(Level.INFO, "Received: " + packet.toXML());
            Message incomingMessage = (Message) packet;
            GcmPacketExtension gcmPacket =
                    (GcmPacketExtension) incomingMessage.
                            getExtension(GCM_NAMESPACE);
            String json = gcmPacket.getJson();
            try {
                JSONObject jsonObject = new JSONObject(json);

                // present for "ack"/"nack", null otherwise
                Object messageType = jsonObject.get("message_type");

                if (messageType == null) {
                    // Normal upstream data message
                    handleUpstreamMessage(jsonObject);

                    // Send ACK to CCS
                    String messageId = (String) jsonObject.get("message_id");
                    String from = (String) jsonObject.get("from");
                    String ack = createJsonAck(from, messageId);
                    send(ack);
                } else if ("ack".equals(messageType.toString())) {
                    // Process Ack
                    handleAckReceipt(jsonObject);
                } else if ("nack".equals(messageType.toString())) {
                    // Process Nack
                    handleNackReceipt(jsonObject);
                } else if ("control".equals(messageType.toString())) {
                    // Process control message
                    handleControlMessage(jsonObject);
                } else {
                    logger.log(Level.WARNING,
                            "Unrecognized message type (%s)",
                            messageType.toString());
                }
            } catch (JSONException e) {
                logger.log(Level.SEVERE, "Error parsing JSON " + json, e);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to process packet", e);
            }
        }

    }

    private class MyStanzaInterceptor implements StanzaListener {
        @Override
        public void processPacket(Stanza packet) {
            logger.log(Level.INFO, "Sent: {0}", packet.toXML());
        }

    }
}