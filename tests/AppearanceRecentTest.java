package com.gathin.tvhome;
import java.util.*;
public final class AppearanceRecentTest {
    public static void main(String[] args){
        int fallback=0xffa9ead8;
        if(AccentPalette.choose(new int[]{0xff000000,0xffffffff,0xff777777,0x00ff0000},fallback)!=fallback)throw new AssertionError("Neutral/transparent fallback");
        if(AccentPalette.choose(new int[]{0xffff0000,0xffff0000,0xff0000ff},fallback)!=0xffff7f7f)throw new AssertionError("Dominant color/readability");
        List<String> original=Arrays.asList("a","b","c","d","e","f");
        if(!RecentApps.updated(original,"c").equals(Arrays.asList("c","a","b","d","e","f")))throw new AssertionError("Deduplication");
        if(!RecentApps.updated(original,"g").equals(Arrays.asList("g","a","b","c","d","e")))throw new AssertionError("History cap");
        if(!original.get(0).equals("a"))throw new AssertionError("Input mutation");
        System.out.println("PASS: wallpaper palette fallback/color and bounded recent history.");
    }
}
