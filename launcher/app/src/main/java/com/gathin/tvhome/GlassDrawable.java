package com.gathin.tvhome;

import android.graphics.*;
import android.graphics.drawable.Drawable;

/** Clear glass: a faint diagonal sheen, a bright top highlight and a thin light rim. No blur, so whatever is behind reads through. */
final class GlassDrawable extends Drawable {
    private final float radius, density;
    private final boolean focus;
    private final Paint sheen=new Paint(Paint.ANTI_ALIAS_FLAG), shine=new Paint(Paint.ANTI_ALIAS_FLAG), rim=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path clip=new Path();
    private final RectF edge=new RectF();

    GlassDrawable(float radiusPx,float density){this(radiusPx,density,false);}
    /** focus=true: brighter sheen and rim, used for the selected control. */
    GlassDrawable(float radiusPx,float density,boolean focus){
        this.radius=radiusPx;this.density=density;this.focus=focus;
        rim.setStyle(Paint.Style.STROKE);rim.setStrokeWidth(Math.max(1f,density*(focus?1.5f:1f)));rim.setColor(focus?0xd9ffffff:0x66ffffff);
    }
    @Override protected void onBoundsChange(Rect b){
        clip.reset();clip.addRoundRect(new RectF(b),radius,radius,Path.Direction.CW);
        sheen.setShader(new LinearGradient(b.left,b.top,b.right,b.bottom,focus?new int[]{0x42ffffff,0x18ffffff,0x2affffff}:new int[]{0x1cffffff,0x06ffffff,0x12ffffff},new float[]{0f,.45f,1f},Shader.TileMode.CLAMP));
        shine.setShader(new LinearGradient(0,b.top,0,b.top+radius*1.2f,focus?0x66ffffff:0x3dffffff,0x00ffffff,Shader.TileMode.CLAMP));
        edge.set(b);edge.inset(rim.getStrokeWidth()/2,rim.getStrokeWidth()/2);
    }
    @Override public void draw(Canvas canvas){
        Rect b=getBounds();if(b.isEmpty())return;
        int save=canvas.save();canvas.clipPath(clip);
        canvas.drawRect(b,sheen);canvas.drawRect(b.left,b.top,b.right,b.top+radius*1.2f,shine);
        canvas.restoreToCount(save);
        canvas.drawRoundRect(edge,radius,radius,rim);
    }
    @Override public void setAlpha(int alpha){}
    @Override public void setColorFilter(ColorFilter filter){}
    @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
