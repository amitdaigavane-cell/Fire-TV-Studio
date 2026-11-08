package com.gathin.tvhome;
public class WakeRecoveryTest {
 static void check(int expected,String pkg,String cls,boolean settings,boolean wake){if(WakeRecovery.decide(pkg,cls,"com.gathin.tvhome",settings,wake)!=expected)throw new AssertionError(pkg+" "+cls);}
 public static void main(String[] args){
  if(!WakeRecovery.forceHomeOnWake(true,false))throw new AssertionError("Real sleep must retain Home recovery");
  if(WakeRecovery.forceHomeOnWake(false,false)||WakeRecovery.forceHomeOnWake(true,true)||WakeRecovery.forceHomeOnWake(false,true))throw new AssertionError("Dream dismissal/duplicate unlock must not force Home");
  check(2,"com.amazon.avod","PlaybackActivity",false,false);
  check(2,"com.netflix.ninja","PlaybackActivity",false,false);
  check(0,"com.amazon.bueller.dreams","Dream",false,false);
  check(0,"com.android.systemui","Dialog",false,false);
  check(0,"","",false,false);
check(1,"com.amazon.tv.launcher","HomeActivity",false,false);check(1,"com.amazon.tv.launcher","android.widget.FrameLayout",false,true);check(0,"com.amazon.tv.launcher","android.widget.FrameLayout",false,false);check(2,"com.amazon.tv.launcher","MainSettingsActivity",false,true);check(2,"com.amazon.tv.launcher","HomeActivity",true,true);check(1,"com.gathin.tvhome","MainActivity",false,true);check(0,"android","KeyguardActivity",false,true);check(0,"com.android.systemui","Dialog",false,true);check(0,"com.amazon.bueller.dreams","Dream",false,true);check(2,"com.netflix.ninja","MainActivity",false,true);check(2,"com.amazon.tv.settings","SettingsActivity",false,true);check(0,"","",false,true);System.out.println("PASS: 21 wake/dream recovery cases: Amazon Home, wake-only unknown Home, Settings/grace protection, own Home, temporary system windows, screensaver, external apps, missing window metadata.");}
}
