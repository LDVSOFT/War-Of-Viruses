package net.ldvsoft.warofviruses;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.larvalabs.svgandroid.SVGParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;

import static android.graphics.Color.BLUE;
import static android.graphics.Color.GREEN;
import static android.graphics.Color.HSVToColor;
import static android.graphics.Color.RED;
import static android.graphics.Color.WHITE;
import static android.graphics.Color.argb;
import static android.graphics.Color.rgb;

/**
 * Created by ldvsoft on 17.10.15.
 */
public class BoardCellButton extends ImageView {
    protected final static int CROSS_FG   = argb(0, 255, 0  , 0  );
    protected final static int CROSS_BG   = argb(0, 127, 0  , 0  );
    protected final static int ZERO_FG    = argb(0, 0  , 0  , 255);
    protected final static int ZERO_BG    = argb(0, 0  , 0  , 127);
    protected final static int NEUTRAL_FG = argb(0, 255, 255, 255);
    protected final static int NEUTRAL_BG = argb(0, 127, 127, 127);
    protected final static int BORDER     = argb(0, 0  , 255, 0  );

    protected final static int EMPTY_FG   = argb(0, 200, 200, 200);
    protected final static int EMPTY_BG   = argb(0, 240, 240, 240);

    protected static Drawable cellEmpty;
    protected static Drawable cellEmpty_forCross;
    protected static Drawable cellEmpty_forZero;
    protected static Drawable cellCross;
    protected static Drawable cellCross_forCross;
    protected static Drawable cellCross_forZero;
    protected static Drawable cellCrossDead;
    protected static Drawable cellCrossDead_forZero;
    protected static Drawable cellZero;
    protected static Drawable cellZero_forCross;
    protected static Drawable cellZero_forZero;
    protected static Drawable cellZeroDead;
    protected static Drawable cellZeroDead_forCross;


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

    protected static int getLowColor(float hue) {
        return hueToColor(hue, 0.43f, 1.00f);
    }

    public static void loadDrawables(Context context, int hueCross, int hueZero) {
        int crossHigh = getHighColor(hueCross);
        int crossLow  = getLowColor(hueCross);
        int zeroHigh  = getHighColor(hueZero);
        int zeroLow   = getLowColor(hueZero);
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
            return;
        }
        Hashtable<Integer, Integer> colors = new Hashtable<>(10);
        colors.put(CROSS_FG, crossHigh);
        colors.put(CROSS_BG, crossLow);
        colors.put(ZERO_FG, zeroHigh);
        colors.put(ZERO_BG, zeroLow);
        colors.put(NEUTRAL_FG, EMPTY_FG);
        colors.put(NEUTRAL_BG, EMPTY_BG);

        colors.put(BORDER, EMPTY_BG);
        cellEmpty             = SVGParser.getSVGFromString(empty    , colors).createPictureDrawable();

        colors.put(BORDER, crossHigh);
        cellEmpty_forCross    = SVGParser.getSVGFromString(empty    , colors).createPictureDrawable();
        cellCross_forCross    = SVGParser.getSVGFromString(cross    , colors).createPictureDrawable();
        cellZero_forCross     = SVGParser.getSVGFromString(zero     , colors).createPictureDrawable();
        cellZeroDead_forCross = SVGParser.getSVGFromString(zeroDead , colors).createPictureDrawable();

        colors.put(BORDER, crossLow );
        cellCross             = SVGParser.getSVGFromString(cross    , colors).createPictureDrawable();
        cellZeroDead          = SVGParser.getSVGFromString(zeroDead , colors).createPictureDrawable();

        colors.put(BORDER, zeroHigh );
        cellEmpty_forZero     = SVGParser.getSVGFromString(empty    , colors).createPictureDrawable();
        cellCross_forZero     = SVGParser.getSVGFromString(cross    , colors).createPictureDrawable();
        cellCrossDead_forZero = SVGParser.getSVGFromString(crossDead, colors).createPictureDrawable();
        cellZero_forZero      = SVGParser.getSVGFromString(zero     , colors).createPictureDrawable();

        colors.put(BORDER, zeroLow  );
        cellZero              = SVGParser.getSVGFromString(zero     , colors).createPictureDrawable();
        cellCrossDead         = SVGParser.getSVGFromString(crossDead, colors).createPictureDrawable();
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
