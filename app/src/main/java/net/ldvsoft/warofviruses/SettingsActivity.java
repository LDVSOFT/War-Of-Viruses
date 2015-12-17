package net.ldvsoft.warofviruses;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public void changeNickname(View view) {
        String nickname = ((EditText) findViewById(R.id.edit_nickname)).getText().toString();
        long userId = getSharedPreferences(WoVPreferences.PREFERENCES, MODE_PRIVATE).getLong(WoVPreferences.CURRENT_USER_ID, -1);
        User user = DBOpenHelper.getInstance(this).getUserById(userId);
        user.setNickNameStr(nickname);
        DBOpenHelper.getInstance(this).addUser(user);
    }
}
