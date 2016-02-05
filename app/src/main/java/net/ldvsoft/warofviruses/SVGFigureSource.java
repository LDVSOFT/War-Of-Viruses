package net.ldvsoft.warofviruses;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.larvalabs.svgandroid.SVGParser;

import java.io.InputStream;
import java.util.Hashtable;

import static android.graphics.Color.HSVToColor;
import static android.graphics.Color.argb;

/**
 * The original FigureSource, that loads SVGs and changes their color palette to selected by a player.
 */
public abstract class SVGFigureSource implements FigureSource {
    /**
     * Original colors in SVG to be changed with real ones.
     */
    private final static int CROSS_FG   = argb(0, 255, 0  , 0  );
    private final static int CROSS_BG   = argb(0, 127, 0  , 0  );
    private final static int ZERO_FG    = argb(0, 0  , 0  , 255);
    private final static int ZERO_BG    = argb(0, 0  , 0  , 127);
    private final static int NEUTRAL_FG = argb(0, 255, 255, 255);
    private final static int NEUTRAL_BG = argb(0, 127, 127, 127);
    private final static int BORDER     = argb(0, 0  , 255, 0  );
    private final static int EMPTY_FG   = argb(0, 200, 200, 200);
    private final static int EMPTY_BG   = argb(0, 240, 240, 240);

    private Context context;
    /**
     * Actual colors to be used. They are calculated from hues.
     */
    private int crossHigh, crossMedium, crossLow;
    private int zeroHigh, zeroMedium, zeroLow;

    public SVGFigureSource(Context context) {
        this.context = context;
        setHueCross(WoVPreferences.DEFAULT_CROSS_COLOR);
        setHueZero(WoVPreferences.DEFAULT_ZERO_COLOR);
    }

    private static int hueToColor(float hue, float saturation, float value) {
        return HSVToColor(new float[]{hue, saturation, value});
    }

    private static int getHighColor(float hue) {
        return hueToColor(hue, 1.00f, 1.00f);
    }

    private static int getMediumColor(float hue) {
        return hueToColor(hue, 0.70f, 1.00f);
    }

    private static int getLowColor(float hue) {
        return hueToColor(hue, 0.43f, 1.00f);
    }

    @Override
    public void setHueCross(int newHueCross) {
        crossHigh   = getHighColor(newHueCross);
        crossMedium = getMediumColor(newHueCross);
        crossLow    = getLowColor(newHueCross);
    }

    @Override
    public void setHueZero(int newHueZero) {
        zeroHigh    = getHighColor(newHueZero);
        zeroMedium  = getMediumColor(newHueZero);
        zeroLow     = getLowColor(newHueZero);
    }

    @Override
    public Drawable loadFigure(BoardCellState state) {
        Hashtable<Integer, Integer> colors = new Hashtable<>();
        colors.put(NEUTRAL_FG, EMPTY_FG);
        colors.put(CROSS_FG, crossHigh);
        colors.put(ZERO_FG, zeroHigh);

        colors.put(NEUTRAL_BG, EMPTY_BG);
        if (state.isHighlighted()) {
            colors.put(CROSS_BG, crossMedium);
            colors.put(ZERO_BG, zeroMedium);
        } else {
            colors.put(CROSS_BG, crossLow);
            colors.put(ZERO_BG, zeroLow);
        }

        switch (state.getFocus()) {
            case CROSS:
                colors.put(BORDER, crossHigh);
                break;
            case ZERO:
                colors.put(BORDER, zeroHigh);
                break;
            case NONE:
                switch (state.getCellType().getOwner()) {
                    case CROSS:
                        colors.put(BORDER, colors.get(CROSS_BG));
                        break;
                    case ZERO:
                        colors.put(BORDER, colors.get(ZERO_BG));
                        break;
                    case NONE:
                        colors.put(BORDER, colors.get(NEUTRAL_BG));
                        break;
                }
                break;
        }
        int resourceId = getResourceId(state);

        InputStream is = context.getResources().openRawResource(resourceId);
        return SVGParser.getSVGFromInputStream(is, colors).createPictureDrawable();
    }

    protected abstract int getResourceId(BoardCellState state);
}
