package net.ldvsoft.warofviruses;

/**
 * Created by ldvsoft on 11.12.15.
 */
public class User {
    private long id;
    private String googleToken;
    private int type;
    private String nickNameStr;
    private String nickNameId;
    private int colorCross;
    private int colorZero;
    private User invitationTarget;
    private byte[] nicknameStr;

    public User(
            long id,
            String googleToken,
            int type,
            String nickNameStr, String nickNameId,
            int colorCross, int colorZero,
            User invitationTarget) {
        this.id = id;
        this.googleToken = googleToken;
        this.type = type;
        this.nickNameStr = nickNameStr;
        this.nickNameId = nickNameId;
        this.colorCross = colorCross;
        this.colorZero = colorZero;
        this.invitationTarget = invitationTarget;
    }

    public String getFullNickname() {
        return nickNameStr + "#" + nickNameId;
    }

    public long getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public String getGoogleToken() {
        return googleToken;
    }

    public String getNickNameStr() {
        return nickNameStr;
    }

    public String getNickNameId() {
        return nickNameId;
    }

    public int getColorCross() {
        return colorCross;
    }

    public int getColorZero() {
        return colorZero;
    }
}
