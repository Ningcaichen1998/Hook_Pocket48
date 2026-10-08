# Pocket48 Hook

一个针对口袋48（SNH48 官方粉丝应用，包名 `com.pocket.snh48.activity`）的 **LSPosed / Xposed 模块**，提供四项可独立开关的功能：

1. **去除流量弹框** —— 蜂窝网络下观看直播/视频时，跳过「当前是蜂窝网络播放，是否继续？」确认弹框，直接播放。
2. **修改昵称最小字数改为 1** —— 绕过「昵称不能少于2个字」的限制，允许设置单字昵称。
3. **悬浮液态玻璃底栏** —— 把主界面底部导航栏改成悬浮、圆角（胶囊形）、半透明真·毛玻璃质感。
4. **进入房间免关注** —— 搜索成员点击头像进入房间时，未关注也不再弹「请先关注」，直接进入。

四项功能均提供**可视化设置界面**，可独立开关。

---

## 为什么用 Hook 方案，而不是改 APK 重打包

口袋48 的直播功能使用**阿里云播放器（AliyunPlayer SDK）**，其 License 与**官方签名**绑定。

直接反编译修改 APK 后必须重新签名，而签名一旦改变，播放器在 `prepare`（拉流准备）阶段校验 License 会失败：

```
license init failed, reason is The app does not exist.
license check failed when prepare -> license authorization failed (errorCode 536948736)
```

表现为「进入直播间无限黑屏转圈」。因此直接改 APK 的路走不通（该方案已废弃并清理）。

本模块改用 **LSPosed 运行时 Hook**：安装**原版 APK**（保留官方签名，License 正常），在运行时动态修改逻辑，绕过弹框和字数校验、改造底栏，不动签名、不影响 License。

---

## 功能详情

### 1. 去除流量弹框

- **Hook 目标**：`com.pocket.snh48.activity.p2` 的静态方法 `竈鼕爩貜(Activity, 回调)`。
- **原逻辑**：WiFi/免流时同步直接播放；蜂窝网络时弹 `AlertDialog`（标题「提示」、正文「当前是蜂窝网络播放，是否继续?」、按钮「继续播放 / 我要免流 / 取消」），点击「继续播放」才触发播放回调。
- **Hook 逻辑**：`beforeHookedMethod` 中直接反射调用回调的 `鷙籲矡()`（播放），并 `setResult(null)` 跳过原方法（不弹框）。

### 2. 修改昵称最小字数改为 1

- **校验逻辑**：`EditNickNameActivity$鷙籲矡.onClick(View)` 中 `if (nick.length() < 2)` 提示「昵称不能少于2个字」。
- **提交方法**：`EditNickNameActivity.竈爩()` 重新读取输入框 `cetUsername` 的文本，提交到 `EditNickNameViewModel.糴鷙鬚蠶(String)`。
- **Hook 逻辑（两步）**：
  1. Hook onClick：昵称长度为 1 时，临时补一个尾随空格（`"X"` → `"X "`）通过 `< 2` 校验；
  2. Hook 提交方法 `竈爩()`：提交前 trim 掉尾随空格，提交干净的 1 字昵称 `"X"`。

### 3. 悬浮液态玻璃底栏

- **Hook 目标**：`com.pocket.snh48.activity.view.main.MainActivity` 的 `onCreate`，通过 DataBinding 字段 `ActivityMainBinding.bottomNavigationView` 与 `ActivityMainBinding.viewPager` 获取底部导航栏和内容区。
- **原样式**：Material `BottomNavigationView`，白色背景、贴屏幕底部、直角；内容区（ViewPager）底部约束为「贴底栏顶部」。
- **Hook 逻辑**：`afterHookedMethod` 中 `post` 到布局完成后执行 `applyLiquidGlass`：
  - **内容区占满全屏**：把 ViewPager 高度设为 `MATCH_PARENT`，并挂 `OnPreDrawListener` 自愈（防止 App 复原布局参数），消除底栏四周露出的根布局灰底（`#E8E8E8`）；
  - **悬浮**：修改 `MarginLayoutParams`（左右/底部 margin 14dp）；
  - **圆角**：自定义 `ViewOutlineProvider` 固定圆角轮廓，毛玻璃就位后改为**胶囊轮廓**（圆角 = 高度一半，最大圆角），并 `clipToOutline` 裁剪；
  - **玻璃质感**：50% 白（`#80FFFFFF`）底 + 12dp 阴影；
  - **真·毛玻璃**：延迟截图底栏背后内容（截图前先隐藏底栏自身）→ 纯 Java **Stack Blur** 算法模糊（半径 22dp）→ 叠加 15% 白（`#26FFFFFF`）作为背景。

### 4. 进入房间免关注

- **Hook 目标**：`com.pocket.snh48.home.ui.search.SearchActivity$龘龘籲蠶鼕` 的 `簾籲蠶爩(long, boolean)`（搜索成员列表点击头像进入房间的回调）。
- **原逻辑**：点击成员头像后回调带「是否已关注」标志，未关注时 `j9.糴鷙鬚蠶(..., "请先关注")` 弹提示并阻断进入；已关注才走 `SNHActivityUtils.m7036()` 跳转 `/person/detail`（进入房间）。
- **Hook 逻辑**：`beforeHookedMethod` 中把第二个参数（是否已关注）强制置为 `true`，直接进入房间。

### 5. 设置界面

- `MainActivity`（带桌面图标，LSPosed 管理器内亦可打开），提供四个开关，状态写入 SharedPreferences（key：`play_flow`、`nick_min`、`glass_bottom`、`enter_room_no_follow`）。
- Hook 端通过 `XSharedPreferences` 跨进程读取开关，按开关决定是否注入对应 hook。
- 切换开关后需重启口袋48（或重启手机）生效。

---

## 部署

### 前置条件

- 手机已 **root**（Magisk / KernelSU 等）。
- 已安装 **LSPosed**（或兼容的 Xposed 框架）。

### 步骤

1. 安装**原版**口袋48 APK（官方签名版）。
2. 安装本模块 `Pocket48去弹框Hook.apk`。
3. 打开 LSPosed 管理器 → 模块 → 勾选「Pocket48 Hook」→ 作用域勾选「口袋48」。
4. 重启手机（或强制停止口袋48 后重新打开）。
5. 打开桌面上的「口袋48 Hook 设置」图标，按需切换四个功能开关。

---

## 技术细节

### 关键 Hook 点（混淆后）

| 用途         | 类                                       | 方法/字段                               |
| ---------- | --------------------------------------- | ----------------------------------- |
| 去弹框        | `com.pocket.snh48.activity.p2`          | `竈鼕爩貜` (`\u7ac8\u9f15\u7229\u8c9c`) |
| 去弹框回调      | `com.pocket.snh48.activity.p2$竈糴蠶齇籲蠶蠶爩` | `鷙籲矡` (`\u9dd9\u7c72\u77e1`)        |
| 昵称 onClick | `...EditNickNameActivity$鷙籲矡`           | `鷙籲矡` (`\u9dd9\u7c72\u77e1`)        |
| 昵称提交       | `...EditNickNameActivity`               | `竈爩` (`\u7ac8\u7229`)               |
| 昵称输入框      | `ActivityEditNickNameBinding`           | `cetUsername`                       |
| 底栏         | `ActivityMainBinding`                   | `bottomNavigationView`              |
| 内容区        | `ActivityMainBinding`                   | `viewPager`                         |
| 进入房间免关注   | `...SearchActivity$龘龘籲蠶鼕`            | `簾籲蠶爩` (`\u7c3e\u7c72\u8836\u7229`) |

> 类名/方法名被 ProGuard 混淆为生僻汉字，需用 Unicode 转义引用。

### 构建（无需 Gradle）

环境：JDK 17 + Android SDK（build-tools 36.0.0、platforms android-37.0）+ Xposed api-82.jar。

```bash
# 编译 Java -> dex -> aapt2 打包 -> zipalign -> 签名
bash build.sh
```

构建产物：`build/Pocket48去弹框Hook.apk`

> 构建依赖的 JDK 17（`../jdk17/`）、Xposed API（`../xposed-api.jar`）、签名工具（`../uber-apk-signer.jar`）位于上一级 `tools/` 目录。

---

## 目录结构

```
hook_module/
├── AndroidManifest.xml        # 模块声明 + MainActivity + xposed 元数据
├── assets/
│   └── xposed_init            # 入口类声明：com.pocket48.hook.MainHook
├── java/com/pocket48/hook/
│   ├── MainHook.java          # IXposedHookLoadPackage 入口，条件注入四个 hook
│   └── MainActivity.java      # 设置界面（四个开关 + SharedPreferences）
├── build.sh                   # 一键构建脚本
└── build/                     # 构建产物（可删除，重新 build.sh 会重建）
```

---

## 说明与风险

- 本模块仅用于个人学习与自用，修改的是自己设备上运行的 App 行为，请遵守相关法律法规与服务条款。
- Hook 依赖逆向得到的混淆字段名/方法名，若 App 升级导致内部结构变化，对应功能可能失效，需要重新定位。
- 修改昵称功能不影响官方「修改次数 / VIP / 鸡腿消耗」等其他校验逻辑。
