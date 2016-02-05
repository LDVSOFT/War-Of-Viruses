package net.ldvsoft.warofviruses;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static net.ldvsoft.warofviruses.GameLogic.*;

/**
 * FigureSet contains loaded Drawables for BoardCellButtons, which it gets from FigureSources.
 * @see FigureSource
 * FigureSet uses three sources for every owner of figures displayed.
 *
 * FigureSet is designed to be created once, then via `changeFigureSource' changing Drawables source
 * and unloading already loaded Drawables that are outdated now. Buttons itself are still required
 * to be updated manually.
 */
public class FigureSet {
    /**
     * For every figure, STATES holds list of BoardCellStates that are owned by that figure.
     * These are used to unload Drawables when changing source.
     */
    private static final Map<PlayerFigure, List<BoardCellState>> STATES = new EnumMap<>(PlayerFigure.class);

    static {
        for (PlayerFigure figure : PlayerFigure.values()) {
            STATES.put(figure, new ArrayList<BoardCellState>());
        }
        for (CellType cellType : CellType.values()) {
            for (boolean isHighlighted : new boolean[]{false, true}) {
                for (PlayerFigure focus : PlayerFigure.values()) {
                    STATES.get(cellType.getOwner()).add(BoardCellState.get(cellType, isHighlighted, focus));
                }
            }
        }
    }

    private Map<PlayerFigure, FigureSource> figureSource = new EnumMap<>(PlayerFigure.class);
    private Map<BoardCellState, Drawable> loadedFigures = new Hashtable<>();

    /**
     * Returns Drawable for given BoardCellState. Loads it if not yet (or has been outdated).
     * @param state cell state
     * @return Drawable to display that state
     */
    public Drawable getFigure(BoardCellState state) {
        if (! loadedFigures.containsKey(state) && figureSource.containsKey(state.getCellType().getOwner())) {
            loadedFigures.put(state, figureSource.get(state.getCellType().getOwner()).loadFigure(state));
        }
        return loadedFigures.get(state);
    }

    /**
     * Changes FigureSource for given figure. All already loaded Drawables for that figure will be
     * unloaded, so that new requests will load new Drawables from new source
     * @param figure figure to change source for
     * @param source new FigureSource
     */
    public void changeFigureSource(PlayerFigure figure, FigureSource source) {
        for (BoardCellState state : STATES.get(figure)) {
            loadedFigures.remove(state);
        }
        figureSource.put(figure, source);
    }

    public void setHueCross(int newHueCross) {
        for (FigureSource source : figureSource.values()) {
            source.setHueCross(newHueCross);
        }
    }

    public void setHueZero(int newHueZero) {
        for (FigureSource source : figureSource.values()) {
            source.setHueZero(newHueZero);
        }
    }
}
