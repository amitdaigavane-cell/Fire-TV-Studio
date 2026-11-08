package com.gathin.tvhome;
import java.util.*;
public final class AppearanceRecentTest {
    public static void main(String[] args){
        List<String> original=Arrays.asList("a","b","c","d","e","f");
        if(!RecentApps.updated(original,"c").equals(Arrays.asList("c","a","b","d","e","f")))throw new AssertionError("Deduplication");
        if(!RecentApps.updated(original,"g").equals(Arrays.asList("g","a","b","c","d","e")))throw new AssertionError("History cap");
        if(!original.get(0).equals("a"))throw new AssertionError("Input mutation");
        System.out.println("PASS: bounded recent history.");
    }
}
