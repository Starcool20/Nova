package com.hackathon.nova.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Path;
import android.os.BatteryManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityRecord;
import com.hackathon.nova.speech.VoiceRecognizer;
import com.hackathon.nova.overlay.OverlayWindow;

public class NovaAccessibilityService extends AccessibilityService {

  private OverlayWindow window;
  private VoiceRecognizer recognize;

  @Override
  public void onAccessibilityEvent(AccessibilityEvent event) {
    // Optional: You can listen to events if you need additional context.
  }

  @Override
  public void onInterrupt() {
    // Required override, can be left empty if no specific handling needed
  }

  @Override
  public boolean onUnbind(Intent intent) {
    recognize.shutdown();
    window.destroy();
    return super.onUnbind(intent);
  }

  @Override
  protected void onServiceConnected() {
    super.onServiceConnected();

    window = new OverlayWindow(this);

    recognize = new VoiceRecognizer(this);
  }

  // Method to execute actions based on GPT-4 code
  public void executeAction(String action, String appName, String buttonText, String direction) {
    switch (action) {
      case "open_app":
        openAppByName(appName);
        break;
      case "click_button":
        clickButtonByText(buttonText);
        break;
      case "scroll":
        scrollScreen(direction);
        break;
      case "go_back":
        performGlobalAction(GLOBAL_ACTION_BACK);
        break;
      case "check_battery":
        checkBatteryPercentage();
        break;
      case "home":
        performGlobalAction(GLOBAL_ACTION_HOME);
        break;
      default:
        Log.w("MyAccessibilityService", "Unknown action: " + action);
    }
  }

  // Open an app by its name (requires package lookup)
  private void openAppByName(String appName) {
    String packageName = getPackageNameForApp(appName);
    if (packageName != null) {
      Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
      if (launchIntent != null) {
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(launchIntent);
      } else {
        Log.e("MyAccessibilityService", "Launch intent for " + appName + " not found.");
      }
    } else {
      Log.e("MyAccessibilityService", "Package name for " + appName + " not found.");
    }
  }

  // Helper method to get package name based on app name
  private String getPackageNameForApp(String appName) {
    // Ideally, GPT-4 should resolve package name, or maintain a mapping of common app names to
    // package names.
    // Placeholder: replace or expand with a dictionary of known apps and their package names.
    if (appName.equalsIgnoreCase("YouTube")) {
      return "com.google.android.youtube";
    }
    return null; // Add more mappings as needed
  }

  // Click a button with specific text
  private void clickButtonByText(String buttonText) {
    // Use Accessibility APIs to find and click button by text (this part needs further development)
    Log.i("MyAccessibilityService", "Click button with text: " + buttonText);
  }

  // Scroll the screen in a specific direction
  private void scrollScreen(String direction) {
    if (direction.equalsIgnoreCase("down")) {
      swipeDown();
    } else if (direction.equalsIgnoreCase("up")) {
      swipeUp();
    }
  }

  private void checkBatteryPercentage() {
    final int batteryPercentage = getBatteryPercentage(this);
  }

  private int getBatteryPercentage(Context context) {
    IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    Intent batteryStatus = context.registerReceiver(null, intentFilter);

    if (batteryStatus != null) {
      int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
      int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

      if (level != -1 && scale != -1) {
        return (int) ((level / (float) scale) * 100);
      }
    }
    return -1; // Returns -1 if battery info is not available
  }

  private void swipeUp() {
    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
    float startX = displayMetrics.widthPixels / 2f;
    float startY = displayMetrics.heightPixels * 0.8f; // Start near the bottom of the screen
    float endX = displayMetrics.widthPixels / 2f;
    float endY = displayMetrics.heightPixels * 0.2f; // End near the top of the screen
    performSwipe(startX, startY, endX, endY, 500); // Adjust duration if needed
  }

  public void swipeDown() {
    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
    float startX = displayMetrics.widthPixels / 2f;
    float startY = displayMetrics.heightPixels * 0.2f; // Start near the top of the screen
    float endX = displayMetrics.widthPixels / 2f;
    float endY = displayMetrics.heightPixels * 0.8f; // End near the bottom of the screen
    performSwipe(startX, startY, endX, endY, 500); // Adjust duration if needed
  }

  private void performSwipe(float startX, float startY, float endX, float endY, long duration) {
    Path path = new Path();
    path.moveTo(startX, startY);
    path.lineTo(endX, endY);

    GestureDescription.StrokeDescription strokeDescription =
        new GestureDescription.StrokeDescription(path, 0, duration);
    GestureDescription gestureDescription =
        new GestureDescription.Builder().addStroke(strokeDescription).build();

    dispatchGesture(gestureDescription, null, null);
  }
}
