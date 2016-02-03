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
    private static final String TAG = "BoardCellButton";

    private static Hashtable<Integer, Integer> getDefaultHashtableInitializer() {
        Hashtable<Integer, Integer> colors = new Hashtable<>(10);
        colors.put(CROSS_FG, crossHigh);
        colors.put(CROSS_BG, crossLow);
        colors.put(ZERO_FG, zeroHigh);
        colors.put(ZERO_BG, zeroLow);
        colors.put(NEUTRAL_FG, EMPTY_FG);
        colors.put(NEUTRAL_BG, EMPTY_BG);

        colors.put(BORDER, EMPTY_BG);
        return colors;
    }

    private static Hashtable<Integer, Integer> getDefaultHighlightedHashtableInitializer() {
        Hashtable<Integer, Integer> colors = new Hashtable<>(10);
        colors.put(CROSS_FG, crossHigh);
        colors.put(ZERO_FG, zeroHigh);
        colors.put(CROSS_BG, crossMedium);
        colors.put(ZERO_BG, zeroMedium);
        colors.put(NEUTRAL_FG, EMPTY_FG);
        colors.put(NEUTRAL_BG, EMPTY_BG);

        colors.put(BORDER, EMPTY_BG);
        return colors;
    }

    private static abstract class SVGLoader {
        protected int svgId;
        abstract public Drawable load(Context context) throws IOException;
    }

    private static class EmptyCellLoader extends SVGLoader {
        EmptyCellLoader(int svgName) {
            this.svgId = svgName;
        }

        @Override
        public Drawable load(Context context) throws IOException{
            return getImage(context, svgId, getDefaultHashtableInitializer());
        }
    }

    private static class CrossActiveLoader extends SVGLoader {
        CrossActiveLoader(int svgSource) {
            this.svgId = svgSource;
        }

        @Override
        public Drawable load(Context context) throws IOException{
            Hashtable<Integer, Integer> colors = getDefaultHashtableInitializer();
            colors.put(BORDER, crossHigh);
            return getImage(context, svgId, colors);
        }
    }

    private static class CrossNonActiveLoader extends SVGLoader {
        CrossNonActiveLoader(int svgSource) {
            this.svgId = svgSource;
        }

        @Override
        public Drawable load(Context context) throws IOException{
            Hashtable<Integer, Integer> colors = getDefaultHashtableInitializer();
            colors.put(BORDER, crossLow);
            return getImage(context, svgId, colors);
        }
    }

    private static class ZeroActiveLoader extends SVGLoader {
        ZeroActiveLoader(int svgSource) {
            this.svgId = svgSource;
        }

        @Override
        public Drawable load(Context context) throws IOException{
            Hashtable<Integer, Integer> colors = getDefaultHashtableInitializer();
            colors.put(BORDER, zeroHigh);
            return getImage(context, svgId, colors);
        }
    }

    private static class ZeroNonActiveLoader extends SVGLoader {
        ZeroNonActiveLoader(int svgSource) {
            this.svgId = svgSource;
        }

        @Override
        public Drawable load(Context context) throws IOException{
            Hashtable<Integer, Integer> colors = getDefaultHashtableInitializer();
            colors.put(BORDER, zeroLow);
            return getImage(context, svgId, colors);
        }
    }

    private static class CrossActiveHighlightedLoader extends SVGLoader {
        CrossActiveHighlightedLoader(int svgSource) {
            this.svgId = svgSource;
        }

        @Override
        public Drawable load(Context context) throws IOException{
            Hashtable<Integer, Integer> colors = getDefaultHighlightedHashtableInitializer();
            colors.put(BORDER, crossHigh);
            return getImage(context, svgId, colors);
        }
    }

    private static class CrossNonActiveHighlightedLoader extends SVGLoader {
        CrossNonActiveHighlightedLoader(int svgSource) {
            this.svgId = svgSource;
        }

        @Override
        public Drawable load(Context context) throws IOException{
            Hashtable<Integer, Integer> colors = getDefaultHighlightedHashtableInitializer();
            colors.put(BORDER, crossMedium);
            return getImage(context, svgId, colors);
        }
    }

    private static class ZeroActiveHighlightedLoader extends SVGLoader {
        ZeroActiveHighlightedLoader(int svgSource) {
            this.svgId = svgSource;
        }

        @Override
        public Drawable load(Context context) throws IOException{
            Hashtable<Integer, Integer> colors = getDefaultHighlightedHashtableInitializer();
            colors.put(BORDER, zeroHigh);
            return getImage(context, svgId, colors);
        }
    }

    private static class ZeroNonActiveHighlightedLoader extends SVGLoader {
        ZeroNonActiveHighlightedLoader(int svgSource) {
            this.svgId = svgSource;
        }

        @Override
        public Drawable load(Context context) throws IOException{
            Hashtable<Integer, Integer> colors = getDefaultHighlightedHashtableInitializer();
            colors.put(BORDER, zeroMedium);
            return getImage(context, svgId, colors);
        }
    }

    private static int crossHigh, crossMedium, crossLow;
    private static int zeroHigh, zeroMedium, zeroLow;

    static Map<BoardCellType, SVGLoader> svgLoaders;
    private static Map<BoardCellType, Drawable> loadedImages;

    static {
        loadedImages = new EnumMap<>(BoardCellType.class);

        setHueCross(WoVPreferences.DEFAULT_CROSS_COLOR);
        setHueZero(WoVPreferences.DEFAULT_ZERO_COLOR);

        svgLoaders = new EnumMap<>(BoardCellType.class);

        svgLoaders.put(BoardCellType.CELL_EMPTY, new EmptyCellLoader(R.raw.board_cell_empty));

        svgLoaders.put(BoardCellType.CELL_EMPTY_FOR_CROSS, new CrossActiveLoader(R.raw.board_cell_empty));
        svgLoaders.put(BoardCellType.CELL_CROSS_FOR_CROSS, new CrossActiveLoader(R.raw.board_cell_cross));
        svgLoaders.put(BoardCellType.CELL_ZERO_FOR_CROSS, new CrossActiveLoader(R.raw.board_cell_zero));
        svgLoaders.put(BoardCellType.CELL_ZERO_DEAD_FOR_CROSS, new CrossActiveLoader(R.raw.board_cell_zero_dead));

        svgLoaders.put(BoardCellType.CELL_CROSS, new CrossNonActiveLoader(R.raw.board_cell_cross));
        svgLoaders.put(BoardCellType.CELL_ZERO_DEAD, new CrossNonActiveLoader(R.raw.board_cell_zero_dead));

        svgLoaders.put(BoardCellType.CELL_EMPTY_FOR_ZERO, new ZeroActiveLoader(R.raw.board_cell_empty));
        svgLoaders.put(BoardCellType.CELL_CROSS_FOR_ZERO, new ZeroActiveLoader(R.raw.board_cell_cross));
        svgLoaders.put(BoardCellType.CELL_CROSS_DEAD_FOR_ZERO, new ZeroActiveLoader(R.raw.board_cell_cross_dead));
        svgLoaders.put(BoardCellType.CELL_ZERO_FOR_ZERO, new ZeroActiveLoader(R.raw.board_cell_zero));

        svgLoaders.put(BoardCellType.CELL_ZERO, new ZeroNonActiveLoader(R.raw.board_cell_zero));
        svgLoaders.put(BoardCellType.CELL_CROSS_DEAD, new ZeroNonActiveLoader(R.raw.board_cell_cross_dead));

        svgLoaders.put(BoardCellType.CELL_ZERO_FOR_CROSS_HIGHLIGHTED, new CrossActiveHighlightedLoader(R.raw.board_cell_zero));

        svgLoaders.put(BoardCellType.CELL_CROSS_HIGHLIGHTED, new CrossNonActiveHighlightedLoader(R.raw.board_cell_cross));
        svgLoaders.put(BoardCellType.CELL_ZERO_DEAD_HIGHLIGHTED, new CrossNonActiveHighlightedLoader(R.raw.board_cell_zero_dead));

        svgLoaders.put(BoardCellType.CELL_CROSS_FOR_ZERO_HIGHLIGHTED, new ZeroActiveHighlightedLoader(R.raw.board_cell_cross));

        svgLoaders.put(BoardCellType.CELL_ZERO_HIGHLIGHTED, new ZeroNonActiveHighlightedLoader(R.raw.board_cell_zero));
        svgLoaders.put(BoardCellType.CELL_CROSS_DEAD_HIGHLIGHTED, new ZeroNonActiveHighlightedLoader(R.raw.board_cell_cross_dead));
    }

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
        if (drawable == getDrawable()) {
            return;
        }
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
        crossHigh   = getHighColor(newHueCross);
        crossMedium = getMediumColor(newHueCross);
        crossLow    = getLowColor(newHueCross);

        loadedImages.clear();
    }

    public static void setHueZero(int newHueZero) {
        zeroHigh    = getHighColor(newHueZero);
        zeroMedium  = getMediumColor(newHueZero);
        zeroLow     = getLowColor(newHueZero);

        loadedImages.clear();
    }

    protected static Drawable getImage(Context context, int id, Map<Integer, Integer> map) {
        InputStream is = context.getResources().openRawResource(id);
        return SVGParser.getSVGFromInputStream(is, map).createPictureDrawable();
    }

    public static Drawable getDrawable(Context context, BoardCellType type) {
        if (loadedImages.containsKey(type)) {
            return loadedImages.get(type);
        }

        try {
            Drawable result = svgLoaders.get(type).load(context);
            loadedImages.put(type, result);
            return result;
        } catch (IOException e) {
            Log.e(TAG, "Failed to load SVG:\n" + Log.getStackTraceString(e));
            return null;
        }
    }
}
