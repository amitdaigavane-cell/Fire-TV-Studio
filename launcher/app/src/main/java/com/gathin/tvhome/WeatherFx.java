package com.gathin.tvhome;

import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.view.View;
import java.util.Random;

/**
 * Idle-screen weather: snow, blizzard, rain, storm, fog, drifting cloud, sun glare on clear days
 * and a moon with twinkling stars on clear nights.
 * Runs only while attached and visible on the idle screen, at about 30 frames a second,
 * with fixed particle pools.
 */
final class WeatherFx extends View {
    private static final long FRAME_MS=33;
    private final Random rnd=new Random();
    private final Paint fill=new Paint(Paint.ANTI_ALIAS_FLAG), stroke=new Paint(Paint.ANTI_ALIAS_FLAG), haze=new Paint();
    private final Path bolt=new Path();
    private final float density;
    private String mode="clear";
    private boolean animate=true;
    private long last, nextBolt;
    private float flash, gust;
    // Particle pool: x, y, size, speed, phase (meaning varies by mode)
    private float[] px=new float[0], py=new float[0], ps=new float[0], pv=new float[0], pp=new float[0];
    private RadialGradient puff;
    private final RadialGradient glow=new RadialGradient(0,0,1,new int[]{0xffffffff,0x66ffffff,0x00ffffff},new float[]{0f,.35f,1f},Shader.TileMode.CLAMP);
    private final Paint rays=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Matrix matrix=new Matrix();

    WeatherFx(Context c){
        super(c);density=getResources().getDisplayMetrics().density;
        stroke.setStyle(Paint.Style.STROKE);stroke.setStrokeCap(Paint.Cap.ROUND);rays.setColor(0xfffff1c4);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);setWillNotDraw(false);
    }

    void mode(String scene,boolean animations){
        if(scene==null)scene="clear";animate=animations;
        if(scene.equals(mode)&&px.length>0)return;
        mode=scene;seed();invalidate();
    }

    private void seed(){
        int w=Math.max(1,getWidth()),h=Math.max(1,getHeight());
        int n;
        switch(mode){case "snow":n=80;break;case "blizzard":n=180;break;case "rain":n=110;break;case "storm":n=170;break;case "fog":n=12;break;case "cloud":n=8;break;case "clear-night":case "partly-night":n=70;break;default:n=0;}
        px=new float[n];py=new float[n];ps=new float[n];pv=new float[n];pp=new float[n];
        for(int i=0;i<n;i++){
            px[i]=rnd.nextFloat()*w*1.15f-w*.1f;py[i]=rnd.nextFloat()*h;pp[i]=rnd.nextFloat()*6.28f;
            switch(mode){
                case "snow":ps[i]=(.8f+rnd.nextFloat()*2.2f)*density;pv[i]=(18+rnd.nextFloat()*30)*density;break;
                case "blizzard":ps[i]=(.7f+rnd.nextFloat()*2.4f)*density;pv[i]=(120+rnd.nextFloat()*160)*density;break;
                case "rain":ps[i]=(10+rnd.nextFloat()*16)*density;pv[i]=(420+rnd.nextFloat()*240)*density;break;
                case "storm":ps[i]=(12+rnd.nextFloat()*16)*density;pv[i]=(620+rnd.nextFloat()*240)*density;break;
                case "fog":py[i]=h*(.33f+(i%3)*.2f)+rnd.nextFloat()*h*.17f;ps[i]=(520+rnd.nextFloat()*420)*density;pv[i]=(4+(i%3)*5)*density;break;
                case "cloud":py[i]=h*(.04f+rnd.nextFloat()*.35f);ps[i]=(.7f+rnd.nextFloat()*.9f)*density;pv[i]=(5+rnd.nextFloat()*9)*density;break;
                case "clear-night":case "partly-night":px[i]=rnd.nextFloat()*w;py[i]=rnd.nextFloat()*h*.62f;ps[i]=(.5f+rnd.nextFloat()*1.3f)*density;pv[i]=.6f+rnd.nextFloat()*1.6f;break;
            }
        }
        puff=new RadialGradient(0,0,1,new int[]{0xffe4eaf4,0x8ce4eaf4,0x00e4eaf4},new float[]{0f,.6f,1f},Shader.TileMode.CLAMP);
        flash=0;gust=0;last=0;nextBolt=SystemClock.uptimeMillis()+1500+rnd.nextInt(3000);
    }

    @Override protected void onSizeChanged(int w,int h,int ow,int oh){haze.setShader(new LinearGradient(0,h*.4f,0,h,0x00dce4f0,0x52dce4f0,Shader.TileMode.CLAMP));seed();}
    @Override protected void onDetachedFromWindow(){super.onDetachedFromWindow();last=0;}

    @Override protected void onDraw(Canvas c){
        int w=getWidth(),h=getHeight();if(w==0||h==0)return;
        long now=SystemClock.uptimeMillis();
        float dt=last==0?0:Math.min(.05f,(now-last)/1000f);last=now;if(!animate)dt=0;
        switch(mode){
            case "snow":
                fill.setColor(0xd9ffffff);
                for(int i=0;i<px.length;i++){py[i]+=pv[i]*dt;pp[i]+=dt;px[i]+=(float)Math.sin(pp[i])*12*density*dt;if(py[i]>h+5){py[i]=-5;px[i]=rnd.nextFloat()*w;}c.drawCircle(px[i],py[i],ps[i],fill);}
                break;
            case "blizzard":{
                gust+=dt;float wind=(260+(float)Math.sin(gust*.7)*140+(float)Math.sin(gust*2.3)*60)*density;
                c.drawColor(0x29ebf2fc);
                stroke.setColor(0xccffffff);
                for(int i=0;i<px.length;i++){float drift=.5f+(i%5)*.16f;px[i]+=wind*drift*dt;py[i]+=pv[i]*dt+(float)Math.sin(pp[i]+=dt*3)*20*density*dt;
                    if(px[i]>w+20||py[i]>h+10){px[i]=rnd.nextFloat()*w*1.15f-w*.25f;py[i]=rnd.nextBoolean()?rnd.nextFloat()*h:-10;}
                    stroke.setStrokeWidth(ps[i]*1.5f);c.drawLine(px[i],py[i],px[i]-wind*drift*.025f,py[i]-pv[i]*.02f,stroke);}
                for(int k=0;k<3;k++)drawPuff(c,((gust*90*density*(k+1)+k*400*density)%(w+340*density))-170*density,h*(.26f+k*.28f),240*density,1f,.10f);
                break;}
            case "rain": case "storm":{
                boolean storm=mode.equals("storm");float slant=storm?.32f:.18f;
                if(storm)c.drawColor(0x470a0e1e);
                stroke.setColor(storm?0x80cddcff:0x73c8dcff);stroke.setStrokeWidth((storm?1.3f:1.1f)*density);
                for(int i=0;i<px.length;i++){py[i]+=pv[i]*dt;px[i]-=pv[i]*slant*dt;if(py[i]>h+5){py[i]=-20*density;px[i]=rnd.nextFloat()*w*1.2f;}c.drawLine(px[i],py[i],px[i]-ps[i]*slant,py[i]+ps[i],stroke);}
                if(storm&&animate){
                    if(now>nextBolt){flash=1;nextBolt=now+2500+rnd.nextInt(5000);makeBolt(w,h);}
                    if(flash>0){
                        c.drawColor(Color.argb((int)(flash*.45f*255),225,232,255));
                        if(flash>.5f){stroke.setColor(Color.argb((int)(flash*255),255,255,255));stroke.setStrokeWidth(2.2f*density);c.drawPath(bolt,stroke);}
                        flash-=dt*(flash>.6f?2.2f:3.5f);if(flash<.35f&&flash>.3f&&rnd.nextBoolean())flash=.8f;
                    }
                }
                break;}
            case "fog":
                for(int i=0;i<px.length;i++){px[i]+=pv[i]*dt;pp[i]+=dt*.25f;if(px[i]-ps[i]/2>w)px[i]=-ps[i]/2;
                    float a=(.13f+(i%3)*.04f)*(.75f+.25f*(float)Math.sin(pp[i]));
                    c.save();c.translate(px[i],py[i]);c.scale(1,.12f+(i%4)*.02f);drawPuff(c,0,0,ps[i]/2,1f,a);c.restore();}
                c.drawRect(0,h*.4f,w,h,haze);
                break;
            case "cloud":
                c.drawColor(0x1a1e2434);
                for(int i=0;i<px.length;i++){px[i]+=pv[i]*dt;if(px[i]-200*ps[i]>w)px[i]=-260*ps[i];
                    for(int k=0;k<5;k++){float ox=((k*37+i*11)%170-85)*ps[i],oy=((k*23+i*7)%34-17)*ps[i],r=(40+(k*13+i*5)%44)*ps[i];drawPuff(c,px[i]+ox,py[i]+oy,r,1f,.14f);}}
                break;
            case "clear": case "partly":{
                // Sun glare: a warm light leak from the top-left corner, slow-turning rays and a few lens flares.
                gust+=dt;float k=mode.equals("partly")?.6f:1f;float pulse=.88f+.12f*(float)Math.sin(gust*.5f);
                float sx=w*.1f,sy=-h*.06f;
                drawGlow(c,sx,sy,w*.62f,0xfff1c4,.42f*k*pulse);drawGlow(c,sx,sy,w*.22f,0xfffbe8,.55f*k*pulse);
                c.save();c.rotate(gust*1.6f,sx,sy);
                for(int i=0;i<12;i++){c.save();c.rotate(i*30+(i%3)*7,sx,sy);rays.setAlpha((int)(255*.05f*k*(.7f+.3f*(float)Math.sin(gust*.8f+i))));c.drawRect(sx,sy-w*.006f,sx+w*.9f,sy+w*.006f,rays);c.restore();}
                c.restore();
                float fx=w*.72f-sx,fy=h*.78f-sy;
                float[] at={.28f,.47f,.64f,.86f};float[] rad={.018f,.045f,.028f,.07f};
                for(int i=0;i<at.length;i++)drawGlow(c,sx+fx*at[i],sy+fy*at[i],w*rad[i],i%2==0?0xfff3d9:0xd8e8ff,.16f*k*pulse);
                break;}
            case "clear-night": case "partly-night":{
                // Moon: a soft halo, a pale disc with faint maria, and twinkling stars.
                gust+=dt;float k=mode.equals("partly-night")?.7f:1f;
                fill.setColor(0xffffffff);
                for(int i=0;i<px.length;i++){pp[i]+=dt*pv[i];fill.setAlpha((int)(255*k*(.25f+.55f*(.5f+.5f*(float)Math.sin(pp[i])))));c.drawCircle(px[i],py[i],ps[i],fill);}
                fill.setAlpha(255);
                float mx=w*.26f,my=h*.2f,mr=Math.min(w,h)*.055f;
                drawGlow(c,mx,my,mr*6.5f,0xcfe0ff,.22f*k);drawGlow(c,mx,my,mr*2.6f,0xeef3ff,.35f*k);
                fill.setColor(0xfff4f1e4);fill.setAlpha((int)(255*.92f*k));c.drawCircle(mx,my,mr,fill);
                fill.setColor(0xffd9d6c8);fill.setAlpha((int)(255*.5f*k));
                c.drawCircle(mx-mr*.32f,my-mr*.2f,mr*.24f,fill);c.drawCircle(mx+mr*.25f,my+mr*.28f,mr*.18f,fill);c.drawCircle(mx+mr*.1f,my-mr*.42f,mr*.12f,fill);
                fill.setAlpha(255);
                break;}
            default: return;
        }
        if(animate&&isShown())postInvalidateDelayed(FRAME_MS);
    }

    private final android.util.SparseArray<PorterDuffColorFilter> tints=new android.util.SparseArray<>();
    private PorterDuffColorFilter tint(int rgb){PorterDuffColorFilter f=tints.get(rgb);if(f==null){f=new PorterDuffColorFilter(0xff000000|rgb,PorterDuff.Mode.MULTIPLY);tints.put(rgb,f);}return f;}
    private void drawGlow(Canvas c,float x,float y,float r,int rgb,float alpha){
        matrix.setScale(r,r);matrix.postTranslate(x,y);glow.setLocalMatrix(matrix);
        fill.setShader(glow);fill.setColorFilter(tint(rgb));fill.setAlpha(Math.max(0,Math.min(255,(int)(alpha*255))));
        c.drawCircle(x,y,r,fill);
        fill.setShader(null);fill.setColorFilter(null);fill.setAlpha(255);
    }
    private void drawPuff(Canvas c,float x,float y,float r,float sy,float alpha){
        matrix.setScale(r,r*sy);matrix.postTranslate(x,y);puff.setLocalMatrix(matrix);
        fill.setShader(puff);fill.setAlpha(Math.max(0,Math.min(255,(int)(alpha*255))));
        c.drawRect(x-r,y-r*sy,x+r,y+r*sy,fill);
        fill.setShader(null);fill.setAlpha(255);
    }
    private void makeBolt(int w,int h){
        bolt.reset();float x=w*(.2f+rnd.nextFloat()*.6f),y=0;bolt.moveTo(x,y);
        float end=h*(.55f+rnd.nextFloat()*.22f);
        while(y<end){x+=(rnd.nextFloat()-.5f)*50*density;y+=(20+rnd.nextFloat()*25)*density;bolt.lineTo(x,y);}
    }
}
