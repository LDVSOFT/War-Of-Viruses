package net.ldvsoft.warofviruses;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class GameActivity extends AppCompatActivity {
    private LinearLayout boardRoot;
    private ImageButton[][] boardButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        boardRoot = (LinearLayout) findViewById(R.id.game_board_root);
        buildBoard();
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
        LayoutParams BOARD_BUTTON_LAYOUT_PARAMS = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float logicalDensity = metrics.density;
        int marginValue = (int) Math.ceil(logicalDensity * (-2));
        BOARD_BUTTON_LAYOUT_PARAMS.setMargins(marginValue, marginValue, marginValue, marginValue);

        if (boardRoot.getChildCount() != 0)
            return;
        boardButtons = new ImageButton[10][10];
        boardRoot.setOrientation(LinearLayout.VERTICAL);
        for (int row = 0; row != 10; row++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            for (int column = 0; column != 10; column++) {
                ImageButton newButton = new ImageButton(this);
                boardButtons[row][column] = newButton;
                rowLayout.addView(boardButtons[row][column], BOARD_BUTTON_LAYOUT_PARAMS);
            }
            boardRoot.addView(rowLayout, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        }
        boardRoot.invalidate();
    }
}
