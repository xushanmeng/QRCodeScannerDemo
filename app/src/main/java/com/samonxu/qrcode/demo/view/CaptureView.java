package com.samonxu.qrcode.demo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.samonxu.qrcode.demo.R;

import java.util.LinkedList;

public class CaptureView extends View {

    private class PossiblePoint {
        public float x, y;
        public long foundTime;
    }

    private static final int MASK_COLOR = 0x80000000;
    private static final int POSSIBLE_POINT_COLOR = 0xC0FFFF00;

    private static final int POSSIBLE_POINT_ALIVE_MS = 200;
    private static final int SCANNER_DURATION = 2000;
    private long startTime = -1;

    private Rect frame;
    private Paint paint;
    private LinkedList<PossiblePoint> possiblePoints;
    private Drawable frameDrawable, scannerDrawable;
    private int scannerHeight = 0;

    public CaptureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public CaptureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CaptureView(Context context) {
        super(context);
        init(context);
    }

    public void init(Context context) {
        frame = new Rect();
        paint = new Paint();
        paint.setAntiAlias(true);
        possiblePoints = new LinkedList<PossiblePoint>();
        frameDrawable = getResources().getDrawable(R.mipmap.qrcode_scan_frame);
        scannerDrawable = getResources().getDrawable(R.mipmap.qrcode_scan_scaner);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int length = (int) (width * 0.6);
        frame.left = width / 2 - length / 2;
        frame.right = width / 2 + length / 2;
        frame.top = height / 2 - length / 2;
        frame.bottom = height / 2 + length / 2;
        frameDrawable.setBounds(frame.left - 10, frame.top - 10, frame.right + 10, frame.bottom + 10);
        scannerHeight = scannerDrawable.getIntrinsicHeight() * frame.width() / scannerDrawable.getIntrinsicWidth();
    }

    public Rect getFrameRect() {
        return frame;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        // Draw mask
        paint.setColor(MASK_COLOR);
        paint.setStyle(Style.FILL);
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom, paint);
        canvas.drawRect(frame.right, frame.top, width, frame.bottom, paint);
        canvas.drawRect(0, frame.bottom, width, height, paint);

        // Draw possible points
        paint.setColor(POSSIBLE_POINT_COLOR);
        paint.setStyle(Style.FILL);
        long current = System.currentTimeMillis();
        while (possiblePoints.size() > 0 && current - possiblePoints.peek().foundTime >= POSSIBLE_POINT_ALIVE_MS) {
            possiblePoints.poll();
        }
        for (int i = 0; i < possiblePoints.size(); i++) {
            PossiblePoint point = possiblePoints.get(i);
            int radius = (int) (5 * (POSSIBLE_POINT_ALIVE_MS - current + point.foundTime) / POSSIBLE_POINT_ALIVE_MS);
            if (radius > 0) {
                canvas.drawCircle(frame.left + point.x, frame.top + point.y, radius, paint);
            }
        }

        // Draw scanner
        long now = System.currentTimeMillis();
        if (startTime < 0) {
            startTime = now;
        }
        int timePast = (int) ((now - startTime) % SCANNER_DURATION);
        if (timePast >= 0 && timePast <= SCANNER_DURATION / 2) {
            int scannerShift = frame.height() * 2 * timePast / SCANNER_DURATION;
            canvas.save();
            canvas.clipRect(frame);
            scannerDrawable.setBounds(frame.left, frame.top + scannerShift, frame.right, frame.top + scannerHeight + scannerShift);
            scannerDrawable.draw(canvas);
            canvas.restore();
        }
        // Draw frame
        frameDrawable.draw(canvas);

        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        PossiblePoint pp = new PossiblePoint();
        pp.foundTime = System.currentTimeMillis();
        pp.x = point.getX();
        pp.y = point.getY();
        if (possiblePoints.size() >= 10) {
            possiblePoints.poll();
        }
        possiblePoints.add(pp);
    }
}
