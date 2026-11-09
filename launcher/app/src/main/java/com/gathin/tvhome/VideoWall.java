package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.ParcelFileDescriptor;
import android.view.Surface;
import android.view.TextureView;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * Muted, looping video wallpaper behind the Home surface. One video is stored in the app's private files
 * (uploaded over ADB through StudioProvider). It plays only while Home is the visible, active surface and
 * pauses whenever an app is open. The still wallpaper stays as the HomeSurface background, so it shows
 * until the first frame and whenever playback fails.
 */
final class VideoWall extends TextureView implements TextureView.SurfaceTextureListener {
    static final long MAX_BYTES=150L*1024*1024;
    private MediaPlayer player;
    private Surface surface;
    private boolean prepared, wantPlay;
    private int videoW, videoH;

    VideoWall(Context c){super(c);setSurfaceTextureListener(this);setOpaque(false);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}

    static File file(Context c){return new File(new File(c.getFilesDir(),"video"),"home.mp4");}
    private static File part(Context c){return new File(new File(c.getFilesDir(),"video"),"home.part");}
    static boolean enabled(Context c){return StudioConfig.prefs(c).getBoolean("video_wallpaper",false)&&file(c).isFile();}

    /** Opens a fresh upload target; the upload becomes active only after commit(). */
    static ParcelFileDescriptor openUpload(Context c)throws FileNotFoundException{
        File dir=new File(c.getFilesDir(),"video");if(!dir.isDirectory()&&!dir.mkdirs())throw new FileNotFoundException("Cannot create video folder");
        return ParcelFileDescriptor.open(part(c),ParcelFileDescriptor.MODE_WRITE_ONLY|ParcelFileDescriptor.MODE_CREATE|ParcelFileDescriptor.MODE_TRUNCATE);
    }
    static long commit(Context c){
        File p=part(c),f=file(c);long size=p.length();
        if(!p.isFile()||size<1024)throw new IllegalStateException("Video upload is empty");
        if(size>MAX_BYTES){p.delete();throw new IllegalStateException("Video is larger than 150 MB");}
        if(f.exists()&&!f.delete())throw new IllegalStateException("Cannot replace the old video");
        if(!p.renameTo(f))throw new IllegalStateException("Cannot store the video");
        StudioConfig.prefs(c).edit().putBoolean("video_wallpaper",true).putLong("video_revision",System.currentTimeMillis()).putLong("ui_revision",StudioConfig.prefs(c).getLong("ui_revision",0)+1).commit();
        return size;
    }
    static void off(Context c){
        file(c).delete();part(c).delete();
        StudioConfig.prefs(c).edit().putBoolean("video_wallpaper",false).putLong("ui_revision",StudioConfig.prefs(c).getLong("ui_revision",0)+1).commit();
    }

    void setPlaying(boolean play){
        wantPlay=play;
        if(player==null||!prepared)return;
        try{if(play&&!player.isPlaying())player.start();else if(!play&&player.isPlaying())player.pause();}catch(IllegalStateException ignored){}
    }

    @Override public void onSurfaceTextureAvailable(SurfaceTexture texture,int w,int h){open(texture);}
    @Override public void onSurfaceTextureSizeChanged(SurfaceTexture texture,int w,int h){crop();}
    @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture texture){release();return true;}
    @Override public void onSurfaceTextureUpdated(SurfaceTexture texture){}

    private void open(SurfaceTexture texture){
        release();
        File f=file(getContext());if(!f.isFile())return;
        try{
            surface=new Surface(texture);player=new MediaPlayer();
            player.setDataSource(f.getAbsolutePath());player.setSurface(surface);player.setLooping(true);player.setVolume(0f,0f);
            player.setOnVideoSizeChangedListener((mp,w,h)->{videoW=w;videoH=h;crop();});
            player.setOnPreparedListener(mp->{prepared=true;videoW=mp.getVideoWidth();videoH=mp.getVideoHeight();crop();if(wantPlay)mp.start();});
            player.setOnErrorListener((mp,what,extra)->{release();setVisibility(GONE);return true;});
            player.prepareAsync();
        }catch(Exception e){release();setVisibility(GONE);}
    }
    /** Centre-crop the video to fill the screen. */
    private void crop(){
        int w=getWidth(),h=getHeight();if(w==0||h==0||videoW==0||videoH==0)return;
        float scale=Math.max((float)w/videoW,(float)h/videoH);
        float sx=videoW*scale/w,sy=videoH*scale/h;
        Matrix m=new Matrix();m.setScale(sx,sy,w/2f,h/2f);setTransform(m);
    }
    void release(){
        prepared=false;
        if(player!=null){try{player.reset();}catch(Exception ignored){}player.release();player=null;}
        if(surface!=null){surface.release();surface=null;}
    }
}
