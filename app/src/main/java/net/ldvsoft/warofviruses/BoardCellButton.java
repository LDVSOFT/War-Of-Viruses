package net.ldvsoft.warofviruses;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;

import java.io.IOException;
import java.io.InputStream;

import static android.graphics.Color.*;

/**
 * Created by ldvsoft on 17.10.15.
 */
public class BoardCellButton extends ImageView {
    final static String X_FG       = colorToString(RED);
    final static String X_BG       = colorToString(rgb(127, 0, 0));
    final static String O_FG       = colorToString(BLUE);
    final static String O_BG       = colorToString(rgb(0, 0, 127));
    final static String BORDER     = colorToString(GREEN);
    final static String NEUTRAL_FG = colorToString(WHITE);
    final static String NEUTRAL_BG = colorToString(rgb(127, 127, 127));

    final static int EMPTY_FG = rgb(200, 200, 200);
    final static int EMPTY_BG = rgb(240, 240, 240);

    protected static Drawable cellEmpty;
    protected static Drawable cellEmpty_borderedX;
    protected static Drawable cellEmpty_borderedO;
    protected static Drawable cellXalive;
    protected static Drawable cellXalive_borderedX;
    protected static Drawable cellXalive_borderedO;
    protected static Drawable cellXdead;
    protected static Drawable cellXdead_borderedO;
    protected static Drawable cellOalive;
    protected static Drawable cellOalive_borderedX;
    protected static Drawable cellOalive_borderedO;
    protected static Drawable cellOdead;
    protected static Drawable cellOdead_borderedX;


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

    private static String colorToString(int color) {
        return String.format("#%06x", 0xFFFFFF & color);
    }

    private static int hueToColor(float hue, float saturation, float value) {
        return HSVToColor(new float[]{hue, saturation, value});
    }

    private static int getHighColor(float hue) {
        return hueToColor(hue, 1.00f, 1.00f);
    }

    private static int getLowColor(float hue) {
        return hueToColor(hue, 0.43f, 1.00f);
    }

    private static String setBorder(String s, int color) {
        return s.replaceAll(BORDER, colorToString(color));
    }

    public static void loadDrawables(Context context, int hueX, int hueO) {
        int Xhigh = getHighColor(hueX);
        int Xlow  = getLowColor(hueX);
        int Ohigh = getHighColor(hueO);
        int Olow  = getLowColor(hueO);
        String Xalive, Xdead, Oalive, Odead, empty;
        try {
            empty  = loadSVG(context, R.raw.board_cell_empty  );
            Xalive = loadSVG(context, R.raw.board_cell_x_alive);
            Xdead  = loadSVG(context, R.raw.board_cell_x_dead );
            Oalive = loadSVG(context, R.raw.board_cell_o_alive);
            Odead  = loadSVG(context, R.raw.board_cell_o_dead );
        } catch (IOException e) {
            e.printStackTrace();
            Log.wtf("BoardCellButton", "Cannot load SVGs!");
            return;
        }
        Xalive = decodeActiveColors(Xalive, Xhigh, Xlow, Ohigh, Olow);
        Xdead  = decodeActiveColors(Xdead , Xhigh, Xlow, Ohigh, Olow);
        Oalive = decodeActiveColors(Oalive, Xhigh, Xlow, Ohigh, Olow);
        Odead  = decodeActiveColors(Odead , Xhigh, Xlow, Ohigh, Olow);
        empty  = empty.replaceAll(NEUTRAL_BG, colorToString(EMPTY_BG)).replaceAll(NEUTRAL_FG, colorToString(EMPTY_FG));
        cellEmpty            = SVGParser.getSVGFromString(setBorder(empty , EMPTY_BG)).createPictureDrawable();
        cellEmpty_borderedX  = SVGParser.getSVGFromString(setBorder(empty , Xhigh   )).createPictureDrawable();
        cellEmpty_borderedO  = SVGParser.getSVGFromString(setBorder(empty , Ohigh   )).createPictureDrawable();
        cellXalive           = SVGParser.getSVGFromString(setBorder(Xalive, Xlow    )).createPictureDrawable();
        cellXalive_borderedX = SVGParser.getSVGFromString(setBorder(Xalive, Xhigh   )).createPictureDrawable();
        cellXalive_borderedO = SVGParser.getSVGFromString(setBorder(Xalive, Ohigh   )).createPictureDrawable();
        cellXdead            = SVGParser.getSVGFromString(setBorder(Xdead , Olow    )).createPictureDrawable();
        cellXdead_borderedO  = SVGParser.getSVGFromString(setBorder(Xdead , Ohigh   )).createPictureDrawable();
        cellOalive           = SVGParser.getSVGFromString(setBorder(Oalive, Olow    )).createPictureDrawable();
        cellOalive_borderedX = SVGParser.getSVGFromString(setBorder(Oalive, Xhigh   )).createPictureDrawable();
        cellOalive_borderedO = SVGParser.getSVGFromString(setBorder(Oalive, Ohigh   )).createPictureDrawable();
        cellOdead            = SVGParser.getSVGFromString(setBorder(Odead , Xlow    )).createPictureDrawable();
        cellOdead_borderedX  = SVGParser.getSVGFromString(setBorder(Odead , Ohigh   )).createPictureDrawable();
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

    protected static String decodeActiveColors(String s, int Xhigh, int Xlow, int Ohigh, int Olow) {
        return s
                .replaceAll(X_FG, colorToString(Xhigh))
                .replaceAll(X_BG, colorToString(Xlow))
                .replaceAll(O_FG, colorToString(Ohigh))
                .replaceAll(O_BG, colorToString(Olow ))
                ;
    }
}
