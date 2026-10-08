package com.pocket48.hook;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends Activity {

    public static final String PREFS = "config";
    public static final String KEY_PLAY = "play_flow";
    public static final String KEY_NICK = "nick_min";
    public static final String KEY_GLASS = "glass_bottom";
    public static final String KEY_ENTER = "enter_room_no_follow";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(20);
        root.setPadding(pad, dp(24), pad, pad);
        root.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("口袋48 Hook 设置");
        title.setTextSize(22);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, dp(20));
        root.addView(title);

        Switch swPlay = new Switch(this);
        swPlay.setText("去除流量弹框（蜂窝网络直接播放）");
        swPlay.setTextSize(16);
        swPlay.setTextColor(Color.BLACK);
        swPlay.setChecked(sp.getBoolean(KEY_PLAY, true));
        swPlay.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton v, boolean checked) {
                sp.edit().putBoolean(KEY_PLAY, checked).apply();
            }
        });
        root.addView(swPlay);

        Switch swNick = new Switch(this);
        swNick.setText("修改昵称最小字数改为 1");
        swNick.setTextSize(16);
        swNick.setTextColor(Color.BLACK);
        swNick.setChecked(sp.getBoolean(KEY_NICK, true));
        swNick.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton v, boolean checked) {
                sp.edit().putBoolean(KEY_NICK, checked).apply();
            }
        });
        root.addView(swNick);

        Switch swGlass = new Switch(this);
        swGlass.setText("悬浮液态玻璃底栏");
        swGlass.setTextSize(16);
        swGlass.setTextColor(Color.BLACK);
        swGlass.setChecked(sp.getBoolean(KEY_GLASS, true));
        swGlass.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton v, boolean checked) {
                sp.edit().putBoolean(KEY_GLASS, checked).apply();
            }
        });
        root.addView(swGlass);

        Switch swEnter = new Switch(this);
        swEnter.setText("进入房间免关注（未关注时直接进入）");
        swEnter.setTextSize(16);
        swEnter.setTextColor(Color.BLACK);
        swEnter.setChecked(sp.getBoolean(KEY_ENTER, true));
        swEnter.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton v, boolean checked) {
                sp.edit().putBoolean(KEY_ENTER, checked).apply();
            }
        });
        root.addView(swEnter);

        TextView tip = new TextView(this);
        tip.setText("修改开关后，重启口袋48（或重启手机）生效");
        tip.setTextSize(14);
        tip.setTextColor(Color.GRAY);
        tip.setPadding(0, dp(20), 0, 0);
        root.addView(tip);

        setContentView(root);
    }

    private int dp(int v) {
        return (int) (getResources().getDisplayMetrics().density * v + 0.5f);
    }
}
