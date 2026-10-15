#!/usr/bin/env bash
# Boots the HOVR_Test emulator (Android 16, Google Play services, arm64) and prepares it for the rider tests:
# waits for boot, installs the dev APK, grants permissions, sets the GPS to Mohali (India marketplace).
# Usage: scripts/start-emulator.sh [path/to/app.apk]
# Set HEADLESS=1 for unattended runs: no emulator window, so closing a window (or the Mac sleeping with
# the window open) can't quit the emulator mid-run -- that's what killed a 37-scenario run on 2026-10-10.
set -euo pipefail
SDK="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
# HOVR_Test: Android 16 / Google Play services, 4 GB RAM, 12 GB data. The stock Pixel_9 (2 GB / 6 GB) ran out
# of memory and disk with two HOVR apps + Appium (random "socket hang up", crashing com.android.phone).
AVD="${AVD:-HOVR_Test}"
EXTRA_FLAGS=""
[ "${HEADLESS:-0}" = "1" ] && EXTRA_FLAGS="-no-window -no-audio -no-boot-anim"
APK="${1:-apps/hovr-rider-dev-v6.3.0+588-release.apk}"
DRIVER_APK="${DRIVER_APK:-apps/hovr-driver-dev-v6.2.4+592-release.apk}"
SERIAL="emulator-5554"
PKG="production.hovr.customer"

if ! adb devices | grep -q "^$SERIAL"; then
  nohup "$SDK/emulator/emulator" -avd "$AVD" -no-snapshot-load -netdelay none -netspeed full -gpu host $EXTRA_FLAGS \
    > /tmp/emulator.log 2>&1 &
  echo "Booting $AVD ..."
fi
until [ "$(adb -s $SERIAL shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do sleep 3; done
echo "Booted: Android $(adb -s $SERIAL shell getprop ro.build.version.release | tr -d '\r')"

adb -s $SERIAL install -r "$APK"
if [ -f "$DRIVER_APK" ]; then adb -s $SERIAL install -r "$DRIVER_APK"; fi
for p in ACCESS_FINE_LOCATION ACCESS_COARSE_LOCATION POST_NOTIFICATIONS; do
  adb -s $SERIAL shell pm grant $PKG android.permission.$p
  adb -s $SERIAL shell pm grant com.example.hovr_driver android.permission.$p 2>/dev/null || true
done
adb -s $SERIAL emu geo fix 76.7230 30.6781
adb -s $SERIAL shell settings put system screen_off_timeout 1800000
echo "Ready. Run tests with:  mvn test -Dplatform=emulator -Dtest=TestRunner -Dcucumber.filter.tags=\"@registration\""
