package com.edith.launcher;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;
    private static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;

    private TextView clock;
    private TextView date;
    private LinearLayout dock;
    private Mascot mascot;
    private ObjectAnimator bob;
    private final Handler handler = new Handler();
    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            updateClock();
            handler.postDelayed(this, 20000);
        }
    };

    private int dp(float v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable box(int color, float radius, int strokeColor) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radius));
        if (strokeColor != 0) g.setStroke(dp(2), strokeColor);
        return g;
    }

    private StateListDrawable focusBg(int normal, int focused, float radius) {
        StateListDrawable s = new StateListDrawable();
        s.addState(new int[]{android.R.attr.state_focused}, box(focused, radius, 0xFFF0ABFC));
        s.addState(new int[]{}, box(normal, radius, 0));
        return s;
    }

    private TextView text(String s, float sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private void addMenuItem(LinearLayout parent, String label, final Runnable action) {
        final TextView t = text(label, 17, Color.WHITE, false);
        t.setPadding(dp(14), dp(10), dp(14), dp(10));
        t.setFocusable(true);
        t.setClickable(true);
        t.setBackground(focusBg(0x00000000, 0xFFF0ABFC, 14));
        t.setOnFocusChangeListener((v, f) -> t.setTextColor(f ? 0xFF2A1560 : Color.WHITE));
        t.setOnClickListener(v -> {
            if (action != null) action.run();
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MATCH, WRAP);
        lp.topMargin = dp(4);
        parent.addView(t, lp);
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        FrameLayout root = new FrameLayout(this);
        root.setBackground(new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xFF2A1560, 0xFF150A38, 0xFF0B0620}));

        final Runnable soon = () ->
                Toast.makeText(this, "Jaldi aa raha hai", Toast.LENGTH_SHORT).show();

        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.VERTICAL);
        menu.setPadding(dp(16), dp(20), dp(16), dp(16));
        menu.setBackground(box(0x33FFFFFF, 20, 0));
        menu.addView(text("EDITH", 32, 0xFFF9A8D4, true));
        TextView sub = text("your AI companion", 12, 0xFFC4B5FD, false);
        sub.setPadding(0, 0, 0, dp(14));
        menu.addView(sub);
        addMenuItem(menu, "Home", null);
        addMenuItem(menu, "Chat", soon);
        addMenuItem(menu, "TV & Apps", () -> {
            if (dock.getChildCount() > 0) dock.getChildAt(0).requestFocus();
        });
        addMenuItem(menu, "Music", soon);
        addMenuItem(menu, "Smart Home", soon);
        addMenuItem(menu, "Settings", () -> {
            try {
                startActivity(new Intent(Settings.ACTION_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } catch (Exception e) {
                soon.run();
            }
        });
        FrameLayout.LayoutParams ml = new FrameLayout.LayoutParams(
                dp(200), WRAP, Gravity.START | Gravity.CENTER_VERTICAL);
        ml.leftMargin = dp(24);
        root.addView(menu, ml);

        LinearLayout center = new LinearLayout(this);
        center.setOrientation(LinearLayout.VERTICAL);
        center.setGravity(Gravity.CENTER_HORIZONTAL);
        mascot = new Mascot(this);
        center.addView(mascot, new LinearLayout.LayoutParams(dp(260), dp(171)));
        TextView bubble = text(
                "Hey, I'm EDITH! Your AI assistant for a smarter, cozier and happier experience.",
                15, Color.WHITE, false);
        bubble.setGravity(Gravity.CENTER);
        bubble.setPadding(dp(16), dp(10), dp(16), dp(10));
        bubble.setBackground(box(0x33FFFFFF, 18, 0x66C4B5FD));
        LinearLayout.LayoutParams bl = new LinearLayout.LayoutParams(dp(360), WRAP);
        bl.topMargin = dp(6);
        center.addView(bubble, bl);
        HorizontalScrollView sv = new HorizontalScrollView(this);
        sv.setHorizontalScrollBarEnabled(false);
        sv.setBackground(box(0x33FFFFFF, 20, 0));
        dock = new LinearLayout(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setPadding(dp(8), dp(8), dp(8), dp(8));
        sv.addView(dock);
        LinearLayout.LayoutParams sl = new LinearLayout.LayoutParams(dp(420), WRAP);
        sl.topMargin = dp(14);
        center.addView(sv, sl);
        root.addView(center, new FrameLayout.LayoutParams(WRAP, WRAP, Gravity.CENTER));

        LinearLayout cbox = new LinearLayout(this);
        cbox.setOrientation(LinearLayout.VERTICAL);
        cbox.setGravity(Gravity.END);
        clock = text("", 26, Color.WHITE, true);
        date = text("", 14, 0xFFC4B5FD, false);
        cbox.addView(clock);
        cbox.addView(date);
        FrameLayout.LayoutParams cl = new FrameLayout.LayoutParams(
                WRAP, WRAP, Gravity.TOP | Gravity.END);
        cl.topMargin = dp(20);
        cl.rightMargin = dp(28);
        root.addView(cbox, cl);

        LinearLayout today = new LinearLayout(this);
        today.setOrientation(LinearLayout.VERTICAL);
        today.setPadding(dp(14), dp(12), dp(14), dp(12));
        today.setBackground(box(0x33FFFFFF, 18, 0));
        today.addView(text("Today", 16, Color.WHITE, true));
        String[] items = {"Be productive", "Stay happy", "Drink water", "Good night"};
        for (String s : items) {
            TextView t = text(s, 14, 0xFFE9D5FF, false);
            t.setPadding(0, dp(8), 0, 0);
            today.addView(t);
        }
        FrameLayout.LayoutParams tl = new FrameLayout.LayoutParams(
                dp(170), WRAP, Gravity.END | Gravity.CENTER_VERTICAL);
        tl.rightMargin = dp(24);
        root.addView(today, tl);

        setContentView(root);
    }

    private void updateClock() {
        Date now = new Date();
        clock.setText(new SimpleDateFormat("h:mm a", Locale.getDefault()).format(now));
        date.setText(new SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(now));
    }

    private void loadApps() {
        dock.removeAllViews();
        final PackageManager pm = getPackageManager();
        List<ResolveInfo> list = new ArrayList<>();
        List<String> seen = new ArrayList<>();
        String[] cats = {Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER};
        for (String c : cats) {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(c);
            for (ResolveInfo r : pm.queryIntentActivities(i, 0)) {
                String p = r.activityInfo.packageName;
                if (p.equals(getPackageName()) || seen.contains(p)) continue;
                seen.add(p);
                list.add(r);
            }
        }
        Collections.sort(list, (a, c) ->
                a.loadLabel(pm).toString().compareToIgnoreCase(c.loadLabel(pm).toString()));
        for (ResolveInfo r : list) dock.addView(tile(r, pm));
    }

    private View tile(final ResolveInfo r, PackageManager pm) {
        LinearLayout t = new LinearLayout(this);
        t.setOrientation(LinearLayout.VERTICAL);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(6), dp(8), dp(6), dp(8));
        t.setFocusable(true);
        t.setClickable(true);
        t.setBackground(focusBg(0x00000000, 0x55F0ABFC, 14));
        ImageView icon = new ImageView(this);
        icon.setImageDrawable(r.loadIcon(pm));
        t.addView(icon, new LinearLayout.LayoutParams(dp(44), dp(44)));
        TextView label = text(r.loadLabel(pm).toString(), 11, Color.WHITE, false);
        label.setSingleLine(true);
        label.setEllipsize(TextUtils.TruncateAt.END);
        label.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(dp(72), WRAP);
        ll.topMargin = dp(4);
        t.addView(label, ll);
        LinearLayout.LayoutParams tl = new LinearLayout.LayoutParams(dp(84), WRAP);
        tl.setMargins(dp(2), 0, dp(2), 0);
        t.setLayoutParams(tl);
        t.setOnFocusChangeListener((v, f) ->
                v.animate().scaleX(f ? 1.1f : 1f).scaleY(f ? 1.1f : 1f).setDuration(120).start());
        t.setOnClickListener(v -> launch(r));
        return t;
    }

    private void launch(ResolveInfo r) {
        try {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setClassName(r.activityInfo.packageName, r.activityInfo.name);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "Ye app nahi khul paya", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadApps();
        updateClock();
        handler.postDelayed(tick, 20000);
        bob = ObjectAnimator.ofFloat(mascot, "translationY", 0f, -dp(8));
        bob.setDuration(1800);
        bob.setRepeatCount(ValueAnimator.INFINITE);
        bob.setRepeatMode(ValueAnimator.REVERSE);
        bob.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(tick);
        if (bob != null) bob.cancel();
    }

    @Override
    public void onBackPressed() {
    }

    private static class Mascot extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        Mascot(Context c) {
            super(c);
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

        @Override
        protected void onDraw(Canvas c) {
            float s = Math.min(getWidth() / 380f, getHeight() / 250f);
            c.save();
            c.translate((getWidth() - 380f * s) / 2f, (getHeight() - 250f * s) / 2f);
            c.scale(s, s);

            fill(0x59F0ABFC);
            c.drawOval(new RectF(120, 201, 260, 227), p);
            stroke(0xFFE879F9, 2);
            c.drawOval(new RectF(128, 203, 252, 225), p);

            Path band = new Path();
            band.moveTo(130, 100);
            band.quadTo(190, 30, 250, 100);
            stroke(0xFFE9D5FF, 5);
            c.drawPath(band, p);

            fill(0xFFF5F0FF);
            c.drawOval(new RectF(156, 152, 224, 212), p);

            Path heart = new Path();
            heart.moveTo(190, 190);
            heart.cubicTo(181, 181, 184, 175, 190, 179);
            heart.cubicTo(196, 175, 199, 181, 190, 190);
            heart.close();
            fill(0xFFF472B6);
            c.drawPath(heart, p);

            fill(0xFFF5F0FF);
            c.drawCircle(158, 172, 10, p);
            c.drawCircle(222, 172, 10, p);

            fill(0xFF60A5FA);
            c.drawOval(new RectF(117, 95, 137, 135), p);
            c.drawOval(new RectF(243, 95, 263, 135), p);

            fill(0xFFF5F0FF);
            c.drawOval(new RectF(128, 58, 252, 162), p);
            fill(0xFF1A1030);
            c.drawOval(new RectF(140, 74, 240, 150), p);

            Path e1 = new Path();
            e1.moveTo(160, 114);
            e1.quadTo(169, 101, 178, 114);
            Path e2 = new Path();
            e2.moveTo(202, 114);
            e2.quadTo(211, 101, 220, 114);
            stroke(0xFFFBCFE8, 4);
            c.drawPath(e1, p);
            c.drawPath(e2, p);

            fill(0x80F472B6);
            c.drawCircle(156, 128, 5, p);
            c.drawCircle(224, 128, 5, p);

            fill(0xFFFBCFE8);
            c.save();
            c.rotate(-25, 177, 52);
            c.drawOval(new RectF(164, 45, 190, 59), p);
            c.restore();
            c.save();
            c.rotate(25, 203, 52);
            c.drawOval(new RectF(190, 45, 216, 59), p);
            c.restore();
            fill(0xFFF472B6);
            c.drawCircle(190, 57, 4, p);

            c.restore();
        }
    }
          }
