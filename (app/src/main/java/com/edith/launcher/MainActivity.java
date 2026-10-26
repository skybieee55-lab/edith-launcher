package com.edith.launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Paint;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String NAME = "Swarit";
    private static final double LAT = 28.61;
    private static final double LON = 77.21;
    private static final String[] FAV = {
            "com.google.android.youtube.tv", "com.netflix.ninja",
            "com.amazon.amazonvideo.livingroom", "in.startv.hotstar",
            "com.spotify.tv.android", "com.jio.media.ondemand"};
    private static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;
    private static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;

    private TextView clock, date, weather, bubbleText;
    private LinearLayout dock, tasksBox;
    private Mascot mascot;
    private SharedPreferences prefs;
    private long lastWeather = 0;
    private final Handler handler = new Handler();
    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            updateClock();
            handler.postDelayed(this, 20000);
        }
    };
    private final Runnable calm = new Runnable() {
        @Override
        public void run() {
            mascot.setState(Mascot.IDLE);
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

    private Glass glass(float r) {
        Glass g = new Glass(this, dp(r));
        g.setOrientation(LinearLayout.VERTICAL);
        return g;
    }

    private void say(String msg, int ms) {
        bubbleText.setText(msg);
        mascot.setState(Mascot.TALKING);
        handler.removeCallbacks(calm);
        handler.postDelayed(calm, ms);
    }

    private String greet() {
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String g = h < 12 ? "Good morning" : (h < 17 ? "Good afternoon" : "Good evening");
        return g + ", " + NAME + "! I'm EDITH, your AI companion.";
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
        prefs = getSharedPreferences("edith", MODE_PRIVATE);
        FrameLayout root = new FrameLayout(this);
        root.addView(new NightSky(this), new FrameLayout.LayoutParams(MATCH, MATCH));

        final Runnable soon = () -> say("Ye feature jaldi aayega!", 2500);

        Glass menu = glass(20);
        menu.setPadding(dp(16), dp(20), dp(16), dp(16));
        menu.addView(text("EDITH", 32, 0xFFF9A8D4, true));
        TextView sub = text("your AI companion", 12, 0xFFC4B5FD, false);
        sub.setPadding(0, 0, 0, dp(14));
        menu.addView(sub);
        addMenuItem(menu, "Home", () -> say(greet(), 3500));
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
        mascot.setFocusable(true);
        mascot.setClickable(true);
        mascot.setOnClickListener(v ->
                say("Hey " + NAME + "! Voice chat jaldi aa raha hai.", 3500));
        center.addView(mascot, new LinearLayout.LayoutParams(dp(228), dp(150)));

        Glass bub = glass(18);
        bub.setPadding(dp(16), dp(10), dp(16), dp(10));
        bubbleText = text("", 14, Color.WHITE, false);
        bubbleText.setGravity(Gravity.CENTER);
        bubbleText.setMinLines(2);
        bub.addView(bubbleText, new LinearLayout.LayoutParams(MATCH, WRAP));
        LinearLayout.LayoutParams bl = new LinearLayout.LayoutParams(dp(380), WRAP);
        bl.topMargin = dp(4);
        center.addView(bub, bl);

        Glass bar = glass(24);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(18), 0, dp(18), 0);
        bar.setFocusable(true);
        bar.setClickable(true);
        bar.setOnClickListener(v -> say("Voice chat next update me aayega!", 3000));
        bar.addView(text("Say something, or press OK to talk...", 14, 0xFFC4B5FD, false));
        LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(dp(380), dp(44));
        barLp.topMargin = dp(10);
        center.addView(bar, barLp);

        Glass dockGlass = glass(22);
        dockGlass.setOrientation(LinearLayout.HORIZONTAL);
        dockGlass.setPadding(dp(6), dp(6), dp(6), dp(6));
        HorizontalScrollView sv = new HorizontalScrollView(this);
        sv.setHorizontalScrollBarEnabled(false);
        dock = new LinearLayout(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setPadding(dp(4), dp(6), dp(4), dp(6));
        sv.addView(dock);
        dockGlass.addView(sv, new LinearLayout.LayoutParams(MATCH, WRAP));
        LinearLayout.LayoutParams dl = new LinearLayout.LayoutParams(dp(440), WRAP);
        dl.topMargin = dp(10);
        center.addView(dockGlass, dl);
        root.addView(center, new FrameLayout.LayoutParams(WRAP, WRAP, Gravity.CENTER));

        LinearLayout cbox = new LinearLayout(this);
        cbox.setOrientation(LinearLayout.VERTICAL);
        cbox.setGravity(Gravity.END);
        clock = text("", 26, Color.WHITE, true);
        date = text("", 14, 0xFFC4B5FD, false);
        weather = text("", 14, 0xFFF9A8D4, false);
        cbox.addView(clock);
        cbox.addView(date);
        cbox.addView(weather);
        FrameLayout.LayoutParams cl = new FrameLayout.LayoutParams(
                WRAP, WRAP, Gravity.TOP | Gravity.END);
        cl.topMargin = dp(20);
        cl.rightMargin = dp(28);
        root.addView(cbox, cl);

        Glass today = glass(18);
        today.setPadding(dp(10), dp(12), dp(10), dp(8));
        TextView tt = text("Today", 16, Color.WHITE, true);
        tt.setPadding(dp(8), 0, 0, dp(4));
        today.addView(tt);
        tasksBox = new LinearLayout(this);
        tasksBox.setOrientation(LinearLayout.VERTICAL);
        today.addView(tasksBox);
        FrameLayout.LayoutParams tl = new FrameLayout.LayoutParams(
                dp(190), WRAP, Gravity.END | Gravity.CENTER_VERTICAL);
        tl.rightMargin = dp(24);
        root.addView(today, tl);

        setContentView(root);
        renderTasks(-1);
    }

    private List<String> loadTasks() {
        String raw = prefs.getString("tasks",
                "0|Be productive\n0|Stay happy\n0|Drink water\n0|Good night");
        List<String> l = new ArrayList<>();
        for (String s : raw.split("\n")) {
            if (s.length() > 2) l.add(s);
        }
        return l;
    }

    private void saveTasks(List<String> l) {
        prefs.edit().putString("tasks", TextUtils.join("\n", l)).apply();
    }

    private void renderTasks(int focusIdx) {
        tasksBox.removeAllViews();
        final List<String> lines = loadTasks();
        for (int i = 0; i < lines.size(); i++) {
            final int idx = i;
            String l = lines.get(i);
            final boolean done = l.startsWith("1|");
            final String txt = l.substring(2);
            TextView t = text((done ? "\u2713  " : "\u2022  ") + txt, 14,
                    done ? 0x99E9D5FF : 0xFFE9D5FF, false);
            if (done) t.setPaintFlags(t.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            t.setPadding(dp(8), dp(7), dp(8), dp(7));
            t.setFocusable(true);
            t.setClickable(true);
            t.setBackground(focusBg(0x00000000, 0x55F0ABFC, 10));
            t.setOnClickListener(v -> {
                lines.set(idx, (done ? "0|" : "1|") + txt);
                saveTasks(lines);
                renderTasks(idx);
            });
            t.setOnLongClickListener(v -> {
                lines.remove(idx);
                saveTasks(lines);
                renderTasks(idx);
                return true;
            });
            tasksBox.addView(t, new LinearLayout.LayoutParams(MATCH, WRAP));
        }
        TextView add = text("+  Add task", 14, 0xFFF9A8D4, false);
        add.setPadding(dp(8), dp(7), dp(8), dp(7));
        add.setFocusable(true);
        add.setClickable(true);
        add.setBackground(focusBg(0x00000000, 0x55F0ABFC, 10));
        add.setOnClickListener(v -> addTask());
        tasksBox.addView(add, new LinearLayout.LayoutParams(MATCH, WRAP));
        if (focusIdx >= 0 && tasksBox.getChildCount() > 0) {
            tasksBox.getChildAt(Math.min(focusIdx, tasksBox.getChildCount() - 1)).requestFocus();
        }
    }

    private void addTask() {
        final List<String> lines = loadTasks();
        if (lines.size() >= 6) {
            Toast.makeText(this, "Max 6 tasks", Toast.LENGTH_SHORT).show();
            return;
        }
        final EditText et = new EditText(this);
        et.setSingleLine(true);
        et.setHint("Task likho");
        new AlertDialog.Builder(this)
                .setTitle("New task")
                .setView(et)
                .setPositiveButton("Add", (d, w) -> {
                    String s = et.getText().toString().replace("\n", " ").trim();
                    if (!s.isEmpty()) {
                        lines.add("0|" + s);
                        saveTasks(lines);
                        renderTasks(lines.size() - 1);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateClock() {
        Date now = new Date();
        clock.setText(new SimpleDateFormat("h:mm a", Locale.getDefault()).format(now));
        date.setText(new SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(now));
    }

    private String describe(int c) {
        if (c == 0) return "Clear sky";
        if (c <= 3) return "Partly cloudy";
        if (c == 45 || c == 48) return "Foggy";
        if (c >= 51 && c <= 57) return "Drizzle";
        if (c >= 61 && c <= 67) return "Rain";
        if (c >= 71 && c <= 77) return "Snow";
        if (c >= 80 && c <= 82) return "Showers";
        if (c >= 95) return "Thunderstorm";
        return "Cloudy";
    }

    private void fetchWeather() {
        lastWeather = System.currentTimeMillis();
        new Thread(() -> {
            try {
                URL u = new URL("https://api.open-meteo.com/v1/forecast?latitude=" + LAT
                        + "&longitude=" + LON + "&current=temperature_2m,weather_code");
                HttpURLConnection c = (HttpURLConnection) u.openConnection();
                c.setConnectTimeout(8000);
                c.setReadTimeout(8000);
                BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) sb.append(line);
                r.close();
                JSONObject cur = new JSONObject(sb.toString()).getJSONObject("current");
                final String t = Math.round(cur.getDouble("temperature_2m")) + "\u00B0C  "
                        + describe(cur.getInt("weather_code"));
                runOnUiThread(() -> weather.setText(t));
            } catch (Exception e) {
                lastWeather = 0;
            }
        }).start();
    }

    private int favRank(String pkg) {
        for (int i = 0; i < FAV.length; i++) {
            if (FAV[i].equals(pkg)) return i;
        }
        return 99;
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
        Collections.sort(list, (a, c) -> {
            int ra = favRank(a.activityInfo.packageName);
            int rc = favRank(c.activityInfo.packageName);
            if (ra != rc) return ra - rc;
            return a.loadLabel(pm).toString().compareToIgnoreCase(c.loadLabel(pm).toString());
        });
        for (ResolveInfo r : list) dock.addView(tile(r, pm));
    }

    private View tile(final ResolveInfo r, PackageManager pm) {
        LinearLayout t = new LinearLayout(this);
        t.setOrientation(LinearLayout.VERTICAL);
        t.setGravity(Gravity.CENTER_HORIZONTAL);
        t.setPadding(dp(4), dp(6), dp(4), dp(6));
        t.setFocusable(true);
        t.setClickable(true);
        t.setBackground(focusBg(0x00000000, 0x55F0ABFC, 14));
        ImageView icon = new ImageView(this);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icon.setPadding(dp(8), dp(8), dp(8), dp(8));
        icon.setBackground(box(0x30FFFFFF, 14, 0));
        icon.setImageDrawable(r.loadIcon(pm));
        t.addView(icon, new LinearLayout.LayoutParams(dp(56), dp(56)));
        TextView label = text(r.loadLabel(pm).toString(), 11, Color.WHITE, false);
        label.setSingleLine(true);
        label.setEllipsize(TextUtils.TruncateAt.END);
        label.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(dp(76), WRAP);
        ll.topMargin = dp(4);
        t.addView(label, ll);
        LinearLayout.LayoutParams tl = new LinearLayout.LayoutParams(dp(88), WRAP);
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
        if (System.currentTimeMillis() - lastWeather > 600000) fetchWeather();
        say(greet(), 3500);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(tick);
        handler.removeCallbacks(calm);
    }

    @Override
    public void onBackPressed() {
    }
    }
