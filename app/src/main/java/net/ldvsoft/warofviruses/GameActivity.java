package net.ldvsoft.warofviruses;

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

import static net.ldvsoft.warofviruses.Game.*;

public class GameActivity extends AppCompatActivity {
    private LinearLayout boardRoot;
    private BoardCellButton[][] boardButtons;
    private TextView gameStateText;

    public Game game = new Game();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Just for now
        findViewById(R.id.game_bar_replay).setVisibility(View.GONE);

        gameStateText = (TextView) findViewById(R.id.game_text_game_status);
        boardRoot = (LinearLayout) findViewById(R.id.game_board_root);
        buildBoard();

        Button passTurnButton = (Button) findViewById(R.id.game_button_passturn);
        passTurnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!game.skipTurn()) {
                    return;
                }
                redrawGame();
            }
        });

        BoardCellButton avatar = (BoardCellButton) findViewById(R.id.game_cross_avatar);
        avatar.setImageDrawable(BoardCellButton.cellXalive);
        avatar.invalidate();
        avatar = (BoardCellButton) findViewById(R.id.game_zero_avatar);
        avatar.setImageDrawable(BoardCellButton.cellOalive);
        avatar.invalidate();

        for (int i = 0; i != BOARD_SIZE; i++)
            for (int j = 0; j != BOARD_SIZE; j++) {
                final int x = i;
                final int y = j;
                boardButtons[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!game.doTurn(x, y)) {
                            return;
                        }
                        redrawGame();
                    }
                });
            }
    }

    private void redrawGame() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                setButton(boardButtons[i][j], game.getCellAt(i, j), game.getCurPlayerFigure());
                boardButtons[i][j].invalidate();
            }
        }

        switch (game.getCurrentGameState()) {
            case RUNNING:
                gameStateText.setText("RUNNING");
                break;
            case DRAW:
                gameStateText.setText("DRAW");
                break;
            case CROSS_WON:
                gameStateText.setText("CROSS_WON");
                break;
            case ZERO_WON:
                gameStateText.setText("ZERO_WON");
                break;
        }

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

    private void setButton(BoardCellButton button, Game.Cell cell, Game.PlayerFigure current) {
        switch (cell.getCellType()) {
            case CROSS:
                if (cell.isActive()) {
                    button.setImageDrawable(BoardCellButton.cellXalive_borderedX);
                } else if (cell.canMakeTurn()) {
                    button.setImageDrawable(BoardCellButton.cellXalive_borderedO);
                } else {
                    button.setImageDrawable(BoardCellButton.cellXalive);
                }
                break;
            case ZERO:
                if (cell.isActive()) {
                    button.setImageDrawable(BoardCellButton.cellOalive_borderedO);
                } else if (cell.canMakeTurn()) {
                    button.setImageDrawable(BoardCellButton.cellOalive_borderedX);
                } else {
                    button.setImageDrawable(BoardCellButton.cellOalive);
                }
                break;
            case DEAD_CROSS:
                if (cell.isActive()) {
                    button.setImageDrawable(BoardCellButton.cellXdead_borderedO);
                } else {
                    button.setImageDrawable(BoardCellButton.cellXdead);
                }
                break;
            case DEAD_ZERO:
                if (cell.isActive()) {
                    button.setImageDrawable(BoardCellButton.cellOdead_borderedX);
                } else {
                    button.setImageDrawable(BoardCellButton.cellOdead);
                }
                break;
            case EMPTY:
                if (!cell.canMakeTurn()) {
                    button.setImageDrawable(BoardCellButton.cellEmpty);
                } else if (current == PlayerFigure.CROSS) {
                    button.setImageDrawable(BoardCellButton.cellEmpty_borderedX);
                } else {
                    button.setImageDrawable(BoardCellButton.cellEmpty_borderedO);
                }
        }
    }

    private void buildBoard() {
        if (boardRoot.getChildCount() != 0) {
            Log.wtf("gameActivity", "Board already present, not building one!");
            return;
        }

        LayoutParams BOARD_BUTTON_LAYOUT_PARAMS = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float logicalDensity = metrics.density;
        int marginValue = (int) Math.ceil(logicalDensity * 1);
        BOARD_BUTTON_LAYOUT_PARAMS.setMargins(marginValue, marginValue, 0, 0);

        BoardCellButton.loadDrawables(this, 30, 210);
        boardButtons = new BoardCellButton[BOARD_SIZE][BOARD_SIZE];
        boardRoot.setOrientation(LinearLayout.VERTICAL);
        for (int row = BOARD_SIZE - 1; row != -1; row--) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            for (int column = 0; column != BOARD_SIZE; column++) {
                BoardCellButton newButton = new BoardCellButton(this);
                newButton.setImageDrawable(BoardCellButton.cellEmpty);
                boardButtons[row][column] = newButton;
                rowLayout.addView(boardButtons[row][column], BOARD_BUTTON_LAYOUT_PARAMS);
            }
            boardRoot.addView(rowLayout, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        }
        boardButtons[0][0].setImageDrawable(BoardCellButton.cellEmpty_borderedX);
        boardButtons[BOARD_SIZE - 1][BOARD_SIZE - 1].setImageDrawable(BoardCellButton.cellEmpty_borderedO);
        game.newGame();
        boardRoot.invalidate();

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                setButton(boardButtons[i][j], game.getCellAt(i, j), game.getCurPlayerFigure());
            }
        }
    }
}
