package com.gathin.tvhome;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

/** Cheap opaque startup composition: no files, network, animation or added delay. */
final class WelcomeScreen extends FrameLayout {
    WelcomeScreen(Context context){
        super(context);
        setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{0xff172c43,0xff101b30,0xff080f1b}));
        LinearLayout content=new LinearLayout(context);content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER);content.setPadding(dp(32),dp(24),dp(32),dp(24));
        addView(content,new LayoutParams(-1,-2,Gravity.CENTER));
        View mark=new View(context);mark.setBackground(new Mark());
        LinearLayout.LayoutParams icon=new LinearLayout.LayoutParams(dp(76),dp(76));icon.bottomMargin=dp(28);content.addView(mark,icon);
        TextView title=label("Welcome back",38,0xfff4f7fc,true);content.addView(title);
        setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
    }
    private int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private TextView label(String text,int size,int color,boolean bold){
        TextView v=new TextView(getContext());v.setText(text);v.setTextSize(size);v.setTextColor(color);
        v.setGravity(Gravity.CENTER);v.setTypeface(Typeface.create("sans-serif",bold?Typeface.BOLD:Typeface.NORMAL));return v;
    }
    private static final class Mark extends Drawable {
        final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        @Override public void draw(Canvas canvas){
            int save=canvas.save();Rect b=getBounds();canvas.translate(b.left,b.top);canvas.scale(b.width()/76f,b.height()/76f);
            paint.setColor(0xff243c51);paint.setStyle(Paint.Style.FILL);canvas.drawRoundRect(0,0,76,76,22,22,paint);
            paint.setColor(0xff9ce1d7);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2.5f);
            canvas.drawRoundRect(16,20,60,50,6,6,paint);canvas.drawLine(29,57,47,57,paint);
            paint.setStyle(Paint.Style.FILL);Path play=new Path();play.moveTo(33,28);play.lineTo(45,35);play.lineTo(33,42);play.close();canvas.drawPath(play,paint);
            canvas.restoreToCount(save);
        }
        @Override public void setAlpha(int alpha){}
        @Override public void setColorFilter(ColorFilter filter){}
        @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }
}
