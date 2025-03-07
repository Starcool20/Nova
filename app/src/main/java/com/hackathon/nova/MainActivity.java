package com.hackathon.nova;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.hackathon.nova.helper.PermissionHelper;
import com.hackathon.nova.overlay.OverlayWindow;
import com.hackathon.nova.service.NovaAccessibilityService;

public class MainActivity extends AppCompatActivity {

    public static int REQUEST_RECORD_AUDIO_PERMISSION = 100;
    public static int REQUEST_ACCESSIBILITY_SERVICE_PERMISSION = 200;
    public static int STORAGE_PERMISSION = 300;
    public static int CONTACT_PERMISSION = 400;
    private static MaterialButton materialbutton1;
    private static boolean listening = false;
    private LinearLayout linear1;


    public static void result() {
        materialbutton1.setText("Disconnect");
        materialbutton1.setBackgroundTintList(ColorStateList.valueOf(0xFF2196F3));
    }

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
                            setup();
                        }
                    }
                });
    }

    private void initializeLogic() {
        _init();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        setup();
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
    private void waitForListening() {
        materialbutton1.setText("Proccessing");
        materialbutton1.setBackgroundTintList(ColorStateList.valueOf(0xFFF44336));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void setup() {
        if (!PermissionHelper.isRecordAudioPermissionGranted(this)) {
            PermissionHelper.requestRecordAudioPermission(this, REQUEST_RECORD_AUDIO_PERMISSION);
        } else if (!PermissionHelper.hasContactListPermission(MainActivity.this)) {
            PermissionHelper.requestContactListPermission(MainActivity.this, CONTACT_PERMISSION);
        } else if (!PermissionHelper.isCallPermissionGranted(MainActivity.this)) {
            PermissionHelper.requestCallPermission(MainActivity.this, STORAGE_PERMISSION);
        } else if (!PermissionHelper.isAccessibilityServiceEnabled(this, NovaAccessibilityService.class)) {
            PermissionHelper.requestAccessibilityPermission(this);
            Toast.makeText(this, "Please enable accessibility service for minito", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("setup", "Done");
            Toast.makeText(this, "Please wait", Toast.LENGTH_SHORT).show();
            waitForListening();
        }
    }
}
