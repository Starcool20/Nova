package com.hackathon.nova.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.hackathon.nova.database.DatabaseHelper;
import com.hackathon.nova.helper.ContactListHelper;
import com.hackathon.nova.helper.InstalledAppsHelper;
import com.hackathon.nova.overlay.OverlayWindow;
import com.hackathon.nova.speech.VoiceRecognizer;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

        getAllInstalledApps();
        getAllContacts();
    }

    private void getAllInstalledApps() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Log.d("ExecutorService", "Background Task Running");

                List<String> allApps = InstalledAppsHelper.getInstalledApps(NovaAccessibilityService.this, true);
                final DatabaseHelper databaseHelper = new DatabaseHelper(NovaAccessibilityService.this);
                StringBuilder sb = new StringBuilder();
                for (String app : allApps) {
                    Log.d("Installed Apps", app);
                    sb.append(app).append("\n");
                }

                databaseHelper.insertData(sb.toString(), "installed_apps");
                databaseHelper.closeDatabase();
            }
        });
        executor.shutdown();  // Shutdown after execution
    }

    private void getAllContacts() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<String> contactList = ContactListHelper.fetchContacts(NovaAccessibilityService.this);
                final DatabaseHelper databaseHelper = new DatabaseHelper(NovaAccessibilityService.this);
                StringBuilder sb = new StringBuilder();
                for (String name : contactList) {
                    Log.d("Contact name ", name);
                    sb.append(name).append("\n");
                }

                databaseHelper.insertData(sb.toString(), "contact_list");
                databaseHelper.closeDatabase();
            }
        });
        executor.shutdown();  // Shutdown after execution
    }

    public void goHome() {
        NovaAccessibilityService.this.performGlobalAction(GLOBAL_ACTION_HOME);
    }
}
