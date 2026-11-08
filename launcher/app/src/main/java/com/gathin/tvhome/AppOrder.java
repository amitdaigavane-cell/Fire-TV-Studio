package com.gathin.tvhome;
import java.util.*;
import org.json.JSONArray;

final class AppOrder {
    static List<String> ordered(List<String> alphabetical,JSONArray saved){
        Set<String> installed=new HashSet<>(alphabetical);LinkedHashSet<String> result=new LinkedHashSet<>();
        if(saved!=null)for(int i=0;i<saved.length();i++){String pkg=saved.optString(i);if(installed.contains(pkg))result.add(pkg);}
        result.addAll(alphabetical);return new ArrayList<>(result);
    }
    static List<String> move(List<String> all,List<String> visible,String pkg,int direction){
        ArrayList<String> result=new ArrayList<>(all);int at=visible.indexOf(pkg),next=at+direction;
        if((direction!=1&&direction!=-1)||at<0||next<0||next>=visible.size())return result;
        int from=result.indexOf(pkg),to=result.indexOf(visible.get(next));
        if(from>=0&&to>=0)Collections.swap(result,from,to);return result;
    }
}
