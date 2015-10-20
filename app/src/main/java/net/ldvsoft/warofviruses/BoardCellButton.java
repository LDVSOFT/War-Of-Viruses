package net.ldvsoft.warofviruses;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.larvalabs.svgandroid.SVGParser;

import java.io.IOException;
import java.io.InputStream;

import static android.graphics.Color.BLUE;
import static android.graphics.Color.GREEN;
import static android.graphics.Color.HSVToColor;
import static android.graphics.Color.RED;
import static android.graphics.Color.WHITE;
import static android.graphics.Color.rgb;

/**
 * Created by ldvsoft on 17.10.15.
 */
public class BoardCellButton extends ImageView {
    protected final static String CROSS_FG   = colorToString(RED);
    protected final static String CROSS_BG   = colorToString(rgb(127, 0, 0));
    protected final static String ZERO_FG    = colorToString(BLUE);
    protected final static String ZERO_BG    = colorToString(rgb(0, 0, 127));
    protected final static String NEUTRAL_FG = colorToString(WHITE);
    protected final static String NEUTRAL_BG = colorToString(rgb(127, 127, 127));
    protected final static String BORDER     = colorToString(GREEN);

    protected final static int EMPTY_FG = rgb(200, 200, 200);
    protected final static int EMPTY_BG = rgb(240, 240, 240);

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

    protected static String colorToString(int color) {
        return String.format("#%06x", 0xFFFFFF & color);
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

    protected static String setBorder(String s, int color) {
        return s.replaceAll(BORDER, colorToString(color));
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
        cross      = decodeActiveColors(cross,      crossHigh, crossLow, zeroHigh, zeroLow);
        crossDead  = decodeActiveColors(crossDead , crossHigh, crossLow, zeroHigh, zeroLow);
        zero       = decodeActiveColors(zero,       crossHigh, crossLow, zeroHigh, zeroLow);
        zeroDead   = decodeActiveColors(zeroDead ,  crossHigh, crossLow, zeroHigh, zeroLow);
        empty = empty.replaceAll(NEUTRAL_BG, colorToString(EMPTY_BG)).replaceAll(NEUTRAL_FG, colorToString(EMPTY_FG));
        cellEmpty             = SVGParser.getSVGFromString(setBorder(empty    , EMPTY_BG    )).createPictureDrawable();
        cellEmpty_forCross    = SVGParser.getSVGFromString(setBorder(empty    , crossHigh   )).createPictureDrawable();
        cellEmpty_forZero     = SVGParser.getSVGFromString(setBorder(empty    , zeroHigh    )).createPictureDrawable();
        cellCross             = SVGParser.getSVGFromString(setBorder(cross    , crossLow    )).createPictureDrawable();
        cellCross_forCross    = SVGParser.getSVGFromString(setBorder(cross    , crossHigh   )).createPictureDrawable();
        cellCross_forZero     = SVGParser.getSVGFromString(setBorder(cross    , zeroHigh    )).createPictureDrawable();
        cellCrossDead         = SVGParser.getSVGFromString(setBorder(crossDead, zeroLow     )).createPictureDrawable();
        cellCrossDead_forZero = SVGParser.getSVGFromString(setBorder(crossDead, zeroHigh    )).createPictureDrawable();
        cellZero              = SVGParser.getSVGFromString(setBorder(zero     , zeroLow     )).createPictureDrawable();
        cellZero_forCross     = SVGParser.getSVGFromString(setBorder(zero     , crossHigh   )).createPictureDrawable();
        cellZero_forZero      = SVGParser.getSVGFromString(setBorder(zero     , zeroHigh    )).createPictureDrawable();
        cellZeroDead          = SVGParser.getSVGFromString(setBorder(zeroDead , crossLow    )).createPictureDrawable();
        cellZeroDead_forCross = SVGParser.getSVGFromString(setBorder(zeroDead , crossHigh   )).createPictureDrawable();
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

    protected static String decodeActiveColors(String s, int crossHigh, int crossLow, int zeroHigh, int zeroLow) {
        return s
                .replaceAll(CROSS_FG, colorToString(crossHigh))
                .replaceAll(CROSS_BG, colorToString(crossLow ))
                .replaceAll(ZERO_FG , colorToString(zeroHigh ))
                .replaceAll(ZERO_BG , colorToString(zeroLow  ))
                ;
    }
}
