package com.gathin.tvhome;
public final class LiveTvKeyPolicyTest {
    static int cases;
    static void check(int actual,int expected){cases++;if(actual!=expected)throw new AssertionError("Live TV case "+cases);}
    public static void main(String[] args){
        LiveTvKeyPolicy p=new LiveTvKeyPolicy();
        for(int key=0;key<400;key++)if(key!=170&&key!=172){
            check(p.event(key,0,0,1,true,true,false),0);check(p.event(key,1,0,1,true,true,false),0);
        }
        for(int key:new int[]{170,172}){
            check(p.event(key,0,0,10,false,true,false),0);
            check(p.event(key,0,0,10,true,false,false),0);
            check(p.event(key,1,0,10,true,true,false),0);
            check(p.event(key,0,0,20,true,true,false),1);
            for(int repeat=1;repeat<5;repeat++)check(p.event(key,0,repeat,20,true,true,false),1);
            check(p.event(key,1,0,20,true,true,false),2);
            check(p.event(key,1,0,20,true,true,false),0);
            check(p.event(key,0,1,30,true,true,false),0);
            check(p.event(key,0,0,40,true,true,false),1);
            check(p.event(key,1,0,40,true,true,true),1);
            check(p.event(key,0,0,50,true,true,false),1);
            check(p.event(key,1,0,50,false,true,false),1);
            check(p.event(key,0,0,60,true,true,false),1);
            check(p.event(key,1,0,60,true,false,false),1);
        }
        System.out.println("PASS: "+cases+" Live TV key policy cases; other buttons pass through.");
    }
}
