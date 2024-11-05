package com.hackathon.nova;

import android.animation.*;
import android.app.*;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import android.widget.LinearLayout;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.button.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import org.json.*;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;
import android.Manifest;
import com.hackathon.nova.service.NovaAccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import com.hackathon.nova.overlay.*;

public class MainActivity extends AppCompatActivity {

  public static int REQUEST_RECORD_AUDIO_PERMISSION = 100;
  public static int REQUEST_ACCESSIBILITY_SERVICE_PERMISSION = 200;
  public static int STORAGE_PERMISSION = 300;
  private LinearLayout linear1;
  private static MaterialButton materialbutton1;
  private static boolean listening = false;

  @Override
  protected void onCreate(Bundle _savedInstanceState) {
    super.onCreate(_savedInstanceState);
    setContentView(R.layout.activity_main);
    initialize(_savedInstanceState);
    initializeLogic();
  }

  private void initialize(Bundle _savedInstanceState) {
    linear1 = findViewById(R.id.linear1);
    materialbutton1 = findViewById(R.id.materialbutton1);

    materialbutton1.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View _view) {
            if (materialbutton1.getText().toString().equals("Disconnect")) {
              materialbutton1.setText("Connect");
              materialbutton1.setBackgroundTintList(ColorStateList.valueOf(0xFFF44336));
            } else {
              checkAndRequestPermissions();
            }
          }
        });
  }

  private void initializeLogic() {
    _init();
  }

  private void checkAndRequestPermissions() {
    // Check RECORD_AUDIO permission for SpeechRecognizer
    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
        != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(
          MainActivity.this,
          new String[] {Manifest.permission.RECORD_AUDIO},
          REQUEST_RECORD_AUDIO_PERMISSION);
    } else if (!isAccessibilityServiceEnabled(MainActivity.this, NovaAccessibilityService.class)) {
      requestAccessibilityPermission();
    } else {
      if (listening) {
        waitForListening();
      } else {
        materialbutton1.setText("Disconnect");
        materialbutton1.setBackgroundTintList(ColorStateList.valueOf(0xFF2196F3));
      }
    }
  }

  private void requestAccessibilityPermission() {
    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
    startActivityForResult(intent, REQUEST_ACCESSIBILITY_SERVICE_PERMISSION);
    Toast.makeText(this, "Please enable Accessibility Service for the app.", Toast.LENGTH_LONG)
        .show();
  }

  public static boolean isAccessibilityServiceEnabled(
      Context context, Class<?> accessibilityServiceClass) {
    int accessibilityEnabled = 0;
    final String service = context.getPackageName() + "/" + accessibilityServiceClass.getName();

    try {
      accessibilityEnabled =
          Settings.Secure.getInt(
              context.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
    } catch (Settings.SettingNotFoundException e) {
      Log.e("AccessibilityService", "Error finding setting: " + e.getMessage());
    }

    if (accessibilityEnabled == 1) {
      String enabledServices =
          Settings.Secure.getString(
              context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

      if (enabledServices != null) {
        TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
        splitter.setString(enabledServices);
        while (splitter.hasNext()) {
          String serviceName = splitter.next();
          if (serviceName.equalsIgnoreCase(service)) {
            return true;
          }
        }
      }
    }

    return false;
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    checkAndRequestPermissions();
  }

  @Override
  protected void onActivityResult(int _requestCode, int _resultCode, Intent _data) {
    super.onActivityResult(_requestCode, _resultCode, _data);
    if (_requestCode == REQUEST_ACCESSIBILITY_SERVICE_PERMISSION) {
      if (!isAccessibilityServiceEnabled(MainActivity.this, NovaAccessibilityService.class)) {
        Toast.makeText(
                this, "Accessibility permission required. Please enable Nova in the accessibility service settings.", Toast.LENGTH_LONG)
            .show();
        requestAccessibilityPermission();
      } else {
        waitForListening();
      }
    }
  }

  public void _init() {
    if (OverlayWindow.isWindowShowing) {
      materialbutton1.setBackgroundTintList(ColorStateList.valueOf(0xFF2196F3));
      materialbutton1.setText("Disconnect");
    } else {
      materialbutton1.setBackgroundTintList(ColorStateList.valueOf(0xFFF44336));
      materialbutton1.setText("Connect");
    }
    materialbutton1.setRippleColor(ColorStateList.valueOf(0xFFFFFFFF));
  }

  public boolean _isConnected() {
    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
        == PackageManager.PERMISSION_GRANTED) {

      // Check Accessibility Service permission
      if (isAccessibilityServiceEnabled(MainActivity.this, NovaAccessibilityService.class)) {
        return true;
      }
    }
    return false;
  }

  private void waitForListening() {
    materialbutton1.setText("Proccessing");
    materialbutton1.setBackgroundTintList(ColorStateList.valueOf(0xFFF44336));
  }

  public static void result() {
    materialbutton1.setText("Disconnect");
    materialbutton1.setBackgroundTintList(ColorStateList.valueOf(0xFF2196F3));
  }
}
