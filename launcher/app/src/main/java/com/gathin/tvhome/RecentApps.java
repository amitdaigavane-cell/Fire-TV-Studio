package com.gathin.tvhome;
import android.content.Context;
import java.util.*;

final class RecentApps {
    static List<String> updated(List<String> old,String pkg){LinkedHashSet<String> result=new LinkedHashSet<>();result.add(pkg);result.addAll(old);List<String> list=new ArrayList<>(result);return new ArrayList<>(list.subList(0,Math.min(6,list.size())));}
    static List<String> read(Context c){String value=StudioConfig.prefs(c).getString("recent_apps","");return value.isEmpty()?Collections.emptyList():Arrays.asList(value.split(","));}
    static void clear(Context c){StudioConfig.prefs(c).edit().remove("recent_apps").apply();}
    static void record(Context c,String pkg){
        if(pkg==null||!HomeSurface.isCatalogApp(pkg)||!StudioConfig.rememberRecentApps(c))return;
        List<String> old=read(c);if(!old.isEmpty()&&old.get(0).equals(pkg))return;
        StudioConfig.prefs(c).edit().putString("recent_apps",String.join(",",updated(old,pkg))).apply();
    }
}
