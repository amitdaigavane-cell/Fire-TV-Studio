package com.gathin.tvhome;

import java.util.regex.Pattern;

/** Recognizes only framework-originated launches of Fire OS Home. No log retention. */
final class HomeSignal {
    private static final Pattern SYSTEM_UID = Pattern.compile("\\bfrom uid (?:1000|0)(?:\\D|$)");
    static boolean matches(String line) {
        return line.contains("START u0 ")
                && line.contains("act=android.intent.action.MAIN")
                && line.contains("android.intent.category.HOME")
                && line.contains("cmp=com.amazon.tv.launcher/.ui.HomeActivity")
                && SYSTEM_UID.matcher(line).find();
    }
}
