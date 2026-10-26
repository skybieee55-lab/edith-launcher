package com.edith.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.view.View;

import java.util.Random;

public class Mascot extends View {
    public static final int IDLE = 0;
    public static final int TALKING = 1;
    public static final int HAPPY = 2;
    public static final int THINKING = 3;
    private static final int[] MOUTH = {2, 7, 4, 8, 3, 6};

    private int state = IDLE;
    private int phase = 0;
    private boolean blink = false;
    private int blinkLeft = 0;
    private int nextBlink = 25;
    private boolean running = false;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Handler h = new Handler();
    private final Random rnd = new Random();
    private final Path path = new Path();
    private final RectF rf = new RectF();

    private final Runnable loop = new Runnable() {
        @Override
        public void run() {
            phase++;
            boolean redraw = state == TALKING || state == THINKING;
            if (blinkLeft > 0) {
                blinkLeft--;
                if (blinkLeft == 0) {
                    blink = false;
                    redraw = true;
                }
            } else if (--nextBlink <= 0) {
                blink = true;
                blinkLeft = 2;
                nextBlink = 20 + rnd.nextInt(25);
                redraw = true;
            }
            if (redraw) invalidate();
            if (running) h.postDelayed(this, 120);
        }
    };

    public Mascot(Context c) {
        super(c);
    }

    public void setState(int s) {
        state = s;
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        running = true;
        h.postDelayed(loop, 120);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        running = false;
        h.removeCallbacks(loop);
    }

    private void fill(int color) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(color);
    }

    private void stroke(int color, float w) {
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(w);
        p.setColor(color);
    }

    private void oval(Canvas c, float l, float t, float r, float b) {
        rf.set(l, t, r, b);
        c.drawOval(rf, p);
    }

    @Override
    protected void onDraw(Canvas c) {
        float s = Math.min(getWidth() / 380f, getHeight() / 250f);
        c.save();
        c.translate((getWidth() - 380f * s) / 2f, (getHeight() - 250f * s) / 2f);
        c.scale(s, s);

        fill(0x59F0ABFC);
        oval(c, 120, 201, 260, 227);
        stroke(0xFFE879F9, 2);
        oval(c, 128, 203, 252, 225);

        path.reset();
        path.moveTo(130, 100);
        path.quadTo(190, 30, 250, 100);
        stroke(0xFFE9D5FF, 5);
        c.drawPath(path, p);

        fill(0xFFF5F0FF);
        oval(c, 156, 152, 224, 212);

        path.reset();
        path.moveTo(190, 190);
        path.cubicTo(181, 181, 184, 175, 190, 179);
        path.cubicTo(196, 175, 199, 181, 190, 190);
        path.close();
        fill(0xFFF472B6);
        c.drawPath(path, p);

        fill(0xFFF5F0FF);
        c.drawCircle(158, 172, 10, p);
        c.drawCircle(222, 172, 10, p);

        fill(0xFF60A5FA);
        oval(c, 117, 95, 137, 135);
        oval(c, 243, 95, 263, 135);

        fill(0xFFF5F0FF);
        oval(c, 128, 58, 252, 162);
        fill(0xFF1A1030);
        oval(c, 140, 74, 240, 150);

        if (blink) {
            stroke(0xFFFBCFE8, 4);
            c.drawLine(160, 114, 178, 114, p);
            c.drawLine(202, 114, 220, 114, p);
        } else if (state == THINKING) {
            fill(0xFFFBCFE8);
            c.drawCircle(169, 112, 5, p);
            c.drawCircle(211, 112, 5, p);
            int dots = phase % 4;
            for (int i = 0; i < dots && i < 3; i++) {
                c.drawCircle(228 + i * 9, 92 - i * 5, 2.5f, p);
            }
        } else {
            float top = state == HAPPY ? 98 : 101;
            path.reset();
            path.moveTo(160, 114);
            path.quadTo(169, top, 178, 114);
            path.moveTo(202, 114);
            path.quadTo(211, top, 220, 114);
            stroke(0xFFFBCFE8, 4);
            c.drawPath(path, p);
        }

        if (state == TALKING) {
            fill(0xFFF472B6);
            float ry = MOUTH[phase % MOUTH.length] * 0.7f;
            oval(c, 183, 132 - ry, 197, 132 + ry);
        } else if (state == THINKING) {
            stroke(0xFFF472B6, 3);
            c.drawLine(186, 132, 194, 132, p);
        } else {
            path.reset();
            path.moveTo(182, 128);
            path.quadTo(190, state == HAPPY ? 140 : 134, 198, 128);
            stroke(0xFFF472B6, 3);
            c.drawPath(path, p);
        }

        fill(0x80F472B6);
        c.drawCircle(156, 128, 5, p);
        c.drawCircle(224, 128, 5, p);

        fill(0xFFFBCFE8);
        c.save();
        c.rotate(-25, 177, 52);
        oval(c, 164, 45, 190, 59);
        c.restore();
        c.save();
        c.rotate(25, 203, 52);
        oval(c, 190, 45, 216, 59);
        c.restore();
        fill(0xFFF472B6);
        c.drawCircle(190, 57, 4, p);

        c.restore();
    }
}
