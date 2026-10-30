package com.smartarrange.app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Read-only, cross-brand launcher inventory and organization proposal. */
public final class MainActivity extends Activity {
    private LinearLayout root;
    private TextView status;
    private LinearLayout results;
    private String planText = "";
    private final String[] groups = {"Communication", "Productivity", "Finance", "Travel & Transport", "Media & Entertainment", "Tools & System", "Other"};

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(20), dp(28), dp(20), dp(28));
        root.setBackgroundColor(Color.rgb(247,248,251)); scroll.addView(root); setContentView(scroll);
        label("SmartArrange", 30, true, Color.rgb(28,42,67));
        label("Your launcher stays in control. SmartArrange only creates a suggested layout.", 15, false, Color.DKGRAY);
        status = label("Checking launcher…", 14, false, Color.rgb(60,85,120));
        button("Scan apps and create organization plan", v -> scan());
        button("Share organization plan", v -> sharePlan());
        button("Open home-app settings", v -> openHomeSettings());
        label("Suggested groups", 20, true, Color.rgb(28,42,67));
        results = new LinearLayout(this); results.setOrientation(LinearLayout.VERTICAL); root.addView(results);
        label("Privacy: app names and categories are processed locally. No internet permission, accessibility service, root, or launcher replacement.", 12, false, Color.GRAY);
        scan();
    }

    private void scan() {
        PackageManager pm = getPackageManager();
        Intent home = new Intent(Intent.ACTION_MAIN); home.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo current = pm.resolveActivity(home, PackageManager.MATCH_DEFAULT_ONLY);
        String homeName = current == null ? "Unknown" : current.loadLabel(pm).toString();
        String homePkg = current == null || current.activityInfo == null ? "unknown" : current.activityInfo.packageName;
        if ("android".equals(homePkg)) homeName += " (chooser / not resolved)";
        Intent launch = new Intent(Intent.ACTION_MAIN); launch.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> available = pm.queryIntentActivities(launch, 0);
        Map<String, List<String>> organized = new LinkedHashMap<>();
        for (String group : groups) organized.put(group, new ArrayList<>());
        Set<String> seen = new LinkedHashSet<>();
        for (ResolveInfo app : available) {
            if (app.activityInfo == null) continue;
            String pkg = app.activityInfo.packageName;
            if (pkg.equals(getPackageName())) continue;
            String label = app.loadLabel(pm).toString();
            String key = pkg + "/" + app.activityInfo.name;
            if (!seen.add(key)) continue;
            organized.get(category(label, pkg)).add(label);
        }
        results.removeAllViews();
        StringBuilder sb = new StringBuilder("SmartArrange proposed layout\nLauncher: ").append(homeName).append(" (").append(homePkg).append(")\n\n");
        int count = 0;
        for (Map.Entry<String,List<String>> group : organized.entrySet()) {
            List<String> apps = group.getValue(); if (apps.isEmpty()) continue;
            Collections.sort(apps, String.CASE_INSENSITIVE_ORDER); count += apps.size();
            TextView heading = text(group.getKey() + " (" + apps.size() + ")", 17, true, Color.rgb(24,78,126));
            heading.setPadding(0, dp(15), 0, dp(5)); results.addView(heading);
            TextView items = text(String.join("  •  ", apps), 14, false, Color.DKGRAY);
            items.setPadding(dp(12), dp(10), dp(12), dp(10)); items.setBackgroundColor(Color.WHITE); results.addView(items);
            sb.append(group.getKey()).append("\n"); for (String app : apps) sb.append("- ").append(app).append("\n"); sb.append("\n");
        }
        status.setText("Current home app: " + homeName + "\nPackage: " + homePkg + "\nLaunchable entries: " + count + "\nAndroid: " + android.os.Build.VERSION.RELEASE);
        planText = sb.toString();
    }

    private String category(String name, String pkg) {
        String x = (name + " " + pkg).toLowerCase(Locale.ROOT);
        if (has(x,"whatsapp","telegram","signal","messenger","phone","dialer","contacts","message","sms","discord","wechat")) return groups[0];
        if (has(x,"sheets","docs","drive","calendar","office","word","excel","notion","notes","keep","tasks","gmail","outlook")) return groups[1];
        if (has(x,"bank","wallet","finance","kwsp","akaun","paypal","wise","touch 'n go","tng","maybank","cimb")) return groups[2];
        if (has(x,"grab","maps","waze","uber","travel","booking","airasia","transport")) return groups[3];
        if (has(x,"youtube","netflix","spotify","music","video","games","tiktok","instagram","gallery","photos")) return groups[4];
        if (has(x,"settings","camera","clock","calculator","files","browser","brave","chrome","market","store","backup","tools")) return groups[5];
        return groups[6];
    }
    private boolean has(String text, String... words) { for(String word:words) if(text.contains(word)) return true; return false; }
    private void sharePlan() {
        if (planText.isEmpty()) { status.setText("Scan apps first."); return; }
        Intent send = new Intent(Intent.ACTION_SEND); send.setType("text/plain"); send.putExtra(Intent.EXTRA_TEXT, planText);
        startActivity(Intent.createChooser(send, "Share suggested layout"));
    }
    private void openHomeSettings() {
        try { startActivity(new Intent(Settings.ACTION_HOME_SETTINGS)); }
        catch (Exception ignored) { startActivity(new Intent(Settings.ACTION_SETTINGS)); }
    }
    private void button(String title, View.OnClickListener listener) {
        Button b = new Button(this); b.setText(title); b.setAllCaps(false); b.setOnClickListener(listener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2); lp.topMargin=dp(8); root.addView(b, lp);
    }
    private TextView label(String text, int size, boolean bold, int color) {
        TextView v=text(text,size,bold,color); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.bottomMargin=dp(10); root.addView(v,lp); return v;
    }
    private TextView text(String value,int size,boolean bold,int color) {
        TextView v=new TextView(this); v.setText(value); v.setTextSize(size); v.setTextColor(color);
        if(bold) v.setTypeface(Typeface.DEFAULT,Typeface.BOLD); return v;
    }
    private int dp(int x) {return Math.round(getResources().getDisplayMetrics().density*x);}
}
