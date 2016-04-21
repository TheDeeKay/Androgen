package com.example.aleksa.androgen;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.TextView;

public class SelectionTextView extends TextView{

    public boolean strikeThrough = false;

    private Paint paint;

    public float textWidth;

    public SelectionTextView(Context context) {
        super(context);
        init(context);
    }

    public SelectionTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        paint = new Paint();

        double alphaPercentage = 0.75;

        paint.setColor(ContextCompat.getColor(context, R.color.themeColor));
        paint.setStrokeWidth(getResources().getDisplayMetrics().density * 6);
        paint.setAlpha((int) (255 * alphaPercentage));
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        if (strikeThrough) {

            float halfWidth = getWidth() / 2;
            float linePositionHeight = getHeight() / 2;

            float overDraw = getResources().getDisplayMetrics().density * 10;

            canvas.drawLine(
                    halfWidth - textWidth / 2 - overDraw, linePositionHeight,
                    halfWidth + textWidth / 2 + overDraw, linePositionHeight,
                    paint);
        }
    }
}
