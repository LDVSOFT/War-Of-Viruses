package net.ldvsoft.warofviruses;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by ldvsoft on 17.10.15.
 */
public class BoardCellButton extends View {
    protected Paint paint;
    protected Rect rect;

    public BoardCellButton(Context context) {
        super(context);
        initTools();
    }

    public BoardCellButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initTools();
    }

    public BoardCellButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initTools();
    }

    protected void initTools() {
        rect = new Rect();
        paint = new Paint();
        paint.setColor(Color.argb(255, 255, 127, 0));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.getClipBounds(rect);
        canvas.drawRect(rect, paint);
    }
}
