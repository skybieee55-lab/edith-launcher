package com.edith.launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.widget.LinearLayout;

public class Glass extends LinearLayout {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Path clip = new Path();
    private final RectF rf = new RectF();
    private final Rect src = new Rect();
    private final Rect dst = new Rect();
    private final float radius;

    public Glass(Context c, float radiusPx) {
        super(c);
        radius = radiusPx;
        setWillNotDraw(false);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas c) {
        int w = getWidth();
        int h = getHeight();
        rf.set(0, 0, w, h);
        clip.reset();
        clip.addRoundRect(rf, radius, radius, Path.Direction.CW);
        NightSky s = NightSky.instance;
        Bitmap bl = NightSky.blur;
        c.save();
        c.clipPath(clip);
        if (s != null && bl != null && s.getWidth() > 0) {
            int[] a = new int[2];
            int[] b = new int[2];
            getLocationOnScreen(a);
            s.getLocationOnScreen(b);
            float fx = bl.getWidth() / (float) s.getWidth();
            float fy = bl.getHeight() / (float) s.getHeight();
            int sx = Math.round((a[0] - b[0]) * fx);
            int sy = Math.round((a[1] - b[1]) * fy);
            src.set(sx, sy, sx + Math.max(1, Math.round(w * fx)), sy + Math.max(1, Math.round(h * fy)));
            dst.set(0, 0, w, h);
            p.setStyle(Paint.Style.FILL);
            p.setColor(0xFFFFFFFF);
            c.drawBitmap(bl, src, dst, p);
        }
        p.setStyle(Paint.Style.FILL);
        p.setColor(0x33FFFFFF);
        c.drawRect(rf, p);
        c.restore();
        boolean hl = isFocused();
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(hl ? 4 : 2);
        p.setColor(hl ? 0xFFF0ABFC : 0x55FFFFFF);
        c.drawRoundRect(rf, radius, radius, p);
    }
}
