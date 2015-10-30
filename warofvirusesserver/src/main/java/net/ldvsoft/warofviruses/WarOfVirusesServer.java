package net.ldvsoft.warofviruses;

import com.github.gist.ArtemGr.SCGI;

import net.sf.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class WarOfVirusesServer {

    public static final String RESULT_SUCCESS = "success";
    public static final String RESULT_FAILURE = "failure";
    public static final String REQUEST_ACTION = "action";
    public static final String ACTION_TEST = "test";
    public static final String PARAM_TOKEN = "token";
    public static final String RESULT = "result";
    public static final JSONObject JSON_RESULT_FAILURE = new JSONObject().accumulate(RESULT, RESULT_FAILURE);

    public static final String SCGI_CONTENT_LENGTH = "CONTENT_LENGTH";

    private static ServerSocket serverSocket;
    private static Thread httpThread;

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket.close();
                    httpThread.interrupt();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));

        httpThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(9001);
                    System.err.print("Server started\n");
                    while (! Thread.currentThread().isInterrupted()) {
                        Socket socket = serverSocket.accept();
                        InputStream is = socket.getInputStream();
                        OutputStream os = socket.getOutputStream();

                        JSONObject answer = JSON_RESULT_FAILURE;

                        processing:
                        try {
                            HashMap<String, String> headers = SCGI.parse(is);

                            int length = Integer.parseInt(headers.get(SCGI_CONTENT_LENGTH));
                            if (length == 0 || length > 8192)
                                throw new IOException("Too long");
                            byte[] buffer = new byte[8192];
                            if (length != is.read(buffer))
                                throw new IOException("UAT?!");
                            String body = new String(buffer);

                            JSONObject request = JSONObject.fromObject(body);

                            System.err.print("Got JSON: ");
                            System.err.print(request.toString());
                            System.err.print("\n");

                            if (!request.containsKey(REQUEST_ACTION))
                                break processing;
                            String action = String.valueOf(request.get(REQUEST_ACTION));

                            System.err.printf("action: %s\n", action);
                            if (!action.equals(ACTION_TEST))
                                break processing;

                            answer = new JSONObject().accumulate("result", RESULT_SUCCESS);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        os.write("Status: 200 OK\r\n".getBytes());
                        os.write("Content-Type: application/json\r\n\r\n".getBytes());
                        System.err.print(answer.toString());
                        os.write(answer.toString().getBytes());
                        os.write("\r\n".getBytes());

                        os.close();
                        is.close();
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.err.print("Server stopped\n");
            }
        });

        httpThread.start();
    }
}
