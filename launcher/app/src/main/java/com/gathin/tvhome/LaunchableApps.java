package com.gathin.tvhome;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** User-launchable apps only, including preinstalled apps and ordinary launcher entries. */
final class LaunchableApps {
    static List<ResolveInfo> query(PackageManager pm, String ownPackage) {
        Map<String, ResolveInfo> apps = new LinkedHashMap<>();
        // Prefer the TV entry point when a package advertises both categories.
        for (String category : new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER}) {
            for (ResolveInfo result : pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(category), 0)) {
                ActivityInfo activity = result.activityInfo;
                if (activity == null || !activity.exported || activity.applicationInfo == null) continue;
                String pkg = activity.packageName;
                if (pkg == null || pkg.equals(ownPackage) || pkg.equals("com.amazon.tv.launcher")) continue;
                // PackageManager's default query excludes disabled components, while still
                // honoring explicit enable overrides for a manifest-disabled component.
                if (!apps.containsKey(pkg)) apps.put(pkg, result);
            }
        }
        List<ResolveInfo> result = new ArrayList<>(apps.values());
        result.sort((a, b) -> {
            int byName = a.loadLabel(pm).toString().compareToIgnoreCase(b.loadLabel(pm).toString());
            return byName != 0 ? byName : a.activityInfo.packageName.compareTo(b.activityInfo.packageName);
        });
        return result;
    }
}
