ROCKET ON TOP - Android project

This is an Android WebView wrapper for the ROCKET ON TOP audio studio UI.
It includes the 500% digital gain controls, EQ, mic gain, echo/reverb,
noise gate/noise maker, audio worklet processor, clipping ceiling and red
fire gaming interface.

Build:
1. Install Android Studio (includes Android SDK) and JDK 17.
2. Install Android SDK Platform 35 and Android SDK Build-Tools.
3. Open this folder in Android Studio, allow Gradle sync, then Build > Build APK(s).
   OR install Gradle and run BUILD-ANDROID.bat.
4. Debug APK output: app/build/outputs/apk/debug/app-debug.apk

Important Android limitations:
- Android chooses the active system audio output (speaker/headset/Bluetooth).
  Per-output device selection through Web Audio setSinkId is not consistently
  supported by Android WebView, so this build uses the system route.
- Audio capture and processing require microphone permission.
- Android may restrict microphone capture in the background; keep the app open.
- 500% is digital gain, not safe acoustic loudness. The processor keeps a
  digital output ceiling enabled to reduce clipping; high gain can still sound
  distorted and should be used cautiously.
- The studio unlock code remains ROCKET (as in the supplied desktop project).
