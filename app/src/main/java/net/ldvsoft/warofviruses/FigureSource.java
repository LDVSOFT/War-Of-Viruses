package net.ldvsoft.warofviruses;

import android.graphics.drawable.Drawable;

/**
 * Created by ldvsoft on 04.02.16.
 */
public interface FigureSource {
    Drawable loadFigure(BoardCellState state);
    void setHueCross(int newHueCross);
    void setHueZero(int newHueZero);
}
