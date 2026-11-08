package com.gathin.tvhome;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;
import org.json.*;

final class ThemeAppearance {
    static final int FALLBACK=0xffa9ead8;
    static void prepare(Context context,JSONObject before,JSONObject clean)throws JSONException{
        JSONArray themes=clean.getJSONArray("themes");
        for(int i=0;i<themes.length();i++){
            JSONObject theme=themes.getJSONObject(i);if(!theme.optString("accentMode").equals("wallpaper"))continue;
            String id=theme.getJSONArray("wallpapers").optString(0,"");int color=FALLBACK;
            // Image IDs are immutable; a saved palette is reused on later edits.
            String key="accent_"+id;
            if(!id.isEmpty()){
                if(StudioConfig.prefs(context).contains(key))color=StudioConfig.prefs(context).getInt(key,FALLBACK);
                else{color=sample(StudioConfig.image(context,id));StudioConfig.prefs(context).edit().putInt(key,color).apply();}
            }
            theme.put("wallpaperAccent",String.format(java.util.Locale.ROOT,"#%06X",color&0xffffff));
        }
    }
    private static int sample(File file){
        Bitmap bitmap=null;try{
            BitmapFactory.Options o=new BitmapFactory.Options();o.inJustDecodeBounds=true;BitmapFactory.decodeFile(file.getPath(),o);
            if(o.outWidth<1||o.outHeight<1)return FALLBACK;o.inSampleSize=1;
            while(o.outWidth/o.inSampleSize>48||o.outHeight/o.inSampleSize>48)o.inSampleSize*=2;
            o.inJustDecodeBounds=false;bitmap=BitmapFactory.decodeFile(file.getPath(),o);if(bitmap==null)return FALLBACK;
            int[] pixels=new int[bitmap.getWidth()*bitmap.getHeight()];bitmap.getPixels(pixels,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());
            return AccentPalette.choose(pixels,FALLBACK);
        }catch(RuntimeException e){return FALLBACK;}finally{if(bitmap!=null)bitmap.recycle();}
    }
}
