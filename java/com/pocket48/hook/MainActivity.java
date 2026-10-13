package com.pocket48.hook;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends Activity {

    public static final String PREFS = "config";
    public static final String KEY_PLAY = "play_flow";
    public static final String KEY_NICK = "nick_min";
    public static final String KEY_GLASS = "glass_bottom";
    public static final String KEY_ENTER = "enter_room_no_follow";
    public static final String KEY_CONTRIB = "contribution_value";
    public static final String KEY_CONTRIB_RMB = "contribution_rmb";
    public static final String KEY_SPLASH = "skip_splash_ad";
    public static final String KEY_CHECKIN = "auto_checkin";
    public static final String KEY_MSG_TIME = "msg_time_sec";
    public static final String KEY_MSG_TEXT_TIME = "msg_text_time";
    public static final String KEY_HIDE_TABS = "hide_channel_tabs";
    public static final String KEY_EXIT_LIVE_DIRECT = "exit_live_direct";
    public static final String KEY_DEFAULT_TAB_JUJU = "default_tab_juju";
    public static final String KEY_JUJU_DIRECT_ROOM = "juju_direct_room";
    public static final String KEY_MSG_REVOKE = "msg_anti_revoke";

    private static final int REQ_PICK_BG = 1001;

    // 主题色
    private static final int C_PRIMARY = 0xFF6C5CE7;
    private static final int C_PRIMARY_LIGHT = 0xFF8B7CF7;
    private static final int C_BG = 0xFFF4F2FB;
    private static final int C_ICON_BG = 0xFFEDE9FE;
    private static final int C_TITLE = 0xFF1F2430;
    private static final int C_SUB = 0xFF8A8FA3;

    private ImageView bgView;
    private View scrim;
    private TextView bgStatusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        // 状态栏着色
        getWindow().setStatusBarColor(C_PRIMARY);
        if (Build.VERSION.SDK_INT >= 23) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(decor.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // 根容器：背景图 + 遮罩 + 主内容
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(C_BG);

        bgView = new ImageView(this);
        bgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        bgView.setVisibility(View.INVISIBLE);
        root.addView(bgView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        scrim = new View(this);
        scrim.setBackgroundColor(0xE6FFFFFF);
        scrim.setVisibility(View.INVISIBLE);
        root.addView(scrim, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        root.addView(main, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        main.addView(buildHeader());

        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout.LayoutParams scrollLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        main.addView(scroll, scrollLp);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        content.setPadding(pad, dp(14), pad, dp(24));
        scroll.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        content.addView(buildBgCard(prefs));
        content.addView(buildFeatureCard("📶", "去流量弹框", "蜂窝网络看直播免确认，直接播放",
                prefs.getBoolean(KEY_PLAY, true), new CompoundButton.OnCheckedChangeListener() {
                    @Override public void onCheckedChanged(CompoundButton v, boolean c) {
                        prefs.edit().putBoolean(KEY_PLAY, c).apply();
                    }
                }));
        content.addView(buildFeatureCard("✏️", "单字昵称", "突破「昵称至少2字」限制，支持1字昵称",
                prefs.getBoolean(KEY_NICK, true), new CompoundButton.OnCheckedChangeListener() {
                    @Override public void onCheckedChanged(CompoundButton v, boolean c) {
                        prefs.edit().putBoolean(KEY_NICK, c).apply();
                    }
                }));
        content.addView(buildFeatureCard("💎", "悬浮液态玻璃底栏", "悬浮圆角 · 真毛玻璃质感",
                prefs.getBoolean(KEY_GLASS, true), new CompoundButton.OnCheckedChangeListener() {
                    @Override public void onCheckedChanged(CompoundButton v, boolean c) {
                        prefs.edit().putBoolean(KEY_GLASS, c).apply();
                    }
                }));
        content.addView(buildFeatureCard("🚪", "进入房间免关注", "未关注也能直接进入成员房间",
                prefs.getBoolean(KEY_ENTER, true), new CompoundButton.OnCheckedChangeListener() {
                    @Override public void onCheckedChanged(CompoundButton v, boolean c) {
                        prefs.edit().putBoolean(KEY_ENTER, c).apply();
                    }
                }));
        content.addView(buildFeatureCard("🏆", "贡献周榜展示贡献值", "显示被隐藏的贡献值列",
                prefs.getBoolean(KEY_CONTRIB, true), new CompoundButton.OnCheckedChangeListener() {
                    @Override public void onCheckedChanged(CompoundButton v, boolean c) {
                        prefs.edit().putBoolean(KEY_CONTRIB, c).apply();
                    }
                }));
        content.addView(buildFeatureCard("💰", "贡献值转人民币", "10 贡献值 = 1 元，自动换算展示",
                prefs.getBoolean(KEY_CONTRIB_RMB, true), new CompoundButton.OnCheckedChangeListener() {
                    @Override public void onCheckedChanged(CompoundButton v, boolean c) {
                        prefs.edit().putBoolean(KEY_CONTRIB_RMB, c).apply();
                    }
                }));
        content.addView(buildFeatureCard("🚀", "去开屏广告/启动页", "跳过启动广告，直接进入主页",
                prefs.getBoolean(KEY_SPLASH, true), new CompoundButton.OnCheckedChangeListener() {
                    @Override public void onCheckedChanged(CompoundButton v, boolean c) {
                        prefs.edit().putBoolean(KEY_SPLASH, c).apply();
                    }
                }));
        content.addView(buildFeatureCard("📅", "自动每日签到", "每日首次启动自动完成签到",
                prefs.getBoolean(KEY_CHECKIN, true), new CompoundButton.OnCheckedChangeListener() {
                    @Override public void onCheckedChanged(CompoundButton v, boolean c) {
                        prefs.edit().putBoolean(KEY_CHECKIN, c).apply();
                    }
                }));
        content.addView(buildFeatureCard("🕐", "消息时间精确到秒", "消息发送时间显示精确到秒",
                prefs.getBoolean(KEY_MSG_TIME, true), new CompoundButton.OnCheckedChangeListener() {
                    @Override public void onCheckedChanged(CompoundButton v, boolean c) {
                        prefs.edit().putBoolean(KEY_MSG_TIME, c).apply();
                    }
                }));
        content.addView(buildFeatureCard("⏱️", "消息文本内写入时间", "把发送时间写入文本消息末尾（气泡内）",
                prefs.getBoolean(KEY_MSG_TEXT_TIME, false), new CompoundButton.OnCheckedChangeListener() {
                    @Override public void onCheckedChanged(CompoundButton v, boolean c) {
                        prefs.edit().putBoolean(KEY_MSG_TEXT_TIME, c).apply();
                    }
                }));
        content.addView(buildFeatureCard("🚫", "隐藏公演/微博/48动态等", "去除聚聚顶栏的公演、微博、48动态、社区公约、青少年使用规范标签",
                prefs.getBoolean(KEY_HIDE_TABS, true), new CompoundButton.OnCheckedChangeListener() {
                    @Override public void onCheckedChanged(CompoundButton v, boolean c) {
                        prefs.edit().putBoolean(KEY_HIDE_TABS, c).apply();
                    }
                }));
        content.addView(buildFeatureCard("📺", "退出直播直接退出", "去掉「继续以小窗模式陪伴成员」提示，直接退出",
                prefs.getBoolean(KEY_EXIT_LIVE_DIRECT, true), new CompoundButton.OnCheckedChangeListener() {
                    @Override public void onCheckedChanged(CompoundButton v, boolean c) {
                        prefs.edit().putBoolean(KEY_EXIT_LIVE_DIRECT, c).apply();
                    }
                }));
        content.addView(buildFeatureCard("🚪", "点头像直达聊天", "左栏点击小偶像头像时，右侧直接进入聊天房间（跳过频道首页）",
                prefs.getBoolean(KEY_JUJU_DIRECT_ROOM, true), new CompoundButton.OnCheckedChangeListener() {
                    @Override public void onCheckedChanged(CompoundButton v, boolean c) {
                        prefs.edit().putBoolean(KEY_JUJU_DIRECT_ROOM, c).apply();
                    }
                }));
        content.addView(buildFeatureCard("🏠", "默认选中聚聚底栏", "打开口袋48后底栏默认定位到「聚聚」",
                prefs.getBoolean(KEY_DEFAULT_TAB_JUJU, true), new CompoundButton.OnCheckedChangeListener() {
                    @Override public void onCheckedChanged(CompoundButton v, boolean c) {
                        prefs.edit().putBoolean(KEY_DEFAULT_TAB_JUJU, c).apply();
                    }
                }));
        content.addView(buildFeatureCard("🛡️", "消息防撤回", "对方撤回的消息保留不消失",
                prefs.getBoolean(KEY_MSG_REVOKE, true), new CompoundButton.OnCheckedChangeListener() {
                    @Override public void onCheckedChanged(CompoundButton v, boolean c) {
                        prefs.edit().putBoolean(KEY_MSG_REVOKE, c).apply();
                    }
                }));

        TextView tip = new TextView(this);
        tip.setText("修改开关后，重启口袋48（或重启手机）生效");
        tip.setTextSize(12);
        tip.setTextColor(C_SUB);
        tip.setGravity(Gravity.CENTER);
        tip.setPadding(0, dp(18), 0, 0);
        content.addView(tip);

        setContentView(root);
        applyBg();
    }

    // ===== 顶部标题区 =====
    private View buildHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(24), dp(26), dp(24), dp(22));
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{C_PRIMARY, C_PRIMARY_LIGHT});
        header.setBackground(gd);

        TextView title = new TextView(this);
        title.setText("口袋48 Hook");
        title.setTextSize(24);
        title.setTextColor(Color.WHITE);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        header.addView(title);

        TextView sub = new TextView(this);
        sub.setText("设置中心 · 全部功能开关");
        sub.setTextSize(13);
        sub.setTextColor(0xB3FFFFFF);
        sub.setPadding(0, dp(4), 0, 0);
        header.addView(sub);

        TextView ver = new TextView(this);
        ver.setText("适配版本：口袋48 v7.1.44 (26092001)");
        ver.setTextSize(11);
        ver.setTextColor(0x8CFFFFFF);
        ver.setPadding(0, dp(2), 0, 0);
        header.addView(ver);
        return header;
    }

    // ===== 背景图操作卡片 =====
    private View buildBgCard(final SharedPreferences prefs) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(roundBg(0xFFFFFFFF, dp(18)));
        card.setElevation(dp(3));

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setGravity(Gravity.CENTER_VERTICAL);

        TextView icon = new TextView(this);
        icon.setText("🖼️");
        icon.setTextSize(20);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(roundBg(C_ICON_BG, dp(22)));
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(44), dp(44));
        row1.addView(icon, iconLp);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        colLp.leftMargin = dp(14);
        row1.addView(col, colLp);

        TextView title = new TextView(this);
        title.setText("模块背景");
        title.setTextSize(16);
        title.setTextColor(C_TITLE);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        col.addView(title);

        bgStatusText = new TextView(this);
        bgStatusText.setTextSize(12);
        bgStatusText.setTextColor(C_SUB);
        bgStatusText.setPadding(0, dp(2), 0, 0);
        col.addView(bgStatusText);

        card.addView(row1);

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams row2Lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        row2Lp.topMargin = dp(14);
        card.addView(row2, row2Lp);

        TextView btnChange = pillButton("更换图片", C_PRIMARY, Color.WHITE);
        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                pickBg();
            }
        });
        row2.addView(btnChange);

        TextView btnReset = pillButton("恢复默认", 0xFFF1F0F5, C_SUB);
        LinearLayout.LayoutParams resetLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        resetLp.leftMargin = dp(12);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                clearBg();
            }
        });
        row2.addView(btnReset, resetLp);

        return card;
    }

    // ===== 功能卡片 =====
    private View buildFeatureCard(String emoji, String title, String subtitle,
                                  boolean checked, final CompoundButton.OnCheckedChangeListener listener) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(16), dp(14), dp(14), dp(14));
        card.setBackground(roundBg(0xFFFFFFFF, dp(18)));
        card.setElevation(dp(3));
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardLp.topMargin = dp(12);

        TextView icon = new TextView(this);
        icon.setText(emoji);
        icon.setTextSize(20);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(roundBg(C_ICON_BG, dp(22)));
        card.addView(icon, new LinearLayout.LayoutParams(dp(44), dp(44)));

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        colLp.leftMargin = dp(14);
        card.addView(col, colLp);

        TextView t = new TextView(this);
        t.setText(title);
        t.setTextSize(16);
        t.setTextColor(C_TITLE);
        t.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        col.addView(t);

        TextView s = new TextView(this);
        s.setText(subtitle);
        s.setTextSize(12);
        s.setTextColor(C_SUB);
        s.setPadding(0, dp(2), 0, 0);
        col.addView(s);

        final Switch sw = new Switch(this);
        sw.setChecked(checked);
        sw.setClickable(false);
        sw.setThumbTintList(new ColorStateList(
                new int[][]{{android.R.attr.state_checked}, {}},
                new int[]{Color.WHITE, 0xFFF4F4F4}));
        sw.setTrackTintList(new ColorStateList(
                new int[][]{{android.R.attr.state_checked}, {}},
                new int[]{C_PRIMARY, 0xFFD9D9DE}));
        sw.setOnCheckedChangeListener(listener);
        card.addView(sw);

        card.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                sw.toggle();
            }
        });

        card.setLayoutParams(cardLp);
        return card;
    }

    // ===== 工具方法 =====
    private TextView pillButton(String text, int bgColor, int textColor) {
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextSize(14);
        btn.setTextColor(textColor);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(dp(18), dp(9), dp(18), dp(9));
        btn.setBackground(roundBg(bgColor, dp(20)));
        return btn;
    }

    private GradientDrawable roundBg(int color, int radius) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadius(radius);
        return gd;
    }

    // ===== 背景图逻辑 =====
    private File bgFile() {
        return new File(getFilesDir(), "module_bg.png");
    }

    private void pickBg() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, REQ_PICK_BG);
        } catch (Throwable t) {
            // 无文件选择器时兜底
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, REQ_PICK_BG);
        }
    }

    private void clearBg() {
        File f = bgFile();
        if (f.exists()) {
            f.delete();
        }
        applyBg();
    }

    private void applyBg() {
        Bitmap bmp = decodeBg(bgFile());
        if (bmp != null) {
            bgView.setImageBitmap(bmp);
            bgView.setVisibility(View.VISIBLE);
            scrim.setVisibility(View.VISIBLE);
            if (bgStatusText != null) {
                bgStatusText.setText("已自定义背景");
            }
        } else {
            bgView.setImageBitmap(null);
            bgView.setVisibility(View.INVISIBLE);
            scrim.setVisibility(View.INVISIBLE);
            if (bgStatusText != null) {
                bgStatusText.setText("默认（浅色渐变）");
            }
        }
    }

    private Bitmap decodeBg(File f) {
        if (f == null || !f.exists()) {
            return null;
        }
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(f.getAbsolutePath(), opts);
            int w = opts.outWidth, h = opts.outHeight;
            opts.inJustDecodeBounds = false;
            int sample = 1;
            while (w / sample > 2048 || h / sample > 2048) {
                sample *= 2;
            }
            opts.inSampleSize = sample;
            return BitmapFactory.decodeFile(f.getAbsolutePath(), opts);
        } catch (Throwable t) {
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK_BG && resultCode == RESULT_OK && data != null && data.getData() != null) {
            saveBg(data.getData());
        }
    }

    private void saveBg(Uri uri) {
        InputStream in = null;
        FileOutputStream out = null;
        try {
            in = getContentResolver().openInputStream(uri);
            if (in == null) {
                return;
            }
            File f = bgFile();
            out = new FileOutputStream(f);
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.flush();
            applyBg();
        } catch (Throwable t) {
            // ignore
        } finally {
            try { if (in != null) in.close(); } catch (Throwable ignore) {}
            try { if (out != null) out.close(); } catch (Throwable ignore) {}
        }
    }

    private int dp(int v) {
        return (int) (getResources().getDisplayMetrics().density * v + 0.5f);
    }
}
