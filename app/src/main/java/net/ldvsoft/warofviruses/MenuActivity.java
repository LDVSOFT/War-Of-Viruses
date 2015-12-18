package net.ldvsoft.warofviruses;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.gson.JsonObject;

import java.io.IOException;

public class MenuActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9000;
    private static final String TAG = "MenuActivity";
    private GameLoadedFromServerReceiver gameLoadedFromServerReceiver = null;
    private BoardCellButton crossButton;
    private BoardCellButton zeroButton;
    private DrawerLayout drawerLayout;
    private GoogleApiClient apiClient;
    private ProgressDialog progressDialog;

    private MenuItem menuSignIn;
    private MenuItem menuSignOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        SharedPreferences preferences = getSharedPreferences(WoVPreferences.PREFERENCES, MODE_PRIVATE);
        if (!preferences.contains(WoVPreferences.CURRENT_USER_ID)) {
            preferences.edit().putLong(WoVPreferences.CURRENT_USER_ID, HumanPlayer.USER_ANONYMOUS.getId()).apply();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        View drawerHeader = navigationView.inflateHeaderView(R.layout.drawer_header);
        crossButton = (BoardCellButton) drawerHeader.findViewById(R.id.avatar_cross);
        zeroButton = (BoardCellButton) drawerHeader.findViewById(R.id.avatar_zero);
        menuSignIn = navigationView.getMenu().findItem(R.id.drawer_sign_in);
        menuSignOut = navigationView.getMenu().findItem(R.id.drawer_sign_out);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.drawer_sign_in:
                         signIn();
                         drawerLayout.closeDrawers();
                         return true;
                    case R.id.drawer_sign_out:
                        signOut();
                        drawerLayout.closeDrawers();
                        return true;
                    case R.id.drawer_clear_db:
                        clearDB();
                        drawerLayout.closeDrawers();
                        return true;
                    case R.id.drawer_settings:
                        Intent intent = new Intent(MenuActivity.this, SettingsActivity.class);
                        startActivity(intent);
                        drawerLayout.closeDrawers();
                        return true;
                    default:
                        Toast.makeText(MenuActivity.this, menuItem.getTitle() + " pressed", Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawers();
                        return true;
                }
            }
        });

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        apiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Toast.makeText(MenuActivity.this, "No Google?", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(apiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(apiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        updateUI(false);
                    }
                });
    }

    private void updateUI(boolean connected) {
        if (connected) {
            menuSignIn.setEnabled(false);
            menuSignOut.setEnabled(true);
        } else {
            menuSignIn.setEnabled(true);
            menuSignOut.setEnabled(false);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("MainActivity", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            String token = acct.getIdToken();

            JsonObject data = new JsonObject();
            data.addProperty(WoVProtocol.GOOGLE_TOKEN, token);
            WoVGcmListenerService.sendGcmMessage(this, WoVProtocol.ACTION_REGISTER, data);
            Toast.makeText(MenuActivity.this, getString(R.string.TOAST_REGISTRATION_IN_PROCESS), Toast.LENGTH_SHORT).show();
            //Actual UI changes will come on message result
        } else {
            WoVGcmListenerService.sendGcmMessage(this, WoVProtocol.ACTION_LOGOUT, null);
        }
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setIndeterminate(true);
        }

        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.hide();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(apiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                //noinspection ResourceType
                BoardCellButton.loadDrawables(MenuActivity.this, 30, 210);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                crossButton.setImageDrawable(BoardCellButton.cellCross);
                zeroButton.setImageDrawable(BoardCellButton.cellZero);
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

    private class RestoreGameDialog {
        private final Runnable loadGame;

        RestoreGameDialog(Runnable loadGame) {
            this.loadGame = loadGame;
        }

        public void execute() {
            new AlertDialog.Builder(MenuActivity.this)
                    .setMessage("Found saved game. What should I do with it?") //todo: more understandable options
                    .setCancelable(false)
                    .setPositiveButton("Load it", new RestoreGame())
                    .setNeutralButton("Do nothing", null)
                    .setNegativeButton("Give up and start new game", new NewGame())
                    .show();
        }

        private class RestoreGame implements Dialog.OnClickListener {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                restoreSavedGame(null);
            }
        }

        private class NewGame implements Dialog.OnClickListener {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Game game = DBOpenHelper.getInstance(MenuActivity.this).getAndRemoveActiveGame();
                game.giveUp(game.getCurrentPlayer()); //todo: give up for me, not for current player!
                DBOpenHelper.getInstance(MenuActivity.this).addGame(game);
                loadGame.run();
            }
        }
    }

    private class PlayAgainstBot implements Runnable{
        @Override
        public void run() {
            Intent intent = new Intent(MenuActivity.this, GameActivity.class);
            intent.putExtra(WoVPreferences.OPPONENT_TYPE, WoVPreferences.OPPONENT_BOT);
            startActivity(intent);
        }
    }

    public void playAgainstBot(View view) {
        if (DBOpenHelper.getInstance(this).hasActiveGame()) {
            new RestoreGameDialog(new PlayAgainstBot()).execute();
            return;
        }
        new PlayAgainstBot().run();
    }

    private class PlayAgainstLocalPlayer implements Runnable{
        @Override
        public void run() {
            Intent intent = new Intent(MenuActivity.this, GameActivity.class);
            intent.putExtra(WoVPreferences.OPPONENT_TYPE, WoVPreferences.OPPONENT_LOCAL_PLAYER);
            startActivity(intent);
        }
    }

    public void playAgainstLocalPlayer(View view) {
        if (DBOpenHelper.getInstance(this).hasActiveGame()) {
            new RestoreGameDialog(new PlayAgainstLocalPlayer()).execute();
            return;
        }
        new PlayAgainstLocalPlayer().run();
    }

    public void viewGameHistory(View view) {
        Intent intent = new Intent(this, GameHistoryActivity.class);
        startActivity(intent);
    }

    private class GameLoadedFromServerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("GameActivity", "networkLoadGame broadcast recieved!");
            Bundle tmp = intent.getBundleExtra(WoVPreferences.GAME_BUNDLE);
            String data = tmp.getString(WoVProtocol.DATA);
            intent = new Intent(MenuActivity.this, GameActivity.class);
            intent.putExtra(WoVPreferences.OPPONENT_TYPE, WoVPreferences.OPPONENT_NETWORK_PLAYER);
            intent.putExtra(WoVPreferences.GAME_JSON_DATA, data);
            unregisterReceiver(gameLoadedFromServerReceiver);
            gameLoadedFromServerReceiver = null;
            startActivity(intent);
        }
    }

    private class PlayOnline implements Runnable {
        @Override
        public void run() {
            gameLoadedFromServerReceiver = new GameLoadedFromServerReceiver();
            registerReceiver(gameLoadedFromServerReceiver, new IntentFilter(WoVPreferences.GAME_LOADED_FROM_SERVER_BROADCAST));
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    WoVGcmListenerService.sendGcmMessage(MenuActivity.this, WoVProtocol.ACTION_USER_READY, null);
                    return null;
                }
            }.execute();
        }
    }

    public void playOnline(View view) {
        if (DBOpenHelper.getInstance(this).hasActiveGame()) {
            new RestoreGameDialog(new PlayOnline()).execute();
            return;
        }
        new PlayOnline().run();
    }

    @Override
    protected void onStop() {
        if (gameLoadedFromServerReceiver != null) {
            unregisterReceiver(gameLoadedFromServerReceiver);
            gameLoadedFromServerReceiver = null;
        }
        super.onStop();
    }

    public void clearDB() {
        DBOpenHelper instance = DBOpenHelper.getInstance(this);
        instance.onUpgrade(instance.getReadableDatabase(), 0, 0);
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    InstanceID.getInstance(MenuActivity.this).deleteInstanceID();
                    InstanceID.getInstance(MenuActivity.this).getToken(MenuActivity.this.getString(R.string.gcm_defaultSenderId),
                            GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                } catch (IOException e) {
                    Log.i(TAG, "/", e);
                }
                return null;
            }
        }.execute();
    }

    public void restoreSavedGame(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(WoVPreferences.OPPONENT_TYPE, WoVPreferences.OPPONENT_RESTORED_GAME);
        startActivity(intent);
    }
}
