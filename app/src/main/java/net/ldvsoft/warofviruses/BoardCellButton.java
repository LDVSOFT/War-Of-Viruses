package net.ldvsoft.warofviruses;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.larvalabs.svgandroid.SVGParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Hashtable;
import java.util.Map;

import static android.graphics.Color.HSVToColor;
import static android.graphics.Color.argb;

/**
 * Created by ldvsoft on 17.10.15.
 */
public class BoardCellButton extends ImageView {
    public enum BoardCellType {
        CELL_EMPTY,
        CELL_EMPTY_FOR_CROSS,
        CELL_EMPTY_FOR_ZERO,
        CELL_CROSS,
        CELL_CROSS_FOR_CROSS,
        CELL_CROSS_FOR_ZERO,
        CELL_CROSS_FOR_ZERO_HIGHLIGHTED,
        CELL_CROSS_HIGHLIGHTED,
        CELL_CROSS_DEAD,
        CELL_CROSS_DEAD_FOR_ZERO,
        CELL_CROSS_DEAD_HIGHLIGHTED,
        CELL_ZERO,
        CELL_ZERO_FOR_CROSS,
        CELL_ZERO_FOR_CROSS_HIGHLIGHTED,
        CELL_ZERO_FOR_ZERO,
        CELL_ZERO_HIGHLIGHTED,
        CELL_ZERO_DEAD,
        CELL_ZERO_DEAD_FOR_CROSS,
        CELL_ZERO_DEAD_HIGHLIGHTED
    }

    private static int hueCross = WoVPreferences.DEFAULT_CROSS_COLOR, hueZero = WoVPreferences.DEFAULT_ZERO_COLOR;

    private static Map<BoardCellType, Drawable> loadedImages = new EnumMap<>(BoardCellType.class);

    protected final static int CROSS_FG   = argb(0, 255, 0  , 0  );
    protected final static int CROSS_BG   = argb(0, 127, 0  , 0  );
    protected final static int ZERO_FG    = argb(0, 0  , 0  , 255);
    protected final static int ZERO_BG    = argb(0, 0  , 0  , 127);
    protected final static int NEUTRAL_FG = argb(0, 255, 255, 255);
    protected final static int NEUTRAL_BG = argb(0, 127, 127, 127);
    protected final static int BORDER     = argb(0, 0  , 255, 0  );

    protected final static int EMPTY_FG   = argb(0, 200, 200, 200);
    protected final static int EMPTY_BG   = argb(0, 240, 240, 240);

    public BoardCellButton(Context context) {
        super(context);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public BoardCellButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public BoardCellButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        if (drawable == getDrawable())
            return;
        super.setImageDrawable(drawable);
        invalidate();
    }

    protected static int hueToColor(float hue, float saturation, float value) {
        return HSVToColor(new float[]{hue, saturation, value});
    }

    protected static int getHighColor(float hue) {
        return hueToColor(hue, 1.00f, 1.00f);
    }

    protected static int getMediumColor(float hue) {
        return hueToColor(hue, 0.70f, 1.00f);
    }

    protected static int getLowColor(float hue) {
        return hueToColor(hue, 0.43f, 1.00f);
    }

    public static void setHueCross(int newHueCross) {
        hueCross = newHueCross;
        loadedImages.clear();
    }

    public static void setHueZero(int newHueZero) {
        hueZero = newHueZero;
        loadedImages.clear();
    }

    protected static Drawable getImage(String svg, Map<Integer, Integer> map) {
        return SVGParser.getSVGFromString(svg, map).createPictureDrawable();
    }

    public static Drawable getDrawable(Context context, BoardCellType type) {
        if (loadedImages.containsKey(type)) {
            return loadedImages.get(type);
        }

        int crossHigh   = getHighColor(hueCross);
        int crossMedium = getMediumColor(hueCross);
        int crossLow    = getLowColor(hueCross);
        int zeroHigh    = getHighColor(hueZero);
        int zeroMedium  = getMediumColor(hueZero);
        int zeroLow     = getLowColor(hueZero);

        String cross, crossDead, zero, zeroDead, empty;

        try {
            empty      = loadSVG(context, R.raw.board_cell_empty     );
            cross      = loadSVG(context, R.raw.board_cell_cross     );
            crossDead  = loadSVG(context, R.raw.board_cell_cross_dead);
            zero       = loadSVG(context, R.raw.board_cell_zero      );
            zeroDead   = loadSVG(context, R.raw.board_cell_zero_dead );
        } catch (IOException e) {
            e.printStackTrace();
            Log.wtf("BoardCellButton", "Cannot load SVGs!");
            return null;
        }

        Hashtable<Integer, Integer> colors = new Hashtable<>(10);
        colors.put(CROSS_FG, crossHigh);
        colors.put(CROSS_BG, crossLow);
        colors.put(ZERO_FG, zeroHigh);
        colors.put(ZERO_BG, zeroLow);
        colors.put(NEUTRAL_FG, EMPTY_FG);
        colors.put(NEUTRAL_BG, EMPTY_BG);

        colors.put(BORDER, EMPTY_BG);

        Drawable result = null;

        if (type == BoardCellType.CELL_EMPTY) {
            result = getImage(empty, colors);
        }

        colors.put(BORDER, crossHigh);
        switch (type) {
            case CELL_EMPTY_FOR_CROSS:
                result = getImage(empty, colors);
                break;
            case CELL_CROSS_FOR_CROSS:
                result = getImage(cross, colors);
                break;
            case CELL_ZERO_FOR_CROSS:
                result = getImage(zero, colors);
                break;
            case CELL_ZERO_DEAD_FOR_CROSS:
                result = getImage(zeroDead, colors);
                break;
        }

        colors.put(BORDER, crossLow);
        switch (type) {
            case CELL_CROSS:
                result = getImage(cross, colors);
                break;
            case CELL_ZERO_DEAD:
                result = getImage(zeroDead, colors);
                break;
        }

        colors.put(BORDER, zeroHigh);
        switch (type) {
            case CELL_EMPTY_FOR_ZERO:
                result =  getImage(empty, colors);
                break;
            case CELL_CROSS_FOR_ZERO:
                result = getImage(cross, colors);
                break;
            case CELL_CROSS_DEAD_FOR_ZERO:
                result = getImage(crossDead, colors);
                break;
            case CELL_ZERO_FOR_ZERO:
                result = getImage(zero, colors);
                break;
        }

        colors.put(BORDER, zeroLow);
        switch (type) {
            case CELL_ZERO:
                result = getImage(zero, colors);
                break;
            case CELL_CROSS_DEAD:
                result = getImage(crossDead, colors);
                break;
        }

        colors.put(CROSS_BG, crossMedium);
        colors.put(ZERO_BG, zeroMedium);

        colors.put(BORDER, crossHigh);

        if (type == BoardCellType.CELL_ZERO_FOR_CROSS_HIGHLIGHTED) {
            result = getImage(zero, colors);
        }

        colors.put(BORDER, crossMedium);
        switch (type) {
            case CELL_CROSS_HIGHLIGHTED:
                result = getImage(cross, colors);
                break;
            case CELL_ZERO_DEAD_HIGHLIGHTED:
                result = getImage(zeroDead, colors);
                break;
        }

        colors.put(BORDER, zeroHigh);
        if (type == BoardCellType.CELL_CROSS_FOR_ZERO_HIGHLIGHTED) {
            result = getImage(cross, colors);
        }

        colors.put(BORDER, zeroMedium);
        switch (type) {
            case CELL_ZERO_HIGHLIGHTED:
                result = getImage(zero, colors);
                break;
            case CELL_CROSS_DEAD_HIGHLIGHTED:
                result = getImage(crossDead, colors);
        }

        assert result != null;
        loadedImages.put(type, result);
        return result;
    }

    protected static String loadSVG(Context context, int id) throws IOException {
        try {
            InputStream is = context.getResources().openRawResource(id);
            byte[] b = new byte[is.available()];
            is.read(b);
            return new String(b);
        } catch (Exception e) {
            Log.wtf("BoardCellButton", "What the hell is with svg?!");
            throw e;
        }
    }
}
