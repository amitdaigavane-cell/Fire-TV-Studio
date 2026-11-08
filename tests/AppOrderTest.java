package com.gathin.tvhome;
import java.util.*;
import org.json.JSONArray;
public final class AppOrderTest {
    static void equal(List<String> actual,String... expected){if(!actual.equals(Arrays.asList(expected)))throw new AssertionError(actual);}
    public static void main(String[] args){
        List<String> all=Arrays.asList("a","b","c","d");
        equal(AppOrder.ordered(all,null),"a","b","c","d");
        equal(AppOrder.ordered(all,new JSONArray().put("c").put("missing").put("c").put("a")),"c","a","b","d");
        equal(AppOrder.move(all,all,"c",-1),"a","c","b","d");
        equal(AppOrder.move(all,Arrays.asList("a","c","d"),"c",-1),"c","b","a","d");
        equal(AppOrder.move(all,all,"a",-1),"a","b","c","d");
        equal(AppOrder.move(all,all,"d",1),"a","b","c","d");
        equal(AppOrder.move(all,all,"missing",1),"a","b","c","d");
        equal(all,"a","b","c","d");
        System.out.println("PASS: library ordering, new/missing apps, visible moves and immutable input.");
    }
}
