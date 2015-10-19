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
                        Toast.makeText(GameActivity.this, String.format("Pressed %d %d", x, y), Toast.LENGTH_SHORT).show();
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
        for (int row = 0; row != BOARD_SIZE; row++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            for (int column = 0; column != BOARD_SIZE; column++) {
                BoardCellButton newButton = new BoardCellButton(this);
                newButton.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                switch ((row * BOARD_SIZE + column) % 4) {
                    case 0:
                        newButton.setImageDrawable(BoardCellButton.cellOalive);
                        break;
                    case 1:
                        newButton.setImageDrawable(BoardCellButton.cellOdead);
                        break;
                    case 2:
                        newButton.setImageDrawable(BoardCellButton.cellXdead);
                        break;
                    case 3:
                        newButton.setImageDrawable(BoardCellButton.cellXalive);
                        break;
                }
                boardButtons[row][column] = newButton;
                rowLayout.addView(boardButtons[row][column], BOARD_BUTTON_LAYOUT_PARAMS);
            }
            boardRoot.addView(rowLayout, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        }
        boardRoot.invalidate();
    }
}
