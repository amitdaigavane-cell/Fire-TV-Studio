package com.gathin.tvhome;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.Build;
import android.view.Gravity;
import java.io.File;

/** One cached display-sized bitmap; visible Home and a custom idle image may retain separate bitmaps. */
final class HomeWallpaper {
    private static Bitmap bitmap;
    private static String loaded="uninitialized";
    static Drawable background(Context context){
        return background(context,StudioConfig.wallpaper(context));
    }
    static Drawable background(Context context,String id){
        File file=id.isEmpty()?null:StudioConfig.image(context,id);String key=id+":"+(file==null?0:file.lastModified());
        if(!key.equals(loaded)){
            loaded=key;bitmap=null;
            if(file!=null&&file.isFile())try{
                if(Build.VERSION.SDK_INT>=28)bitmap=ImageDecoder.decodeBitmap(ImageDecoder.createSource(file),(decoder,info,source)->{
                    float scale=Math.min(1f,Math.min(1920f/info.getSize().getWidth(),1080f/info.getSize().getHeight()));
                    decoder.setTargetSize(Math.max(1,Math.round(info.getSize().getWidth()*scale)),Math.max(1,Math.round(info.getSize().getHeight()*scale)));decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
                });
                else {BitmapFactory.Options options=new BitmapFactory.Options();options.inJustDecodeBounds=true;BitmapFactory.decodeFile(file.getPath(),options);options.inSampleSize=1;while(options.outWidth/options.inSampleSize>1920||options.outHeight/options.inSampleSize>1080)options.inSampleSize*=2;options.inJustDecodeBounds=false;bitmap=BitmapFactory.decodeFile(file.getPath(),options);}
            }catch(java.io.IOException|RuntimeException ignored){bitmap=null;}
        }
        Drawable background=new GradientDrawable(GradientDrawable.Orientation.TL_BR,StudioConfig.layout(context).equals("cinema")?new int[]{0xff222a40,0xff151021}:StudioConfig.layout(context).equals("compact")?new int[]{0xff172945,0xff0b1426}:new int[]{0xff102534,0xff080f1b});
        if(bitmap!=null)background=new CroppedImage(bitmap);
        return new LayerDrawable(new Drawable[]{background,new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0x3007101e,0x9907101e})});
    }
    private static final class CroppedImage extends Drawable {
        private final Bitmap image;
        private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
        private final RectF destination=new RectF();
        CroppedImage(Bitmap image){this.image=image;}
        @Override protected void onBoundsChange(Rect bounds){float scale=Math.max((float)bounds.width()/image.getWidth(),(float)bounds.height()/image.getHeight());float w=image.getWidth()*scale,h=image.getHeight()*scale;destination.set(bounds.exactCenterX()-w/2,bounds.exactCenterY()-h/2,bounds.exactCenterX()+w/2,bounds.exactCenterY()+h/2);}
        @Override public void draw(Canvas canvas){int count=canvas.save();canvas.clipRect(getBounds());canvas.drawBitmap(image,null,destination,paint);canvas.restoreToCount(count);}
        @Override public void setAlpha(int alpha){paint.setAlpha(alpha);invalidateSelf();}
        @Override public void setColorFilter(ColorFilter filter){paint.setColorFilter(filter);invalidateSelf();}
        @Override public int getOpacity(){return PixelFormat.OPAQUE;}
    }
}
