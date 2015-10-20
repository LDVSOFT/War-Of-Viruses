package net.ldvsoft.warofviruses;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

public class GameActivity extends AppCompatActivity {
    private LinearLayout boardRoot;
    private BoardCellButton[][] boardButtons;
    public static final int BOARD_SIZE = 10;
    public Game game = new Game();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        boardRoot = (LinearLayout) findViewById(R.id.game_board_root);
        buildBoard();
        for (int i = 0; i != BOARD_SIZE; i++)
            for (int j = 0; j != BOARD_SIZE; j++) {
                final int x = i;
                final int y = j;
                boardButtons[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        game.doTurn(x, y);
                        for (int i = 0; i < BOARD_SIZE; i++) {
                            for (int j = 0; j < BOARD_SIZE; j++) {
                                setButton(boardButtons[i][j], game.getCellAt(i, j));
                                boardButtons[i][j].invalidate();
                            }
                        }
                        //Toast.makeText(GameActivity.this, String.format("Pressed %d %d", x, y), Toast.LENGTH_SHORT).show();
                    }
                });
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

    private void setButton(BoardCellButton button, Game.Cell cell) {
        if (cell.getCanMove()) {
            switch (cell.getCellType()) {
                case EMPTY:
                    button.setImageDrawable(BoardCellButton.cellEmpty_borderedO);
                    break;
                case CROSS:
                    button.setImageDrawable(BoardCellButton.cellXalive_borderedO);
                    break;
                case ZERO:
                    button.setImageDrawable(BoardCellButton.cellOalive_borderedO);
                    break;
                case DEAD_CROSS:
                    button.setImageDrawable(BoardCellButton.cellXdead_borderedO);
                    break;
                case DEAD_ZERO:
                    button.setImageDrawable(BoardCellButton.cellOdead_borderedX);
                    break;
            }
        }
        else {
            switch (cell.getCellType()) {
                case EMPTY:
                    button.setImageDrawable(BoardCellButton.cellEmpty);
                    break;
                case CROSS:
                    button.setImageDrawable(BoardCellButton.cellXalive);
                    break;
                case ZERO:
                    button.setImageDrawable(BoardCellButton.cellOalive);
                    break;
                case DEAD_CROSS:
                    button.setImageDrawable(BoardCellButton.cellXdead);
                    break;
                case DEAD_ZERO:
                    button.setImageDrawable(BoardCellButton.cellOdead);
                    break;
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
                newButton.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
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
                setButton(boardButtons[i][j], game.getCellAt(i, j));
            }
        }
    }
}
