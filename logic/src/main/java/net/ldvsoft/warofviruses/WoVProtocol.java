package net.ldvsoft.warofviruses;

/**
 * Created by ldvsoft on 23.11.15.
 */
public class WoVProtocol {
    public static final String RESULT = "result";
    public static final String RESULT_SUCCESS = "success";
    public static final String RESULT_FAILURE = "failure";

    public static final String ACTION = "action";
    public static final String ACTION_TEST = "test";
    public static final String ACTION_PING = "ping";

    public static final String PARAM_TOKEN = "token";
    public static final String PING_ID = "id";

    public static final int MAX_MESSAGE_LENGTH = 8192;
}
