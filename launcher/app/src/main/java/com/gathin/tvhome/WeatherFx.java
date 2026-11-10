package com.gathin.tvhome;

import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.view.View;
import java.util.Random;

/**
 * Idle-screen weather: snow, blizzard, rain, storm, fog, drifting cloud, a sun glare with soft
 * light rays on clear days, and moonlight from off screen with twinkling stars, stars that flare
 * now and then and a shooting star every 60 seconds on clear nights. Clear-sky effects are toned
 * down on bright wallpapers (see backdrop()).
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
    private final Paint ray=new Paint(Paint.ANTI_ALIAS_FLAG), spike=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path wedge=new Path();
    private LinearGradient rayShade;
    private float rayLength, gain=1f;
    // Ray set (clear day): base angle, half-width, alpha, phase, drift speed, drift range
    private final float[] ra=new float[10], rw=new float[10], ral=new float[10], rph=new float[10], rsp=new float[10], rdr=new float[10];
    // Stars (clear night): brightness and tint index per star; flares and the shooting star
    private float[] pb=new float[0];private int[] pc=new int[0];
    private static final int[] STAR_TINT={0xffffff,0xffffff,0xfff4e2,0xe2ecff};
    private final int[] flareStar={-1,-1,-1};private final float[] flareLife=new float[3], flareDur=new float[3];
    private long nextFlare, nextShoot;
    private float shootX, shootY, shootVx, shootVy, shootLife=-1;

    WeatherFx(Context c){
        super(c);density=getResources().getDisplayMetrics().density;
        stroke.setStyle(Paint.Style.STROKE);stroke.setStrokeCap(Paint.Cap.ROUND);rays.setColor(0xfffff1c4);
        spike.setStyle(Paint.Style.STROKE);spike.setStrokeCap(Paint.Cap.ROUND);
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
        switch(mode){case "snow":n=80;break;case "blizzard":n=180;break;case "rain":n=110;break;case "storm":n=170;break;case "fog":n=12;break;case "cloud":n=8;break;case "clear-night":case "partly-night":n=85;break;default:n=0;}
        px=new float[n];py=new float[n];ps=new float[n];pv=new float[n];pp=new float[n];
        for(int i=0;i<n;i++){
            px[i]=rnd.nextFloat()*w*1.15f-w*.1f;py[i]=rnd.nextFloat()*h;pp[i]=rnd.nextFloat()*6.28f;
            switch(mode){
                case "snow":ps[i]=(.8f+rnd.nextFloat()*2.2f)*density;pv[i]=(18+rnd.nextFloat()*30)*density;break;
                case "blizzard":ps[i]=(.7f+rnd.nextFloat()*2.4f)*density;pv[i]=(120+rnd.nextFloat()*160)*density;break;
                case "rain":ps[i]=(10+rnd.nextFloat()*16)*density;pv[i]=(420+rnd.nextFloat()*240)*density;break;
                case "storm":ps[i]=(12+rnd.nextFloat()*16)*density;pv[i]=(620+rnd.nextFloat()*240)*density;break;
                case "fog":py[i]=h*(.33f+(i%3)*.2f)+rnd.nextFloat()*h*.17f;ps[i]=(520+rnd.nextFloat()*420)*density;pv[i]=(16+(i%3)*14)*(rnd.nextBoolean()?1f:.75f)*density;break;
                case "cloud":py[i]=h*(.04f+rnd.nextFloat()*.35f);ps[i]=(.7f+rnd.nextFloat()*.9f)*density;pv[i]=(5+rnd.nextFloat()*9)*density;break;
                case "clear-night":case "partly-night":{
                    // Mostly in the upper sky, never behind the weather card (top right) or the clock (lower left).
                    float x,y;do{x=rnd.nextFloat()*w;y=(float)Math.pow(rnd.nextFloat(),1.4)*h*.62f;}while((x>w*.6f&&y<h*.25f)||(x<w*.42f&&y>h*.4f));
                    px[i]=x;py[i]=y;boolean big=rnd.nextFloat()<.12f;ps[i]=(big?1.3f+rnd.nextFloat()*.7f:.45f+rnd.nextFloat()*.6f)*density;pv[i]=.4f+rnd.nextFloat()*1.1f;break;}
            }
        }
        pb=new float[n];pc=new int[n];
        for(int i=0;i<n;i++){pb[i]=ps[i]>1.25f*density?1f:.55f+rnd.nextFloat()*.35f;pc[i]=STAR_TINT[rnd.nextInt(STAR_TINT.length)];}
        for(int i=0;i<ra.length;i++){ra[i]=.42f+i*.105f+(rnd.nextFloat()-.5f)*.05f;rw[i]=.008f+rnd.nextFloat()*.03f;ral[i]=.07f+rnd.nextFloat()*.09f;rph[i]=rnd.nextFloat()*6.28f;rsp[i]=.05f+rnd.nextFloat()*.08f;rdr[i]=.015f+rnd.nextFloat()*.02f;}
        java.util.Arrays.fill(flareStar,-1);shootLife=-1;long t0=SystemClock.uptimeMillis();nextFlare=t0+800;nextShoot=t0+60000;
        puff=new RadialGradient(0,0,1,new int[]{0xffe4eaf4,0x8ce4eaf4,0x00e4eaf4},new float[]{0f,.6f,1f},Shader.TileMode.CLAMP);
        flash=0;gust=0;last=0;nextBolt=SystemClock.uptimeMillis()+1500+rnd.nextInt(3000);
    }

    @Override protected void onSizeChanged(int w,int h,int ow,int oh){
        haze.setShader(new LinearGradient(0,h*.4f,0,h,0x00dce4f0,0x52dce4f0,Shader.TileMode.CLAMP));
        // One ray shader along +x, reused for every ray by rotating the canvas.
        rayLength=(float)Math.hypot(w,h)*1.3f;
        rayShade=new LinearGradient(0,0,rayLength,0,new int[]{0xfffff4d6,0x73fff0cd,0x00fff0cd},new float[]{0f,.45f,1f},Shader.TileMode.CLAMP);
        ray.setShader(rayShade);seed();
    }
    /**
     * Measure how bright the wallpaper behind the effect is (32x18 sample) and set the strength:
     * full on dark wallpapers, down to 35% on bright ones.
     */
    void backdrop(android.graphics.drawable.Drawable d){
        if(d==null)return;
        try{
            Bitmap sample=Bitmap.createBitmap(32,18,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(sample);
            Rect old=new Rect(d.getBounds());d.setBounds(0,0,32,18);d.draw(c);d.setBounds(old);
            int[] pix=new int[32*18];sample.getPixels(pix,0,32,0,0,32,18);sample.recycle();
            double sum=0;for(int p:pix)sum+=(.2126*Color.red(p)+.7152*Color.green(p)+.0722*Color.blue(p))/255.0;
            float lum=(float)(sum/pix.length);gain=Math.max(.35f,Math.min(1f,1.3f-lum*1.25f));
        }catch(RuntimeException ignored){gain=1f;}
    }
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
                    float a=(.16f+(i%3)*.05f)*(.75f+.25f*(float)Math.sin(pp[i]));
                    c.save();c.translate(px[i],py[i]);c.scale(1,.12f+(i%4)*.02f);drawPuff(c,0,0,ps[i]/2,1f,a);c.restore();}
                c.drawRect(0,h*.4f,w,h,haze);
                break;
            case "cloud":
                c.drawColor(0x1a1e2434);
                for(int i=0;i<px.length;i++){px[i]+=pv[i]*dt;if(px[i]-200*ps[i]>w)px[i]=-260*ps[i];
                    for(int k=0;k<5;k++){float ox=((k*37+i*11)%170-85)*ps[i],oy=((k*23+i*7)%34-17)*ps[i],r=(40+(k*13+i*5)%44)*ps[i];drawPuff(c,px[i]+ox,py[i]+oy,r,1f,.14f);}}
                break;
            case "clear": case "partly":{
                // Sun glare from the top-left corner with tapered, soft rays that run past the far corner.
                gust+=dt;float k=(mode.equals("partly")?.6f:1f)*gain;float pulse=.9f+.1f*(float)Math.sin(gust*.5f);
                float sx=w*.073f,sy=-h*.11f;
                drawGlow(c,sx,sy,w*.65f,0xffecc4,.34f*k*pulse);drawGlow(c,sx,sy,w*.23f,0xfffae8,.5f*k);
                for(int i=0;i<ra.length;i++){
                    float a=ra[i]+(float)Math.sin(gust*rsp[i]+rph[i])*rdr[i];float half=rw[i]*(.85f+.15f*(float)Math.sin(gust*.7f+rph[i]));
                    c.save();c.rotate((float)Math.toDegrees(a),sx,sy);c.translate(sx,sy);
                    // Soft edges: a wide faint wedge under a narrower brighter one.
                    for(int layer=0;layer<3;layer++){float wh=half*(1.9f-layer*.45f),al=ral[i]*k*(layer==0?.35f:layer==1?.55f:.8f);
                        float spread=(float)Math.tan(wh)*rayLength;wedge.reset();wedge.moveTo(0,0);wedge.lineTo(rayLength,-spread);wedge.lineTo(rayLength,spread);wedge.close();
                        ray.setAlpha(Math.max(0,Math.min(255,(int)(al*255))));c.drawPath(wedge,ray);}
                    c.restore();
                }
                break;}
            case "clear-night": case "partly-night":{
                // Moonlight from just off screen (top-left), faint twinkling stars, a star that flares
                // every 1 to 3 seconds, and a shooting star every 60 seconds.
                float k=(mode.equals("partly-night")?.7f:1f)*gain;
                drawGlow(c,-w*.083f,-h*.22f,w*.85f,0xbacdff,.38f*k);
                for(int i=0;i<px.length;i++){pp[i]+=dt*pv[i];fill.setColor(0xff000000|pc[i]);fill.setAlpha((int)(255*(.22f+.5f*(.5f+.5f*(float)Math.sin(pp[i])))*pb[i]*k));c.drawCircle(px[i],py[i],ps[i],fill);}
                fill.setAlpha(255);
                if(animate&&px.length>0&&now>nextFlare){
                    for(int f=0;f<flareStar.length;f++)if(flareStar[f]<0){flareStar[f]=rnd.nextInt(px.length);flareLife[f]=0;flareDur[f]=1.3f+rnd.nextFloat()*.6f;break;}
                    nextFlare=now+1000+rnd.nextInt(2000);
                }
                float fk=Math.max(.6f,k);
                for(int f=0;f<flareStar.length;f++){int i=flareStar[f];if(i<0)continue;flareLife[f]+=dt;float p=flareLife[f]/flareDur[f];if(p>=1){flareStar[f]=-1;continue;}
                    float e=(float)Math.sin(Math.PI*p),a=e*fk,len=(6+9*e)*(ps[i]>1.25f*density?1.3f:1f)*density;
                    drawGlow(c,px[i],py[i],len*1.1f,pc[i],.5f*a);
                    c.save();c.translate(px[i],py[i]);c.rotate((float)Math.toDegrees(.12f+p*.25f));
                    drawSpike(c,len,0,pc[i],a,1.2f);drawSpike(c,0,len,pc[i],a,1.2f);drawSpike(c,len*.32f,len*.32f,pc[i],a,.8f);drawSpike(c,len*.32f,-len*.32f,pc[i],a,.8f);
                    c.restore();}
                if(animate&&shootLife<0&&now>nextShoot){shootX=w*(.31f+rnd.nextFloat()*.54f);shootY=h*(.04f+rnd.nextFloat()*.22f);shootVx=-(420+rnd.nextFloat()*160)*density;shootVy=(150+rnd.nextFloat()*80)*density;shootLife=0;nextShoot=now+60000;}
                if(shootLife>=0){shootLife+=dt;shootX+=shootVx*dt;shootY+=shootVy*dt;float fade=Math.max(0,1-shootLife/.9f);
                    float tx=shootX-shootVx*.28f,ty=shootY-shootVy*.28f;
                    stroke.setShader(new LinearGradient(shootX,shootY,tx,ty,Color.argb((int)(242*fade*Math.max(.6f,k)),255,255,255),0x00ffffff,Shader.TileMode.CLAMP));
                    stroke.setStrokeWidth(1.8f*density);c.drawLine(shootX,shootY,tx,ty,stroke);stroke.setShader(null);
                    if(fade<=0)shootLife=-1;}
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
    /** One sparkle spike through the origin, bright in the middle and fading at both ends. */
    private void drawSpike(Canvas c,float dx,float dy,int rgb,float alpha,float width){
        int mid=Color.argb(Math.max(0,Math.min(255,(int)(242*alpha))),Color.red(rgb),Color.green(rgb),Color.blue(rgb)),edge=mid&0x00ffffff;
        spike.setShader(new LinearGradient(-dx,-dy,dx,dy,new int[]{edge,mid,edge},null,Shader.TileMode.CLAMP));
        spike.setStrokeWidth(width*density);c.drawLine(-dx,-dy,dx,dy,spike);spike.setShader(null);
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
