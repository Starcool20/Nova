package com.hackathon.nova.helper;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

public class InstalledAppsHelper {

    public static List<String> getInstalledApps(Context context, boolean includeSystemApps) {
        List<String> installedApps = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageList = packageManager.getInstalledPackages(0);

        for (PackageInfo packageInfo : packageList) {
            ApplicationInfo appInfo = packageInfo.applicationInfo;
            assert appInfo != null;
            boolean isSystemApp = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;

            // Include system apps based on the flag
            if (includeSystemApps || !isSystemApp) {
                installedApps.add(appInfo.loadLabel(packageManager) + " - " + appInfo.packageName);
            }
        }
        return installedApps;
    }
}
