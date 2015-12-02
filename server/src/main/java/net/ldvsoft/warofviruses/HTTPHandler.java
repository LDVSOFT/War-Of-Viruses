package net.ldvsoft.warofviruses;

import com.github.gist.ArtemGr.SCGI;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static net.ldvsoft.warofviruses.WoVProtocol.MAX_MESSAGE_LENGTH;

/**
 * Created by ldvsoft on 23.11.15.
 */
public class HTTPHandler {
    protected static final String SCGI_CONTENT_LENGTH = "CONTENT_LENGTH";

    protected Logger logger = Logger.getLogger(HTTPHandler.class.getName());

    protected WarOfVirusesServer server;

    protected final ServerSocket serverSocket;
    protected final ExecutorService threadPool = Executors.newFixedThreadPool(1);

    public HTTPHandler(WarOfVirusesServer server) throws IOException {
        this.server = server;
        serverSocket = new ServerSocket(Integer.parseInt(server.getSetting("http.port", "9001")));
        new Thread(new Runnable() {
            @Override
            public void run() {
                logger.info("HTTP Server started\n");
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        final Socket socket;
                        synchronized (serverSocket) {
                            if (serverSocket.isClosed())
                                break;
                            socket = serverSocket.accept();
                        }
                        threadPool.submit(new Runnable() {
                            @Override
                            public void run() {
                                accept(socket);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                logger.info("HTTP Server stopped\n");
            }
        }).start();
    }

    public void stop() {
        try {
            synchronized (serverSocket) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void accept(Socket socket) {
        try {
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();

            try {
                HashMap<String, String> headers = SCGI.parse(is);

                int length = Integer.parseInt(headers.get(SCGI_CONTENT_LENGTH));
                if (length == 0 || length > MAX_MESSAGE_LENGTH)
                    throw new IOException("Wrong length");
                byte[] buffer = new byte[length];
                if (length != is.read(buffer))
                    throw new IOException("UAT?!");
                String body = new String(buffer);
                JSONObject message = new JSONObject(body);

                JSONObject answer = server.processHTTP(message);
                if (answer == null) {
                    answer = WarOfVirusesServer.JSON_RESULT_FAILURE;
                }

                String answerString = answer.toString();
                logger.info("Answering with " + answerString);

                // Header
                os.write("Status: 200 OK\r\n".getBytes());
                os.write("Content-Type: application/json\r\n".getBytes());
                //os.write(String.format("Content-Length: %d\r\n\r\n", answerString.length()).getBytes());
                os.write("\r\n".getBytes());
                //Message
                os.write(answerString.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
