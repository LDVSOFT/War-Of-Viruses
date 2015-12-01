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

import static net.ldvsoft.warofviruses.WoVHTTPProtocol.ACTION_TEST;
import static net.ldvsoft.warofviruses.WoVHTTPProtocol.MAX_MESSAGE_LENGTH;
import static net.ldvsoft.warofviruses.WoVHTTPProtocol.REQUEST_ACTION;
import static net.ldvsoft.warofviruses.WoVHTTPProtocol.RESULT;
import static net.ldvsoft.warofviruses.WoVHTTPProtocol.RESULT_FAILURE;
import static net.ldvsoft.warofviruses.WoVHTTPProtocol.RESULT_SUCCESS;

/**
 * Created by ldvsoft on 23.11.15.
 */
public class HTTPHandler {
    public static final JSONObject JSON_RESULT_FAILURE = new JSONObject().put(RESULT, RESULT_FAILURE);
    public static final String SCGI_CONTENT_LENGTH = "CONTENT_LENGTH";

    protected final ServerSocket serverSocket;
    protected final ExecutorService threadPool = Executors.newFixedThreadPool(1);

    public HTTPHandler(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.err.print("HTTP Server started\n");
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        final Socket socket;
                        if (serverSocket.isClosed())
                            break;
                        socket = serverSocket.accept();
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
                System.err.print("HTTP Server stopped\n");
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

    protected JSONObject process(JSONObject request) {
        if (!request.has(REQUEST_ACTION))
            return JSON_RESULT_FAILURE;
        String action = String.valueOf(request.get(REQUEST_ACTION));

        System.err.printf("action: %s\n", action);
        if (!action.equals(ACTION_TEST))
            return JSON_RESULT_FAILURE;

        return new JSONObject().accumulate("result", RESULT_SUCCESS);
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

                JSONObject request = new JSONObject(body);
                System.err.print("Got JSON: ");
                System.err.print(request.toString());
                System.err.print("\n");

                JSONObject answer = process(request);

                os.write("Status: 200 OK\r\n".getBytes());
                os.write("Content-Type: application/json\r\n\r\n".getBytes());
                System.err.print(answer.toString());
                os.write(answer.toString().getBytes());
                os.write("\r\n".getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
