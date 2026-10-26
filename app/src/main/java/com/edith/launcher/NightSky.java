package com.edith.launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

public class NightSky extends View {
    public static NightSky instance;
    public static Bitmap sky;
    public static Bitmap blur;
    private final Paint p = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final Rect dst = new Rect();

    public NightSky(Context c) {
        super(c);
        instance = this;
        if (sky == null) build();
    }

    private static void build() {
        int w = 640;
        int h = 360;
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(b);
        Paint q = new Paint(Paint.ANTI_ALIAS_FLAG);
        q.setShader(new LinearGradient(0, 0, 0, h,
                new int[]{0xFF0F0A2E, 0xFF2A1560, 0xFF7B2F8E, 0xFFD0618F},
                new float[]{0f, 0.45f, 0.8f, 1f}, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, q);
        q.setShader(null);
        Random r = new Random(7);

        q.setColor(0xFFFFFFFF);
        for (int i = 0; i < 70; i++) {
            q.setAlpha(80 + r.nextInt(170));
            c.drawCircle(r.nextInt(w), r.nextInt(190), 0.6f + r.nextFloat() * 1.2f, q);
        }

        for (int i = 0; i < 8; i++) {
            q.setColor(0xFFFFB3D9);
            q.setAlpha(35);
            c.drawCircle(r.nextInt(w), 120 + r.nextInt(180), 12 + r.nextInt(18), q);
        }

        ArrayList<int[]> bl = new ArrayList<>();
        q.setColor(0xFF1A0F3D);
        int x = 0;
        while (x < w) {
            int bw = 14 + r.nextInt(26);
            int bh = 30 + r.nextInt(70);
            c.drawRect(x, h - bh, x + bw, h, q);
            bl.add(new int[]{x, bw, bh});
            x += bw + r.nextInt(3);
        }

        for (int i = 0; i < 170; i++) {
            int[] bd = bl.get(r.nextInt(bl.size()));
            int wx = bd[0] + 2 + r.nextInt(Math.max(1, bd[1] - 4));
            int wy = h - bd[2] + 2 + r.nextInt(Math.max(1, bd[2] - 4));
            q.setColor(0xFFFFD27F);
            q.setAlpha(110 + r.nextInt(145));
            c.drawRect(wx, wy, wx + 2, wy + 2, q);
        }

        sky = b;
        blur = Bitmap.createScaledBitmap(b, 48, 27, true);
    }

    @Override
    protected void onDraw(Canvas c) {
        dst.set(0, 0, getWidth(), getHeight());
        c.drawBitmap(sky, null, dst, p);
    }
}
