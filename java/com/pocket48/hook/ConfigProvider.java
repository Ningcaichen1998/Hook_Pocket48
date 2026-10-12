package com.pocket48.hook;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

/**
 * 配置跨进程共享 Provider。
 * XSharedPreferences 在部分 LSPosed 环境读不到模块新写入的开关值，
 * 改用 ContentProvider 让 hook 端（目标 App 进程）通过 ContentResolver 读取最新配置。
 */
public class ConfigProvider extends ContentProvider {

    private static final String PREFS = "config";

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SharedPreferences sp = getContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        MatrixCursor c = new MatrixCursor(new String[]{"key", "value"});
        String[] keys = {
            "play_flow", "nick_min", "glass_bottom", "enter_room_no_follow",
            "contribution_value", "contribution_rmb", "skip_splash_ad", "auto_checkin",
            "msg_time_sec", "msg_text_time", "msg_anti_revoke"
        };
        for (String k : keys) {
            boolean def = !"msg_text_time".equals(k); // msg_text_time 默认 false，其余默认 true
            c.addRow(new Object[]{k, String.valueOf(sp.getBoolean(k, def))});
        }
        return c;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
