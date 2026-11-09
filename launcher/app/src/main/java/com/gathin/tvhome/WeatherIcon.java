package com.gathin.tvhome;

import android.graphics.*;
import android.graphics.drawable.Drawable;

/** Small vector weather icons drawn with Canvas, keyed by WeatherInfo.scene(). */
final class WeatherIcon extends Drawable {
    private static final int SUN=0xffffd36a, CLOUD=0xffe8eef8, DARK_CLOUD=0xffc9d2e2, RAIN=0xff8fc6ff, SNOW=0xffbfe0ff, BOLT=0xffffd36a, MOON=0xfff3eed8;
    private static final PorterDuffXfermode CUT=new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    private final Paint fill=new Paint(Paint.ANTI_ALIAS_FLAG), line=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path();
    private String scene="clear";

    WeatherIcon(){line.setStyle(Paint.Style.STROKE);line.setStrokeCap(Paint.Cap.ROUND);}
    void scene(String value){if(value==null)value="clear";if(!value.equals(scene)){scene=value;invalidateSelf();}}

    @Override public void draw(Canvas c){
        Rect b=getBounds();if(b.isEmpty())return;
        float s=Math.min(b.width(),b.height())/24f;
        int save=c.save();c.translate(b.left+(b.width()-24*s)/2f,b.top+(b.height()-24*s)/2f);c.scale(s,s);
        line.setStrokeWidth(1.6f);
        switch(scene){
            case "clear": sun(c,12,12,4.2f,true);break;
            case "partly": sun(c,9,9,3.6f,false);cloud(c,9,19,CLOUD,.85f);break;
            case "clear-night": moon(c,12,12,6.2f);break;
            case "partly-night": moon(c,9,9,4.4f);cloud(c,9,19,CLOUD,.85f);break;
            case "cloud": cloud(c,7,18,CLOUD,1f);break;
            case "fog":
                line.setColor(CLOUD);line.setStrokeWidth(1.8f);
                c.drawLine(4,9,20,9,line);c.drawLine(6,13,18,13,line);c.drawLine(4,17,20,17,line);break;
            case "rain":
                cloud(c,7,14,CLOUD,.95f);line.setColor(RAIN);
                c.drawLine(8,17,7,20,line);c.drawLine(12,17,11,20,line);c.drawLine(16,17,15,20,line);break;
            case "storm":
                cloud(c,7,14,DARK_CLOUD,.95f);fill.setColor(BOLT);path.reset();
                path.moveTo(12.5f,13);path.lineTo(10,17);path.lineTo(12.4f,17);path.lineTo(10.8f,21);path.lineTo(15,15.6f);path.lineTo(12.5f,15.6f);path.lineTo(14,13);path.close();c.drawPath(path,fill);break;
            case "snow":
                cloud(c,7,14,CLOUD,.95f);fill.setColor(SNOW);
                c.drawCircle(8,18,1.2f,fill);c.drawCircle(12,20,1.2f,fill);c.drawCircle(16,18,1.2f,fill);break;
            case "blizzard":
                cloud(c,7,13,CLOUD,.95f);line.setColor(CLOUD);line.setStrokeWidth(1.4f);
                c.drawLine(3,16,12,16,line);c.drawLine(6,19,18,19,line);c.drawLine(14,16,20,16,line);
                fill.setColor(SNOW);c.drawCircle(9,21.5f,1f,fill);c.drawCircle(17,21.5f,1f,fill);break;
            default: sun(c,12,12,4.2f,true);
        }
        c.restoreToCount(save);
    }
    private void sun(Canvas c,float x,float y,float r,boolean rays){
        fill.setColor(SUN);c.drawCircle(x,y,r,fill);
        if(!rays)return;
        line.setColor(SUN);
        for(int i=0;i<8;i++){double a=Math.PI/4*i;float x1=(float)(x+Math.cos(a)*(r+2.3)),y1=(float)(y+Math.sin(a)*(r+2.3)),x2=(float)(x+Math.cos(a)*(r+4.4)),y2=(float)(y+Math.sin(a)*(r+4.4));c.drawLine(x1,y1,x2,y2,line);}
    }
    /** Crescent moon: a pale disc with a second disc cut out of it. */
    private void moon(Canvas c,float x,float y,float r){
        int layer=c.saveLayer(x-r-2,y-r-2,x+r+2,y+r+2,null);
        fill.setColor(MOON);c.drawCircle(x,y,r,fill);
        fill.setXfermode(CUT);c.drawCircle(x+r*.55f,y-r*.35f,r*.85f,fill);fill.setXfermode(null);
        c.restoreToCount(layer);
    }
    /** A cloud whose flat base sits at baseY, starting near x. */
    private void cloud(Canvas c,float x,float baseY,int color,float scale){
        fill.setColor(color);
        c.save();c.translate(x,baseY);c.scale(scale,scale);
        c.drawCircle(3.2f,-3.4f,3.4f,fill);c.drawCircle(7.6f,-6f,4.6f,fill);c.drawCircle(11.6f,-3.6f,3.6f,fill);
        c.drawRect(3.2f,-3.6f,11.6f,0f,fill);c.drawCircle(3.2f,-3.4f,3.4f,fill);
        c.restore();
    }
    @Override public void setAlpha(int alpha){fill.setAlpha(alpha);line.setAlpha(alpha);}
    @Override public void setColorFilter(ColorFilter filter){fill.setColorFilter(filter);line.setColorFilter(filter);}
    @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
