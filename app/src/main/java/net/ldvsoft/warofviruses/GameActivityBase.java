package net.ldvsoft.warofviruses;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.List;

import static net.ldvsoft.warofviruses.GameLogic.BOARD_SIZE;
import static net.ldvsoft.warofviruses.GameLogic.Cell;
import static net.ldvsoft.warofviruses.GameLogic.CellType;
import static net.ldvsoft.warofviruses.GameLogic.PlayerFigure;

public abstract class GameActivityBase extends AppCompatActivity {
    protected static final int PLAY_SERVICES_DIALOG = 9001;

    protected LinearLayout boardRoot;
    protected BoardCellButton[][] boardButtons;
    protected FigureSet figureSet = new FigureSet();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game_base);

        boardRoot = (LinearLayout) findViewById(R.id.game_board_root);
        buildBoard();
        /* FIXME */
        for (PlayerFigure figure : PlayerFigure.values()) {
            figureSet.setFigureSource(figure, DefaultFigureSource.NAME);
        }
    }

    protected void redrawGame(GameLogic gameLogic) {
        if (gameLogic == null) {
            return;
        }

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                setButton(boardButtons[i][j], gameLogic.getCellAt(i, j), gameLogic.getCurrentPlayerFigure(), false);
            }
        }
        for (PlayerFigure figure : new PlayerFigure[] {GameLogic.getOpponentPlayerFigure(gameLogic.getCurrentPlayerFigure()), gameLogic.getCurrentPlayerFigure()}) {
            List<GameEvent> lastOpponentEvents = gameLogic.getLastEventsBy(figure);
            for (GameEvent event : lastOpponentEvents) {
                if (event.type != GameEvent.GameEventType.TURN_EVENT) {
                    break;
                }
                int i = event.getTurnX();
                int j = event.getTurnY();
                setButton(boardButtons[i][j], gameLogic.getCellAt(i, j), gameLogic.getCurrentPlayerFigure(), true);
            }
        }

        BoardCellButton avatar = (BoardCellButton) findViewById(R.id.game_cross_avatar);
        boolean isActive = gameLogic.getCurrentPlayerFigure() == PlayerFigure.CROSS;
        avatar.setFigure(figureSet, BoardCellState.get(CellType.CROSS, false, isActive ? PlayerFigure.CROSS : PlayerFigure.NONE));

        avatar = (BoardCellButton) findViewById(R.id.game_zero_avatar);
        isActive = gameLogic.getCurrentPlayerFigure() == PlayerFigure.ZERO;
        avatar.setFigure(figureSet, BoardCellState.get(CellType.ZERO, false, isActive ? PlayerFigure.ZERO : PlayerFigure.NONE));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game, menu);
        return true;
    }

    public boolean checkGoogleServices() {
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        int result = availability.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (availability.isUserResolvableError(result)) {
                availability.getErrorDialog(this, result, PLAY_SERVICES_DIALOG).show();
            } else {
                Toast.makeText(this, "No Google Play Services.", Toast.LENGTH_SHORT).show();
            }
            return false;
        } else {
            return true;
        }
    }


    private void setButton(BoardCellButton button, Cell cell, PlayerFigure current, boolean highlight) {
        PlayerFigure focus = cell.isActive() || cell.canMakeTurn() ? current : PlayerFigure.NONE;
        button.setFigure(figureSet, BoardCellState.get(cell.getCellType(), highlight, focus));
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
        redrawGame(null);
    }
}
