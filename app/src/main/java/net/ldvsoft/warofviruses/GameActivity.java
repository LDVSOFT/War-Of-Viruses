package net.ldvsoft.warofviruses;

import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import static net.ldvsoft.warofviruses.GameLogic.*;
import static net.ldvsoft.warofviruses.MenuActivity.*;

public class GameActivity extends AppCompatActivity {
    public static final int PLAY_SERVICES_DIALOG = 9001;

    private LinearLayout boardRoot;
    private BoardCellButton[][] boardButtons;
    private TextView gameStateText;

    private HumanPlayer humanPlayer = new HumanPlayer();
    private boolean isEnemyLocalPlayer = false;

    private BroadcastReceiver tokenSentReceiver;

    public Game game = new Game();
    private BroadcastReceiver loadedGameReceiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        setCurrentGameListeners();
        switch (intent.getIntExtra(OPPONENT_TYPE, -1)) {
            case OPPONENT_BOT:
                game.startNewGame(humanPlayer, new AIPlayer(PlayerFigure.ZERO));
                isEnemyLocalPlayer = false;
                break;
            case OPPONENT_LOCAL_PLAYER:
                game.startNewGame(humanPlayer, new HumanPlayer());
                isEnemyLocalPlayer = true;
                break;
            default:
                Log.wtf("GameActivity", "Could not start new game: incorrect opponent type");
        }
        setContentView(R.layout.activity_game);

        tokenSentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = prefs.getBoolean(WoVPreferences.GCM_SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    Toast.makeText(GameActivity.this, "YEEEEEEEY!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(GameActivity.this, "Oh no, Oh no, Oh no-no-no-no(", Toast.LENGTH_SHORT).show();
                }
            }
        };

        // Just for now
        findViewById(R.id.game_bar_replay).setVisibility(View.GONE);

        gameStateText = (TextView) findViewById(R.id.game_text_game_status);
        boardRoot = (LinearLayout) findViewById(R.id.game_board_root);
        buildBoard();

        initButtons();
    }

    private void initButtons() {
        Button skipTurnButton = (Button) findViewById(R.id.game_button_passturn);
        skipTurnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEnemyLocalPlayer) {
                    if (!game.skipTurn(game.getCurrentPlayer())) {
                        return;
                    }
                } else {
                    if (!game.skipTurn(humanPlayer)) {
                        return;
                    }
                }
                redrawGame();
            }
        });

        Button giveUpButton = (Button) findViewById(R.id.game_button_giveup);
        giveUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEnemyLocalPlayer) {
                    game.giveUp(game.getCurrentPlayer());
                } else {
                    game.giveUp(humanPlayer);
                }
            }
        });

        for (int i = 0; i != BOARD_SIZE; i++)
            for (int j = 0; j != BOARD_SIZE; j++) {
                final int x = i;
                final int y = j;
                boardButtons[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isEnemyLocalPlayer) {
                            game.doTurn(game.getCurrentPlayer(), x, y);
                        } else {
                            game.doTurn(humanPlayer, x, y);
                        }
                    }
                });
            }
    }
    @Override
    protected void onStop() {
        Log.d("GameActivity", "onStop");
        saveCurrentGame();
        unregisterReceiver(loadedGameReceiver);
        super.onStop();
    }

    protected void onResume() {
        super.onResume();
        LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(tokenSentReceiver, new IntentFilter(WoVPreferences.GCM_REGISTRATION_COMPLETE));
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadedGameReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("GameActivity", "Broadcast receiver message");
                if (intent.hasExtra(WoVPreferences.LOAD_GAME_KEY)) {
                    Log.d("GameActivity", "Game load message received");
                    game = Game.fromBytes(intent.getByteArrayExtra(WoVPreferences.LOAD_GAME_KEY));
                    setCurrentGameListeners();
                    isEnemyLocalPlayer = true; //at least for now...
                    initButtons();
                    redrawGame();
                }
            }
        };
        registerReceiver(loadedGameReceiver, new IntentFilter(WoVPreferences.LOAD_GAME_BROADCAST));
        Log.d("GameActivity", "onStart");
        Intent intent = new Intent(this, GameHistoryDBService.class);
        intent.putExtra(WoVPreferences.LOAD_GAME_KEY, "");
        startService(intent);
    }

    private void setCurrentGameListeners() {
        game.setOnGameStateChangedListener(new Game.OnGameStateChangedListener() {
            @Override
            public void onGameStateChanged() {
                redrawGame();
            }
        });
        game.setOnGameFinishedListener(new Game.OnGameFinishedListener() {
            @Override
            public void onGameFinished() {
                saveCurrentGame();
            }
        });
    }

    private void saveCurrentGame() {
        Intent intent = new Intent(this, GameHistoryDBService.class);
        intent.putExtra(WoVPreferences.GAME_KEY, game.toBytes());
        intent.putExtra(WoVPreferences.GAME_IS_FINISHED_KEY, game.isFinished());
        startService(intent);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager
                .getInstance(this)
                .unregisterReceiver(tokenSentReceiver);
        super.onPause();
    }

    private void redrawGame() {
        GameLogic gameLogic = game.getGameLogic();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                setButton(boardButtons[i][j], gameLogic.getCellAt(i, j), game.getGameLogic().getCurrentPlayerFigure());
            }
        }

        BoardCellButton avatar = (BoardCellButton) findViewById(R.id.game_cross_avatar);
        if (gameLogic.getCurrentPlayerFigure() == PlayerFigure.CROSS) {
            avatar.setImageDrawable(BoardCellButton.cellCross_forCross);
        } else {
            avatar.setImageDrawable(BoardCellButton.cellCross);
        }
        avatar = (BoardCellButton) findViewById(R.id.game_zero_avatar);
        if (gameLogic.getCurrentPlayerFigure() == PlayerFigure.ZERO) {
            avatar.setImageDrawable(BoardCellButton.cellZero_forZero);
        } else {
            avatar.setImageDrawable(BoardCellButton.cellZero);
        }

        gameStateText.setText(gameLogic.getCurrentGameState().toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.test) {
            GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
            int result = availability.isGooglePlayServicesAvailable(this);
            if (result != ConnectionResult.SUCCESS) {
                if (availability.isUserResolvableError(result)) {
                    availability.getErrorDialog(this, result, PLAY_SERVICES_DIALOG).show();
                } else {
                    Toast.makeText(this, "No Google Play Services.", Toast.LENGTH_SHORT).show();
                }
            } else {
                startService(new Intent(this, WoVRegistrationIntentService.class));
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setButton(BoardCellButton button, GameLogic.Cell cell, GameLogic.PlayerFigure current) {
        switch (cell.getCellType()) {
            case CROSS:
                if (cell.isActive()) {
                    button.setImageDrawable(BoardCellButton.cellCross_forCross);
                } else if (cell.canMakeTurn()) {
                    button.setImageDrawable(BoardCellButton.cellCross_forZero);
                } else {
                    button.setImageDrawable(BoardCellButton.cellCross);
                }
                break;
            case ZERO:
                if (cell.isActive()) {
                    button.setImageDrawable(BoardCellButton.cellZero_forZero);
                } else if (cell.canMakeTurn()) {
                    button.setImageDrawable(BoardCellButton.cellZero_forCross);
                } else {
                    button.setImageDrawable(BoardCellButton.cellZero);
                }
                break;
            case DEAD_CROSS:
                if (cell.isActive()) {
                    button.setImageDrawable(BoardCellButton.cellCrossDead_forZero);
                } else {
                    button.setImageDrawable(BoardCellButton.cellCrossDead);
                }
                break;
            case DEAD_ZERO:
                if (cell.isActive()) {
                    button.setImageDrawable(BoardCellButton.cellZeroDead_forCross);
                } else {
                    button.setImageDrawable(BoardCellButton.cellZeroDead);
                }
                break;
            case EMPTY:
                if (!cell.canMakeTurn()) {
                    button.setImageDrawable(BoardCellButton.cellEmpty);
                } else if (current == GameLogic.PlayerFigure.CROSS) {
                    button.setImageDrawable(BoardCellButton.cellEmpty_forCross);
                } else {
                    button.setImageDrawable(BoardCellButton.cellEmpty_forZero);
                }
        }
    }

    private void buildBoard() {
        if (boardRoot.getChildCount() != 0) {
            Log.wtf("gameActivity", "Board already present, not building one!");
            return;
        }

        // Init buttons' layout params, they require context to get 1dp in pixels
        LayoutParams boardButtonLayoutParams = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int marginValue = (int) Math.ceil(metrics.density * 1);
        boardButtonLayoutParams.setMargins(marginValue, marginValue, 0, 0);

        BoardCellButton.loadDrawables(this, 30, 210);
        boardButtons = new BoardCellButton[BOARD_SIZE][BOARD_SIZE];

        for (int row = BOARD_SIZE - 1; row != -1; row--) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            for (int column = 0; column != BOARD_SIZE; column++) {
                boardButtons[row][column] = new BoardCellButton(this);
                rowLayout.addView(boardButtons[row][column], boardButtonLayoutParams);
            }
            boardRoot.addView(rowLayout, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        }


        boardRoot.invalidate();
        redrawGame();
    }
}
