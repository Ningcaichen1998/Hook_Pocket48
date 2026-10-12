package com.pocket48.hook;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

    // ===== 进入房间免关注（搜索成员点进房间时跳过「请先关注」） =====
    private static final String SEARCH_CB_CLS = "com.pocket.snh48.home.ui.search.SearchActivity$\u9f98\u9f98\u7c72\u8836\u9f15"; // $龘龘籲蠶鼕
    private static final String ENTER_ROOM_METHOD = "\u7c3e\u7c72\u8836\u7229"; // 簾籲蠶爩(long, boolean)

    // ===== 贡献周榜展示贡献值（把隐藏的贡献值列显示出来） =====
    private static final String CONTRIB_ADAPTER = "com.pocket.snh48.activity.rd1";
    private static final String CONTRIB_BIND_METHOD = "\u8c9c\u7c3e\u9b1a\u98b1\u7229"; // 貜簾鬚颱爩 (onBindViewHolder)
    private static final int ID_CONTRIB_AMOUNT = 0x7f0a0fd2; // tv_contribution_amount
    private static final long CONTRIB_PER_YUAN = 10L; // 10 贡献值 = 1 元（10000 贡献值 ≈ 1000 元）

    // ===== 去开屏广告/启动页 =====
    private static final String SPLASH_ACT = "com.pocket.snh48.activity.view.splash.SplashActivity";
    private static final String SPLASH_DO_NEXT = "doNext";      // 接口 ku 方法，未混淆
    private static final String SPLASH_NO_ACTION = "noActionDo"; // 接口 ku 方法，清空广告数据并跳主页

    // ===== 自动签到（每日首次启动） =====
    private static final String CHECKIN_HELPER = "com.pocket.snh48.base.utils.punch.CheckinDataHelper";
    private static final String CHECKIN_METHOD = "\u7ac8\u9f15\u7229\u8c9c"; // 竈鼕爩貜 (m44649 检查今日并自动签到)

    // ===== 消息时间精确到秒 =====
    private static final String DATE_FORMATTER = "com.pocket.snh48.lib.yunxin.chatkit.utils.DateFormatter";
    private static final String DF_TEMPLATE = "com.pocket.snh48.lib.yunxin.chatkit.utils.DateFormatter$Template";
    private static final String DF_FORMAT_METHOD = "\u9dd9\u7c72\u77e1"; // 鷙籲矡 (m52138(Date, Template))
    private static final String NIM_TIME = "com.netease.nimlib.p.z";     // NIM 相对时间工具
    private static final String TIME_I9 = "com.pocket.snh48.activity.i9"; // 口袋48 自有时间工具（房间时间标签真正来源）
    private static final String I9_SEG_METHOD = "\u8836\u9b1a\u8c9c\u7cf4\u9dd9\u7229\u77e1";   // 蠶鬚貜糴鷙爩矡 (m17247(Date)「凌晨/上午/下午/晚上 hh:mm」)
    private static final String I9_DAY_METHOD = "\u7c3e\u7229\u98b1\u9dd9\u9f98\u9dd9\u9dd9\u7c72"; // 簾爩颱鷙龘鷙鷙籲 (m17238(long,boolean)「今天/昨天 HH:mm」)
    // 房间消息时间 TextView 被 onBind 隐藏，需额外 Hook 改为可见
    private static final String MSG_IMESSAGE = "com.pocket.snh48.lib.yunxin.bean.IMessage";
    private static final String MSG_BUBBLE = "com.pocket.snh48.base.bean.juju.BubbleInfo";
    private static final String MSG_INCOMING = "com.pocket.snh48.lib.yunxin.chatkit.messages.MessageHolders$BaseIncomingMessageViewHolder";
    private static final String MSG_OUTCOMING = "com.pocket.snh48.lib.yunxin.chatkit.messages.MessageHolders$BaseOutcomingMessageViewHolder";
    private static final int ID_MSG_TIME = 0x7f0a09ed; // id/messageTime（口袋48 资源 id，运行时直接比对）
    // 频道文本消息（房间）：布局无 messageTime 控件，动态在气泡外下方添加时间 TextView
    private static final String CHANNEL_TEXT_VH = "com.pocket.snh48.lib.yunxin.chatkit.messages.group.ChannelTextMsgVH";
    private static final String CHANNEL_IMAGE_VH = "com.pocket.snh48.lib.yunxin.chatkit.messages.group.ChannelImageMessageViewHolder";
    private static final String YUNXIN_MSG = "com.pocket.snh48.lib.yunxin.bean.YunXinMessage";
    private static final int ID_BUBBLE = 0x7f0a01af;     // id/bubble（气泡容器）
    private static final int ID_MSG_TIME_ADD = 0x7f7f0001; // 动态添加的时间 TextView 固定 id
    private static final int ID_NAME = 0x7f0a13a0;        // 昵称 TextView（气泡 topToBottom 指向它，用于反推字段）

    // 运行时反推出的 ConstraintLayout.LayoutParams 混淆约束字段（缓存）
    private static volatile Field F_topToBottom;

    // ===== 消息防撤回 =====
    private static final String REVOKE_M = "com.netease.nimlib.biz.c.j.m";
    private static final String REVOKE_A = "com.netease.nimlib.biz.e.a";
    private static final String REVOKE_U = "com.netease.nimlib.biz.e.k.u";
    private static final String REVOKE_AA = "com.netease.nimlib.biz.e.k.aa";

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
        if (sp.getBoolean("enter_room_no_follow", true)) {
            hookEnterRoom(lp);
        }
        boolean contribVal = sp.getBoolean("contribution_value", true);
        boolean contribRmb = sp.getBoolean("contribution_rmb", true);
        if (contribVal || contribRmb) {
            hookContribution(lp, contribRmb);
        }
        if (sp.getBoolean("skip_splash_ad", true)) {
            hookSplashAd(lp);
        }
        if (sp.getBoolean("auto_checkin", true)) {
            hookAutoCheckin(lp);
        }
        if (sp.getBoolean("msg_time_sec", true)) {
            hookMessageTime(lp);
        }
        if (sp.getBoolean("msg_anti_revoke", true)) {
            hookAntiRevoke(lp);
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

    /** 悬浮液态玻璃底栏：底栏悬浮 + 圆角 + 半透明玻璃质感，内容区占满全屏 */
    private void hookBottomBar(XC_LoadPackage.LoadPackageParam lp) {
        try {
            final Class<?> mainAct = XposedHelpers.findClass(MAIN_ACT, lp.classLoader);
            XposedHelpers.findAndHookMethod(mainAct, "onCreate", android.os.Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        final Object binding = XposedHelpers.getObjectField(param.thisObject, "binding");
                        final View bottomNav = (View) XposedHelpers.getObjectField(binding, "bottomNavigationView");
                        final View viewPager = (View) XposedHelpers.getObjectField(binding, "viewPager");
                        bottomNav.post(new Runnable() {
                            @Override
                            public void run() {
                                applyLiquidGlass(bottomNav, viewPager);
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

    /** 进入房间免关注：搜索成员点击头像进入房间时，把「已关注」标志强制为 true，跳过「请先关注」提示直接进入 */
    private void hookEnterRoom(XC_LoadPackage.LoadPackageParam lp) {
        try {
            final Class<?> cls = XposedHelpers.findClass(SEARCH_CB_CLS, lp.classLoader);
            XposedHelpers.findAndHookMethod(cls, ENTER_ROOM_METHOD, long.class, boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (param.args.length >= 2) {
                        param.args[1] = true; // 强制视为已关注
                    }
                }
            });
            XposedBridge.log("[pocket48] hooked enter-room follow gate");
        } catch (Throwable t) {
            XposedBridge.log("[pocket48] hook enter-room follow gate error: " + t);
        }
    }

    /** 贡献周榜：把隐藏的贡献值列显示出来；showRmb 时再把贡献值换算成人民币数额一并展示 */
    private void hookContribution(XC_LoadPackage.LoadPackageParam lp, final boolean showRmb) {
        try {
            final Class<?> rd1 = XposedHelpers.findClass(CONTRIB_ADAPTER, lp.classLoader);
            XposedBridge.hookAllMethods(rd1, CONTRIB_BIND_METHOD, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        Object holder = param.args[0];
                        if (holder == null) {
                            return;
                        }
                        View itemView = (View) XposedHelpers.getObjectField(holder, "itemView");
                        if (itemView == null) {
                            return;
                        }
                        TextView amount = (TextView) itemView.findViewById(ID_CONTRIB_AMOUNT);
                        if (amount == null) {
                            return;
                        }
                        View parent = (View) amount.getParent();
                        if (parent != null && parent.getVisibility() != View.VISIBLE) {
                            parent.setVisibility(View.VISIBLE);
                        }
                        if (showRmb) {
                            applyContributionRmb(amount);
                        }
                    } catch (Throwable t) {
                        XposedBridge.log("[pocket48] contribution value error: " + t);
                    }
                }
            });
            XposedBridge.log("[pocket48] hooked week contribution display (rmb=" + showRmb + ")");
        } catch (Throwable t) {
            XposedBridge.log("[pocket48] hook contribution error: " + t);
        }
    }

    /** 去开屏广告：拦截 doNext()，直接走 noActionDo() 清空广告数据并跳转主页，跳过图片/视频开屏广告 */
    private void hookSplashAd(XC_LoadPackage.LoadPackageParam lp) {
        try {
            final Class<?> splash = XposedHelpers.findClass(SPLASH_ACT, lp.classLoader);
            XposedHelpers.findAndHookMethod(splash, SPLASH_DO_NEXT, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    try {
                        XposedHelpers.callMethod(param.thisObject, SPLASH_NO_ACTION);
                    } catch (Throwable t) {
                        XposedBridge.log("[pocket48] splash noActionDo error: " + t);
                    }
                    param.setResult(null);
                }
            });
            XposedBridge.log("[pocket48] hooked splash ad skip");
        } catch (Throwable t) {
            XposedBridge.log("[pocket48] hook splash ad error: " + t);
        }
    }

    /** 自动签到：每日首次进入主页后延迟触发签到 */
    private void hookAutoCheckin(XC_LoadPackage.LoadPackageParam lp) {
        try {
            final Class<?> mainAct = XposedHelpers.findClass(MAIN_ACT, lp.classLoader);
            final Class<?> helperCls = XposedHelpers.findClass(CHECKIN_HELPER, lp.classLoader);
            XposedHelpers.findAndHookMethod(mainAct, "onCreate", android.os.Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        final Activity act = (Activity) param.thisObject;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                tryAutoCheckin(act, helperCls);
                            }
                        }, 2000);
                    } catch (Throwable t) {
                        XposedBridge.log("[pocket48] auto checkin schedule error: " + t);
                    }
                }
            });
            XposedBridge.log("[pocket48] hooked auto checkin");
        } catch (Throwable t) {
            XposedBridge.log("[pocket48] hook auto checkin error: " + t);
        }
    }

    /** 每天只触发一次签到（用目标 App 的 sp 记录日期），创建 CheckinDataHelper 并调用检查方法 */
    private void tryAutoCheckin(Activity act, Class<?> helperCls) {
        try {
            SharedPreferences sp = act.getSharedPreferences("pocket48_hook_checkin", Context.MODE_PRIVATE);
            String today = new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());
            if (today.equals(sp.getString("last", ""))) {
                return;
            }
            sp.edit().putString("last", today).apply();
            Object helper = XposedHelpers.newInstance(helperCls, act);
            XposedHelpers.callMethod(helper, CHECKIN_METHOD);
            XposedBridge.log("[pocket48] auto checkin triggered");
        } catch (Throwable t) {
            XposedBridge.log("[pocket48] auto checkin error: " + t);
        }
    }

    /** 消息时间精确到秒：DATE_FORMATTER 的 TIME 模板 + NIM 相对时间工具（直播间/会话时间标签） */
    private void hookMessageTime(XC_LoadPackage.LoadPackageParam lp) {
        try {
            final Class<?> df = XposedHelpers.findClass(DATE_FORMATTER, lp.classLoader);
            final Class<?> templateCls = XposedHelpers.findClass(DF_TEMPLATE, lp.classLoader);
            XposedHelpers.findAndHookMethod(df, DF_FORMAT_METHOD, Date.class, templateCls, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    try {
                        Object tmpl = param.args[1];
                        Date date = (Date) param.args[0];
                        if (tmpl != null && "TIME".equals(tmpl.toString()) && date != null) {
                            param.setResult(new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date));
                        }
                    } catch (Throwable t) {
                        XposedBridge.log("[pocket48] msg time error: " + t);
                    }
                }
            });
            XposedBridge.log("[pocket48] hooked message time to seconds (DateFormatter)");
        } catch (Throwable t) {
            XposedBridge.log("[pocket48] hook message time error: " + t);
        }
        // NIM z 类：时间标签（「晚上 hh:mm」「今天 HH:mm」）
        try {
            final Class<?> zCls = XposedHelpers.findClass(NIM_TIME, lp.classLoader);
            // a(Date): 「凌晨/上午 KK:mm」「下午/晚上 hh:mm」→ 追加秒
            XposedHelpers.findAndHookMethod(zCls, "a", Date.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        appendSeconds(param, (Date) param.args[0], "KK:mm", "hh:mm");
                    } catch (Throwable ignore) {
                    }
                }
            });
            // a(long, boolean): 「今天/昨天 HH:mm」→ 追加秒（z=true 今天时内部走 a(Date)，已被上面覆盖）
            XposedHelpers.findAndHookMethod(zCls, "a", long.class, boolean.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        if ((Boolean) param.args[1]) return;
                        appendSeconds(param, new Date((Long) param.args[0]), "HH:mm", null);
                    } catch (Throwable ignore) {
                    }
                }
            });
            XposedBridge.log("[pocket48] hooked NIM time to seconds");
        } catch (Throwable t) {
            XposedBridge.log("[pocket48] hook NIM time error: " + t);
        }
        // i9 类：口袋48 自有时间工具，房间时间标签的真正来源
        try {
            final Class<?> i9 = XposedHelpers.findClass(TIME_I9, lp.classLoader);
            // m17247(Date): 「凌晨/上午 KK:mm」「下午/晚上 hh:mm」
            XposedHelpers.findAndHookMethod(i9, I9_SEG_METHOD, Date.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        appendSeconds(param, (Date) param.args[0], "KK:mm", "hh:mm");
                    } catch (Throwable ignore) {
                    }
                }
            });
            // m17238(long, boolean): 「今天/昨天/星期/日期 HH:mm」（z=true 今天时内部走 m17247，已被上面覆盖）
            XposedHelpers.findAndHookMethod(i9, I9_DAY_METHOD, long.class, boolean.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        appendSeconds(param, new Date((Long) param.args[0]), "HH:mm", null);
                    } catch (Throwable ignore) {
                    }
                }
            });
            XposedBridge.log("[pocket48] hooked i9 time to seconds");
        } catch (Throwable t) {
            XposedBridge.log("[pocket48] hook i9 time error: " + t);
        }
        // 房间消息：BaseIncoming/BaseOutcoming 的 onBind 把时间 TextView 隐藏，改为可见
        try {
            final Class<?> imsg = XposedHelpers.findClass(MSG_IMESSAGE, lp.classLoader);
            final Class<?> bubble = XposedHelpers.findClass(MSG_BUBBLE, lp.classLoader);
            XC_MethodHook visHook = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        Object tv = XposedHelpers.getObjectField(param.thisObject, "time");
                        if (tv instanceof View) {
                            ((View) tv).setVisibility(View.VISIBLE);
                        }
                    } catch (Throwable t) {
                        XposedBridge.log("[pocket48] room time visible error: " + t);
                    }
                }
            };
            XposedHelpers.findAndHookMethod(XposedHelpers.findClass(MSG_INCOMING, lp.classLoader), "onBind", imsg, bubble, visHook);
            XposedHelpers.findAndHookMethod(XposedHelpers.findClass(MSG_OUTCOMING, lp.classLoader), "onBind", imsg, bubble, visHook);
            XposedBridge.log("[pocket48] hooked room message time visible");
        } catch (Throwable t) {
            XposedBridge.log("[pocket48] hook room message time error: " + t);
        }
        // 兜底：不依赖签名，全局拦截 setVisibility(GONE)，命中 messageTime id 强制改可见
        try {
            XposedHelpers.findAndHookMethod(View.class, "setVisibility", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    try {
                        if (((int) param.args[0]) != View.GONE) return;
                        View v = (View) param.thisObject;
                        if (v.getId() == ID_MSG_TIME) {
                            param.args[0] = View.VISIBLE;
                        }
                    } catch (Throwable ignore) {
                    }
                }
            });
            XposedBridge.log("[pocket48] hooked messageTime setVisibility guard");
        } catch (Throwable t) {
            XposedBridge.log("[pocket48] hook messageTime visibility error: " + t);
        }
        // 频道文本/图片消息（房间）：布局无 messageTime，动态在气泡外下方添加时间 TextView
        try {
            final Class<?> vhText = XposedHelpers.findClass(CHANNEL_TEXT_VH, lp.classLoader);
            final Class<?> vhImage = XposedHelpers.findClass(CHANNEL_IMAGE_VH, lp.classLoader);
            final Class<?> yunxinMsg = XposedHelpers.findClass(YUNXIN_MSG, lp.classLoader);
            final Class<?> bubbleCls = XposedHelpers.findClass(MSG_BUBBLE, lp.classLoader);
            XC_MethodHook channelBindHook = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        Date createdAt = (Date) XposedHelpers.callMethod(param.args[0], "getCreatedAt");
                        if (createdAt == null) return;
                        String timeStr = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(createdAt);
                        View itemView = (View) XposedHelpers.getObjectField(param.thisObject, "itemView");
                        if (!(itemView instanceof ViewGroup)) return;
                        addTimeBelowBubble((ViewGroup) itemView, timeStr);
                    } catch (Throwable t) {
                        XposedBridge.log("[pocket48] channel time below error: " + t);
                    }
                }
            };
            XposedHelpers.findAndHookMethod(vhText, "onBind", yunxinMsg, bubbleCls, channelBindHook);
            XposedHelpers.findAndHookMethod(vhImage, "onBind", yunxinMsg, bubbleCls, channelBindHook);
            XposedBridge.log("[pocket48] hooked channel text/image time below");
        } catch (Throwable t) {
            XposedBridge.log("[pocket48] hook channel time error: " + t);
        }
    }

    /**
     * 在气泡下方显示时间（v2.20 方案：topToBottom 约束定 y + translationX 定 x）。
     * v2.19 实测：topToBottom 反推可靠（时间 y 正确出现在气泡下方），
     * 但 startToStart/endToEnd 反推出错（时间 x 贴最左）。
     * 因此：垂直方向只设已验证可靠的 topToBottom=气泡；水平方向 CL 会把无水平约束的
     * 子 View 放到左缘，用 translationX 平移到气泡下方居中（CL 布局不会重置 translation）。
     */
    private void addTimeBelowBubble(ViewGroup root, String timeStr) {
        try {
            final View bubble = root.findViewById(ID_BUBBLE);
            if (bubble == null) return;
            TextView tv = (TextView) root.findViewById(ID_MSG_TIME_ADD);
            if (tv == null) {
                ViewGroup.LayoutParams bubbleLp = bubble.getLayoutParams();
                // 仅支持 ConstraintLayout 场景
                if (bubbleLp == null || !bubbleLp.getClass().getName().contains("ConstraintLayout$LayoutParams")) {
                    return;
                }
                Class<?> clLp = bubbleLp.getClass();
                Object lpObj = clLp.getConstructor(int.class, int.class)
                        .newInstance(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                tv = new TextView(root.getContext());
                tv.setId(ID_MSG_TIME_ADD);
                tv.setTextSize(9);
                tv.setTextColor(0xFF8A8FA3);
                tv.setGravity(android.view.Gravity.CENTER);

                // 垂直：topToBottom=气泡（字段反推已实测可靠）
                deriveTopToBottomField(root, clLp);
                if (F_topToBottom == null) {
                    XposedBridge.log("[pocket48] derive topToBottom failed, skip time");
                    return;
                }
                F_topToBottom.setInt(lpObj, ID_BUBBLE);
                ((ViewGroup.MarginLayoutParams) lpObj).topMargin = dpPx(root, 1);

                root.addView(tv, (ViewGroup.LayoutParams) lpObj);
                // 气泡底部对齐根容器底部，时间在气泡下方会被裁剪；底部加 padding 腾出空间（紧凑化）
                root.setPadding(root.getPaddingLeft(), root.getPaddingTop(), root.getPaddingRight(),
                        root.getPaddingBottom() + dpPx(root, 13));
                // 收紧 item 自带的底部外边距，减小两条消息之间的间距
                ViewGroup.LayoutParams rootLp = root.getLayoutParams();
                if (rootLp instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) rootLp;
                    int reduce = dpPx(root, 4);
                    mlp.bottomMargin = Math.max(0, mlp.bottomMargin - reduce);
                    root.setLayoutParams(mlp);
                }
            }
            tv.setText(timeStr);
            // 水平：布局完成后用 translationX 平移到气泡下方居中
            final TextView tvF = tv;
            final View bF = bubble;
            tvF.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (bF.getWidth() == 0) return;
                        int w = tvF.getWidth() > 0 ? tvF.getWidth() : tvF.getMeasuredWidth();
                        int x = bF.getLeft() + (bF.getWidth() - w) / 2;
                        if (x < 0) x = bF.getLeft();
                        if (tvF.getTranslationX() != x) tvF.setTranslationX(x);
                    } catch (Throwable ignore) {
                    }
                }
            });
        } catch (Throwable t) {
            XposedBridge.log("[pocket48] add time below error: " + t);
        }
    }

    /**
     * 反推 ConstraintLayout.LayoutParams 中被混淆的 topToBottom 字段（只做一次，缓存）。
     * 依据：气泡 topToBottom 指向昵称 TextView(ID_NAME)，故气泡 lp 中值为 ID_NAME 的 int 字段即 topToBottom。
     */
    private void deriveTopToBottomField(View root, Class<?> clLp) {
        if (F_topToBottom != null) return;
        synchronized (this) {
            if (F_topToBottom != null) return;
            View bubble = root.findViewById(ID_BUBBLE);
            View name = root.findViewById(ID_NAME);
            if (bubble == null || name == null) return;
            try {
                Object lp = bubble.getLayoutParams();
                for (Field f : clLp.getDeclaredFields()) {
                    if (f.getType() != int.class) continue;
                    f.setAccessible(true);
                    if (f.getInt(lp) == name.getId()) {
                        F_topToBottom = f;
                        break;
                    }
                }
            } catch (Throwable t) {
                XposedBridge.log("[pocket48] derive topToBottom error: " + t);
            }
            XposedBridge.log("[pocket48] derived topToBottom: " + (F_topToBottom != null));
        }
    }

    private int dpPx(View v, int dp) {
        return (int) (v.getResources().getDisplayMetrics().density * dp + 0.5f);
    }

    /** 消息防撤回：拦截 NIM 撤回通知处理（u=单条撤回 / aa=批量撤回），消息不被删除 */
    private void hookAntiRevoke(XC_LoadPackage.LoadPackageParam lp) {
        try {
            final Class<?> mCls = XposedHelpers.findClass(REVOKE_M, lp.classLoader);
            final Class<?> aCls = XposedHelpers.findClass(REVOKE_A, lp.classLoader);
            final Class<?> uCls = XposedHelpers.findClass(REVOKE_U, lp.classLoader);
            final Class<?> aaCls = XposedHelpers.findClass(REVOKE_AA, lp.classLoader);
            XposedHelpers.findAndHookMethod(mCls, "a", aCls, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    try {
                        Object arg = param.args[0];
                        if (arg != null && (uCls.isInstance(arg) || aaCls.isInstance(arg))) {
                            param.setResult(null);
                            XposedBridge.log("[pocket48] blocked message revoke");
                        }
                    } catch (Throwable t) {
                        XposedBridge.log("[pocket48] anti revoke error: " + t);
                    }
                }
            });
            XposedBridge.log("[pocket48] hooked message anti-revoke");
        } catch (Throwable t) {
            XposedBridge.log("[pocket48] hook anti-revoke error: " + t);
        }
    }

    /** 通用：把 hook 结果尾部的时间部分（pattern 匹配，如「晚上 08:14」的 08:14）替换为带秒版本 */
    private static void appendSeconds(XC_MethodHook.MethodHookParam param, Date d, String p1, String p2) {
        String old = (String) param.getResult();
        if (old == null || old.length() < 5 || d == null) return;
        String[] pats = p2 == null ? new String[]{p1} : new String[]{p1, p2};
        for (String p : pats) {
            String hm = new SimpleDateFormat(p, Locale.getDefault()).format(d);
            if (hm.length() > 0 && old.endsWith(hm)) {
                String withSec = new SimpleDateFormat(p + ":ss", Locale.getDefault()).format(d);
                param.setResult(old.substring(0, old.length() - hm.length()) + withSec);
                return;
            }
        }
    }

    /** 把贡献值文本追加换算后的人民币数额，例如 "12345" -> "12345\n≈ ¥1234.5"（幂等，解析失败即跳过） */
    private void applyContributionRmb(TextView amount) {
        try {
            String raw = amount.getText().toString();
            if (raw == null || raw.length() == 0) {
                return;
            }
            long charm;
            try {
                charm = Long.parseLong(raw.trim());
            } catch (NumberFormatException e) {
                return; // 已处理过或不是纯数字
            }
            long yuan = charm / CONTRIB_PER_YUAN;
            long jiao = charm % CONTRIB_PER_YUAN;
            String rmb = jiao == 0 ? ("¥" + yuan) : ("¥" + yuan + "." + jiao);
            amount.setText(charm + "\n≈ " + rmb);
        } catch (Throwable t) {
            XposedBridge.log("[pocket48] contribution rmb error: " + t);
        }
    }

    private void applyLiquidGlass(View bottomNav, View viewPager) {
        try {
            float d = bottomNav.getResources().getDisplayMetrics().density;
            int m = Math.round(14 * d);   // 悬浮边距
            int r = Math.round(34 * d);   // 圆角（加大）

            // 1. 内容区(ViewPager)占满全屏：高度改为 MATCH_PARENT（比改约束更可靠），
            //    并加 onPreDraw 自愈，防止 App 之后复原布局参数
            if (viewPager != null) {
                final View vp = viewPager;
                Runnable fixer = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ViewGroup.LayoutParams p = vp.getLayoutParams();
                            boolean dirty = false;
                            if (p.height != ViewGroup.LayoutParams.MATCH_PARENT) {
                                p.height = ViewGroup.LayoutParams.MATCH_PARENT;
                                dirty = true;
                            }
                            try {
                                if (XposedHelpers.getIntField(p, "bottomToTop") != -1) {
                                    XposedHelpers.setIntField(p, "bottomToTop", -1);
                                    dirty = true;
                                }
                                if (XposedHelpers.getIntField(p, "bottomToBottom") != 0) {
                                    XposedHelpers.setIntField(p, "bottomToBottom", 0);
                                    dirty = true;
                                }
                            } catch (Throwable ignore) {
                            }
                            if (dirty) {
                                vp.setLayoutParams(p);
                                XposedBridge.log("[pocket48] viewPager fixed to full-screen");
                            }
                        } catch (Throwable t) {
                            XposedBridge.log("[pocket48] viewPager fixer error: " + t);
                        }
                    }
                };
                fixer.run();
                // 自愈：每次绘制前检查，若被 App 改回去则强制恢复
                viewPager.getViewTreeObserver().addOnPreDrawListener(new android.view.ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        try {
                            ViewGroup.LayoutParams p = vp.getLayoutParams();
                            boolean dirty = false;
                            if (p.height != ViewGroup.LayoutParams.MATCH_PARENT) {
                                p.height = ViewGroup.LayoutParams.MATCH_PARENT;
                                dirty = true;
                            }
                            try {
                                if (XposedHelpers.getIntField(p, "bottomToTop") != -1) {
                                    XposedHelpers.setIntField(p, "bottomToTop", -1);
                                    dirty = true;
                                }
                                if (XposedHelpers.getIntField(p, "bottomToBottom") != 0) {
                                    XposedHelpers.setIntField(p, "bottomToBottom", 0);
                                    dirty = true;
                                }
                            } catch (Throwable ignore) {
                            }
                            if (dirty) {
                                vp.setLayoutParams(p);
                            }
                        } catch (Throwable ignore) {
                        }
                        return true;
                    }
                });

                viewPager.setPadding(
                        viewPager.getPaddingLeft(),
                        viewPager.getPaddingTop(),
                        viewPager.getPaddingRight(),
                        0);
            }

            // 2. 底栏悬浮
            ViewGroup.LayoutParams lp = bottomNav.getLayoutParams();
            if (lp instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
                mlp.leftMargin = m;
                mlp.rightMargin = m;
                mlp.bottomMargin = m;
                bottomNav.setLayoutParams(mlp);
            }

            // 3. 圆角半透明玻璃背景 + 阴影（加大圆角、降低不透明度）
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(0x80FFFFFF);       // 50% 白，更通透
            bg.setCornerRadius(r);
            bottomNav.setBackground(bg);

            // 固定圆角轮廓：不依赖背景生成（换成 LayerDrawable 后轮廓会变矩形导致圆角失效）
            bottomNav.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 34 * view.getResources().getDisplayMetrics().density);
                }
            });
            bottomNav.setClipToOutline(true);
            bottomNav.setElevation(Math.round(12 * d));

            // 4. 真·毛玻璃：延迟截图内容 + 模糊 + 作为背景
            final int blurRadius = Math.round(22 * d);
            bottomNav.postDelayed(new Runnable() {
                @Override
                public void run() {
                    applyBlurBackground(bottomNav, blurRadius);
                }
            }, 400);
            XposedBridge.log("[pocket48] applied liquid glass bottom bar");
        } catch (Throwable t) {
            XposedBridge.log("[pocket48] applyLiquidGlass error: " + t);
        }
    }

    /** 截图底栏背后内容，Stack Blur 模糊后叠加半透明白作为背景（真毛玻璃） */
    private void applyBlurBackground(View bottomNav, int radius) {
        try {
            if (bottomNav.getWidth() <= 0 || bottomNav.getHeight() <= 0) return;
            Bitmap captured = captureBackground(bottomNav);
            if (captured == null) return;
            Bitmap blurred = stackBlur(captured, radius);

            GradientDrawable overlay = new GradientDrawable();
            overlay.setColor(0x26FFFFFF);   // 15% 白叠加，更通透

            LayerDrawable layer = new LayerDrawable(new Drawable[]{
                    new BitmapDrawable(bottomNav.getResources(), blurred),
                    overlay
            });
            bottomNav.setBackground(layer);
            // 毛玻璃背景就位后，重设为「胶囊」轮廓（圆角 = 高度一半，最大圆角）
            bottomNav.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    float pill = Math.min(view.getWidth(), view.getHeight()) / 2f;
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), pill);
                }
            });
            bottomNav.setClipToOutline(true);
            XposedBridge.log("[pocket48] applied blur background");
        } catch (Throwable t) {
            XposedBridge.log("[pocket48] applyBlurBackground error: " + t);
        }
    }

    private Bitmap captureBackground(View v) {
        try {
            View root = v.getRootView();
            int w = root.getWidth(), h = root.getHeight();
            if (w <= 0 || h <= 0) return null;
            // 截图时先隐藏底栏自己，避免把底栏旧外观截进毛玻璃里
            int oldVis = v.getVisibility();
            v.setVisibility(View.INVISIBLE);
            Bitmap full = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(full);
            root.draw(canvas);
            v.setVisibility(oldVis);
            int[] loc = new int[2];
            v.getLocationOnScreen(loc);
            int[] rl = new int[2];
            root.getLocationOnScreen(rl);
            int x = loc[0] - rl[0];
            int y = loc[1] - rl[1];
            int cw = Math.min(v.getWidth(), w - x);
            int ch = Math.min(v.getHeight(), h - y);
            if (cw <= 0 || ch <= 0) return null;
            return Bitmap.createBitmap(full, x, y, cw, ch);
        } catch (Throwable t) {
            XposedBridge.log("[pocket48] captureBackground error: " + t);
            return null;
        }
    }

    /** 经典 Stack Blur 算法（纯 Java，两遍水平+垂直） */
    private Bitmap stackBlur(Bitmap bitmap, int radius) {
        if (radius < 1) return bitmap;
        int w = bitmap.getWidth(), h = bitmap.getHeight();
        int[] pixels = new int[w * h];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);

        int wm = w - 1, hm = h - 1;
        int div = radius * 2 + 1;
        int[] r = new int[w * h], g = new int[w * h], b = new int[w * h];
        int[] vmin = new int[Math.max(w, h)];
        int[] dv = new int[256 * div];
        for (int i = 0; i < 256 * div; i++) dv[i] = i / div;

        int yw = 0, yi = 0;
        for (int y = 0; y < h; y++) {
            int rsum = 0, gsum = 0, bsum = 0;
            for (int i = -radius; i <= radius; i++) {
                int p = pixels[yi + Math.min(wm, Math.max(i, 0))];
                rsum += (p & 0xFF0000) >> 16;
                gsum += (p & 0x00FF00) >> 8;
                bsum += (p & 0x0000FF);
            }
            for (int x = 0; x < w; x++) {
                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];
                if (y == 0) vmin[x] = Math.min(x + radius + 1, wm);
                int p = pixels[yw + vmin[x]];
                int p0 = pixels[yw + Math.max(x - radius, 0)];
                rsum += ((p & 0xFF0000) >> 16) - ((p0 & 0xFF0000) >> 16);
                gsum += ((p & 0x00FF00) >> 8) - ((p0 & 0x00FF00) >> 8);
                bsum += (p & 0x0000FF) - (p0 & 0x0000FF);
                yi++;
            }
            yw += w;
        }

        for (int x = 0; x < w; x++) {
            int rsum = 0, gsum = 0, bsum = 0;
            int yp = -radius * w;
            for (int i = -radius; i <= radius; i++) {
                int idx = Math.max(0, yp) + x;
                rsum += r[idx];
                gsum += g[idx];
                bsum += b[idx];
                yp += w;
            }
            yi = x;
            for (int y = 0; y < h; y++) {
                pixels[yi] = 0xFF000000 | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];
                if (x == 0) vmin[y] = Math.min(y + radius + 1, hm) * w;
                rsum += r[x + Math.min(y + radius, hm) * w] - r[x + Math.max(y - radius, 0) * w];
                gsum += g[x + Math.min(y + radius, hm) * w] - g[x + Math.max(y - radius, 0) * w];
                bsum += b[x + Math.min(y + radius, hm) * w] - b[x + Math.max(y - radius, 0) * w];
                yi += w;
            }
        }

        Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        result.setPixels(pixels, 0, w, 0, 0, w, h);
        return result;
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
