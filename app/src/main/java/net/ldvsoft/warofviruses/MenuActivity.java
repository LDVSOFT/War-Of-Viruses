package net.ldvsoft.warofviruses;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.UUID;

public class MenuActivity extends AppCompatActivity {
    private final static String USER_TOKEN = "3"; //todo REMOVE IT!!!
    public final static String OPPONENT_TYPE = "net.ldvsoft.warofviruses.OPPONENT_TYPE";
    public final static int OPPONENT_BOT = 0;
    public final static int OPPONENT_LOCAL_PLAYER = 1;
    public static final int OPPONENT_NETWORK_PLAYER = 2;

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView view = (NavigationView) findViewById(R.id.navigation_view);
        view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.drawer_clear_db:
                        clearDB();
                        return true;
                    default:
                        Toast.makeText(MenuActivity.this, menuItem.getTitle() + " pressed", Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawers();
                        return true;
                }
            }
        });

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                BoardCellButton.loadDrawables(MenuActivity.this, 30, 210);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                ((BoardCellButton) findViewById(R.id.avatar_cross)).setImageDrawable(BoardCellButton.cellCross);
                ((BoardCellButton) findViewById(R.id.avatar_zero )).setImageDrawable(BoardCellButton.cellZero );
            }
        }.execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void playAgainstBot(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(OPPONENT_TYPE, OPPONENT_BOT);
        startActivity(intent);
    }

    public void playAgainstLocalPlayer(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(OPPONENT_TYPE, OPPONENT_LOCAL_PLAYER);
        startActivity(intent);
    }

    public void viewGameHistory(View view) {
        Intent intent = new Intent(this, GameHistoryActivity.class);
        startActivity(intent);
    }

    public void playOnline(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(OPPONENT_TYPE, OPPONENT_NETWORK_PLAYER);
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        Bundle data = new Bundle();
        data.putString(WoVProtocol.ACTION, WoVProtocol.ACTION_USER_READY);
        String id = UUID.randomUUID().toString();
        try {
            gcm.send(this.getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com", id, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        startActivity(intent);
    }

    public void clearDB() {
        DBOpenHelper instance = DBOpenHelper.getInstance(this);
        instance.onUpgrade(instance.getReadableDatabase(), 0, 0);
    }
}
