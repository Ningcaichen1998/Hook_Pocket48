#!/bin/bash
set -e
TOOLS="C:/Users/Administrator/WorkBuddy/2026-10-07-22-50-17/tools"
BT="C:/Users/Administrator/AppData/Local/Android/Sdk/build-tools/36.0.0"
PLATFORM="C:/Users/Administrator/AppData/Local/Android/Sdk/platforms/android-37.0"
JAVA="C:/Users/Administrator/WorkBuddy/2026-10-07-22-50-17/tools/jdk17/jdk-17.0.2/bin/java.exe"
JAVAC="C:/Users/Administrator/WorkBuddy/2026-10-07-22-50-17/tools/jdk17/jdk-17.0.2/bin/javac.exe"

cd "$TOOLS/hook_module"
rm -rf build && mkdir -p build/classes build/dex

echo "=== 1. 编译 Java ==="
"$JAVAC" -encoding UTF-8 -source 8 -target 8 \
  -classpath "$TOOLS/xposed-api.jar;$PLATFORM/android.jar" \
  -d build/classes \
  $(find java -name '*.java') 2>&1
echo "编译完成"

echo "=== 2. d8 转 dex ==="
"$JAVA" -cp "$BT/lib/d8.jar" com.android.tools.r8.D8 \
  --release --lib "$PLATFORM/android.jar" \
  --output build/dex \
  $(find build/classes -name '*.class') 2>&1
echo "dex 完成"

echo "=== 3. aapt2 link ==="
"$BT/aapt2.exe" link \
  -o build/base.apk \
  -I "$PLATFORM/android.jar" \
  --manifest AndroidManifest.xml \
  -A assets \
  --min-sdk-version 21 --target-sdk-version 37 2>&1
echo "aapt2 完成"

echo "=== 4. 加入 classes.dex ==="
cp build/dex/classes.dex build/classes.dex
cd build
"$JAVA" -cp "$TOOLS/jdk17/jdk-17.0.2/lib/tools.jar" 2>/dev/null || true
# 用 zip 加入 dex
"C:/Users/Administrator/.workbuddy/binaries/python/versions/3.13.12/python.exe" -c "
import zipfile
z = zipfile.ZipFile('base.apk', 'a')
z.write('classes.dex', 'classes.dex')
z.close()
print('classes.dex 已加入')
"
echo "=== 5. zipalign ==="
"$BT/zipalign.exe" -f 4 base.apk aligned.apk 2>&1
echo "=== 6. 签名 ==="
"$JAVA" -jar "$TOOLS/uber-apk-signer.jar" --apks aligned.apk --allowResign 2>&1 | tail -5
echo "=== 完成 ==="
ls -la aligned-aligned-debugSigned.apk 2>/dev/null || ls -la *debugSigned.apk 2>/dev/null || ls -la
