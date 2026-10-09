#!/usr/bin/env bash
# Boots the Pixel_9 emulator (Android 16, Google Play, arm64) and prepares it for the rider tests:
# waits for boot, installs the dev APK, grants permissions, sets the GPS to Mohali (India marketplace).
# Usage: scripts/start-emulator.sh [path/to/app.apk]
set -euo pipefail
SDK="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
AVD="${AVD:-Pixel_9}"
APK="${1:-apps/hovr-rider-dev-v6.3.0+586-release.apk}"
SERIAL="emulator-5554"
PKG="production.hovr.customer"

if ! adb devices | grep -q "^$SERIAL"; then
  nohup "$SDK/emulator/emulator" -avd "$AVD" -no-snapshot-load -netdelay none -netspeed full -gpu host \
    > /tmp/emulator.log 2>&1 &
  echo "Booting $AVD ..."
fi
until [ "$(adb -s $SERIAL shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do sleep 3; done
echo "Booted: Android $(adb -s $SERIAL shell getprop ro.build.version.release | tr -d '\r')"

adb -s $SERIAL install -r "$APK"
for p in ACCESS_FINE_LOCATION ACCESS_COARSE_LOCATION POST_NOTIFICATIONS; do
  adb -s $SERIAL shell pm grant $PKG android.permission.$p
done
adb -s $SERIAL emu geo fix 76.7230 30.6781
adb -s $SERIAL shell settings put system screen_off_timeout 1800000
echo "Ready. Run tests with:  mvn test -Dplatform=emulator -Dtest=TestRunner -Dcucumber.filter.tags=\"@registration\""
