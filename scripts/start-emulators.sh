#!/usr/bin/env bash
# Boots TWO emulators so the rider and driver suites can run side by side:
#   emulator-5554  HOVR_Test    rider app   (production.hovr.customer)
#   emulator-5556  HOVR_Driver  driver app  (com.example.hovr_driver)
# Both get the GPS set to Toronto (Canada / GTA marketplace) by default; MARKET=india uses Mohali instead.
# Usage: scripts/start-emulators.sh        (HEADLESS=1 for no windows)
set -euo pipefail
SDK="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
EXTRA=""; [ "${HEADLESS:-0}" = "1" ] && EXTRA="-no-window -no-audio -no-boot-anim"
RIDER_APK="${RIDER_APK:-apps/hovr-rider-dev-v6.3.0+588-release.apk}"
DRIVER_APK="${DRIVER_APK:-apps/hovr-driver-dev-v6.2.4+592-release.apk}"
if [ "${MARKET:-canada}" = "india" ]; then LAT=30.6781; LNG=76.7230; else LAT=43.6532; LNG=-79.3832; fi

boot() { # name port
  if ! adb devices | grep -q "^emulator-$2"; then
    nohup "$SDK/emulator/emulator" -avd "$1" -port "$2" -no-snapshot-load -netdelay none -netspeed full -gpu host $EXTRA \
      > "/tmp/emulator-$2.log" 2>&1 &
    echo "Booting $1 on emulator-$2 ..."
  fi
}
ready() { # serial
  until [ "$(adb -s "$1" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do sleep 3; done
}
prep() { # serial apk pkg
  adb -s "$1" install -r "$2" >/dev/null
  for p in ACCESS_FINE_LOCATION ACCESS_COARSE_LOCATION POST_NOTIFICATIONS CAMERA ACCESS_BACKGROUND_LOCATION; do
    adb -s "$1" shell pm grant "$3" android.permission.$p 2>/dev/null || true
  done
  adb -s "$1" emu geo fix "$LNG" "$LAT" >/dev/null
  adb -s "$1" shell settings put system screen_off_timeout 1800000
  echo "$1 ready: $3"
}
boot HOVR_Test 5554
boot HOVR_Driver 5556
ready emulator-5554; ready emulator-5556
prep emulator-5554 "$RIDER_APK" production.hovr.customer
prep emulator-5556 "$DRIVER_APK" com.example.hovr_driver
echo "Both emulators are up (GPS $LAT,$LNG)."
