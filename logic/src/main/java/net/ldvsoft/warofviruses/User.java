package net.ldvsoft.warofviruses;

/**
 * Created by ldvsoft on 11.12.15.
 */
public class User {
    private long id;
    private String googleToken;
    private String nickNameStr;
    private String nickNameId;
    private int colorCross;
    private int colorZero;
    private User invitationTarget;

    public User(
            long id,
            String googleToken,
            String nickNameStr, String nickNameId,
            int colorCross, int colorZero,
            User invitationTarget) {
        this.id = id;
        this.googleToken = googleToken;
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
}
