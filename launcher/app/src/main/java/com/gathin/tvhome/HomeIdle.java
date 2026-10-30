package com.gathin.tvhome;

import android.graphics.Color;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Runs only in the focused Home surface. No service, media polling or global timeout changes. */
final class HomeIdle {
    private final HomeSurface home;
    private boolean active, consumeKey;
    private long touched;
    private FrameLayout shade;
    private TextView clock;
    private int position;
    private final Runnable tick=this::tick;
    HomeIdle(HomeSurface home){this.home=home;}
    void active(boolean value){
        if(active==value)return;
        active=value;home.setKeepScreenOn(value);
        if(value){touched=SystemClock.uptimeMillis();home.postDelayed(tick,HomeIdlePolicy.DIM_MS);}
        else stop();
    }
    void stop(){
        active=false;home.setKeepScreenOn(false);home.removeCallbacks(tick);hide();
    }
    boolean key(KeyEvent e){
        if(e.getAction()==KeyEvent.ACTION_DOWN){
            if(shade!=null)consumeKey=true;
            hide();touched=SystemClock.uptimeMillis();home.removeCallbacks(tick);
            if(active)home.postDelayed(tick,HomeIdlePolicy.DIM_MS);
        }
        boolean result=consumeKey;
        if(e.getAction()==KeyEvent.ACTION_UP)consumeKey=false;
        return result;
    }
    private void tick(){
        if(!active||!home.hasWindowFocus()||home.getWindowVisibility()!=View.VISIBLE)return;
        int action=HomeIdlePolicy.action(SystemClock.uptimeMillis()-touched);
        if(action==HomeIdlePolicy.SLEEP){
            // Releasing KEEP_SCREEN_ON is also the fallback if OS sleep is unavailable.
            stop();HomeRedirectService.sleep();return;
        }
        if(action==HomeIdlePolicy.DIM){
            if(shade==null){
                shade=new FrameLayout(home.getContext());shade.setBackgroundColor(Color.BLACK);
                shade.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
                clock=new TextView(home.getContext());clock.setTextColor(0xff444444);clock.setTextSize(22);
                shade.addView(clock,new FrameLayout.LayoutParams(-2,-2));
                home.addView(shade,new FrameLayout.LayoutParams(-1,-1));
            }
            clock.setText(new SimpleDateFormat("HH:mm",Locale.getDefault()).format(new Date()));
            // A single position change per minute, no frame-by-frame animation or bright logo.
            int[][] spots={{1,1},{7,5},{3,7},{6,2},{1,5},{5,7}};int[] spot=spots[position++%spots.length];
            clock.setTranslationX(Math.max(0,home.getWidth()-180)*spot[0]/8f);
            clock.setTranslationY(Math.max(0,home.getHeight()-80)*spot[1]/8f);
        }
        home.postDelayed(tick,60000);
    }
    private void hide(){if(shade!=null){home.removeView(shade);shade=null;clock=null;}}
}
