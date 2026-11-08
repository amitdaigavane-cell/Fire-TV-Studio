package com.gathin.tvhome;

/** Only TV/Guide keys. Never consumes navigation, Home, power or playback keys. */
final class LiveTvKeyPolicy {
    static final int PASS=0, CONSUME=1, LAUNCH=2;
    private int held=-1;
    private long started;
    static boolean supported(int key){return key==170||key==172;} // KEYCODE_TV / KEYCODE_GUIDE
    int event(int key,int action,int repeats,long downTime,boolean enabled,boolean available,boolean canceled){
        if(!supported(key))return PASS;
        if(action==0){
            if(held==key&&started==downTime)return CONSUME;
            if(repeats!=0||!enabled||!available)return PASS;
            held=key;started=downTime;return CONSUME;
        }
        if(action==1&&held==key&&started==downTime){
            held=-1;return enabled&&available&&!canceled?LAUNCH:CONSUME;
        }
        return PASS;
    }
}
