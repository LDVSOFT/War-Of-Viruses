package net.ldvsoft.warofviruses;

import android.content.Intent;
import android.os.Bundle;
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

import static net.ldvsoft.warofviruses.GameLogic.*;
import static net.ldvsoft.warofviruses.MenuActivity.*;

public class GameActivity extends AppCompatActivity {
    private LinearLayout boardRoot;
    private BoardCellButton[][] boardButtons;
    private TextView gameStateText;

    private HumanPlayer humanPlayer = new HumanPlayer();
    private boolean isEnemyLocalPlayer = false;

    public Game game = new Game();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        game.setOnGameStateChangedListener(new Game.OnGameStateChangedListener() {
            @Override
            public void onGameStateChanged() {
                redrawGame();
            }
        });
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

        // Just for now
        findViewById(R.id.game_bar_replay).setVisibility(View.GONE);

        gameStateText = (TextView) findViewById(R.id.game_text_game_status);
        boardRoot = (LinearLayout) findViewById(R.id.game_board_root);
        buildBoard();

        Button skipTurnButton = (Button) findViewById(R.id.game_button_passturn);
        skipTurnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEnemyLocalPlayer) {
                    if (!game.skipTurn(game.getCurrentPlayer())) {
                        return;
                    }
                }
                else {
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
                }
                else {
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
    protected void onSaveInstanceState(Bundle outState) {


        super.onSaveInstanceState(outState);
    }

    private void redrawGame() {
        GameLogic gameLogic = game.getGameLogic();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                setButton(boardButtons[i][j], gameLogic.getCellAt(i, j), game.getGameLogic().getCurPlayerFigure());
            }
        }

        BoardCellButton avatar = (BoardCellButton) findViewById(R.id.game_cross_avatar);
        if (gameLogic.getCurPlayerFigure() == PlayerFigure.CROSS) {
            avatar.setImageDrawable(BoardCellButton.cellCross_forCross);
        } else {
            avatar.setImageDrawable(BoardCellButton.cellCross);
        }
        avatar = (BoardCellButton) findViewById(R.id.game_zero_avatar);
        if (gameLogic.getCurPlayerFigure() == PlayerFigure.ZERO) {
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
