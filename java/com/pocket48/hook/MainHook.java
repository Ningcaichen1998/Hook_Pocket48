package com.pocket48.hook;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    private static final String PKG = "com.pocket.snh48.activity";

    // ===== 去流量弹框 =====
    private static final String P2 = "com.pocket.snh48.activity.p2";
    private static final String CALLBACK = "com.pocket.snh48.activity.p2$\u7ac8\u7cf4\u8836\u9f47\u7c72\u8836\u8836\u7229"; // p2$竈糴蠶齇籲蠶蠶爩
    private static final String HOOK_METHOD = "\u7ac8\u9f15\u7229\u8c9c"; // 竈鼕爩貜
    private static final String PLAY_METHOD = "\u9dd9\u7c72\u77e1";       // 鷙籲矡

    // ===== 修改昵称最小字数改为 1 =====
    private static final String NICK_ACT = "com.pocket.snh48.mine.ui.edit.nickname.EditNickNameActivity";
    private static final String NICK_ONCLICK_CLS = NICK_ACT + "$\u9dd9\u7c72\u77e1"; // $鷙籲矡
    private static final String M_ONCLICK = "\u9dd9\u7c72\u77e1"; // 鷙籲矡
    private static final String M_SUBMIT = "\u7ac8\u7229";       // 竈爩
    private static final String F_OUTER = "\u9dd9\u7c72\u77e1";  // 鷙籲矡 (outer 字段)

    // ===== 悬浮液态玻璃底栏 =====
    private static final String MAIN_ACT = "com.pocket.snh48.activity.view.main.MainActivity";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lp) {
        if (!PKG.equals(lp.packageName)) {
            return;
        }
        XSharedPreferences sp = new XSharedPreferences("com.pocket48.hook", "config");
        sp.reload();
        if (sp.getBoolean("play_flow", true)) {
            hookPlayFlow(lp);
        }
        if (sp.getBoolean("nick_min", true)) {
            hookNickName(lp);
        }
        if (sp.getBoolean("glass_bottom", true)) {
            hookBottomBar(lp);
        }
    }

    /** 去流量弹框：直接触发播放回调，跳过弹框 */
    private void hookPlayFlow(XC_LoadPackage.LoadPackageParam lp) {
        try {
            final Class<?> p2 = XposedHelpers.findClass(P2, lp.classLoader);
            final Class<?> cb = XposedHelpers.findClass(CALLBACK, lp.classLoader);
            for (final Method m : p2.getDeclaredMethods()) {
                Class<?>[] pt = m.getParameterTypes();
                if (m.getName().equals(HOOK_METHOD) && pt.length == 2
                        && pt[0] == Activity.class && pt[1] == cb) {
                    XposedBridge.hookMethod(m, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            Object callback = param.args[1];
                            if (callback != null) {
                                try {
                                    Method play = callback.getClass().getMethod(PLAY_METHOD);
                                    play.invoke(callback);
                                } catch (Throwable t) {
                                    XposedBridge.log("[pocket48] play invoke failed: " + t);
                                }
                            }
                            param.setResult(null);
                        }
                    });
                    XposedBridge.log("[pocket48] hooked play flow: " + HOOK_METHOD);
                }
            }
        } catch (Throwable t) {
            XposedBridge.log("[pocket48] hook play flow error: " + t);
        }
    }

    /** 修改昵称最小字数改为 1：长度 1 时临时补尾随空格通过校验，提交前再 trim 掉 */
    private void hookNickName(XC_LoadPackage.LoadPackageParam lp) {
        try {
            final Class<?> onClickCls = XposedHelpers.findClass(NICK_ONCLICK_CLS, lp.classLoader);
            XposedBridge.hookAllMethods(onClickCls, M_ONCLICK, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    try {
                        Object outer = XposedHelpers.getObjectField(param.thisObject, F_OUTER);
                        EditText et = getNickEditText(outer);
                        if (et != null) {
                            String nick = et.getText().toString();
                            if (nick.length() == 1) {
                                et.setText(nick + " ");
                            }
                        }
                    } catch (Throwable t) {
                        XposedBridge.log("[pocket48] nick onclick hook error: " + t);
                    }
                }
            });

            final Class<?> actCls = XposedHelpers.findClass(NICK_ACT, lp.classLoader);
            XposedBridge.hookAllMethods(actCls, M_SUBMIT, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    try {
                        EditText et = getNickEditText(param.thisObject);
                        if (et != null) {
                            String nick = et.getText().toString();
                            if (nick.length() == 2 && nick.endsWith(" ")) {
                                et.setText(nick.substring(0, 1));
                            }
                        }
                    } catch (Throwable t) {
                        XposedBridge.log("[pocket48] nick submit hook error: " + t);
                    }
                }
            });

            XposedBridge.log("[pocket48] hooked nickname min-length");
        } catch (Throwable t) {
            XposedBridge.log("[pocket48] hook nickname error: " + t);
        }
    }

    /** 悬浮液态玻璃底栏：底栏悬浮 + 圆角 + 半透明玻璃质感 */
    private void hookBottomBar(XC_LoadPackage.LoadPackageParam lp) {
        try {
            final Class<?> mainAct = XposedHelpers.findClass(MAIN_ACT, lp.classLoader);
            XposedHelpers.findAndHookMethod(mainAct, "onCreate", android.os.Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        final Object binding = XposedHelpers.getObjectField(param.thisObject, "binding");
                        final View bottomNav = (View) XposedHelpers.getObjectField(binding, "bottomNavigationView");
                        bottomNav.post(new Runnable() {
                            @Override
                            public void run() {
                                applyLiquidGlass(bottomNav);
                            }
                        });
                    } catch (Throwable t) {
                        XposedBridge.log("[pocket48] glass hook error: " + t);
                    }
                }
            });
            XposedBridge.log("[pocket48] hooked bottom bar glass");
        } catch (Throwable t) {
            XposedBridge.log("[pocket48] hook bottom bar error: " + t);
        }
    }

    private void applyLiquidGlass(View v) {
        try {
            float d = v.getResources().getDisplayMetrics().density;
            int m = Math.round(14 * d);   // 悬浮边距
            int r = Math.round(24 * d);   // 圆角

            ViewGroup.LayoutParams lp = v.getLayoutParams();
            if (lp instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
                mlp.leftMargin = m;
                mlp.rightMargin = m;
                mlp.bottomMargin = m;
                v.setLayoutParams(mlp);
            }

            GradientDrawable bg = new GradientDrawable();
            bg.setColor(0xE6FFFFFF);       // 半透明白
            bg.setCornerRadius(r);
            v.setBackground(bg);

            v.setElevation(Math.round(12 * d));
            v.setClipToOutline(true);
            XposedBridge.log("[pocket48] applied liquid glass bottom bar");
        } catch (Throwable t) {
            XposedBridge.log("[pocket48] applyLiquidGlass error: " + t);
        }
    }

    private EditText getNickEditText(Object activity) {
        try {
            Object binding = XposedHelpers.getObjectField(activity, "binding");
            Object et = XposedHelpers.getObjectField(binding, "cetUsername");
            if (et instanceof EditText) {
                return (EditText) et;
            }
        } catch (Throwable t) {
            XposedBridge.log("[pocket48] getNickEditText error: " + t);
        }
        return null;
    }
}
