package net.ldvsoft.warofviruses;

public class WarOfVirusesServer {
    public static void main(String[] args) {
        try {
            final HTTPHandler httpHandler = new HTTPHandler(9001);

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    httpHandler.stop();
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Server failed to start!\n");
            System.exit(1);
        }
    }
}
